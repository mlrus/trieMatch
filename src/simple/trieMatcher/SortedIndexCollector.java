//This is unpublished source code. Michah Lerner 2006

package trieMatch.simple.trieMatcher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Lexicographical ordering of items for trie.
 * @author Michah.Lerner
 *
 */
public class SortedIndexCollector {
	final static String _tokenPatternString = "[a-zA-Z0-9_]+";
	String tokenPatternString;
	Pattern tokenPattern;

	SortedIndexCollector() {
		this(_tokenPatternString);
	}

	SortedIndexCollector(String tokenPatternString) {
		this.tokenPatternString = tokenPatternString;
		tokenPattern = Pattern.compile(tokenPatternString);
	}

	IndexedItem indexedItem(String string) {
		return new IndexedItem(string.toLowerCase(Locale.getDefault()));
	}

	public class IndexedItem {
		List<Integer> indexList;
		List<String> stringList;
		int queryLength;
		
		IndexedItem(String string) {
			List<StringPos> 
			locList = new ArrayList<StringPos>();
			Matcher m = tokenPattern.matcher(string);
			int index = 0;
			while (m.find()) {
				locList.add(new StringPos(string.substring(m.start(), m.end()), index++));
			}
			Collections.sort(locList);
			this.stringList  = getStrings(locList);
			this.indexList   = getIndices(locList);
			this.queryLength = locList.size();//stringList.size(); 
		}

		List<String> getStrings(List<StringPos> locList) {
			List<String> res = new ArrayList<String>();
			for (StringPos sp : locList)
				res.add(sp.string);
			return res;
		}

		List<Integer> getIndices(List<StringPos> locList) {
			List<Integer> res = new ArrayList<Integer>();
			for (StringPos sp : locList)
				res.add(sp.index);
			return res;
		}

		 public class StringPos implements Comparable<StringPos> {
			String string;
			int index;
			StringPos(String string, int index) {
				this.string = string;
				this.index = index;
			}
			public int compareTo(StringPos o) {
				return this.string.compareToIgnoreCase(o.string);
			}
		}
	}
}
