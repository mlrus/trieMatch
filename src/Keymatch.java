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


import com.google.enterprise.sdk.onebox.*;
import java.io.FileReader;
import java.io.File;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.HashMap;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import java.util.Enumeration;
import java.util.ArrayList;

/**
 * Implementation of a OneBox provider that mimics Keymatch (more or less)
 *
 */
public class Keymatch extends OneBoxProvider {

  private static HashMap exactMatchHash = new HashMap();
  private static HashMap phraseMatchHash = new HashMap();
  private static HashMap keywordMatchHash = new HashMap();

  // TODO - handle matchGroups, authType
  String getURL(String apiMaj, String apiMin,
                String oneboxName, String dateTime, String ipAddr,
                String lang, String query, String[] matchGroups) {
    // We don't have access to HttpServletRequest
    String urlPrefix = getInitParameter("url_prefix");
    return urlPrefix + "apiMaj=" + apiMaj + "&apiMin=" + apiMin + "&oneboxName=" + oneboxName +
      "&dateTime=" + dateTime + "&ipAddr=" + ipAddr + "&lang=" + lang + "&query=" + query +
      "&authType=none";
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
      s = e+pattern.length();
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
      } else if (prevCharIsSpace == false) {
        canonicalQ.append(" ");
        prevCharIsSpace = true;
      }
    }
    // Need to retrim since may introduce a space if the initial char in the
    // query is non-alphanumeric
    return canonicalQ.toString().trim();
  }

  public void init() throws ServletException {

    Date d = new Date();
    log("Start loading keymatches: " + d.toString());
    BufferedReader inputStream = null;
    try {
      String keymatchFile = getInitParameter("keymatch_file");
      log("Keymatch filename: \""+(new File(keymatchFile)).getAbsolutePath()+"\"");
      inputStream = new BufferedReader(new FileReader(keymatchFile));
      String l;
      while ((l = inputStream.readLine()) != null) {
        // TODO how are commas handled in keymatch files
        String[] tokens = l.split(",");
        if (tokens.length < 4) {
          continue;
        } else {
          String keymatch = canonicalizeQuery(tokens[0]);
          String matchType = tokens[1];
          String url = tokens[2];
          String matchString = tokens[3];
          KeymatchObj keymatchObj = new KeymatchObj(url, matchString, keymatch);
          if (matchType.equals("ExactMatch")) {
            exactMatchHash.put(keymatch, keymatchObj);
          }
          if (matchType.equals("PhraseMatch")) {
            phraseMatchHash.put(keymatch, keymatchObj);
          }
          if (matchType.equals("KeywordMatch")) {
            String words[] = keymatch.split(" ");
            for (int i = 0; i < words.length; i++) {
              ArrayList matches;
              if (keywordMatchHash.containsKey(words[i])) {
                matches = (ArrayList) keywordMatchHash.get(words[i]);
              } else {
                matches = new ArrayList();
              }
              matches.add(keymatchObj);
              keywordMatchHash.put(words[i], matches);
            }
          }
        }
      }
    } catch (IOException ioe) {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      ioe.printStackTrace(pw);
      log(sw.toString());
    } finally {
      if (inputStream != null) {
        try {
          inputStream.close();
        } catch (IOException ioe2) { }
      }
    }
    d = new Date();
    log("Finish loading keymatches: " + d.toString());
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
  protected IOneBoxResults provideOneBoxResults(String apiMaj, String apiMin,
                                                String oneboxName, String dateTime, String ipAddr,
                                                String lang, String query, String[] matchGroups) {

    String thisURL = getURL(apiMaj, apiMin, oneboxName, dateTime, ipAddr, lang, query, matchGroups);

    // Create results object
    OneBoxResults res = new OneBoxResults();
    res.setProviderText("keymatch");

    // Check api version for compatibility
    int apiVersion = ((Integer.parseInt(apiMaj) << 0xFF) | (Integer.parseInt(apiMin)));
    if (apiVersion < ((1 << 0xFF) | 0)) {   // 1.0
      res.setFailure(FailureCode.lookupFailure,
                     "OneBox API versions older than 1.0 not supported by provider");
      return res;
    }

    int matchCount = 0;
    String canonicalQuery = canonicalizeQuery(query);
    log("Canonical query: " + canonicalQuery);

    if (exactMatchHash.containsKey(canonicalQuery)) {
      if (res.canAddResult()) {
        KeymatchObj ko = (KeymatchObj) exactMatchHash.get(canonicalQuery);
        ModuleResult mr = new ModuleResult(escapeXML(ko.matchString), escapeXML(ko.url));
        mr.addField(new Field("matchType", "ExactMatch"));
        mr.addField(new Field("hashLookup", canonicalQuery));
        mr.addField(new Field("keymatch", ko.keymatch));
        res.addResult(mr);
        matchCount++;
      }
    }

    // First get the list of possible keymatches
    String[] words = canonicalQuery.split(" ");
    ArrayList allMatchObjects = new ArrayList();
    for (int i = 0; i < words.length; i++) {
      if (keywordMatchHash.containsKey(words[i])) {
        ArrayList matchObjects = (ArrayList) keywordMatchHash.get(words[i]);
        for (int j = 0; j < matchObjects.size(); j++) {
          KeymatchObj ko = (KeymatchObj) matchObjects.get(j);
          if (!allMatchObjects.contains(ko)) {
            allMatchObjects.add(ko);
          }
        }
      }
    }
    // Now determine which keymatches are complete
    int i = 0;
    while (i < allMatchObjects.size()) {
      KeymatchObj ko = (KeymatchObj) allMatchObjects.get(i);
      // Each of these keymatch words must be in the canonicalized query words
      String keymatchWords[] = ko.keymatch.split(" ");
      ArrayList canonicalWords = new ArrayList();
      for (int j = 0; j < words.length; j++) {
        canonicalWords.add(words[j]);
      }
      for (int k = 0; k < keymatchWords.length; k++) {
        if (!canonicalWords.contains(keymatchWords[k])) {
          allMatchObjects.remove(i);
          i--;
          break;
        }
      }
      i++;
    }

    for (int j = 0; j < allMatchObjects.size(); j++) {
      if (res.canAddResult()) {
        KeymatchObj ko = (KeymatchObj) allMatchObjects.get(j);
        log("Keymatch match: " + ko.toString());
        ModuleResult mr = new ModuleResult(escapeXML(ko.matchString), escapeXML(ko.url));
        mr.addField(new Field("matchType", "Keyword"));
        mr.addField(new Field("hashLookup", canonicalQuery));
        mr.addField(new Field("keymatch", ko.keymatch));
        res.addResult(mr);
        matchCount++;
      }
    }

    Enumeration phrases = new Phrases(canonicalQuery);
    String phrase;
    for (; phrases.hasMoreElements() ;) {
      phrase = (String) phrases.nextElement();
      log("Phrase: " + phrase);
      if (phraseMatchHash.containsKey(phrase)) {
        if (res.canAddResult()) {
          KeymatchObj ko = (KeymatchObj) phraseMatchHash.get(phrase);
          ModuleResult mr = new ModuleResult(escapeXML(ko.matchString), escapeXML(ko.url));
          mr.addField(new Field("matchType", "PhraseMatch"));
          mr.addField(new Field("hashLookup", phrase));
          mr.addField(new Field("keymatch", ko.keymatch));
          res.addResult(mr);
          matchCount++;
        }
      }
    }

    if (matchCount == 0) {
      res.setFailure(FailureCode.lookupFailure, "No matches found for: " + canonicalQuery);
    } else {
      res.setResultsTitleLink("Matches: " + matchCount, escapeXML(thisURL));
    }
    return res;
  }
}


class KeymatchObj {

  String url;
  String matchString;
  String keymatch;

  KeymatchObj(String url, String matchString, String keymatch) {
    this.url = url;
    this.matchString = matchString;
    this.keymatch = keymatch;
  }

  public String toString() {
    return "URL: " + url + " MatchString: " + matchString + " Keymatch: " + keymatch;
  }
}

class Phrases implements Enumeration {

  String[] words;
  int i = 0;
  int j = 0;
  boolean hasMore = true;

  Phrases(String canonicalQuery) {
    this.words = canonicalQuery.split(" ");
  }

  public boolean hasMoreElements() {
    return hasMore;
  }

  public Object nextElement() {
    if (! hasMore) {
      return null;
    }
    StringBuffer next = new StringBuffer();
    for (int k = 0; k <= j; k++) {
      if (k != 0) {
        next.append(" ");
      }
      next.append(words[i + k]);
    }
    // System.out.println(next.toString());
    moveIndex();
    return next.toString();
  }

  void moveIndex() {
    // System.out.println("Calling moveIndex with i = " + i + " and j = " + j);
    if (i <= words.length - 1 && i + j <= words.length - 2) {
      j++;
    } else if (i <= words.length - 2) {
      i++;
      j = 0;
    } else {
      hasMore = false;
    }
    // System.out.println("Return from moveIndex with i = " + i + " and j = " + j);
  }

  public static void main(String [ ] args) {
      Phrases phrases = new Phrases("foo bar baz");
      for (; phrases.hasMoreElements() ;) {
        String phrase = (String) phrases.nextElement();
      }
  }

}
