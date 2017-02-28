//This is unpublished source code. Michah Lerner 2006

package trieMatch.keywordMatcher;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;

import trieMatch.keywordMatcher.KeyMatch.MatchType;
import trieMatch.util.Constants;


/**
 * Implementation of items that get matched.  
 * @author Michah.Lerner
 *
 */
public class KMDefinition implements Comparable<KMDefinition>, Comparator<KMDefinition> {
	static int RULETYPEscoreFactor=Constants.RULETYPEscoreFactor;       //3,//1,
	static int MATCHLENGTHscoreFactor=Constants.MATCHLENGTHscoreFactor; //10;//1;//10;
	StructuredElement element;
	MatchType kType;
	String url;
	String description;
	int score;

    // weighting 1/sim(key,description) can assign more credit to shorter keymatches.
	final static Similarity similarity = new ResultScorer_cos();//ResultScorer_prefixPhrase();
    static final public float sim(StructuredElement element, String description) {
        return similarity.sim(element,description);
    }
    static final public float sim(StructuredElement element, StructuredElement description) {
        return similarity.sim(element,description); 
    }
    
	String asString() {
		return element.asString()+"; "+String.format("%-12s",kType)+"; "+url+"; keys="+element.asString()+"; "+description+"; "+score;
	}
	@Override
	public String toString() {
		return asString();
	}
	public String keymatchText() {
		return element.asString();
	}
	public String url() { 
		return url;
	}
	public String description() {
		return description;
	}
	public String matchType() {
		switch(kType) {
		case KEYWORDMATCH: return "Keyword";
		case INORDERMATCH: return "InorderMatch";
		case PHRASEMATCH: return "PhraseMatch";
		case EXACTMATCH: return "ExactMatch";
		default: return kType.name();
		}
	}
    
	public String getScore() { return String.format("%d", score); }
	/**
	 * Build the definition of an element, which is placed into the sequence by the given Sequencer.  The sequencer
	 * may impose consistant total orders that are different from the locale lexicographical order.
	 * @param s Item to split and sequence
	 * @param elementIndexer implementation of method to sequence the tokenized item
	 */
	KMDefinition(String s, Sequencer elementIndexer) {
		this(elementIndexer,s.split("[ \"]*,[ \"]*",4));
	}
	
	/**
	 * Build definition from prior record 
	 * @param valueRecord record of values to use, including the kind of match, url, and baseline score
	 */
	KMDefinition(KMDefinition valueRecord) {
		this.element = valueRecord.element;
		this.kType = valueRecord.kType;
		this.url = valueRecord.url;
		this.description = valueRecord.description;
		this.score = valueRecord.score;
	}

	/** 
	 * Build definition from a varargs of string fields, with the given sequencer 
	 * @param elementIndexer The sequencer to be used for placing items into order
	 * @param fields Items to be sequenced for the new definition
	 */
	KMDefinition(Sequencer elementIndexer, String... fields) {
		if (fields.length >= 1) {
			element = elementIndexer.arrange(fields[0].trim());
		}
		if (fields.length >= 2) try {
			kType = MatchType.valueOf(fields[1].trim().toUpperCase(Locale.getDefault()));
		} catch (IllegalArgumentException e) {
			System.err.println("ERROR: assuming ExactMatch for "+Arrays.toString(fields));
			kType = MatchType.EXACTMATCH;
		}
		if (fields.length >= 3) url = fields[2].trim();
		if (fields.length >= 4) description = fields[3].trim();
		this.score = 1 + (1 + kType.ordinal()) * RULETYPEscoreFactor + Math.round(10f*sim(element,description));

       	if(url==null||description==null) {
			System.err.printf("ERROR: null fields \"%s\" \"%s\" \"%s\"\n",kType,url,description);
		}
	}

	/**
	 * Define the relative value of the match type
	 * @param newFactor the multiplier of the matchType, based on 1==keyword, 2=phrase, 3=exact
	 * @return score factor
	 */
	public static int setRuletypeScorefactor(int newFactor) {
		int oldFactor = RULETYPEscoreFactor;
		RULETYPEscoreFactor = newFactor;
		return oldFactor;
	}
	/**
	 * Define the relative value of the match length
	 * @param newFactor the multiplier of the length
	 * @return matchlength scoring factor
	 */
	public static int setMatchlengthScorefactor(int newFactor) {
		int oldFactor = RULETYPEscoreFactor;
		RULETYPEscoreFactor = newFactor;
		return oldFactor;
	}
	
	/**
	 * Compare two definitions according to url, then score. (equals scores return the caseless comparator result)
	 * @param o1 first definition
	 * @param o2 second definition
	 * @return result
	 */

	public int compare(KMDefinition o1, KMDefinition o2) {
		if (o1.url.equalsIgnoreCase(o2.url)) return (o1.score - o2.score);
		return (o1.url.compareToIgnoreCase(o2.url));
	}

	public int compareTo(KMDefinition o) {
		if (this.url.equalsIgnoreCase(o.url)) return (this.score - o.score);
		return this.url.compareToIgnoreCase(o.url);
	}
	
	
}
