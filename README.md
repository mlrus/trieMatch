# trieMatch

The trieMatch provides efficient solution to the KeyMatch problem posed by Google. The solution runs standalone, and also in the Google Search Appliance (GSA) as a *Onebox* extension. 

The simple baseline algorithm code is in  ```src/simple```.  The fully fleshed-out code is ```src/trieMATCH```.  See ```src/README.html``` for detailed instructoon on installation as a *OneBox*.

Lexical trie and discretionary sequencing

1. Sort texts (lexicographic or frequency+lexical); store as a trie and retain the index vectors; i.e., 'dog gnat cat' ==> ((cat, dog, gnat), (2, 0, 1)) and st
2. Find longest match (or matches) using the greedy method
3. Validate match-constraints (exact, phrase, inorder) by comparison on index vectors of input and matching text
4. Extended to prefix matches with taillist

## Simple Sweet Code

(The download version adds structure, multi-threading/interface/methods, etc.)

```
public class Tiers {
    Map tier;            // next step
    boolean end = false; // path completed?
    List valueRecord;    // collected results
    
    Tiers() { tier = new HashMap(); }
    
    List findMaxMatch() { return findMaxMatch(0); }
    
    List findMaxMatch(int pos) {
        if (pos >= termsListHolder.size()) {
            if (!end) return null;
            return validResults(valueRecord, depthStack);
        }
        Tiers nTier = tier.get(termsListHolder.get(pos));
        if (nTier != null) {
            // The current term is matchable
            depthStack.push(termsIndexHolder.get(pos));
            List v1 = nTier.findMaxMatch(pos + 1);
            depthStack.pop();
            List v2 = null;
            v2 = findMaxMatch(pos + 1);
            if (v1 == null) {
                return v2;
            }
            if (v2 == null) {
                return v1;
            }
            if (addall) {
                v1.addAll(v2); return v1;
            }
            if(v1==null||v1.size()==0)
                return v2;
            if(v2==null||v2.size()==0)
                return v1;
            if (v1.get(0).score < v2.get(0).score)
                return v2;
            return v1;
        }
        return findMaxMatch(pos + 1);
        // (keep tail recursion)
    }
}
``` 

The auxiliary depthStack distinguishes between sequential, inorder and unordered results.

Fleshed out,
```
//This is unpublished source code.
//(Pedantic version, not thread-safe)

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.Stack;
import java.util.TreeMap;

import MatchType;
import ValueRecord;
import IndexedItem;

/**
 * Trie-based matching over totally ordered items.
 * @author Michah.Lerner
 *
 */

public class Tiergen {
    boolean addall; // true::accumulate matches false::only keep longest match
    boolean exp;    // true::exp prefix matches
    Tiers tiers;
    Stack depthStack;
    SortedIndexCollector instanceCollector;
    IndexedItem indexedItem ;
    
    Tiergen() {
        this(false);
    }
    
    Tiergen(KeyMatch km) {
        this(km.keysetStore);
    }
    
    Tiergen(Tiergen tiergen) {
        instanceCollector = new SortedIndexCollector();
        depthStack        = new Stack<Integer>();
        this.addall       = tiergen.addall;
        this.exp          = tiergen.exp;
        this.tiers        = new Tiers();
        this.tiers.valueRecord = tiergen.tiers.valueRecord;
    }
    
    Tiergen(boolean addall) {
        this.addall       = addall;
        this.exp          = false;
        instanceCollector = new SortedIndexCollector();
        depthStack        = new Stack<Integer>();
        tiers             = new Tiers();
    }
    Tiergen(boolean addall, boolean exp) {
        this.addall       = addall;
        this.exp          = exp;
        instanceCollector = new SortedIndexCollector();
        depthStack        = new Stack<Integer>();
        tiers             = new Tiers();
    }
    
    public void add(List<String> c, ValueRecord valueRecord) {
        tiers.add(c, valueRecord);
    }
    
    public void printTiers(PrintStream out) {
        tiers.printTiers(out);
    }
    
    /**
     * Head function for finding the maximal match in log time
     * @param s Item to find
     * @return Items found
     */
    public  List<ValueRecord> findMaxMatch(String s) {
        depthStack.clear();
        indexedItem = instanceCollector.indexedItem(s);
        List<ValueRecord> answer = tiers.findMaxMatch();
        return answer;
    }
    
    static final List<ValueRecord> emptyValueRecord = new ArrayList<ValueRecord>();
    
    /**
     * Storage of the template items that we can find in the input
     * @author Michah.Lerner
     *
     */
    public class Tiers {
        Map<String, Tiers> tier;        // next step
        boolean end = false;            // path completed?
        List<ValueRecord> valueRecord;  // path value(s)
        
        Tiers() {
            tier = new TreeMap<String, Tiers>();
            //HashMap<String, Tiers>();
        }
        
        
        List<ValueRecord> findMaxMatch() {
            return exp?findMaxPrefixMatch(0):findMaxMatch(0);
        }
        
        /**
         * Recursion from position <em>pos</em> to find the maximum match.
         * @param pos current term in the input that is being matched
         * @return list of matches
         */
        List<ValueRecord> findMaxMatch(int pos) {
            if (pos >= indexedItem.queryLength) { return end ? validForMatchtype(valueRecord) : emptyValueRecord; }
            Tiers nTier = tier.get(indexedItem.stringList.get(pos));
            
            if (nTier != null) {
                depthStack.push(pos);
                List<ValueRecord> v1 = nTier.findMaxMatch(pos + 1);
                depthStack.pop();
                List<ValueRecord> v2 = findMaxMatch(pos + 1);
                if (v1.size() == 0) return v2;
                if (v2.size() == 0) return v1;
                if (addall) {
                    v1.addAll(v2);
                    return v1;
                }
                return (v1.get(0).score<v2.get(0).score)?v2:v1;
            }
            return findMaxMatch(pos + 1); // tail recursive form.
        }
        
        
    /**
     *  Check for identically ordered contentIndex sets, between the matched input
     *  and the phrasematch or exactmatch text.  This optimization is equivalent to
     *  a more expensive string comparison, as the input is known to contain all the
     *  terms of the match text.
     *
     * @param vr
     * @return list of ValueRecords conforming to the semantics of
     * ExactMatch, PhraseMatch and KeywordMatch
     */
        
        boolean sameSequence(ValueRecord vr) {
            if (vr.keyIndices.size() != depthStack.size()) return false;
            int sdiff = vr.keyIndices.get(0) - indexedItem.indexList.get(depthStack.get(0));
            for (int depth = 1; depth < Math.min(vr.keyIndices.size(), depthStack.size()); depth++) {
                if (vr.keyIndices.get(depth) - indexedItem.indexList.get(depthStack.get(depth)) != sdiff) { return false; }
            }
            return true;
        }
        
        boolean inSequence(ValueRecord vr) {
            Integer[] trace = new Integer[indexedItem.queryLength + 1];
            for (int depth = 0; depth < Math.min(vr.keyIndices.size(), depthStack.size()); depth++) {
                trace[indexedItem.indexList.get(depthStack.get(depth))] = vr.keyIndices.get(depth);
            }
            Integer prior = 0;
            for (Integer ref : trace)
                if (ref == null) continue;
                else if (ref < prior) return false;
                else prior = ref;
            return true;
        }
        
        List<ValueRecord> validForMatchtype(List<ValueRecord> valueRecord) {
            List<ValueRecord> valueResult = new ArrayList<ValueRecord>();
            for (ValueRecord vr : valueRecord) {
                if ((vr.kType == MatchType.KEYWORDMATCH) ||
                    (vr.kType == MatchType.INORDERMATCH && inSequence(vr)) ||
                    (vr.kType == MatchType.PHRASEMATCH && sameSequence(vr))||
                    (vr.kType == MatchType.EXACTMATCH && depthStack.size() == indexedItem.queryLength && sameSequence(vr))) {
                    valueResult.add(vr);
                }
            }
            return valueResult;
        }
        
/**
 * Construct a sorted trie at the word level.  The trie stores all matchtext.
 * The leaf nodes contain a ValueRecord, which holds the type, url and description
 * @param matchText
 * @param valueRecord
 */
        public void add(List<String> matchText, ValueRecord valueRecord) {
            if (matchText == null || matchText.size() == 0) return;
            Tiers addpoint = this;
            Tiers prior = null;
            List<String> copyOfList = new ArrayList<String>(matchText);
            Collections.sort(copyOfList);
            for (String head : copyOfList) {
                if (!addpoint.tier.containsKey(head)) {
                    Tiers nextPoint = new Tiers();
                    addpoint.tier.put(head, nextPoint);
                    prior = addpoint;
                    addpoint = nextPoint;
                } else {
                    prior = addpoint;
                    addpoint = addpoint.tier.get(head);
                }
                prior.tier.put(head, addpoint);
            }
            addpoint.end = true;
            if(addpoint.valueRecord==null)addpoint.valueRecord = new ArrayList<ValueRecord>();
            addpoint.valueRecord.add(valueRecord);
        }
        
        void printTiers(PrintStream out) {
            printTiers(out, new Stack<String>());
        }
        
        private void printTiers(PrintStream out, Stack<String> name) {
            if (end) out.println(name);
            for (String te : tier.keySet()) {
                name.push(te);
                tier.get(te).printTiers(out, name);
                name.pop();
            }
        }
        
        List<Map.Entry<String, Tiers>> getPrefixes(Map<String, Tiers> current, String name) {
            if (current instanceof SortedMap) return findPrefixesB((SortedMap<String, Tiergen.Tiers>) current, name);
            return findPrefixesA(current, name);
        }
        
        List<Map.Entry<String, Tiers>> findPrefixesA(Map<String, Tiers> current, String name) {
            List<Map.Entry<String, Tiers>> result = new ArrayList<Map.Entry<String, Tiers>>();
            for (Map.Entry<String, Tiers> me : current.entrySet()) {
                if (me.getKey().startsWith(name)) result.add(me);
            }
            return result;
        }
        
        List<Map.Entry<String, Tiers>> findPrefixesB(SortedMap<String, Tiers> current, String name) {
            List<Map.Entry<String, Tiers>> result = new ArrayList<Map.Entry<String, Tiers>>();
            SortedMap<String, Tiers> sub = current.tailMap(name);
            for (Map.Entry<String, Tiers> me : sub.entrySet()) {
                if (me.getKey().startsWith(name)) result.add(me);
                else break;  
            }
            return result;
        }
    }
}
```
