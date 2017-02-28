//This is unpublished source code. Michah Lerner 2006

package trieMatch.util.aggregation;

import trieMatch.util.Constants;


/** 
 * AND (max) aggregation.
 * @author Michah.Lerner
 *
 */
public class AggregateMIN extends AggregatorBase {
	Integer scale = Constants.DEFAULT_AGGREGATION_SCALEFACTOR;
    public AggregateMIN() {
		super();
	}
    
	public int aggregate(int v1, int v2) {
        v2=(int) (scale*1D/(v2+1D));
		return Math.max(v1,v2);
	}
}
