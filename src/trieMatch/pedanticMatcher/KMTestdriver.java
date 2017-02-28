//This is unpublished source code. Michah Lerner 2006

package trieMatch.pedanticMatcher;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/** 
 * Test driver for simple sweet version of key match 
 * @author Michah.Lerner
 *
 */

public class KMTestdriver {	
	
	public static void usage() {
		System.err.println("usage: java -jar file.jar [options] KMatchFilename testinputsFilename [outputFilename]");
		System.err.println("       options: -all/-longest     -echo/;-noEcho     -deleteOutputFile");
		System.err.println("                -exp [prefix matching]");
		System.exit(0);
	}

	public static void main(String[] args) throws FileNotFoundException {
		final int KMatchFile=0, testInputsfile=1, outputFile=2;
		boolean echoMatches = false;
		boolean addall = false;
		boolean exp = false;
		boolean deleteOutputfile = false;
		List<String> filenames = new ArrayList<String>();
		for (int i = 0; i < args.length; i++) {
			if (args[i].equalsIgnoreCase("-h"))
				usage();
			else if (args[i].equalsIgnoreCase("-all"))
				addall = true;
			else if (args[i].equalsIgnoreCase("-exp"))
				exp = true;
			else if (args[i].equalsIgnoreCase("-longest"))
				addall = false;
			else if (args[i].equalsIgnoreCase("-echo"))
				echoMatches = true;
			else if (args[i].equalsIgnoreCase("-noEcho"))
				echoMatches = false;
			else if (args[i].equalsIgnoreCase("-deleteOutputFile"))
				deleteOutputfile = true;
			else
				filenames.add(args[i]);
		}
		filenames.add(null);

		if (filenames.size() < 2) { usage(); System.exit(0); }
		String kmFilename = filenames.get(KMatchFile);
		String testInputs = filenames.get(testInputsfile);
		String outputName = filenames.get(outputFile);
		checkOutputfile(outputName,deleteOutputfile);
		System.out.println(new Date() + ": Options all="+addall+"; exp="+exp);
		System.out.println(new Date() + ": KMatches from " + kmFilename);
		System.out.println(new Date() + ": Test input from " + testInputs);
		System.out.println(new Date() + ": Results outputs " + (outputName==null?"not printed":outputName));
		System.out.println(new Date() + ": Compute "+(addall?"all":(exp?"prefix":"longest"))+" results.");

		KMatch km = new KMatch(addall,exp,kmFilename);
		
		System.out.println(new Date() + ": Loaded " + km.KMatchDetail.size() + " KMatches.");
		if (echoMatches) {
			km.keysetStore.printTiers(System.out);
			System.out.println("----------------");
		}
		System.out.println(new Date() + ": begin processing " + testInputs);
		km.processFile(testInputs, outputName, km);
		System.out.println(new Date() + ": done processing " + testInputs);
	}

	public static boolean checkOutputfile(String name, boolean deletableOutputfile) {
		if (name == null) return false;
		File f = new File(name);
		if (!f.exists() || (deletableOutputfile && f.isFile() && f.delete())) return true;
		return false;
	}
}
