//This is unpublished source code. Michah Lerner 2006

package trieMatch.util.aggregation;

import trieMatch.Interfaces.Aggregator;
import trieMatch.util.Constants;

/**
 * OR (sum) aggregations
 * @author Michah.Lerner
 *
 */
public class AggregateSUM extends AggregatorBase {
	public AggregateSUM() {
		super();
	}
	public int aggregate(int v1, int v2) {
		return v1+v2;
	}
}
