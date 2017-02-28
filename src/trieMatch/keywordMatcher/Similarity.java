/**
 * 
 */
package trieMatch.keywordMatcher;

import java.util.List;

/**
 * @author Michah.Lerner
 *
 */
public interface Similarity {

	/**
	 * Subjective quality score between two structures
	 * @param element element to compare to
	 * @param other thing to compare with <code>this</code>
	 * @return subjective quality score
	 */
	public float sim(StructuredElement element, StructuredElement other);
	public float sim(StructuredElement element, String other);
	public float sim(List<String> l1, List<String> l2);

}
