//This is unpublished source code. Michah Lerner 2006

package trieMatch.util;

import trieMatch.Interfaces.Aggregator;
import trieMatch.util.aggregation.*;

/**
 * This defines 65 (aprox.) initial values utilized by the applications of (1)
 * generating keywords by <em>symbolic &amp; Bayes match generation;</em> (2)
 * computing per-entity probabilities over keyword combinations for multiple
 * information sources (required for the naïve Bayes algorithm); <i>very</i>
 * fast trie-based keyword matching of arbitrary text to keyamtches. Only one of
 * the fields in this class is declared as final. <br>
 * <br>
 * <u><em>All non-final  fields can be reconfigured automatically, enabling 
 * for <b>super-quick</b> and <b>highly-reliable</b> agile development 
 * through elimination of additional interface code</em></u>.
 * This is achieved very simply by passing the
 * reconfiguration target as parameter to an instance of the utility class
 * <code>RSP</code>; this works as well in a more streamlined style where
 * classes extend from an RSP (see the RSP javadoc or code).
 * 
 * <br>
 * <br>
 * The new values are defined in a config.xml file, as well as through the
 * command line tuning argument <b>-D<i>name</i></b> or <b>-D<i>name</i>=<i>value</i></b>.
 * The first form assumes the variable is a boolean and assigns the value of
 * true. The second form will make the assignment according to the declared type
 * of the field. <br>
 * <br>
 * <b>It is an error to provide a value that is incompatible with the field
 * declaration.</b>
 * 
 * @author Michah.Lerner
 * @see RSP
 * 
 */
public class Constants {
	/**  (match generator) Default property file name, override with -prop ^lt;name&gt; */
	public static String _propertyFilename = "defaultProperties.xml";
	/**  (match generator) Internal key for the property that  gives the display name for the index */
	public final static String indexKey = "shortIndexName";

	// Keymatch type thresholds
	/** (match generator) Minimum threshold to be considered a valid set definition (keywordmatch) */
	public static Double threshold_isaSet = 1D-10*Double.MIN_VALUE;
	/** (match generator) Minimum threshold to be considered a valid sequence definition (phrasematch) */
	public static Double threshold_isaSequence=0.75D-10*Double.MIN_VALUE; //0.5D
	/**(match generator)  Minimum threshold to be considered a valid exact match (exactmatch) */
	public static Double threshold_isaExactmatch=0.15D;

	/** (match generator) When a match "covers" another longer match, should the longer match be ignored? */
	static public Boolean subsume = true;

	/** (match generator) Reduce score of matches that have words of length one or length two? */
	static public Boolean adjustifShortWords = true;
	/** (match generator) Generate diagnostic explainations to output log? */
	static public Boolean explain = false;
	/** (match generator) Generated detailed explainations about use of entropy in word subsetting? */
	static public Boolean explainEntropy = false;
	/** (match generator) Write each keymatch to the log as generated, to show incremental progress? */
	static public Boolean showAsGened = false;
	/** (match generator) User can set corpusCompare to a filename to compare two corpus for word occurrences rates. */
	static public Boolean corpusCompare = false;

	/** (match generator) When producing statistical input, show errors from invalid inputs? */
	public static Boolean _showIfInvalidInput=false;
	/** (match generator) When producing statistical input, show errors of invalid results? */
	public static Boolean _showIfInvalidResult=false;
	/** (match generator) When producing statistical input, show errors from queries unanswered do to no data? */
	public static Boolean _showIfNoDataForQuery=false;
	/** (match generator) When producing statistical input, show results for trivial identities like p(X|X) ? */
	public static Boolean _showIfIdentity=false;
	/** (match generator) When producing statistical input, valid results that occur without error? */
	public static Boolean _showIfOK=true;
	/** (match generator) When producing statistical input, show special neighborhood integration results?  */
	public static Boolean _showIfNeighborhood=true;
	/** (match generator) When producing statistical input, show results for items that have zero probability */
	public static Boolean _showIfZero=false;
	/** (match generator) When producing statistical input, show results for items that occur with certainty */
	public static Boolean _showIfOne=false;
	public static Boolean _showIfOther=true;
	/** (match generator)  When producing statistical input, show if the computed value is 1.0D ? */
	public static Boolean _showIfValueONE=false;
	/** (match generator) When producing statistical input, show if the computed value is 0.0D ? */
	public static Boolean _showIfValueZERO=false;
	/** (match generator)  When producing statistical input, show if the computed value is not a numbe (NaN)? */
	public static Boolean _showIfValueNaN=false;
	/** (match generator) Should low-probability exact one word matches be dropped when there are higher probability matches of longer length? */
	public static Boolean _dropOptionalExactSingletons=true;
	/** (match generator) When low-probability exact one word matches are dropped, should they be logged? */
	public static Boolean _showDroppedExactSingletons=false;
	/** (match generator) When multiple sequences matches (phrasematches) are generated, should longer ones be dropped if they are just a shorted one plus a suffix? */
	public static Boolean _dropSequenceSuffixes=true;
	/** (match generator) When dropping multiple sequence matches, should they be logged? */
	public static Boolean _showDroppedSequenceSuffixes=false;
	/** (match generator) When dropping duplicate keys, should they be logged? */
	public static Boolean _showDroppedDuplicates=false;

	/** (match generator) How much tracing to log?  Value zero is no tracing, 100 is maximum tracing */
	public static Integer traceLevel = 0;

	public static final Double closeToZero= 0D+1.0E-50;
	public static final Double closeToOne = 1D-1.0E-50; 

	/** (match generator) Threshold for #words to switch into iterative match generation */
	static public Integer iteratorIf=7;
	/** (match generator) Threshold for #words to start generating from sample of words instead of full phrase */
	static public Integer sampleIf=8;
	/** (match generator) Maximum number of terms to consider in any expansion */
	static public Integer maxExpansionTerms=2*sampleIf;
	/** (match generator) Maximum number of keymatches for any input */
	static public Integer _maxEntitiesLimit=Integer.MAX_VALUE;

	/** (match generator) Default neighborhood to spot a phrase, beyond the number of words in the phrase */
	static public Integer phraseLength = 2; // span phraseLength + numArgs 
	/** (match generator) Minimum number of characters in a token */
	static public Integer minTokenLen  = 1; // tokens must at least this long

	/** (match generator) Display format for the index display name */
	public static String indexKeyFormat = "%25s";	//Format controls

	/** (match generator) Stopwords to add, in addition to the ones hardcoded into StopperText.java */
	public static String additionalStopwords="";

	/** (match generator) Default name for text analyzer, if not defined in NVP that defines the index */
	public static String analyzerName = "SimpleAnalyzer";
	/** (match generator) Default name for text field, if not defined in NVP that defines the index */
	public static String fieldName = "contents";
	/** (match generator) Default name for search criterion, if not defined in NVP that defines the index */
	public static String queryFormat = "ALL";
	/** (match generator) Default names of indices to use for validation, if not overidden in properties or command line */
	public static String indexNames = " contents,company";

	public static Integer _arraylistAllocation = 8;
	// Text processing

	//public static String patternString ="[\\w&&[^_]]+"; // cannonical splitter
	/** (match generator) String that defines what a token may consist of */
	public static String patternString ="([\\w&&[^_]]+[-&']?)+"; // does not split on embedded hyphen or amper

//	above, also allows embedded numeric comma when there isn't a leading zero
	/** (match generator) Allow digit strings (no leading zero) with embedded comma,
	 *  also do not split on embedded hyphen or amper<br> 
	 *  1,000,000,000,010,4  &rarr; 
	 *  02,000,000,000,010,7 &rarr; 
	 *  &nbsp;&nbsp;intprefix &larr; <code>[1-9][0-9]*,</code>
	 *  &nbsp;&nbsp;int &larr; <code>
	 *  
	 *  
	 *  
	 *  
	 *  */
	public static String _patternString ="(:?[1-9]([0-9]*,?)([0-9]+,?)*)|(:?[\\w&&[^_]]+[-&']?)+"; 

	/** (match generator) Remove apostrophe-based suffixes and prefixes? from input names? */
	public static Boolean removeCommonPunctuation=true;
	/** (match generator) Process as sets of words instead of lists of words (deduplicate) */
	public static Boolean processPhraseAsSet=true;
	/** (match generator) Replace foreign accented characters with their US normal characters */
	public static Boolean AmericanizeDyacritics=true;
	/** (match generator) Characters that must be escaped when posing queries to internal search engines */
	public static final char[] escapables = new char[] { '\\','+', '-', '&', '|', '!', '(', ')', '{', '}','[', ']', '^', '"', '~', '*', '?', ':'  };
	/** (match generator)  Characters that should be dropped when posing queries to internal search engines */
	public static char[] dropables = new char[] { '(', ')', ',', '"' , '\\'};
	/** (match generator)  String format of escapables */
	public static String escStr = String.copyValueOf(escapables);
	/** (match generator)  String format of droppables */
	public static String dropStr= String.copyValueOf(dropables);
	/** (match generator)  Regex pattern for droppables */
	public static String dropPatternString = "\\(|\\)|'|\"|\\\\";
	/** (match genreator)  Drop things that are totally numeric? */
	public static Boolean dropNumericStrings=false;

	public final static Double OneMinusEpsilon = 1.0D - 10 * Double.MIN_VALUE;

	public static Integer percentile = 90;


	/** (match generator)   size of general caches */
	public final static int cacheSize=1024;
	/** (match generator)   size of general caches */
	public final static float  cacheLoadFactor=0.75f;
	//In-memory document store
	/** (match generator)   size of document caches */
	public final static int _documentStore_cacheSize=1024;
	/** (match generator)   size of document caches */
	public final static float _documentStore_cacheLoadFactor=0.75f;
	//Corpus sample hit counter
	/** (match generator)   size of hit-counter caches */
	public final static int    _hitCounter_Cache_size=1024;
	/** (match generator)   size of hit-counter caches */
	public final static float  _hitCounter_Cache_loadFactor=0.75f;

	public static Integer _NCACHE_maxNeighborhood = 500;
	public static Integer _NCACHE_maxHCbound = 512;

	/** (trie-based matching) Number of results that matcher should provide as output list */
	public static int maxResultCount     = 25;
	/** (match generator)  Number of results that matcher should hold for internal aggregation and maximization */
	public static int maxInternalResults = 4096;
	/** (match generator)  Ranking control, for scoring of keymatch search results */
	public static Double INORDERbonus = 0.25D; 	// Boost for strings matched in order

	/** (trie-based matching) Scoring of aggregated results */
	public final static int AGGREGATION_INCR = 2;
	/** (trie-based matching)  Scoring of aggregated results by "ALL" option */
	public final static int RULETYPEscoreFactor=2;     //3,//1,
	/** (trie-based matching)  Scoring of aggregated results by "ALL" option */
	public final static int MATCHLENGTHscoreFactor=3;  //10;//1
	/** (trie-based matching)  Scoring of aggregated results by "ALL" option */
	public final static int SCALE_FACTOR=100;

	public final static String DEFAULT_AGGREGATOR_BASE = "trieMatch.util.aggregation";
	public final static String DEFAULT_AGGREGATOR_NAME = "AggregateSUMacc";
	public final static String DEFAULT_AGGREGATOR_PARM = null;
	public final static Integer DEFAULT_AGGREGATION_INCR = 1;
    public final static Integer DEFAULT_AGGREGATION_SCALEFACTOR = 100000;
	public final static Aggregator DEFAULT_AGGREGATOR = new AggregateSUM();

	/** (input) */
	public static Boolean showItemWarnings=false;
}
