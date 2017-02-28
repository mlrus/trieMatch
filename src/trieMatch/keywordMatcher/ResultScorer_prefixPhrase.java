//	This is unpublished source code. Michah Lerner 2006

package trieMatch.keywordMatcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import trieMatch.util.Constants;

/**
 * Heuristic scoring by word and string comparisons, considering word order and size.  Hand tuned.
 *  <strong>Good performance requires the matcher is configured to use a TreeSet for prefix matching</strong>
 * @author Michah.Lerner
 *
 */

public class ResultScorer_prefixPhrase implements Similarity {

	/**
	 * Distinguish scoring for names under prefix matching.  An exact prefix is the better match.
	 * @param keyString first string
	 * @param otherString second string
	 * @return subjective quality score
	 */
	public static float prefixMatch(String keyString, String otherString) {
		if (keyString == null || otherString == null || 
				keyString.length() == 0 || !otherString.startsWith(keyString)) { return 0F; }
		for (int prefix = 0; prefix < keyString.length(); prefix++)
			if (keyString.charAt(prefix) != otherString.charAt(prefix))
				return (float) (prefix * prefix) / keyString.length() / otherString.length();
		return 1F;
	}

	/**
	 * Subjective quality score between two structures
	 * @param other Structure to compare with <code>this</code>
	 * @return subjective quality score
	 */
	public float sim(StructuredElement element, StructuredElement other) {
		ArrayList<String> a1 = new ArrayList<String>(element.size());
		ArrayList<String> a2 = new ArrayList<String>(element.size());
		for (Atom a : element)
			a1.add(a.value);
		for (Atom a : other)
			a2.add(a.value);
		float res = sim(a1, a2);
		return res;
	}

	public float sim(StructuredElement element, String other) {
		List<String> a1 = new ArrayList<String>(element.size());
		for (Atom a : element)
			a1.add(a.value);
		float res = sim(a1, Arrays.asList(other.toLowerCase().split("[^A-Za-z0-9]")));
		return res;
	}

	public float sim(List<String> l1, List<String> l2) {
		double simc = 0D;
		int pos = 0;
		int numMatched = 0;
		TreeSet<String> tset = new TreeSet<String>(l2);
		Iterator<String> i1 = l1.iterator();
		Iterator<String> i2 = l2.iterator();
		while (i1.hasNext() && i2.hasNext()) {
			String s1 = i1.next();
			String s2 = i2.next();
			pos++;
			float plen = prefixMatch(s1, s2); // score the words' inorder fit
			if (plen > 0) {
				plen += Math.pow(Constants.INORDERbonus, pos); // boost towards head
			}
			float olen = prefixMatch(s1, leastKnownElement(s1, tset)); // unboosted out-of-order fit
			float mval = Math.max(plen, olen);
			if (mval > 0) {
				numMatched++;
				simc += mval;
			}
		}
		int maxlen = Math.max(l1.size(), l2.size());
		float matchedTokenRatio = (float) numMatched / maxlen; // length-adjust
		float result = (float) (simc * simc * matchedTokenRatio);
		return result;
	}

	/**
	 * Lookahead function, to the successor element that is not a prefix. 
	 * @param string item to look for
	 * @param set items to look in
	 * @return subset to look in
	 */

	String leastKnownElement(String string, SortedSet<String> set) {
		SortedSet<String> tail = set.tailSet(string);
		if (tail.size() == 0) return null;
		return tail.first();
	}
}

	

