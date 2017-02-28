//This is unpublished source code.


/* Pedantic version for illustrative purposes only */
/* Version is not validated and is not threadsafe. */

package trieMatch.pedanticMatcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeSet;
import java.util.Map.Entry;

/**
 * Build key match definitions, and reduce multiple matches to an entity.  <em>Pedantic "sweet" version</em>.
 * @author Michah.Lerner
 *
 */
public class KMatch {
	
	public class ValueRecord implements Comparator<ValueRecord> {
		int score;
		String name;
		MatchType kType;
		List<String> keys;
		List<Integer> keyIndices;
		String url;
		String description;
		
		ValueRecord(String line) {
			this(line.split("[ \t]*,[ \t]*"));		
		}
		
		ValueRecord(String[] fields) {
			this(fields[0],fields[1],fields[2],fields[3]);
		}
		
		ValueRecord(String name, String type, String url, String description) {
			this.name = name;
			this.kType = MatchType.valueOf(type.toUpperCase(Locale.getDefault()));
			this.url = url;
			this.description = description;
			SortedIndexCollector.IndexedItem indexedItem = instanceCollector.indexedItem(name);
			this.score = indexedItem.queryLength;
			this.keys = indexedItem.stringList;
			this.keyIndices = indexedItem.indexList;
		}

		public ValueRecord() {
			score = Integer.MIN_VALUE;
		}

		int weight() {
			return score * 3 + kType.ordinal();
		}

		public int compare(ValueRecord o1, ValueRecord o2) {
			if (o1.url.equals(o2.url)) return (o1.weight()-o2.weight());
			return (o1.url.compareToIgnoreCase(o2.url));
		}
	}
	
	public Tiergen keysetStore;
	public Map<Collection<String>, Collection<ValueRecord>> KMatchDetail;
	SortedIndexCollector instanceCollector;

	public KMatch(boolean addall, boolean exp, String filename) {
		keysetStore = new Tiergen(addall,exp);
		KMatchDetail = new HashMap<Collection<String>, Collection<ValueRecord>>();
		instanceCollector = new SortedIndexCollector();
		readKeywordMatches(filename);
	}
	public KMatch(boolean addall, String filename) {
		this(addall,false,filename);
	}
	
	public KMatch(KMatch km) {
		this.keysetStore = new Tiergen(km.keysetStore.addall);
		this.keysetStore = km.keysetStore; 
		this.keysetStore.depthStack = new Stack<Integer>();
		this.KMatchDetail = km.KMatchDetail;
		this.instanceCollector = new SortedIndexCollector();
	}
	
	void readKeywordMatches(String filename) {
		BufferedReader input;
		try {
			input = openInput(filename);
			String line;
			int lineNo = 0;
			int dup = 0;
			while (input.ready()) {
				line = input.readLine();
				lineNo++;
				if (line == null || line.length() == 0) continue;
				ValueRecord valueRecord = new ValueRecord(line);
				keysetStore.add(valueRecord.keys, valueRecord);
				if (!KMatchDetail.containsKey(valueRecord.keys))
					KMatchDetail.put(valueRecord.keys, new ArrayList<ValueRecord>()); else dup++;
				KMatchDetail.get(valueRecord.keys).add(valueRecord);
			}
			System.out.println(new Date() + ": read " + lineNo + " lines from input " + filename + ", " + dup + " dups");			input.close();
		} catch (Exception e) {
			try {
				System.out.println("error reading " + (new File(filename)).getCanonicalPath());
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		}
	}
	
	List<String> readQueries(String testInputs) {
		BufferedReader queryFile;
		try {
			queryFile = KMatch.openInput(testInputs);
			List<String> queries = new ArrayList<String>();
			do {
				String s = queryFile.readLine();
				if (s == null || s.length() == 0) continue;
				queries.add(s.toLowerCase(Locale.getDefault()));
			} while (queryFile.ready());
			queryFile.close();
			return queries;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	void processFile(String in, String out, KMatch km) throws FileNotFoundException {
		int qcounter = 0;
		long sumNano = 0;
		long tSearch, tStop;
		PrintStream outstream = (out != null) ? safePrintStream(out) : null;
		KMatch km2 = new KMatch(km);
		List<String>queries = readQueries(in);
		for(String s : queries) {
				if (s == null || s.length() == 0) continue;
				tSearch = System.nanoTime();
				List<ValueRecord> ans = km2.keysetStore.findMaxMatch(s.toLowerCase(Locale.getDefault()));
				Collection<Entry<String, Integer>> rans = reduce(ans);
				tStop = System.nanoTime();
				sumNano += (tStop-tSearch);
				tStop = tStop - tSearch;
				qcounter++;
				if (outstream != null) {
					outstream.println("\nTestcase: " + s);
					outstream.println("Results: " + ans.size() + " total, " + rans.size() + " unique, time = " + tStop + " ns.");
						for (Entry<String, Integer> st : rans)outstream.println("  " + st.getKey() + " \tscore="+st.getValue());
						for (int spacer = 0; spacer < ans.size() - rans.size(); spacer++)
							outstream.println();
				}
				ans = null;
			} 
		
		System.out.printf("%s: queries processed=%d, search time=%8.3e sec, %8.3e sec/query",
				new Date(), qcounter, (sumNano/1.0e9), (sumNano / (1.0e9 * qcounter)));
		
		System.out.println(" [structure="+km2.keysetStore.tiers.tier.getClass().getName()+"]");
	}

	public Collection<Entry<String, Integer>> reduce(List<ValueRecord> list) {
		if (list == null || list.size() == 0) return new ArrayList<Entry<String,Integer>>();
		RecordTally st = new RecordTally();
		for (ValueRecord vr : list)
			st.addItem(vr, vr.weight());
		Collection<Map.Entry<String, Integer>> res = st.sortEntries();
		return res;
	}

	public class RecordTally {
		Map<String, Integer> smap;
		RecordTally() {
			smap = new HashMap<String, Integer>();
		}

		void addItem(ValueRecord vr, int tally) {
			Integer i = smap.get(vr.url);
			if(i==null)i=0;
			smap.put(vr.url,i+tally);
		}

		SortedSet<Entry<String, Integer>> sortEntries() {
			SortedSet<Map.Entry<String,Integer>> sss = new TreeSet<Map.Entry<String,Integer>>(new CompareVREntries());
			sss.addAll(smap.entrySet());
			return sss;
		}
		
		class CompareVREntries implements Comparator<Map.Entry<String, Integer>> {
			public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
				if(o1.getValue().equals(o2.getValue()))return o1.getKey().compareTo(o2.getKey());
				return -o1.getValue().compareTo(o2.getValue());
			}
		}
		
		List<Map.Entry<String, Integer>> sortEntries2() {
			List<Map.Entry<String, Integer>> lme = new ArrayList<Map.Entry<String, Integer>>(smap.entrySet());
			Collections.sort(lme, new CompareVREntries());
			return lme;
		}

	}

	enum MatchType { // "Enum constants are given ordinal values based on the order they are declared ..."
		KEYWORDMATCH, INORDERMATCH, PHRASEMATCH, EXACTMATCH;
	}

	static boolean stdFilename(String filename) {
		return filename == null || filename.length() == 0 || filename.equals("-");
	}

	static PrintStream fileFoundHandler(String s) throws FileNotFoundException {
		throw new FileNotFoundException("File exists: " + s);
	}

	public static BufferedReader openInput(String filename) throws Exception {
		return new BufferedReader((stdFilename(filename) ? (new InputStreamReader(System.in)) : (new FileReader(filename))));
	}

	static public PrintStream safePrintStream() throws FileNotFoundException {
		return safePrintStream(null);
	}

	static public PrintStream safePrintStream(String filename) throws FileNotFoundException {
		return stdFilename(filename) ? (new PrintStream(System.out)) : ((!(new File(filename)).exists()) ? (new PrintStream(
				filename)) : fileFoundHandler(filename));
	}

}
