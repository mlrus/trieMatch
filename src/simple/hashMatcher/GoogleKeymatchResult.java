package trieMatch.simple.hashMatcher;

/**
 * @author Paul.Sidykh
 * @version $Id$
 */


public class GoogleKeymatchResult {
    
    private String matchType;
    private String matchString;
    private String hashLookup;
    private String keymatch;
    private String ticker;
    private String url;
    
    public GoogleKeymatchResult(String matchType, String matchString,
            String hashLookup, String keymatch, String ticker, String url) {
        super();
        this.matchType = matchType;
        this.matchString = matchString;
        this.hashLookup = hashLookup;
        this.keymatch = keymatch;
        this.ticker = ticker;
        this.url = url;
    }

    public String getHashLookup() {
        return hashLookup;
    }

    public String getKeymatch() {
        return keymatch;
    }

    public String getMatchType() {
        return matchType;
    }

    public String getTicker() {
        return ticker;
    }

    public String getMatchString() {
        return matchString;
    }

    public int getKeymatchWordsNumber() {
        if (keymatch != null && keymatch != null) {
            String[] words = keymatch.split(" ");
            return words.length;
        }
        return 0;
    }
    
    @Override
	public String toString() {
        final StringBuffer buffer = new StringBuffer();
        buffer.append("GoogleKeymatchResult[\n");
        buffer.append("\thashLookup = [").append(hashLookup).append("]\n");
        buffer.append("\tkeymatch = [").append(keymatch).append("]\n");
        buffer.append("\tmatchString = [").append(matchString).append("]\n");
        buffer.append("\tmatchType = [").append(matchType).append("]\n");
        buffer.append("\tticker = [").append(ticker).append("]\n");
        buffer.append("\turl = [").append(url).append("]\n");
        buffer.append("]");
        return buffer.toString();
    }

    
    public String getUrl() {
        return url;
    }

}
