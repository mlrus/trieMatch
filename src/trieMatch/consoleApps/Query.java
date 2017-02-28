//This is unpublished source code. Michah Lerner 2006

package trieMatch.consoleApps;

//Non-threaded driver for keymatches.

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import trieMatch.keywordMatcher.KeyMatch;
import trieMatch.keywordMatcher.Tiers;
import trieMatch.keywordMatcher.TrieMatcher;
import trieMatch.keywordMatcher.KeyMatch.EvalType;
import trieMatch.keywordMatcher.KeyMatch.MatcherActionDefinition;
import trieMatch.util.Constants;
import trieMatch.util.TOD;

public class Query {

	final int keymatchFile = 0, testInputsfile = 1, outputFile = 2;
	PrintStream outstream;
	static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	static String defaultArgstring = "c:/temp/matchtext.base.031 $in - -exp";
	String filler = String.format("%105s | ", "");
	int nominalLength = 92; 
	boolean _useDP= false;
	boolean _echo = false;               // echo match inputs?
    boolean SHOWALL=false;  // show all results, or just show the top result?
	String aggregatorName = Constants.DEFAULT_AGGREGATOR_NAME;
	String aggregatorParm = Constants.DEFAULT_AGGREGATOR_PARM;		

	MatcherActionDefinition resultRequest = MatcherActionDefinition.longest;
	String kmFilename, queryFilename, outputName;
	EvalType evalType;
	static int numTestcols     = 2;
	static int testQueryCol    = 1;
	static int testResponseCol = 0;
	boolean usesSymbology = false;
	boolean echo;

	public static void main(String[] args) {		
		System.out.println(TOD.now() + ": EXECUTION BEGINS");
		Query query = new Query();
		LocalStore localStore = query.setup(args);
		List<String> res = null;
		while((res=query.runMatch(localStore))!=null)
			for(String s : res) System.out.println(s);
		System.out.println(TOD.now() + ": EXECUTION COMPLETE");
		System.exit(0);
	}

	public class LocalStore {
		KeyMatch kmPrimary;
		TrieMatcher trieMatcher;
	}

	public LocalStore setup(String[] args) {
		processArgs(args);
		LocalStore localStore=new LocalStore();
		Tiers.mapClass=resultRequest.getMapClass(); 
		TrieMatcher.setAggregator(aggregatorName, aggregatorParm);
		System.out.println(TOD.now() + ": begin reading keymatches from " + kmFilename);
		try {
			localStore.kmPrimary = new KeyMatch(kmFilename);
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println(TOD.now() + ": read " + 
				localStore.kmPrimary.symbolToName.getSize() + " symbols " + 
				localStore.kmPrimary.symbolToName.getFullSize() + " descriptions.");
		localStore.trieMatcher = resultRequest.select(localStore.kmPrimary.tiers, localStore.kmPrimary.elementIndexer);
		return localStore;
	}

	public List<String> runMatch(LocalStore localStore) {
		return runMatch(localStore,null);
	}

	public List<String> runMatch(LocalStore localStore, String query) {
	    if(query!=null) {
	        System.out.println("QUERY="+query);
	        List<String>ans=eval(localStore,query.trim());
	        return ans;
	    }
	    if (queryFilename.equalsIgnoreCase("$in")) {
	        return readEval(localStore);
	    }
	    List<String> queryList = readQueries(queryFilename);
	    showConfiguration(localStore.kmPrimary, queryList);
	    System.out.println(TOD.now() + ": Begin writing results to file " + outputName);		
	    long t0=System.currentTimeMillis();
	    for(String q : queryList) {
	        List<String>ans = eval(localStore, q);
	        if(outstream!=null) {
	            for(String s:ans) {
	                outstream.printf("%50s ==> %s\n",q,s);
	                if(!SHOWALL)break;
	            }
	        } 
	    }
	    long t1=System.currentTimeMillis();
	    long totalTime=t1-t0;	
	    showStats(totalTime,queryList.size());
	    return null;
	}

	public void showStats(long mtime, int nQueries) {
		System.out.println(TOD.now() + ": Issued total of " + nQueries + " requests");
		System.out.println(TOD.now() + ": " + "'single'" + " thread;  " + "all" + " queries/batch; " + "one"
				+ " batch generated, " + "one" + " started, " + "one"
				+ " returned, " + "one" + " completed.");
		System.out.println(TOD.now() + ": ::SUMMARY:: " +
				"queryRate=" + (nQueries / (double) mtime) + " qpms" +
				";  queriesProcessed=" + nQueries +
				";  showResults="+(outstream==null?"suppressed":"enabled") +
				";  timeElapsed=" + mtime + " ms" +
				";  timeUtilized=" + RequestProcessor.pTime() + " ms" +
				";  numThreads=" + "'one'" + 
				";  chunkSize=" + "all" +" queries;  " +
				summarizeArgs(nQueries));
	}	

	public String summarizeArgs(int nItems) {
		return "numInputs=" + nItems + ";  numIter=" +
		"one" + 
		";  numThread=" + "'one'" +
		";  aggregator="+aggregatorName+(aggregatorParm!=null?("("+aggregatorParm+")"):"")+
		";  searchFlavor="+resultRequest+";  keymatchFile=\""+kmFilename+
		"\";  queryFile=\""+queryFilename+"\"";
	}

	public List<String> readEval(LocalStore localStore) {
		System.out.println("?");
		String queryText = getLine().trim();
		return eval(localStore, queryText);
	}

	public List<String> eval(LocalStore localStore, String queryText) {
		return localStore.kmPrimary.processQuery(localStore.trieMatcher, queryText);
	}

	public String getLine(){
		String s=null;
		try {
			while (!in.ready())
				Thread.sleep(100);
			s = in.readLine();
			return s;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return s;
	}

	static String getQueryPart(String[] line) {
		if(testQueryCol<line.length)return line[testQueryCol];
		return null;
	}
	static String getResponsePart(String[] line) {
		if(testResponseCol<line.length)return line[testResponseCol];
		return null;
	}

	public void usage() {
		System.err.println("usage: java -jar file.jar [options] keymatchFilename testinputsFilename [outputFilename]");
		System.err.println("       options: -all/-longest/-top/-exp       -echo/-noEcho    -deleteOutputFile  -preExpand  -permuteExpand");
		System.err.println("                -nArg #");
		System.err.println("\n       the -exp gives \"experimental\" prefix matching and supports \"lookahead\".");
		System.err.println("\n       For interactive input use: java -Xmx1540M -jar c:\\mlrus\\kmQ.jar 5000.input $in - -all");
		System.exit(0);
	}

	public void processArgs(String[] args) {
		boolean deleteOutputfile = false;
		List<String> filenames = new ArrayList<String>();
		evalType=EvalType.eINP;
		boolean grab_numTestcols = false;
		for (int i = 0; i < args.length; i++) {
			if (args[i].equalsIgnoreCase("-h"))
				usage();
			else if (grab_numTestcols) {
				numTestcols=Integer.parseInt(args[i]); 
				grab_numTestcols=false;
				System.out.println("Expect " + numTestcols + " of input per test line");
			}
			else if (args[i].equalsIgnoreCase("-aggName"))aggregatorName = args[++i];
			else if (args[i].equalsIgnoreCase("-aggParm"))aggregatorParm = args[++i];		
			else if (args[i].equalsIgnoreCase("-all")) resultRequest = MatcherActionDefinition.all;
			else if (args[i].equalsIgnoreCase("-longest")) resultRequest = MatcherActionDefinition.longest;
			else if (args[i].equalsIgnoreCase("-top")) resultRequest = MatcherActionDefinition.top;
			else if (args[i].equalsIgnoreCase("-exp")) resultRequest = MatcherActionDefinition.exp; // EXPERIMENTAL !!
			else if (args[i].equalsIgnoreCase("-echo")) echo=true;
			else if (args[i].equalsIgnoreCase("-noEcho")) echo=false;
			else if (args[i].equalsIgnoreCase("-nArg")) grab_numTestcols=true;
			else if (args[i].equalsIgnoreCase("-deleteOutputFile"))
				deleteOutputfile = true;
			else
				filenames.add(args[i]);
		}
		filenames.add(null);
		if (filenames.size() < 2) {
			usage();
			System.exit(0);
		}
		kmFilename = filenames.get(keymatchFile);
		queryFilename = filenames.get(testInputsfile);
		outputName = filenames.get(outputFile);
		checkOutputfile(outputName, deleteOutputfile);
		try {
			outstream = (outputName != null) ? safePrintStream(outputName) : null;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public  void showArgs() {
		try {
			System.out.println(TOD.now() + ": Keymatches from " + (new File(kmFilename).getCanonicalPath()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(TOD.now() + ": Aggregator name " + aggregatorName);
		System.out.println(TOD.now() + ": Aggregator parm " + aggregatorParm);
		System.out.println(TOD.now() + ": Test input from " + queryFilename);
		System.out.println(TOD.now() + ": Results outputs " + (outputName == null ? "not printed" : outputName));
		System.out.println(TOD.now() + ": Results outputs " + resultRequest + " result.");
		System.out.println(TOD.now() + ": Compute evalType " + evalType);
	}

	public  void showConfiguration(KeyMatch kmPrimary, List<String> queryList) {
		if (echo) {
			System.out.println("Rules:");
			kmPrimary.tiers.printTiers(System.out);
		}
	}

	List<String> readQueries(String testInputs) {
		BufferedReader queryFile;
		try {
			queryFile = KeyMatch.openInput(testInputs);
			List<String> queries = new ArrayList<String>();
			do {
				String s = queryFile.readLine();
				if (s == null || s.length() == 0) continue;
				queries.add(s);//.toLowerCase(Locale.getDefault()));
			} while (queryFile.ready());
			queryFile.close();
			return queries;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public  String showAndSetFmt(String query, String tkr, int size) {
		outstream.print("\nName:" + query + ".");
		if (tkr != null) outstream.print(" \tSymbol: \"" + tkr + "\".");
		outstream.println(" \tSubquery count: " + size + ".");
		return String.format("%%%ds | dEdit=%%-2d | ", nominalLength - query.length() - (tkr != null ? tkr.length() : 0));
	}

	public static  boolean checkOutputfile(String name, boolean deletableOutputfile) {
		if(stdFilename(name))return false;
		File f = new File(name);
		if (!f.exists() || (deletableOutputfile && f.isFile() && f.delete())) return true;
		return false;
	}

	static boolean stdFilename(String filename) {
		return filename == null || filename.length() == 0 || filename.equals("-");
	}

	PrintStream fileFoundHandler(String s) throws FileNotFoundException {
		throw new FileNotFoundException("File exists: " + s);
	}

	public  BufferedReader openInput(String filename) throws Exception {
		return new BufferedReader((stdFilename(filename) ? (new InputStreamReader(System.in)) : (new FileReader(filename))));
	}

	public PrintStream safePrintStream() throws FileNotFoundException {
		return safePrintStream(null);
	}

	public PrintStream safePrintStream(String filename) throws FileNotFoundException {
		return stdFilename(filename) ? (new PrintStream(System.out)) : ((!(new File(filename)).exists()) ? (new PrintStream(
				filename)) : fileFoundHandler(filename));
	}
}
