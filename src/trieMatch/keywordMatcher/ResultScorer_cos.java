package trieMatch.keywordMatcher;


//  This is unpublished source code. Michah Lerner 2006


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

    /**
     * Heuristic scoring by word and string comparisons, considering word order and size.  Hand tuned.
     *  <strong>Good performance requires the matcher is configured to use a TreeSet for prefix matching</strong>
     * @author Michah.Lerner
     *
     */

public class ResultScorer_cos implements Similarity {

    /**
     * Distinguish scoring for names under prefix matching.  An exact prefix is the better match.
     * @param keyString first string
     * @param otherString second string
     * @return subjective quality score
     */
    public static float prefixMatch(String keyString, String otherString) {
        if (keyString == null || otherString == null || keyString.length() == 0 || !otherString.startsWith(keyString)) {
            return 0F;
        }
        for (int prefix = 0; prefix < keyString.length(); prefix++)
            if (keyString.charAt(prefix) != otherString.charAt(prefix)) return (float) (prefix * prefix)
            / keyString.length() / otherString.length();
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
        Set<String> set = new HashSet<String>(l1);
        int both = 0;
        for (String s : l2)
            if (set.contains(s)) both++;
        set.addAll(l2);
        return ((float) both) / ((float) (set.size()));
    }

}
 
