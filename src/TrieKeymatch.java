/*
 * Copyright (C) 2006 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;

import com.google.enterprise.sdk.onebox.FailureCode;
import com.google.enterprise.sdk.onebox.Field;
import com.google.enterprise.sdk.onebox.IOneBoxResults;
import com.google.enterprise.sdk.onebox.ModuleResult;
import com.google.enterprise.sdk.onebox.OneBoxProvider;
import com.google.enterprise.sdk.onebox.OneBoxResults;

import trieMatch.keywordMatcher.KMDefinition;
import trieMatch.keywordMatcher.KeyMatch;
import trieMatch.keywordMatcher.Tiers;
import trieMatch.keywordMatcher.TrieMatcher;
import trieMatch.keywordMatcher.KeyMatch.MatcherActionDefinition;
import trieMatch.util.Constants;

/**
 * Implementation of a OneBox provider that mimics Keymatch (more or less)
 *
 */
public class TrieKeymatch extends OneBoxProvider {
    static String defaultAggregatorName = Constants.DEFAULT_AGGREGATOR_NAME;
    static String defaultAggregatorParm = Constants.DEFAULT_AGGREGATOR_PARM;   
    private static MatcherActionDefinition searchFlavor;
    private static KeyMatch kmPrimary;
    private static String infoString = "";

    // TODO - handle matchGroups, authType
    String getURL(String apiMaj, String apiMin, String oneboxName, String dateTime, String ipAddr, String lang,
            String query, String[] matchGroups) {
        // We don't have access to HttpServletRequest
        String urlPrefix = getInitParameter("url_prefix");
        return urlPrefix + "apiMaj=" + apiMaj + "&apiMin=" + apiMin + "&oneboxName=" + oneboxName + "&dateTime="
        + dateTime + "&ipAddr=" + ipAddr + "&lang=" + lang + "&query=" + query + "&authType=none";
    }

    /*
     Replace special characters with XML escapes:
     & (ampersand) is replaced by &amp;
     < (less than) is replaced by &lt;
     > (greater than) is replaced by &gt;
     " (double quote) is replaced by &quot;
     */
    String escapeXML(String str) {
        return replace(replace(replace(replace(str, "&", "&amp;"), "<", "&lt;"), ">", "&gt;"), "\"", "&quot;");
    }

    String replace(String str, String pattern, String replace) {
        int s = 0;
        int e = 0;
        StringBuffer result = new StringBuffer();
        while ((e = str.indexOf(pattern, s)) >= 0) {
            result.append(str.substring(s, e));
            result.append(replace);
            s = e + pattern.length();
        }
        result.append(str.substring(s));
        return result.toString();
    }

    /**
     * Canonicalizes the search query:
     *  lower case
     *  trim leading and trailing spaces
     *  convert punctuation to space chars
     *  collapse multiple space chars to a single space
     *  TODO - remove special query terms, negative queries, how handle hyphens and dots
     */
    private String canonicalizeQuery(String query) {
        String lowerQ = query.toLowerCase();
        String trimLowerQ = lowerQ.trim();
        char[] qArray = new char[trimLowerQ.length()];
        StringBuffer canonicalQ = new StringBuffer("");
        qArray = trimLowerQ.toCharArray();
        boolean prevCharIsSpace = false;
        for (int i = 0; i < trimLowerQ.length(); i++) {
            char ch = qArray[i];
            if (Character.isLetterOrDigit(ch)) {
                canonicalQ.append(ch);
                prevCharIsSpace = false;
            }
            else if (prevCharIsSpace == false) {
                canonicalQ.append(" ");
                prevCharIsSpace = true;
            }
        }
        // Need to retrim since may introduce a space if the initial char in the query is non-alphanumeric
        return canonicalQ.toString().trim();
    }

    @Override
    public void init() throws ServletException {
        Date d = new Date();
        String searchFlavorString = getInitParameter("SearchFlavor");
        String aggregatorName = getInitParameter("AggregatorName");
        String aggregatorParm = getInitParameter("AggregatorParm");   
        if (searchFlavorString == null || searchFlavorString.length()==0) searchFlavorString = "all";
        if (aggregatorName == null || aggregatorName.length()==0) aggregatorName = defaultAggregatorName;
        if (aggregatorParm == null || aggregatorParm.length()==0) aggregatorParm = defaultAggregatorParm;
        searchFlavor = MatcherActionDefinition.valueOf(searchFlavorString);
        infoString = "trieMatcher::" + searchFlavor+" aggregator::"+aggregatorName+(aggregatorParm!=null?("::"+aggregatorParm):"");
        log(infoString + " loading keymatches: " + d.toString());
        Tiers.mapClass = searchFlavor.getMapClass(); // MUST assign class (prefix needs it for TreeSet)
        TrieMatcher.setAggregator(aggregatorName, aggregatorParm);
        try {
            String keymatchFile = getInitParameter("keymatch_file");
            kmPrimary = new KeyMatch(keymatchFile);
        } catch (IOException ioe) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ioe.printStackTrace(pw);
            log(sw.toString());
        }
        d = new Date();
        log("Finish loading keymatches: " + d.toString());
        log("Loaded " + kmPrimary.nItems+" from " + kmPrimary.sourceFilename);
    }

    /**
     *
     *
     * @param apiMaj
     * @param apiMin
     * @param oneboxName
     * @param dateTime
     * @param ipAddr
     * @param lang
     * @param query
     * @param matchGroups
     * @return
     */
    protected IOneBoxResults provideOneBoxResults(String apiMaj, String apiMin, String oneboxName, String dateTime,
            String ipAddr, String lang, String query, String[] matchGroups) {
        long ms=System.currentTimeMillis();//!
        long na=System.nanoTime();//!
        String thisURL = getURL(apiMaj, apiMin, oneboxName, dateTime, ipAddr, lang, query, matchGroups);

        // Create results object
        OneBoxResults res = new OneBoxResults();
        res.setProviderText("keymatch");

        // Check api version for compatibility
        int apiVersion = ((Integer.parseInt(apiMaj) << 0xFF) | (Integer.parseInt(apiMin)));
        if (apiVersion < ((1 << 0xFF) | 0)) { // 1.0
            res.setFailure(FailureCode.lookupFailure, "OneBox API versions older than 1.0 not supported by provider");
            return res;
        }

        int matchCount = 0;
        String canonicalQuery = canonicalizeQuery(query);
        log("Canonical query: " + canonicalQuery);

        TrieMatcher trieMatcher = searchFlavor.select(kmPrimary.tiers);
        List<KMDefinition> ans = trieMatcher.findMatch(canonicalQuery);
        Collection<KMDefinition> rans = trieMatcher.kmReducer(ans);
        for (KMDefinition aggItem : rans) {
            if (!res.canAddResult()) break;
            ModuleResult mr = new ModuleResult(escapeXML(aggItem.description()), escapeXML(aggItem.url()));
            mr.addField(new Field("matchType", aggItem.matchType()));
            mr.addField(new Field("trieLookup", canonicalQuery));
            mr.addField(new Field("infoString", infoString));
            mr.addField(new Field("score", aggItem.getScore()));
            mr.addField(new Field("keymatch", aggItem.keymatchText()));
            res.addResult(mr);
            matchCount++;
        }

        if (matchCount == 0) {
            res.setFailure(FailureCode.lookupFailure, "No matches found for: " + canonicalQuery);
        }
        else {
            res.setResultsTitleLink("Matches: " + matchCount, escapeXML(thisURL));
        }
        long ms2=System.currentTimeMillis();//!
        long na2=System.nanoTime();//!
        log(String.format("thr%d %d+%d:%d+%d",Thread.currentThread().getId(),ms,na,ms2,na2));
        return res;
    }
}
