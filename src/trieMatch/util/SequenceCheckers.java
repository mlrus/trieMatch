//This is unpublished source code. Michah Lerner 2006

package trieMatch.util;

import java.util.Collection;
import java.util.Iterator;

/** 
 * Feature extractor, checks if the terms of an item occur in the same order within a group of words,
 * and also checks if the terms of an item occur sequentially without any gaps between the terms
 * @author Michah.Lerner
 *
 */
public class SequenceCheckers {
	/**
	 * Check if all the terms of the item occur within the group, and in the same order. The order does not have to
	 * be sequential as long as its the same subsequence.  Intervening non-matching terms make no difference.
	 * @param groupOfWords to check against
	 * @param item collection of items to check for
	 * @return true if the items occur in the same order within the group
	 */
	static public boolean inOrder(Collection<String>groupOfWords, Collection<String>item) {
		Iterator<String>i1 = groupOfWords.iterator();
		Iterator<String>i2 = item.iterator();
		String groupWord = i1.next();
		String itemWord =i2.next();
		while(i1.hasNext()&&i2.hasNext()) {
			if(groupWord.equalsIgnoreCase(itemWord)) itemWord=i2.next();
			groupWord=i1.next();
		}
		return(!i2.hasNext());
	}
	
	/**
	 * Check if all the terms of the item occur consecutively within the group. The order must be identical, but
	 * need not start at the first term of the group or end at the last term of the group. 
	 * @param groupOfWords to check against
	 * @param item collection of items to check for
	 * @return true if the items occur consecutively within the group
	 */
	static public boolean inSequence(Collection<String>groupOfWords, Collection<String>item) {
		Iterator<String>group = groupOfWords.iterator();
		Iterator<String>words = item.iterator();
		String groupWord = group.next();
		String itemWord = words.next();
		while(group.hasNext()&&!groupWord.equalsIgnoreCase(itemWord)) groupWord=group.next(); 
		while(groupWord.equalsIgnoreCase(itemWord)) {
			if(!words.hasNext())return true; 
			if(!group.hasNext())return false;
			groupWord=group.next();
			itemWord=words.next();
		}
		return false;
	}
}
