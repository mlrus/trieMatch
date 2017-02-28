//This is unpublished source code. Michah Lerner 2006

package trieMatch.util.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Common IO routines, ensuring safe open that does not overwrite an existing file.  Will lowercase but not Americanize input.
 * @author Michah.Lerner
 *
 */
public class IO {
	public static List<String> readInput(String fn) {
		return readInput(fn, false);
	}

	public static List<String> readInput(String fn, boolean toLowerCase) {
		BufferedReader q = null;
		List<String> lines = new ArrayList<String>();
		try {
			q = openInput(fn);
			do {
				String s = q.readLine();
				if (s == null || s.length() == 0) continue;
				lines.add(toLowerCase ? s.toLowerCase(Locale.getDefault()) : s);
			} while (q.ready());
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			System.out.println("got " + lines.size() + " lines from " + fn);
			if(q!=null)q.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lines;
	}

	public static boolean checkOutputfile(String name, boolean deletableOutputfile) {
		if (stdFilename(name)) return false;
		File f = new File(name);
		if (!f.exists() || (deletableOutputfile && f.isFile() && f.delete())) return true;
		return false;
	}

	static boolean stdFilename(String filename) {
		return filename == null || filename.length() == 0 || filename.equalsIgnoreCase("-");
	}

	static PrintStream fileFoundHandler(String s) throws FileNotFoundException {
		throw new FileNotFoundException("File exists: " +(new File(s)).getAbsolutePath());
	}

	public static BufferedReader openInput(String filename) throws Exception {
		try {
			return new BufferedReader((stdFilename(filename) ? (new InputStreamReader(System.in)) : (new FileReader(filename))));
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	static public PrintStream safePrintStream() throws FileNotFoundException {
		return safePrintStream(null);
	}

	static public PrintStream safePrintStream(String filename) throws FileNotFoundException {
		System.out.println("stdFilename("+filename+")?="+stdFilename(filename));
		return stdFilename(filename) ? (new PrintStream(System.out)) : (!new File(filename).exists() ? new PrintStream(filename)
		: fileFoundHandler(filename));
	}

}
