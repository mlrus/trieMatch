//This is unpublished source code. Michah Lerner 2006

package trieMatch.keywordMatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

	/**
	 * Class sequencer for building ordered contentIndex by placing items into sequence,
	 * conditionally reusing storage (for atoms and structuredElements) according to the
	 * to the value of reuseStorage (which should be set FALSE during query processing).
	 * Any order can be used; it should be stable, but need not be lexicographic. 
 *  @author Michah.Lerner
	 */
class Sequencer {

	final static String _tokenPatternString = "[a-zA-Z0-9_]+"; // "[\\S]+";
	final StructuredElement instance = new StructuredElement();
	String tokenPatternString;
	Pattern tokenPattern;
	StructuredElement element;
	boolean reuseStorage;
	Sequencer() {
		this(false);
	}
	Sequencer(boolean reuseStorage) {
		this(_tokenPatternString,reuseStorage);
	}

	Sequencer(String tokenPatternString) {
		this(tokenPatternString,false);
	}
	
	Sequencer(String tokenPatternString, boolean reuseStorage) {
		this.reuseStorage = reuseStorage;
		this.tokenPatternString = tokenPatternString;
		tokenPattern = Pattern.compile(tokenPatternString);
	}

	StructuredElement arrange(String string) {
		List<String>pieces=new ArrayList<String>();
		Matcher m = tokenPattern.matcher(string);
		while (m.find()) {
			pieces.add(string.substring(m.start(), m.end()).toLowerCase(Locale.getDefault()));
		}
		element = instance.newElement(pieces,reuseStorage);
		element.arrangeAtoms();
		return element;
	}
}
