//This is unpublished source code. Michah Lerner 2006

package trieMatch.util.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.PrintStream;

/**
 * Americanize 30+ lower-case characters and their corresponding 30+ upper-case characters.  The Americanization
 * provides replacement characters that correspond to the similar glyph stripped of its diacrytic (!).  That is, the
 * characters <b>‡·‚„‰Â</b>  become the character <b>a</b>, the characters <b>Ÿ⁄€‹</b> become <b>U</b>, etc.  
 * As a special case, the characters <b>ˇü</b> each become a space. This is because the frequent displayed value of 
 * character <b>ˇ</b> is the glyph <code>0xFF</code>.
 * <br><br>NOTE: the use bold or italics above is merely for clarity of presentation, and does not indicate a change in 
 * the character style for the modified character.  It is only "de-dyacritics" that is done here.
 *<br><br>In an environment where we do not wish to distinguish between the accented and non-accented forms of a word,
 * tt is <b>match generation and input processing</b> should do the same things, whether it is
 * the construction of searchable entities or the construction of search specifications.
 * @author Michah.Lerner
 *
 */
public class CopyClean {

	static BufferedReader input = null;
	static PrintStream outStream = null;

	/**
	 * Replace non-Ascii characters with their Ascii lookalikes.
	 * @param input string
	 * @return Cleaned up input string, with foreign accented characters replaced by their unaccented equivalents
	 */
	public static String stringCleaner(String input) {
		if(input==null||input.length()==0)return input;
		final String[][] Xchars = new String[][] { { "‡·‚„‰Â", "a" }, { "¿¡¬√ƒ≈", "A" }, { "ËÈÍÎ", "e" }, { "»… À", "E" },
				{ "ÏÌÓÔ", "i" }, { "ÃÕŒœ", "I" }, { "Ò", "n" }, { "—", "N" }, { "ÚÛÙıˆ", "o" }, { "“”‘’÷", "O" }, { "˘˙˚¸", "u" },
				{ "Ÿ⁄€‹", "U" }, { "˝", "y" }, { "›", "Y" }, { "ˇü", " " } };
		String output = input;
		for (String[] str : Xchars) {
			char[] from = str[0].toCharArray();
			char to = str[1].toCharArray()[0];
			for (char ch : from)
				while (output.indexOf(ch) >= 0) {
					output = output.replace(ch, to);
				}
		}
		return output;
	}

	static void openInputFile(String filename) {
		try {
			input = new BufferedReader((filename != null) ? (new FileReader(filename)) : (new InputStreamReader(System.in)));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws Exception {
		input = null;
		outStream = System.out;
		if (args.length < 1) System.exit(0);
		openInputFile(args[0]);
		outStream = new PrintStream(new File(args[1]));
		String s = input.readLine();
		outStream.println(stringCleaner(s));
		do {
			s = input.readLine();
			if (s == null) break;
			outStream.println(stringCleaner(s));
		} while (input.ready());
		input.close();
		outStream.close();
	}
}
