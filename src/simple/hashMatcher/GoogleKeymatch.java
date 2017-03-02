package trieMatch.simple.hashMatcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Implementation of a OneBox provider that mimics GoogleKeymatch (more or less)
 * 
 */
public class GoogleKeymatch {
    boolean showComparisonOutput = false;	
    private static final String BLANK_SPACE = " ";
    private static final String PHRASE_MATCH = "PhraseMatch";
    private static final String KEYWORD = "Keyword";
    private static final String EXACT_MATCH = "ExactMatch";
    private static final String keymatchFileName_KEY = "keymatch.file.name";

    // private static Logger logger = Logger.getLogger(GoogleKeymatch.class);

    private static HashMap<String, KeymatchObj> exactMatchHash = new HashMap<String, KeymatchObj>();
    private static HashMap<String, KeymatchObj> phraseMatchHash = new HashMap<String, KeymatchObj>();
    private static HashMap<String, ArrayList<KeymatchObj>> keywordMatchHash = new HashMap<String, ArrayList<KeymatchObj>>();

    private static GoogleKeymatch _instance = null;

    public String[] S = new String[0];

    private GoogleKeymatch() {
        initialize();
    }

    public static synchronized GoogleKeymatch getInstance() {
        if (_instance == null) {
            _instance = new GoogleKeymatch();
        }
        return _instance;
    }

    public static synchronized GoogleKeymatch getNewInstance() {
        return new GoogleKeymatch();
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    /*
     * Replace special characters with XML escapes: & (ampersand) is replaced by
     * &amp; < (less than) is replaced by &lt; > (greater than) is replaced by
     * &gt; " (double quote) is replaced by &quot;
     */
    private String escapeXML(String str) {
        return replace(replace(replace(replace(str, "&", "&amp;"), "<", "&lt;"), ">", "&gt;"), "\"", "&quot;");
    }

    private String replace(String str, String pattern, String replace) {
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
     * Canonicalizes the search query: lower case trim leading and trailing
     * spaces convert punctuation to space chars collapse multiple space chars
     * to a single space TODO - remove special query terms, negative queries,
     * how handle hyphens and dots
     */
    private String canonicalizeQuery(String query) {
        String lowerQ = query.toLowerCase(Locale.getDefault());
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
                canonicalQ.append(BLANK_SPACE);
                prevCharIsSpace = true;
            }
        }
        // Need to retrim since may introduce a space if the initial char in the query is non-alphanumeric
        return canonicalQ.toString().trim();
    }

    private void initialize() {
        BufferedReader inputStream = null;
        int cnt = 0;
        String cannonicalKeymatchFN = null;
        try {
            String keymatchFile = System.getProperty(keymatchFileName_KEY);
            cannonicalKeymatchFN = new File(keymatchFile).getCanonicalPath();
            System.out.println(TOD.now() + ": Start loading keymatches from " + cannonicalKeymatchFN);
            inputStream = new BufferedReader(new FileReader(keymatchFile));
            String l;
            while ((l = inputStream.readLine()) != null) {
                // TODO how are commas handled in keymatch files
                String[] tokens = l.split(" *, *", 4);
                if (tokens.length < 4) {
                    continue;
                }
                cnt++;
                String keymatch = canonicalizeQuery(tokens[0]);
                String matchType = tokens[1];
                String url = tokens[2];
                String matchString = tokens[3];
                KeymatchObj keymatchObj = new KeymatchObj(url, matchString, keymatch);
                if (matchType.equals(EXACT_MATCH)) {
                    exactMatchHash.put(keymatch, keymatchObj);
                }
                if (matchType.equals(PHRASE_MATCH)) {
                    phraseMatchHash.put(keymatch, keymatchObj);
                }
                if (matchType.equals("KeywordMatch")) {
                    String words[] = keymatch.split(BLANK_SPACE);
                    for (String word : words) {
                        ArrayList<KeymatchObj> matches;
                        if (keywordMatchHash.containsKey(word)) {
                            matches = keywordMatchHash.get(word);
                        }
                        else {
                            matches = new ArrayList<KeymatchObj>();
                            keywordMatchHash.put(word, matches);
                        }
                        matches.add(keymatchObj);
                    }
                }
            }
        } catch (IOException ioe) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ioe.printStackTrace(pw);
            System.out.println("debug: " + sw.toString());
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ioe2) { 
                    // empty
                }
            }
            System.out.println(TOD.now() + ": read " + cnt + "  lines from input " + cannonicalKeymatchFN);
        }
    }

    private String extractTickerFromUrl(String resultUrl) {
        String ticker = null;
        if (resultUrl != null) {
            int tickerPos = resultUrl.indexOf("ticker=");
            if (tickerPos > 0) {
                tickerPos += 7;
                int ampPos = resultUrl.indexOf('&', tickerPos);
                int dotPos = resultUrl.indexOf('.', tickerPos);
                int tickerendPos = ampPos;
                if (ampPos < 0) {
                    tickerendPos = dotPos;
                } else if (ampPos > 0 && dotPos > 0) {
                    tickerendPos = ampPos < dotPos ? ampPos : dotPos;
                } else if (ampPos > 0 && dotPos < 0) {
                    tickerendPos = ampPos;
                }
                if (tickerendPos > 0) {
                    ticker = resultUrl.substring(tickerPos, tickerendPos);
                } else {
                    ticker = resultUrl.substring(tickerPos);
                }
            }
        }
        return ticker;
    }

    public List<GoogleKeymatchResult> getKeymatchResult(String query) {
        int matchCount = 0;
        String canonicalQuery = canonicalizeQuery(query);
        List<GoogleKeymatchResult>res = new ArrayList<GoogleKeymatchResult>();
        if (exactMatchHash.containsKey(canonicalQuery)) {
            if (true) {
                KeymatchObj ko = (KeymatchObj) exactMatchHash.get(canonicalQuery);
                GoogleKeymatchResult mr = new GoogleKeymatchResult(EXACT_MATCH,
                        escapeXML(ko.matchString), canonicalQuery, ko.keymatch,
                        extractTickerFromUrl(ko.url), ko.url);
                res.add(mr);
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
            if (true) {
                KeymatchObj ko = (KeymatchObj) allMatchObjects.get(j);
                GoogleKeymatchResult mr = new GoogleKeymatchResult(KEYWORD,
                        escapeXML(ko.matchString), canonicalQuery, ko.keymatch,
                        extractTickerFromUrl(ko.url), ko.url);
                res.add(mr);
                matchCount++;
            }
        }

        Enumeration phrases = new Phrases(canonicalQuery);
        String phrase;
        for (; phrases.hasMoreElements() ;) {
            phrase = (String) phrases.nextElement();
            if (phraseMatchHash.containsKey(phrase)) {
                if (true) {
                    KeymatchObj ko = (KeymatchObj) phraseMatchHash.get(phrase);
                    GoogleKeymatchResult mr = new GoogleKeymatchResult(PHRASE_MATCH,
                            escapeXML(ko.matchString), canonicalQuery, ko.keymatch,
                            extractTickerFromUrl(ko.url), ko.url);
                    res.add(mr);
                    matchCount++;
                }
            }
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
    @Override
    public String toString() {
        return "URL: " + url + " MatchString: " + matchString + " GoogleKeymatch: " + keymatch;
    }
}

class Phrases implements Enumeration {
    String[] words;
    int i = 0;
    int j = 0;

    boolean hasMore = true;

    Phrases(String canonicalQuery) {
        this.words = canonicalQuery.split(" +");
    }

    public boolean hasMoreElements() {
        return hasMore;
    }

    public Object nextElement() {
        if (!hasMore) { return null; }
        StringBuffer next = new StringBuffer();
        for (int k = 0; k <= j; k++) {
            if (k != 0) {
                next.append(" ");
            }
            next.append(words[i + k]);
        }
        moveIndex();
        return next.toString();
    }

    void moveIndex() {
        if (i <= words.length - 1 && i + j <= words.length - 2) {
            j++;
        } else if (i <= words.length - 2) {
            i++;
            j = 0;
        } else {
            hasMore = false;
        }
    }
}