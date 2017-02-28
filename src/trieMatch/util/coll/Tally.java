//This is unpublished source code. Michah Lerner 2006

package trieMatch.util.coll;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import trieMatch.util.Numerics;


/** 
 * Simple frequency tally of the items that are added to the tally.  Does not generate anything; merely tallies the occurrences.
 * @author Michah.Lerner
 * @see TallyCList for tallies of the combinations generable as sequences (lists) from each input
 * @see TallyCSet for tallies of the cominbations generable as sets (order independent, no duplicates) from each input
 * @param <S> The type of items which are tallied.
 */
public class Tally<S extends Comparable<? super S>> extends HashMap<S,Integer>{ //Hash<S,Integer> {
	private static final long serialVersionUID = 6633245585112560091L;
	Integer total;
	private final Integer ONE=1;
	public Tally() {
		total=0;
	}

	/**
	 *  Increment the count for this item, putting it into the map if it is not there already
	 * @param s the item that should have its counter incremented
	 * @return the observed frequency count of the item
	 */
	public Integer inc(final S s) {
		return inc(s,ONE);
	}
	public Integer inc(final S s, int count) {
		total+=count;
		return this.containsKey(s)?put(s,get(s)+count):put(s,count);
	}
	public void incAll(Collection<S> coll) {
		for(S s : coll)inc(s);
	}
	public Integer getTotal() {
		return total;
	}

	/**
	 * Update the statistics of one tally, by adding in the statistics of the other tally.
	 * @param other the tally that should be added to the current tally
	 * @return ths current tally updated withby adding the statistics of the other tally
	 */
	public Tally add(Tally<S>other) {
		for(Map.Entry<S,Integer> me : other.entrySet()) {
			S s = me.getKey();
			Integer i = me.getValue();
			if(!this.containsKey(s))this.put(s,i);
			else this.put(s,this.get(s)+i);
			total++;
		}
		return this;
	}
	
	/** 
	 * Get the count of how many times the item s has been observed
	 * @param s item to look for
	 * @return count of how many times it was added
	 */
	Integer get(final S s) { return super.get(s); }
	
	/**
	 * Return this tally.
	 * @return this tally
	 */
	public Map<S,Integer>get()    { return this; }
	
	/**
	 * Compute the actual item frequencies, where the sum of the frequencies equals one. 
	 * @return a float vector containing the frequencies for each item, using the native order 
	 * of the method.  
	 * <em>NOTE: <em> Classes that use this should recompute the frequencies after an item is added.
	 * This is not merely to be sure the item's statistics are current, but also to ensure the hash order is 
	 * the same as the array order, for the retrieved items.  If items need to be added without recomputing
	 * the frequencies, then change the HashSet to a LinkedHashSet.  
	 */
	public float [] getFrequencies() {
		float[] result = new float[this.size()];
		int itemno=0;
		float unitWeight=1F/total;
		for(Integer itemFreq : this.values())result[itemno++]=itemFreq*unitWeight;
		return result;
	}
	
	/**
	 * Get the full frequency of any word that is found in the collection.
	 * @param word
	 * @return the frequency of the word occurrence
	 */
	public double getFrequency(S word) {
		Integer count = get(word);
		if(count==null||total==0)return 0D;
		return this.get(word)/(double)total;
	}
	
	/**
	 * Validate that the hash order has not corrupted the correspondance between values and inputs. 
	 * @return true if the frequencies are still consistent with the iterator order, false if recomputation
	 * is needed or an LinkedHashSet should be used.
	 */
	public boolean validate() {
		float[] freqs = getFrequencies();
		int i = 0;
		for(S value : this.keySet()) {
			Double d = getFrequency(value);
			Double relErr = Numerics.relativeError(d,Double.valueOf(freqs[i]));
			if(relErr>Numerics.closeEnough) {
				System.out.println("ERROR IN POSITION " + i + " GOT " + d + " expect " + freqs[i] + " relative error is " + relErr);
				return false;
			}
			i++;
		}
		return true;
		}
	}
//	Object[] getByFreq()   {
//	Sortable l = new Sortable();
//	Object[] o = l.sort2(this);
//	return o;
//}
//Object[] getByValue()   {
//	Sortable l = new Sortable();
//	Object[] o = l.sort(this);
//	return o;
//}
	
