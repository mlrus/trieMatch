//This is unpublished source code. Michah Lerner 2006

package trieMatch.util.aggregation;

import trieMatch.Interfaces.Aggregator;
import trieMatch.util.Constants;

/** 
 * AND (max) aggregation.
 * @author Michah.Lerner
 *
 */
public class AggregateMAX extends AggregatorBase {
	public AggregateMAX() {
		super();
	}
	public int aggregate(int v1, int v2) {
		return Math.max(v1,v2);
	}
}
