//This is unpublished source code. Michah Lerner 2006

package trieMatch.util.aggregation;


	/** 
	 * AggregateSUMacc combines max with unit counter aggregation.
	 * @author Michah.Lerner
	 *
	 */

	public class AggregateSUMacc extends AggregatorBase  {
		public AggregateSUMacc() {
			super();
		}
		public AggregateSUMacc(String params) {
			this(Integer.parseInt(params));
		}
		public AggregateSUMacc(Integer increment) {
			this.increment=increment;
		}
		public int aggregate(int v1, int v2) {
			return v1+v2+increment;
		}

}
