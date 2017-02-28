//This is unpublished source code. Michah Lerner 2006

package trieMatch.util.aggregation;

import trieMatch.util.Constants;


/** 
 * AND (max) plus delta aggregation.
 * @author Michah.Lerner
 *
 */
public class AggregateMINacc extends AggregatorBase {
    Integer scale = Constants.DEFAULT_AGGREGATION_SCALEFACTOR;
	public AggregateMINacc() {
        super();

	}
	public AggregateMINacc(String params) {
		this(Integer.parseInt(params));
	}
	public AggregateMINacc(Integer scale) {
		this.scale=scale;
	}
	public int aggregate(int v1, int v2) {
            v2=(int) (scale*1D/(v2+1D));
            return Math.max(v1,v2);
	}
}
