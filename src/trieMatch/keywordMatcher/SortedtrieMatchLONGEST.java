//This is unpublished source code. Michah Lerner 2006

package trieMatch.keywordMatcher;

import java.util.HashMap;
import java.util.List;

/**
 * Implementation retrieves the <strong>LONGEST</strong> matching criterion (longest matches)
 * @author Michah.Lerner
 *
 */
public class SortedtrieMatchLONGEST extends TrieMatcher  {
		
	public Class getMapClass() {
		return HashMap.class;
	}
	
	public SortedtrieMatchLONGEST() {
		super();
	}

	public SortedtrieMatchLONGEST(Tiers tiers) {     // Allocates a sequencer (thread safety)
		super(tiers);
	}
	
	public SortedtrieMatchLONGEST(Tiers tiers, Sequencer sequencer) {  // Accepts a sequencer
		super(tiers, sequencer);
	}
	

	@Override
	List<KMDefinition> findMaxMatch(Tiers current, int pos) {
		if (pos >= lookingFor.size()) { return current.end ? validForMatchtype(current.instances) : emptyMatchable; }
		Tiers nTier = current.successors.get(lookingFor.get(pos).value);
		if (nTier != null) {
			List<KMDefinition> v1 = findMoreMaxMatch(nTier, pos + 1);
			List<KMDefinition> v2 = findMaxMatch(current, pos + 1);
			if (v1.size() == 0) return v2;
			if (v2.size() == 0) return v1;
			return (v1.get(0).score < v2.get(0).score) ? v2 : v1;
		}
		return findMaxMatch(current, pos + 1);
	}
}
