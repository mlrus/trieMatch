//This is unpublished source code. Michah Lerner 2006

package trieMatch.keywordMatcher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeSet;
import java.util.Map.Entry;

import trieMatch.Interfaces.Aggregator;
import trieMatch.keywordMatcher.KeyMatch.MatchType;
import trieMatch.util.aggregation.AggregatorBase;
import trieMatch.util.coll.Pair;

/**
 * Code common to all overidding specializations.
 * @author Michah.Lerner
 *
 */

public abstract class TrieMatcher implements Matcher {
	abstract List<KMDefinition> findMaxMatch(Tiers tierH, int pos);
	static final boolean SHOWdetail = false;
	static final List<KMDefinition> emptyMatchable =  new ArrayList<KMDefinition>(1);
	static final Collection<Pair<String, Integer>> emptyReduction = new ArrayList<Pair<String, Integer>>(1);
	static public Aggregator aggregator;
	Tiers tierHead;               //Assume multiple non-synchronized readers
	Sequencer sequencer;
	Stack<Integer> depthStack;
	StructuredElement lookingFor;

    //These can be overidden by the items that extend TrieMatcher 
	Similarity similarity = new ResultScorer_cos();//ResultScorer_prefixPhrase();
	public float sim(StructuredElement element, String description) {
	    return similarity.sim(element,description);
	}
	public float sim(StructuredElement element, StructuredElement description) {
	    return similarity.sim(element,description); 
	}
    
	public TrieMatcher() {
		this.sequencer = new Sequencer();
		this.depthStack = new Stack<Integer>();	
	}

	public TrieMatcher(Tiers tiers) {     // Allocates a sequencer (thread safety)
		this();
		this.tierHead = tiers;
	}

	public TrieMatcher(Tiers tiers, Sequencer sequencer) {  // Accepts a sequencer
		this.tierHead = tiers;
		this.sequencer = sequencer;
		this.depthStack = new Stack<Integer>();
	}

	public static void setAggregator(String aggregatorName, String aggregatorParm) {
		aggregator = AggregatorBase.getAggregator(aggregatorName, aggregatorParm);	
	}

	public List<KMDefinition> findMatch(String s) {
		if(s==null||s.length()==0)return emptyMatchable;
		lookingFor = sequencer.arrange(s);
		return findMaxMatch(tierHead, 0);
	}

	List<KMDefinition> findMoreMaxMatch(Tiers tier, int pos) {
		depthStack.push(pos - 1);
		List<KMDefinition> res = findMaxMatch(tier, pos);
		depthStack.pop();
		return res;
	}

	enum SequenceType { sameInorder, sameNotInorder, different }

	/**
	 * Check for identically ordered contentIndex sets, between the matched input and
	 * the phrasematch or exactmatch text. This optimization is equivalent to a
	 * more expensive value comparison, as the input is known to contain all the
	 * indexedTerms of the match text.
	 * 
	 */
	private boolean sameSequence(KMDefinition km) {
		if (km.element.size() != depthStack.size()) return false;
		int sdiff = km.element.getIndex(0) - lookingFor.getIndex(depthStack.get(0));
		for (int depth = 1; depth < Math.min(km.element.size(), depthStack.size()); depth++) {
			if (km.element.getIndex(depth) - lookingFor.getIndex(depthStack.get(depth)) != sdiff) {
				return false; }
		}
		return true;
	}

	boolean inSequence(KMDefinition km) {
		Integer[] sequenceVerifier = new Integer[lookingFor.size() + 1];
		for (int depth = 0; depth < Math.min(km.element.size(), depthStack.size()); depth++) {
			sequenceVerifier[lookingFor.getIndex(depthStack.get(depth))] = km.element.getIndex(depth);
		}
		Integer prior = 0;
		for (Integer x : sequenceVerifier)
			if (x == null) continue;
			else if (x < prior) return false;
			else prior = x;
		return true;
	}

	void showStacks(KMDefinition km) {
		System.out.println("insequence="+inSequence(km));
		System.out.println("-----siz="+km.element.size());
		for (int depth = 0; depth < Math.min(km.element.size(), depthStack.size()); depth++) {	
			int kmL = km.element.getIndex(depth);
			String kmString = km.element.getValue(depth);
			int dp = depthStack.get(depth);
			int looking = lookingFor.getIndex(dp);
			String itemString = lookingFor.getValue(dp);
			System.out.printf("%03d  %03d  %03d  %03d | %10s %10s\n", depth, kmL, dp, looking, kmString, itemString);	
		}
	}

	/**
	 * validForMatchtype implements keymatch semantic
	 * 
	 * @param kmDef
	 * @return List of valid keymatches
	 */

	protected List<KMDefinition> validForMatchtype(List<KMDefinition> kmDef) {
		List<KMDefinition> valueResult = new ArrayList<KMDefinition>();
		for (KMDefinition km : kmDef) { 
			if ( 	(km.kType == MatchType.KEYWORDMATCH)      ||   
					(km.kType == MatchType.INORDERMATCH 
							&& inSequence(km))                ||
							(km.kType == MatchType.PHRASEMATCH 
									&& sameSequence(km))              ||
									(km.kType == MatchType.EXACTMATCH 
											&& depthStack.size() == lookingFor.size()
											&& sameSequence(km))   )  {
				valueResult.add(km);
			}
		}
		return valueResult;
	}

	/**
	 * reduce aggregates scores according to implementation of RecordTally.
	 * <b>NOTE:</b> 
	 * When the useSimilarity is set (Constants.useSimilarity) this will 
	 * bias the score by a similarity function.  This similarity function
	 * compares the users' input with the attributes of the keymatch. In
	 * one case it uses the description, in the other it uses the keywords.
	 * Neither one does explicit stopword removal.  
	 * The invocation of the resultScorer.sim takes either a string 
	 * or a structuredElement.  The string form handles the raw description stored
	 * in kmDef.description. The structuredElement form handles the keywords
	 * @param list
	 * @return List unique keymatches, orderd by aggregated score
	 */
	@SuppressWarnings("static-access")
	public Collection<Pair<String, Integer>> reduce(List<KMDefinition> list) {
		if(SHOWdetail) {
			System.out.println("REDUCE: " );
			for(Atom atom : lookingFor) System.out.print(atom.index+":"+atom.value+" ");
			System.out.println();
			for(KMDefinition kmDef : list) {
				System.out.println(kmDef.asString());
			}
		}
		if (list == null || list.size() == 0) return emptyReduction;
		RecordTally st = new RecordTally();
		for (KMDefinition kmDef : list) {
		    st.addItem(kmDef, kmDef.score);
		}
		Collection<Pair<String, Integer>> res = st.sortEntries();
		return res;
	}

    
	public Collection<KMDefinition>kmReducer(List<KMDefinition> list) {
		Map<String,KMDefinition>reverseMap=new HashMap<String,KMDefinition>();
		for(KMDefinition kmd : list) {
			if(reverseMap.get(kmd.url())==null)
				reverseMap.put(kmd.url(),new KMDefinition(kmd));
		}
		Collection<Pair<String,Integer>> reduced = reduce(list);
		Collection<KMDefinition>result=new ArrayList<KMDefinition>();
		for(Pair<String,Integer> p : reduced) {
			KMDefinition km = reverseMap.get(p.s());
			km.score=p.t();
			result.add(km);
		}
		return result;
	}

	/**
	 * Implement the business logic for aggregation and ranking of results.
	 * @author Michah.Lerner
	 *
	 */
	//aggregates on the url field
	private class RecordTally {
		Map<String, Integer> smap;
		RecordTally() {
			smap = new HashMap<String, Integer>();
		}

		void addItem(KMDefinition km, int tally) {
			Integer i = smap.get(km.url);
			if (i == null) i = 0;
			smap.put(km.url, aggregator.aggregate(i,tally));
		}

		SortedSet<Pair<String, Integer>> sortEntries() {
			SortedSet<Pair<String, Integer>> sts = new TreeSet<Pair<String, Integer>>(new CompareEntries());
			for(Entry<String,Integer>me : smap.entrySet())
				sts.add(new Pair<String,Integer>(me.getKey(),me.getValue()));
			return sts;
		}

		class CompareEntries implements Comparator<Pair<String, Integer>> {
			public int compare(Pair<String, Integer> o1, Pair<String, Integer> o2) {
				if (o1.t().equals(o2.t())) return o1.s().compareTo(o2.s());
				return -o1.t().compareTo(o2.t());
			}
		}
	}
}
