//This is unpublished source code. Michah Lerner 2006

package trieMatch.consoleApps;

//Multi-thread driver, with thread queues based on O'Reilly's Java Cookbook

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import trieMatch.consoleApps.Query.LocalStore;
import trieMatch.keywordMatcher.KeyMatch;
import trieMatch.keywordMatcher.Tiers;
import trieMatch.keywordMatcher.TrieMatcher;
import trieMatch.keywordMatcher.KeyMatch.EvalType;
import trieMatch.keywordMatcher.KeyMatch.MatcherActionDefinition;
import trieMatch.util.Constants;
import trieMatch.util.TOD;
import trieMatch.util.coll.L2Map;

/**
 * Class MultiQuery provides a invokes the fast trie matcher with multiple threads.  This is a thread-safe
 * Java class.  All threads share the same copy of the read-only trie index. Each thread has its own local
 * store for parameters, stack, explicit recursions, etc.
 * @author Michah.Lerner
 *
 */
public class MultiQuery {
	static final int _numThreads = 4;       // default # threads
	static final int _chunkSize = 100;      // default chunksize
	static final boolean _echo = false;     // echo match inputs?
	static final int keymatchFile = 0, testInputsfile = 1, outputFile = 2;
	static boolean SHOWALL=false;
	static int chunkSize;
	static int numThreads;
	public static MatcherActionDefinition searchFlavor = MatcherActionDefinition.longest;
	static boolean echo;
	static EvalType evalType;
	static boolean verify=false;
	static int iterations;
	static KeyMatch kmPrimary=null;	
	static PrintStream outstream;
	static String inputKMFilename;
	static CountDownLatch processingDone=null;
	static boolean deleteOutputfile = false;
	static List<String> filenames = new ArrayList<String>();
	static String kmFilename = null, queryFilename = null, outputName = null;
	static String aggregatorName = Constants.DEFAULT_AGGREGATOR_NAME;
	static String aggregatorParm = Constants.DEFAULT_AGGREGATOR_PARM;	

	public static void main(String args[]) throws Exception {
		System.out.println(TOD.now() + ": ");
		processArgs(args);
		showArgs();
		Tiers.mapClass=searchFlavor.getMapClass();  // !!must set class before use!!
		TrieMatcher.setAggregator(aggregatorName, aggregatorParm);
		kmPrimary = new KeyMatch(kmFilename);
		List<String> queryList = readQueries(queryFilename);
		showConfiguration(queryList.size());
		System.out.println(TOD.now() + ": Begin processing " + queryFilename);
		processQuerylist(queryList);
		System.out.println(TOD.now() + ": EXECUTION COMPLETE");
		System.exit(0);
	}

	public static void usage() {
		System.err.println("usage: java -jar file.jar [options] keymatchFilename testinputsFilename [outputFilename]");
		System.err.println("       options:  -all/-longest/-top    -echo/-noEcho      -deleteOutputFile");
		System.err.println("                 -TH=#threads          -IT=#iterations    -CH=#queries/iteration/thread");
		System.err.println("                 -VERIFY               -pre[combinations [-per[mute also]]");
		System.exit(0);
	}

	/**
	 * Generate list of work rowID, with "chunkSize" rowID to process
	 * @param allQueries
	 * @return List of work rowID.
	 */
	public static ArrayList<Request> enqueueQueries(List<String> allQueries) {
		ArrayList<Request> allRequests = new ArrayList<Request>();
		List<String> queries = new ArrayList<String>();
		for (String query : allQueries) {
			queries.add(query);
			if (queries.size() == MultiQuery.chunkSize) {
				allRequests.add(new Request(queries));
				queries = new ArrayList<String>();
			}
		}
		if (queries.size() > 0) allRequests.add(new Request(queries));
		return allRequests;
	}

	static void processArgs(String args[]) {
		echo = _echo;
		chunkSize = _chunkSize;
		numThreads = _numThreads;
		iterations = 0;
		evalType=KeyMatch.EvalType.eINP;
		for (int i = 0; i < args.length; i++) {
			if (args[i].equalsIgnoreCase("-h")) {
				usage();
			} 
			else if (args[i].equalsIgnoreCase("-aggName")) aggregatorName = args[++i];
			else if (args[i].equalsIgnoreCase("-aggParm")) aggregatorParm = args[++i];	
			if (args[i].equalsIgnoreCase("-all")) {
				searchFlavor = MatcherActionDefinition.all; }
			else if (args[i].equalsIgnoreCase("-longest")) {
				searchFlavor = MatcherActionDefinition.longest; }
			else if (args[i].equalsIgnoreCase("-top")) {
				searchFlavor = MatcherActionDefinition.top; }
			else if (args[i].equalsIgnoreCase("-exp")) {
				searchFlavor = MatcherActionDefinition.exp; }       // prefix
			else if (args[i].startsWith("-TH=")) {
				numThreads = Integer.parseInt(args[i].substring(4));
				System.out.println("set # threads = " + numThreads);
			} else if (args[i].startsWith("-CH=")) {
				chunkSize = Integer.parseInt(args[i].substring(4));
				System.out.println("set # rowID per iteration per thread = " + chunkSize);
			} else if (args[i].startsWith("-IT=")) {
				iterations = Integer.parseInt(args[i].substring(4));
				System.out.println("set # iterations = " + iterations);
			} else if (args[i].equalsIgnoreCase("-echo")) echo = true;
			else if (args[i].equalsIgnoreCase("-noEcho")) echo = false;
			else if (args[i].equalsIgnoreCase("-verify")) verify=true;
			else if (args[i].equalsIgnoreCase("-deleteOutputFile"))deleteOutputfile = true;
			else
				filenames.add(args[i]);
		}
		filenames.add(null);
		if (filenames.size() < 2) {
			usage();
			System.exit(0);
		}
		kmFilename = filenames.get(keymatchFile);
		MultiQuery.inputKMFilename = kmFilename;
		queryFilename = filenames.get(testInputsfile);
		outputName = filenames.get(outputFile);
		Query.checkOutputfile(outputName, deleteOutputfile);
		try {
			outstream = (outputName != null) ? safePrintStream(outputName) : null;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public static void showArgs() {
		System.out.println(TOD.now() + ": Aggregator name " + aggregatorName);
		System.out.println(TOD.now() + ": Aggregator parm " + aggregatorParm);
		System.out.println(TOD.now() + ": Keymatches from " + kmFilename);
		System.out.println(TOD.now() + ": Test input from " + queryFilename);
		System.out.println(TOD.now() + ": Results outputs " + (outputName == null ? "not printed" : outputName));
		System.out.println(TOD.now() + ": Compute " + searchFlavor + " result.");
	}

	public static void showConfiguration(int nItems) {
		if (iterations > 0) {
			chunkSize = (int) Math.ceil((double) nItems / iterations / numThreads);
			System.out.println("Requested " + iterations + " iterations, reset chunkSize to " + chunkSize);
		}
		System.out.println(TOD.now() + ": #input=" + nItems + " #iterations="
				+ (int) Math.ceil((double) nItems / (numThreads * chunkSize)) + " numThreads=" + numThreads
				+ " chunksize=" + chunkSize);
		if (echo) kmPrimary.tiers.printTiers(System.out);
	}

	public static void showStats() {
		System.out.println(TOD.now() + ": Issued total of " + RequestProcessor.numQueries() + " requests using " +
				+ numThreads + " threads, " + chunkSize + " queries/batch; " + Request.numRequests()
				+ " batches generated, " + RequestProcessor.numStarted() + " started, " + RequestProcessor.numReturned()
				+ " returned, " + RequestProcessor.numCompleted() + " completed.");
	}

	public static void showStats(long mtime, int nQueries) {
		showStats();
		
		System.out.printf("%s: ::SUMMARY:: queryRate=%7.3f qpms,  queriesProcessed=%-6d,  " +
				"showResults=%12s;  " +
				"timeElapsed=%-8d ms;  " +
				"timeUtilized=%-8d ms;  " +
				"numThreads=%-3d;  " +
				"queries/chunk=%-6d;  " +
				"chunks/thread=%-6d;  " +
				"aggregator=%s;  flavor=%s;  keymatches=%s;  queries=%s\n",
				TOD.now(),
				(nQueries / (double) mtime), nQueries, 
				(outstream==null?"suppressed":"enabled"), mtime,
				RequestProcessor.pTime(), numThreads, 
				chunkSize, 
				(int) Math.ceil((double) nQueries / (numThreads * chunkSize)) ,
				aggregatorName+(aggregatorParm!=null?("("+aggregatorParm+")"):""),
				searchFlavor, kmFilename, queryFilename);
	}

	/**
	 * Assign work to thread processors, synchronizing by means of a countdown latch set to
	 * the number of requests units to process.  This uses a simple "barrier" (await()). The
	 * work-queue is defined as the list of requests (allRequests).  The size and number of 
	 * requests if implemented by the enqueueQueries method.
	 * @param queryList
	 * @throws InterruptedException
	 */
	public static void processQuerylist(List<String>queryList) throws InterruptedException {
		ActiveRequestQueue server = new ActiveRequestQueue(numThreads);
		List<Request> allRequests = enqueueQueries(queryList);
		processingDone = new CountDownLatch(allRequests.size());
		long mtime = System.currentTimeMillis();
		for (Request r : allRequests) {
			server.acceptRequest(r);
		}
		processingDone.await();
		mtime = System.currentTimeMillis() - mtime;
		//KeyMatch.quiet = false; 
		showStats(mtime, queryList.size());
	}

	static List<String> readQueries(String testInputs) {
		BufferedReader queryFile;
		try {
			queryFile = KeyMatch.openInput(testInputs);
			List<String> queries = new ArrayList<String>();
			do {
				String s = queryFile.readLine();//.toLowerCase(Locale.getDefault());
				if (s == null || s.length() == 0) continue;
				queries.add(s);
			} while (queryFile.ready());
			queryFile.close();
			return queries;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
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

/**
 * Queueing code based on O'Reilly text.
 * @author Michah.Lerner
 *
 */
@SuppressWarnings("unchecked")
class FIFO_Queue {
	java.util.ArrayList v = new java.util.ArrayList();

	public void add(Object o) {
		v.add(o);
	}

	public Object pop() {
		Object o = v.remove(0);
		return o;
	}

	public boolean isEmpty() {
		return (v.isEmpty());
	}

	public int size() {
		return v.size();
	}
}

class Request {
	static int numRequests = 0;
	long threadName;
	List<String> query;
	RequestResult requestResult;
	int requestID;
	EvalType evalType;
	TrieMatcher trieMatcher_;
	L2Map<Integer,String> l2map_;
	KeyMatch km;
	PrintStream outstream;

	public Request(List<String> query) {
		this.query = query;
		this.requestID = ++numRequests;
		this.threadName = Thread.currentThread().getId();
		this.requestResult = new RequestResult();
	}

	public static synchronized int numRequests() {
		return numRequests;
	}
}

class RequestProcessor {
	private static int numStarted = 0;
	private static int numReturned = 0;
	private static int numCompleted = 0;
	private static int numQueriesIssued = 0;
	private static long pTimeAccounted = 0;

	public RequestResult processRequest(Request r) {
	    incStart();
	    r.requestResult.tStart = System.currentTimeMillis();
	    for(String q : r.query) {
	        r.requestResult.nQueries++;

//	        List<KMDefinition>ans = r.trieMatcher_.findMatch(q);
//	        Collection<Pair<String, Integer>> rans = r.trieMatcher_.reduce(ans); 
//            if(r.outstream!=null) {
//                StringBuffer sb = new StringBuffer();
//              for(Pair p : rans) {
//                  sb.append(String.format("%50s ==> %s (score %s)\n",q,p.s(),p.t()));
//                  if(!MultiQuery.SHOWALL)break;
//              }
//                r.outstream.print(sb.toString());
//            } 
                    
            List<String>rans =   r.km.processQuery(r.trieMatcher_,q);
	        if(r.outstream!=null) {
	            StringBuffer sb = new StringBuffer();
                for(String st : rans) {
                    sb.append(String.format("%50s ==> %s\n",q,st));
                    if(!MultiQuery.SHOWALL)break;
                }
	            r.outstream.print(sb.toString());
	        } 
	    }
	    r.requestResult.tEnd = System.currentTimeMillis();
	    RequestResult rr = r.requestResult;
	    incReturned();
	    return rr;
	}

	public List<String> eval(LocalStore localStore, String queryText) {
		return localStore.kmPrimary.processQuery(localStore.trieMatcher, queryText);
	}

	public static synchronized int numStarted() {
		return numStarted;
	}

	public static synchronized int numReturned() {
		return numReturned;
	}

	public static synchronized int numCompleted() {
		return numCompleted;
	}

	public static synchronized int numQueries() {
		return numQueriesIssued;
	}

	public static synchronized long pTime() {
		return pTimeAccounted;
	}

	// No, statics fields do not get automatic synchronization.  Methods must do this. 
	public static synchronized void incStart() {
		numStarted++;
	}

	public static synchronized void incReturned() {
		numReturned++;
	}

	public static synchronized void incCompleted() {
		numCompleted++;
	}

	public static synchronized void incCompleted(Request result) {
		numCompleted++;
		numQueriesIssued+=result.requestResult.nQueries;
		pTimeAccounted+=(result.requestResult.tEnd-result.requestResult.tStart);
	}
}

class RequestResult {
	int nQueries;
	long tStart;
	long tEnd;
	Object result;
}

class PassiveRequestQueue {
	FIFO_Queue queue = new FIFO_Queue();

	public synchronized void acceptRequest(Request r) {
		queue.add(r);
		notify();
	}

	boolean shutdown = false;

	public synchronized Request releaseRequest() {
		for (;;) {
			if (shutdown) {
				while (!queue.isEmpty()) {
					queue.pop();
				}
				return null;
			}
			if (!queue.isEmpty()) { return (Request) queue.pop(); }
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public synchronized void niceShutdown() {
		shutdown = true;
		System.out.println(TOD.now() + ": Shutting down");
		notifyAll();
	}
}

class ActiveRequestQueue extends PassiveRequestQueue implements Runnable {
	ActiveRequestQueue[] servers;      // Public queue -- list of private servers
	RequestProcessor requestProcessor; // Private queue -- list of RequestProcessors
	Thread myThread;                   // Handle on thread to access for control
	ActiveRequestQueue queueServer;    // Handle on public queue which holds objects [alias]
	boolean isAvailable = true;        // Availabilty of virtual server (thread)
	String serverName;                 // Task specific resources -- server name
	TrieMatcher trieMatcher_;
	KeyMatch keyMatcher_;
	EvalType evalType;
	PrintStream outstream;

	private ActiveRequestQueue(ActiveRequestQueue q) {
		queueServer = q;
		requestProcessor = new RequestProcessor();
	}

	/**
	 * Allocate a thread-safe virtual server which has read-only (unlocked) access
	 * to the trie. All requests share the previously allocated kmPrimary.tiers. 
	 * @param num_servers
	 */
	public ActiveRequestQueue(int num_servers) {
		servers = new ActiveRequestQueue[num_servers];
		Thread t;
		for (int i = 0; i < servers.length; i++) {
			servers[i] = new ActiveRequestQueue(this);
			servers[i].serverName = String.format("virt%d", i);
			servers[i].trieMatcher_ = MultiQuery.searchFlavor.select(MultiQuery.kmPrimary.tiers);
			servers[i].evalType     = MultiQuery.evalType;
			servers[i].keyMatcher_  = MultiQuery.kmPrimary;
			servers[i].outstream    = MultiQuery.outstream;		
			(t = new Thread(servers[i])).start();
			servers[i].myThread = t;
			@SuppressWarnings("unused")
			long id = servers[i].myThread.getId();
		}
	}

	/**
	 * Activate the virtual server in this execution environment.  The trierMatcher_ holds
	 * an instance with shared readable trie and unshared writable results space.
	 */

	public void run() {
		Request request;
		RequestResult result;
		for (;;) {
			request = queueServer.releaseRequest();
			if (request == null) {
				isAvailable = true;
				return;
			}
			request.threadName = myThread.getId();
			request.trieMatcher_ = trieMatcher_;
			request.km = keyMatcher_;
			request.evalType = evalType;
			request.outstream= outstream;

			this.isAvailable = false;
			result = this.requestProcessor.processRequest(request);
			RequestProcessor.incCompleted(request);
			returnResult(result);
			this.isAvailable = true;
			MultiQuery.processingDone.countDown();
		}
	}

	public boolean isAvailable() {
		return isAvailable;
	}

	public void returnResult(RequestResult r) {
		return;
	}
}
