//This is unpublished source code. Michah Lerner 2006

package trieMatch.keywordMatcher;

// Important prefix-processing bug fix made 17DEC2006
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import trieMatch.util.Constants;

/**
* Implementation retrieves the <strong>EXP</strong> matching criterion (prefix with lookahead support).
* The internal setting <code>earlyExitOnExactMatch</code>  can be set, so that matched tokens won't do prefix
* lookups.  <strong>This matcher must have a TreeSet for good performance</strong>, as gets automatically configured
* by the driver programs.
* <B>NOTE:</B> THIS ASSUMES AND REQUIRES THAT TREESET IS USED. OTHERWISE, 
* USE THE ALTERNATE findPrefixesA GIVEN BELOW
* @author Michah.Lerner
*
*/

public class SortedtrieMatchEXP extends TrieMatcher  {
	static boolean earlyExitOnExactMatch=false;
	Similarity similarity = new ResultScorer_prefixPhrase();
	public  Class getMapClass() {
		return TreeMap.class;
	}
	
	public SortedtrieMatchEXP() {
		super();
	}
	
	public SortedtrieMatchEXP(Tiers tiers) {     // Allocates a sequencer (thread safety)
		super(tiers);
	}
	
	public SortedtrieMatchEXP(Tiers tiers, Sequencer sequencer) {  // Accepts a sequencer
		super(tiers, sequencer);
	}
	
	@Override
	List<KMDefinition> findMaxMatch(Tiers current, int pos) {
		if (pos >= lookingFor.size()) return current.end ? validForMatchtype(current.instances) : emptyMatchable;
		Collection<Map.Entry<String, Tiers>> prefixMaps = findPrefixesB(current.successors, lookingFor.get(pos).value);
		if (prefixMaps != null) {
			List<KMDefinition> nList = new ArrayList<KMDefinition>();
			List<KMDefinition> v2 = findMaxMatch(current, pos + 1);
			if (v2.size() != 0) nList.addAll(v2);
			if (nList.size() <= Constants.maxInternalResults) for (Map.Entry<String, Tiers> me : prefixMaps) {
				List<KMDefinition> v1 = findMoreMaxMatch(me.getValue(), pos + 1);
				if (v1.size() != 0) nList.addAll(v1);
				if (nList.size() > Constants.maxInternalResults) break;
			}
			return nList;
		}
		return findMaxMatch(current, pos + 1);
	}
	
	@Override
	public float sim(StructuredElement element, String description) {
		//Similarity similarity = new ResultScorer_prefixPhrase();
		return similarity.sim(element,description);
	}
	
	String successorString(String name) {
		String n2=null;
		char c = name.charAt(name.length()-1);
		c++;
		if(name.length()==1)n2=String.valueOf(c);
		else n2=name.substring(0,name.length()-1)+String.valueOf(c);
		return n2;
	}

//	/**
//	 * Non TreeSet lookup (slower)
//	 * @param current
//	 * @param name
//	 * @return matched entries
//	 */
//	Collection<Map.Entry<String,Tiers>>findPrefixesA(Map<String,Tiers> current, String name) {
//		Collection<Map.Entry<String,Tiers>> result = new ArrayList<Map.Entry<String,Tiers>>();
//		for(Map.Entry<String,Tiers> me : current.entrySet()) {
//			if(me.getKey().startsWith(name))
//				result.add(me);
//		}
//		return result;
//	}
	
	/** 
	 * TreeSet prefix lookup (faster)
	 * @param current
	 * @param name
	 * @return matched entries
	 */
	@SuppressWarnings("unchecked")
	Collection<Map.Entry<String,Tiers>>findPrefixesB(Map<String,Tiers> current, String name) {
		if(current.size()<1)return null;
		SortedMap<String,Tiers>s = ((TreeMap)current);
		if(name.compareTo(s.lastKey())>0) return null;
		Collection<Map.Entry<String,Tiers>> result = new ArrayList<Map.Entry<String,Tiers>>();
		for(Map.Entry<String,Tiers> me : s.tailMap(name).entrySet())
			if(me.getKey().startsWith(name))
				result.add(me);
			else break;
		return result.size()>0?result:null;
	}
}
	
