package trieMatch.simple.hashMatcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * SAMPLE USAGE: java -Xmx1540M -jar ..\GKeyTester.jar matchtext.base.029 keymatch_test.txt -
 * The trailing hyphen is the output file (stdout) but you can give a filename instead.
 *
 */

public class TestRig002 {

	static PrintStream outstream = null;
	final static int maxColumns = 4;
    public static final String keymatchFileName_KEY = "keymatch.file.name";
	public static void usage() { 
		System.out.println("java prog [options] inputRules inputTests output"); 
		System.out.println("  options are:  -p  on/off show of successful tests,");
		System.out.println("                -f  off/on sho of failed tests.");
	} 
	
	public static void main(String[] args) {
		System.out.println("split to max of maxColumns=" + maxColumns);
		boolean showpasses = false;
		boolean showfailures = true;
		final int keymatchFile = 0, testInputsfile = 1, outputFile = 2;
		boolean deleteOutputfile = false;
		List<String> filenames = new ArrayList<String>();
		for (int i = 0; i < args.length; i++) {
			if (args[i].equalsIgnoreCase("-h")) {
				usage();
				System.exit(0);
			} else if (args[i].equalsIgnoreCase("-deleteOutputFile")) deleteOutputfile = true;
			else if (args[i].startsWith("-p")) showpasses = !showpasses;
			else if (args[i].startsWith("-f")) showfailures = !showfailures;
			else {
				filenames.add(args[i]);
			}
		}

		if (filenames.size() != 3) {
			usage();
			System.exit(0);
		}
		String kmFilename = filenames.get(keymatchFile);
		String testInputs = filenames.get(testInputsfile);
		String outputName = filenames.get(outputFile);
		checkOutputfile(outputName, deleteOutputfile);
		System.out.println("keymatches from " + kmFilename);
		System.out.println("test input from " + testInputs);
		System.out.println("Results outputs " + (outputName == null ? "not printed" : outputName));

		try {
			outstream = (outputName != null) ? safePrintStream(outputName) : null;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		System.setProperty(keymatchFileName_KEY, kmFilename);

		List<String> queryList = readQueries(testInputs);
		System.out.println("Got " + queryList.size() + " queries.");
		GoogleKeymatch instance = GoogleKeymatch.getInstance();
		try {
			System.out.println("\n\n");
			System.out.println(new Date() + ": begin processing " + (new File(testInputs).getCanonicalPath()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		Long mtime = System.currentTimeMillis();
		int count_queries = 0;
		int count_noAnswer = 0;
		int count_wrongAnswer = 0;
		int count_correctAnswer = 0;
		for (String query : queryList) {
			count_queries++;
			String l[] = query.split("[ \"]*,[ \"]*", maxColumns);
			String queryString = l[0];
			String expectString = l.length > 1 ? l[1] : "*NONE*";
			String otherExpects = l.length > 2 ? l[2] : "*NONE*";
			Boolean expectedGrade = l.length > 3 ? Boolean.parseBoolean(l[3]) : true;
			expectString = expectString.replaceAll("&", "&amp;").trim();
			otherExpects = otherExpects.replaceAll("&", "&amp;").trim();
			List<GoogleKeymatchResult> resultList = instance.getKeymatchResult(queryString);
            GoogleKeymatchResult result = resultList.size()>0?resultList.get(0):null;
			String resultString = (result == null) ? "[no result]" : result.getMatchString();
			if (expectString.equals("*NONE*")) {
				outstream.printf("%50s ==> %s\n", queryString, resultString);
			} else {
				boolean pass = result != null && resultString.equalsIgnoreCase(expectString);
				if (pass) count_correctAnswer++;
				if (!pass) {
					if (result == null) count_noAnswer++;
					else count_wrongAnswer++;
				}
				boolean metaAnswer = expectedGrade.equals(pass);
				if (metaAnswer) {
					outstream.printf("PASS:  testcase:  %-30s ", queryString.trim());
					if (pass) {
						outstream.printf(": expect \"" + expectString.trim() + "\", got \"" + resultString.trim() + "\"\n");
						outstream.printf("OK   : %-40s returned result %-40s not deprecated ans  \"%s\"\n", queryString.trim(),
								resultString.trim(), otherExpects.trim());
					}
					if (!pass) {
						if (resultString.trim().equalsIgnoreCase(otherExpects.trim())) {
							outstream.printf(" : did not give wrong %-40s : got planned  preempt \"%s\"\n", queryString.trim(),
									expectString.trim(), otherExpects.trim());
						} else {
							outstream.printf("BEEPA : %-40s got search resp \"%s\" NOT planned "
									+ "preempt \"%s\" or erroneous \"%s\"\n", queryString.trim(), resultString.trim(), expectString
									.trim(), otherExpects.trim());
						}
					}
				} else {
					if (pass) outstream.print("*FAIL : ");
					else outstream.print("*BEEPB : ");
					outstream.printf("testcase: %-30s : got \"%-30s\" for  negated exp=\"%s\"  alt=\"%s\"\n", 
							queryString.trim(), resultString.trim(), expectString.trim(), otherExpects.trim());
				}
			}
		}
			mtime = System.currentTimeMillis() - mtime;
			System.out.println();
			System.out.println(new Date() + ": SUMMARY OF TEST RUN");
			System.out.println(new Date() + ": queries issued          = " + count_queries);
			System.out.println(new Date() + ": queries no answer       = " + count_noAnswer);
			System.out.println(new Date() + ": queries wrong answer    = " + count_wrongAnswer);
			System.out.println(new Date() + ": queries correct answer  = " + count_correctAnswer);
			System.out.println(new Date() + ": % correct overall       = "
					+ (int) (0.5D + 100D * count_correctAnswer / count_queries) + "%");
			System.out.println(new Date() + ": % correct answered      = "
					+ (int) (0.5D + 100D * count_correctAnswer / (count_queries - count_noAnswer)) + "%");
			System.out.println(new Date() + ": elapsedTime             = " + mtime + " ms.");
			System.out.println(new Date() + ": time/query              = "
					+ String.format("%6.4f ms.", ((double) mtime / queryList.size())));
			System.out.println(new Date() + ": TEST RUN COMPLETE");
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