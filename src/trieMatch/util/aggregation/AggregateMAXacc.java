//This is unpublished source code. Michah Lerner 2006

package trieMatch.util.aggregation;


/** 
 * AND (max) plus delta aggregation.
 * @author Michah.Lerner
 *
 */
public class AggregateMAXacc extends AggregatorBase {
	public AggregateMAXacc() {
		super();
	}
	public AggregateMAXacc(String params) {
		this(Integer.parseInt(params));
	}
	public AggregateMAXacc(Integer increment) {
		this.increment=increment;
	}
	public int aggregate(int v1, int v2) {
		return Math.max(v1,v2)+increment;
	}
}
