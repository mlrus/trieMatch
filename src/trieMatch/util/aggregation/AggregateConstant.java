//This is unpublished source code. Michah Lerner 2006

package trieMatch.util.aggregation;

import trieMatch.Interfaces.Aggregator;
import trieMatch.util.Constants;

/** 
 * AggregateSUMacc combines max with unit counter aggregation.
 * @author Michah.Lerner
 *
 */

public class AggregateConstant extends AggregatorBase implements Aggregator {
	/** 
	 * Constant
	 *
	 */
	int increment=Constants.DEFAULT_AGGREGATION_INCR;
	public AggregateConstant() {
		super();
	}
	public AggregateConstant(String parm) {
		this(Integer.parseInt(parm));
	}
	public AggregateConstant(Integer increment) {
		this.increment=increment;
	}
	public int aggregate(int v1, int v2) {
		return v1+increment; // ignores v2; just counts by the increment
	}

}
