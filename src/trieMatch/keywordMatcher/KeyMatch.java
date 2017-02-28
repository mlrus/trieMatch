//This is unpublished source code. Michah Lerner 2006

package trieMatch.keywordMatcher;

//Keymatch record processing -- read, store and result aggregation

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import trieMatch.util.Constants;
import trieMatch.util.coll.L2Map;
import trieMatch.util.coll.Pair;

/**
 * Key match record processing.  Provides read, store and result aggregation. 
 * @author Michah.Lerner
 *
 */
public class KeyMatch {


	public enum MatchType { // "Enum constants are given ordinal values based on the order they are declared ..."
		/** Accepts when the template matches a subset of the input text */
		KEYWORDMATCH, 
		/** Accepts when the template matches an ordered subset of the input text (first-to-last, skipped words OK) */
		INORDERMATCH, 
		/** Accepts when the tempalte matches a sequence of input text (can skip only at start and end, not between words)  */
		PHRASEMATCH, 
		/** Accept only when the template matches all words of the input, in the same order of occurrence */
		EXACTMATCH;
	}

	/** 
	 * Allow prepreocessing of input text according to the code given.  For testing.
	 * @author Michah.Lerner
	 *
	 */
	public enum EvalType {
		eNONE, 
		/** input */
		eINP, 
		/** combinations */
		eCOM, 
		/** permutations */
		ePCOM
	} 

	public enum MatcherActionDefinition {
		/** Search for the longest match, ignoring the accumulation multiple matches to the same entity */
		longest {
			@Override
			public Class getMapClass() {
				return new SortedtrieMatchLONGEST().getMapClass();
			}
			@Override
			public TrieMatcher select(Tiers tiers, Sequencer sequencer) {
				return new SortedtrieMatchLONGEST(tiers, sequencer);
			}
			@Override
			public TrieMatcher select(Tiers tiers) {
				return new SortedtrieMatchLONGEST(tiers);
			}
		},
		/** Acquire all matches, accumulating the partial scores of multiple matches to the same entity */
		all {
			@Override
			public Class getMapClass() {
				return new SortedtrieMatchALL().getMapClass();
			}
			@Override
			public TrieMatcher select(Tiers tiers, Sequencer sequencer) {
				return new SortedtrieMatchALL(tiers, sequencer);
			}
			@Override
			public TrieMatcher select(Tiers tiers) {
				return new SortedtrieMatchALL(tiers);
			}
		}, 
		/** Return just the first result(s) of the <code>all</code> form of matcher */
		top {
			@Override
			public Class getMapClass() {
				return new SortedtrieMatchTOP().getMapClass();
			}
			@Override
			public TrieMatcher select(Tiers tiers, Sequencer sequencer) {
				return new SortedtrieMatchTOP(tiers, sequencer);
			}
			@Override
			public TrieMatcher select(Tiers tiers) {
				return new SortedtrieMatchTOP(tiers);
			}
		}, 
		exp {
			@Override
			public Class getMapClass() {
				return new SortedtrieMatchEXP().getMapClass();
			}
			@Override
			public TrieMatcher select(Tiers tiers, Sequencer sequencer) {
				return new SortedtrieMatchEXP(tiers, sequencer);
			}
			@Override
			public TrieMatcher select(Tiers tiers) {
				return new SortedtrieMatchEXP(tiers);
			}
		};
		public abstract Class getMapClass();
		public abstract TrieMatcher select(Tiers tiers, Sequencer sequencer);
		public abstract TrieMatcher select(Tiers tiers);
	}

	public Tiers tiers;
	public Sequencer elementIndexer = null;
	public L2Map<String, String> symbolToName = new L2Map<String, String>(); // aux optional structure
	public Integer nItems = null;
    public String sourceFilename = null;
    
	/**
	 * Allocate a totally new keymatcher.  This should only be done once per filename,
	 * since it consumes storage and we have no need to proliferate read-only copies
	 * of the tier stucture.
	 */

	public KeyMatch() {
		elementIndexer = new Sequencer(true);
		tiers = new Tiers(elementIndexer);
	}

	/**
	 * Allocate and read.  Makes a totally new keymatcher, and initializes it 
	 * with the contents of the given file.
	 * @param filename a file containing keymatch terms [keys,type,name,description]
	 * @throws IOException 
	 */
	public KeyMatch(String filename) throws IOException {
		this();
		readKeywordMatches(filename);
	}

	/**
	 * Allocates a keymatch with a full copy of the tier structure.  Suitable only for 
	 * shipping the structure to another CPU in the non-shared memory environment.  Do
	 * not use this function in the shared memory environment. 
	 * @param km
	 */
	public KeyMatch(KeyMatch km) {
		elementIndexer = new Sequencer(true);
		tiers = new Tiers(km.tiers);
	}

	/**
	 * Process a query by getting the answer from the configured trieMatcher
	 * and then aggregating the responses, returning a formatted result.
	 * @param trieMatcher
	 * @param queryText
	 * @return list of formatted results
	 */

	public List<String>processQuery(TrieMatcher trieMatcher, String queryText) {
		List<KMDefinition> ans = trieMatcher.findMatch(queryText);
		Collection<Pair<String, Integer>> rans = trieMatcher.reduce(ans);
		return formatResults(rans);
	}	

	public List<String>formatResult(Collection<KMDefinition>kmdefs) {
		List<String>answerList=new ArrayList<String>();
		int maxEmit=Constants.maxResultCount;
		for(KMDefinition kmd : kmdefs) {
			answerList.add(formatResult(kmd));
			if(--maxEmit<=0)break;
		}
		return answerList;
	}

	List<String> formatResults(Collection<Pair<String, Integer>> rans) {
		List<String>answerList=new ArrayList<String>();
		int maxEmit=Constants.maxResultCount;
		for (Pair<String, Integer> st : rans) {
			String nameFound = (symbolToName.get(st.s()).toArray(new String[1]))[0];
			String ans = String.format("%-15s  %s (score=%d)", st.s(),  nameFound, st.t());
			answerList.add(ans);
			if (maxEmit--<=0) break;
		}
		return answerList;
	}
	String formatResult(KMDefinition kmd) {
		return String.format("%-15s  %s (score=%d)", kmd.url, kmd.description, kmd.score);
	}

	static Long freememory;
	public static void doGC() {
		Runtime.getRuntime().gc();
		if(freememory==null)freememory=Runtime.getRuntime().freeMemory();
		else System.out.println("Consumed " + (freememory-Runtime.getRuntime().freeMemory()));
	}

	void readKeywordMatches(String filename) throws IOException {
		BufferedReader input;
        int lineNo = 0;
        if (elementIndexer == null) elementIndexer = new Sequencer(true);
		elementIndexer.reuseStorage=true;
		try {
			input = openInput(filename);
			String line;
			while (input.ready()) {
				line = input.readLine();
				lineNo++;
				if (line == null || line.length() == 0) continue;
				KMDefinition kmDef = new KMDefinition(line, elementIndexer);
				tiers.add(kmDef);
				symbolToName.add(kmDef.url, kmDef.description);
			}
			elementIndexer.reuseStorage=false;
            input.close();
			//tiers.printTiers(System.out);
		} catch (Exception e) {
			try {
				System.out.println("error reading " + (new File(filename)).getCanonicalPath());
			} catch (IOException e1) {
				e1.printStackTrace();
				throw new IOException(filename);
			}
			e.printStackTrace();
		}
        nItems=lineNo;
        sourceFilename=filename;
	}

	static boolean stdFilename(String filename) {
		return filename == null || filename.length() == 0 || filename.equals("-");
	}

	public static BufferedReader openInput(String filename) throws IOException {
		return new BufferedReader((stdFilename(filename) ? (new InputStreamReader(System.in)) : (new FileReader(filename))));
	}
}
