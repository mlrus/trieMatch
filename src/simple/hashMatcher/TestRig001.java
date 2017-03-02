package trieMatch.simple.hashMatcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * SAMPLE USAGE: java -Xmx1540M -jar ..\GKeyTester.jar matchtext.base.029 keymatch_test.txt -
 * The trailing hyphen is the output file (stdout) but you can give a filename instead.
 *
 */

public class TestRig001 {

	static PrintStream outstream = null;
	final static int maxColumns = 2;
    static int numRepetitions=5;
    public static final String keymatchFileName_KEY = "keymatch.file.name";
	public static void usage() { 
		System.out.println("java prog [options] inputRules inputTests output"); 
		System.out.println("  options are:  -p  on/off show of successful tests,");
		System.out.println("                -f  off/on sho of failed tests.");
	} 
	
	public static void main(String[] args) {
        System.out.println(TOD.now() + ": TestRig001 (HashMap) EXECUTION BEGINS.");
		System.out.println("split to max of maxColumns="+maxColumns);
		boolean showpasses=false;
		boolean showfailures=true;
		final int keymatchFile=0, testInputsfile=1, outputFile=2;
		boolean deleteOutputfile = false;
		List<String> filenames = new ArrayList<String>();
		for (int i = 0; i < args.length; i++) {
			if (args[i].equalsIgnoreCase("-h")){
				usage();
				System.exit(0);
			}
			else if (args[i].equalsIgnoreCase("-deleteOutputFile")) deleteOutputfile = true;
			else if (args[i].startsWith("-p")) showpasses=!showpasses;
			else if (args[i].startsWith("-f")) showfailures=!showfailures;
			else {
				filenames.add(args[i]);
		}
		}

		if (filenames.size() <2) { usage(); System.exit(0); }
		String kmFilename = filenames.get(keymatchFile);
		String testInputs = filenames.get(testInputsfile);
		String outputName = (filenames.size()>2)?filenames.get(outputFile):null;
		checkOutputfile(outputName,deleteOutputfile);
		System.out.println("keymatches from " + kmFilename);
		System.out.println("test input from " + testInputs);
		System.out.println("Results outputs " + (outputName==null?"not printed":outputName));
		
		try {
			outstream = (outputName != null) ? safePrintStream(outputName) : null;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		System.setProperty(keymatchFileName_KEY, kmFilename);
        GoogleKeymatch instance = GoogleKeymatch.getInstance();
		List<String> queryList = readQueries(testInputs);
        System.out.println(TOD.now() + ": begin processing " + queryList.size() + " queries.");
		for(int repetitionNumber=0;repetitionNumber<numRepetitions;repetitionNumber++) {
		    Long mtime = System.currentTimeMillis();
		    int count_queries=0;
		    int count_noAnswer=0;
		    int count_wrongAnswer=0;
		    int count_correctAnswer=0;
		    int qCounter=0;
		    for (String query : queryList) {
		        count_queries++;
		        String l[] = query.split("[ \"]*~[ \"]*", maxColumns);
		        String queryString = l[0];
		        String expectString = l.length > 1 ? l[1] : "*NONE*";
		        expectString = expectString.replaceAll("&", "&amp;");
		        List<GoogleKeymatchResult> resultList = instance.getKeymatchResult(queryString);
		        qCounter++;
		        if (outstream != null) {
		            outstream.println("Results: " + resultList.size() + " total");
		            for (GoogleKeymatchResult gr : resultList)
		                outstream.printf("%50s ==> %s\t%s\n",query,gr.getMatchString(),gr.getKeymatch());
		        }

//		        String resultString = null; 
//		        if (result == null) {
//		        count_noAnswer++;
//		        }
//		        else {
//		        resultString = result.getUrl() + "  " + result.getMatchString();
//		        if (expectString.equals("*NONE*")) {
//		        if (outstream != null) outstream.printf("%50s ==> %s\n", queryString, resultString);
//		        }
//		        else {
//		        boolean pass = resultString.equalsIgnoreCase(expectString);
//		        if (pass) {
//		        count_correctAnswer++;
//		        if (showpasses && (outstream != null)) {
//		        outstream.println("PASS:   \"" + queryString + "\"");
//		        }
//		        }
//		        else {
//		        count_wrongAnswer++;
//		        if (showfailures && outstream != null) {
//		        outstream.println("\n**FAIL: \"" + queryString + "\"");
//		        outstream.println("EXPECT: \"" + expectString + "\"");
//		        outstream.println("FOUND:  \"" + resultString + "\"");
//		        }
//		        }
//		        }
//		        }
		    }
		    mtime = System.currentTimeMillis()-mtime;
            
            System.out.printf("%s: queries processed=%d, search time=%d msec, %8f msec/query [hashtable based]\n",
                    TOD.now(), count_queries, mtime, ((double) mtime / queryList.size())); 
		    if(count_noAnswer+count_wrongAnswer+count_correctAnswer!=0) {
		        System.out.println(TOD.now() + ": SUMMARY OF HASH-BASED TEST RUN");
		        System.out.println(TOD.now() + ": queries issued          = " + count_queries);
		        System.out.println(TOD.now() + ": queries no answer       = " + count_noAnswer);
		        System.out.println(TOD.now() + ": queries wrong answer    = " + count_wrongAnswer);
		        System.out.println(TOD.now() + ": queries correct answer  = " + count_correctAnswer);
		        System.out.println(TOD.now() + ": % correct overall       = " + (int)(0.5D+100D*count_correctAnswer/count_queries) + "%");
		        System.out.println(TOD.now() + ": % correct answered      = " + (int)(0.5D+100D*count_correctAnswer/(count_queries-count_noAnswer))+"%");
		        System.out.println(TOD.now() + ": elapsedTime             = " + mtime + " ms.");
		        System.out.println(TOD.now() + ": time/query              = " + String.format("%6.4f ms.", ((double) mtime / queryList.size())));
		        System.out.println(TOD.now() + ": TEST RUN COMPLETE");
		    }
		}
		System.exit(0);
	}
	

	public static boolean checkOutputfile(String name, boolean deletableOutputfile) {
		if (name == null) return false;
		File f = new File(name);
		if (!f.exists() || (deletableOutputfile && f.isFile() && f.delete())) return true;
		return false;
	}

	static boolean stdFilename(String filename) {
		return filename == null || filename.length() == 0 || filename.equals("-");
	}

	static PrintStream FileFoundHandler(String s) throws FileNotFoundException {
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
				filename)) : FileFoundHandler(filename));
	}
	static List<String> readQueries(String testInputs) {
		BufferedReader queryFile;
		try {
			queryFile = openInput(testInputs);
			List<String> queries = new ArrayList<String>();
			do {
				String s = queryFile.readLine();//toLowerCase(Locale.getDefault());
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
}

class TOD {
    public static SimpleDateFormat dateTime = 
        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS z");
    public static String now() {
        return dateTime.format(new Date());
    }
}