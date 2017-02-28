//This is unpublished source code. Michah Lerner 2006

package trieMatch.util.data;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Split coma-separated data in a quote-aware manner. There are up to _maxColumns columns,
 * and comas inside of quoted sequences will not split the content.  This is important only when
 * the input lines contain comas within quotes in any field otehr than the last field.  
 * @author Michah.Lerner
 *
 */
public class TSplitter {
	final static String qPat = "(?:\"([^\"]*)\"(?= *,))";
	final static String nqPat = "([^\",]*)";
	final static String pat = "(?:" + qPat + "|" + nqPat + ") *,? *";
	final static Pattern splitter = Pattern.compile(pat);
	final static String cretanPatternString = ".*[^\\p{Graph}\\p{Blank}].*";
	final static int _maxColumns=2;
	int numColumns;
	
	public TSplitter() {
		this(_maxColumns);
	}
	
	public TSplitter(int numColumns) {
		this.numColumns=numColumns;
	}
		
	/**
	 * Quote-aware list building of a string
	 * @param string
	 * @return a list of strings, having no more than numColumns items.
	 */
	public List<String> consume(String string) {
		int n = numColumns;
		List<String> itemList = new ArrayList<String>();
		Matcher m = splitter.matcher(string);
		int g=0;
		while (m.find()&&(--n>0)) {
			for (g = 1; g <= m.groupCount(); g++) {
				if(m.group(g)!=null&&m.group(g).length()!=0) {
					itemList.add(m.group(g));
				}
			}
		}
		String res = string.substring(m.end());
		if(res.length()!=0)
			itemList.add(m.group()+res);
		return itemList;
	}
	
	String getLine() throws Exception {
		String s;
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		while (!in.ready())
			Thread.sleep(100);
		s = in.readLine();
		return s;
	}

	/**
	 * Read lines from file with given name, returning a list of the columns of each line.
	 * @param name
	 * @return List of files contents, as a list of list of strings.
	 * @throws Exception
	 */
	List<List<String>> readFile (String name) throws Exception {
		return readFile(new BufferedReader((new FileReader(name))));
	}
	
	/**
	 * Read lines from file, Americanizing foreign accented characters, and flagging any leftover non-standard characters.
	 * @param r
	 * @return list with one element per line of the file, where the element is a list of the columns of the line.
	 * @throws Exception
	 */
	List<List<String>> readFile(BufferedReader r) throws Exception {
		LineNumberReader lr=new LineNumberReader(r);
		List<List<String>> result = new ArrayList<List<String>>();
		while(lr.ready()) {
			String line =CopyClean.stringCleaner( lr.readLine() );		
			if(line.split(cretanPatternString).length!=1) {
				System.out.println("WARNING: non-standard characters in line "+lr.getLineNumber());
			}
			List<String> lineSplit = consume(line);
			if(lineSplit.size()!=numColumns) System.out.println("WARNING: not " + numColumns + 
					" on line " + lr.getLineNumber() + " : " + line);
			result.add(lineSplit);
		}
		return result;
	}
	
//	/**
//	 * Test routine.
//	 * @param args
//	 * @throws Exception
//	 */
//	public static void main(String args[]) throws Exception {
//		TSplitter tsplitter = new TSplitter(4);
//		while(true) {
//			String line = tsplitter.getLine();
//			List<String>l = tsplitter.consume(line);
//			for(String s : l) System.out.print("["+s+"]");
//			System.out.println();
//		}
//	}
	
}

