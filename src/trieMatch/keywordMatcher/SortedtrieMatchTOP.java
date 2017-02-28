//This is unpublished source code. Michah Lerner 2006

package trieMatch.keywordMatcher;

import java.util.List;

/**
 * Implementation retrieves the <strong>TOP</strong> matching criterion (best, from the "ALL" matches)
 * @author Michah.Lerner
 *
 */

public class SortedtrieMatchTOP extends SortedtrieMatchALL  {

	public SortedtrieMatchTOP() {     // Allocates a sequencer (thread safety)
		super();
	}
	public SortedtrieMatchTOP(Tiers tiers) {     // Allocates a sequencer (thread safety)
		super(tiers);
	}
	public SortedtrieMatchTOP(Tiers tiers, Sequencer sequencer) {  // Accepts a sequencer
		super(tiers, sequencer);
	}
	
	@Override
	public List<KMDefinition> findMatch(String s) {
		lookingFor = sequencer.arrange(s);
		List<KMDefinition> res = findMaxMatch(tierHead, 0);
		return(res.size()>0)?res.subList(0,1):res;
	}
}
