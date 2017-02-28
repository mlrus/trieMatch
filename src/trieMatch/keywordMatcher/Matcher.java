//This is unpublished source code. Michah Lerner 2006

package trieMatch.keywordMatcher;

import java.util.List;

/**
 * Interface to find a list of matching definitions, given an input S
 * @author Michah.Lerner
 *
 */
public interface Matcher {
	Class getMapClass() ;
	List<KMDefinition> findMatch(String s);
}
