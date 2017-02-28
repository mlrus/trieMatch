//This is unpublished source code. Michah Lerner 2006

package trieMatch.keywordMatcher;

import java.util.Collection;

/** 
 * Defines Interface supports putting items into the appropriate order, which is typically lexicographic but only has to be unique.
 * @author Michah.Lerner
 *
 * @param <T> Type of the items that form the collections of things to search
 */
public interface Element<T> {
//	void add(T atom);
//	void add(String item, int index);
	void arrangeAtoms();
	String getValue(int i);
	int getIndex(int i);
	Collection<String> allValues();
}
