//This is unpublished source code. Michah Lerner 2006

package trieMatch.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;


/** 
 * Numeric processing of vectors of floats, doubles or integers. Provides 
 * entropy computation and information score, which the commons-math
 * does not provide.  Also provides several other functions (min, max, sum, variance) 
 * although those are better provided by commons-math.
 * @author Michah.Lerner
 *
 */
public final class Numerics {
	public final static Double closeEnough = 5E-5;
	double max= Float.NEGATIVE_INFINITY;
	double min= Float.POSITIVE_INFINITY;
	double sum=(float) 0.0;
	double mean=Float.NaN;
	double sd= Double.NaN;
	
	Numerics(float[] v) {
		for(int i=0;i<v.length;i++) {
			min = Math.min(min,v[i]);
			max = Math.max(max, v[i]);
			sum += v[i];
		}
		mean = sum / v.length;
		float ssd = (float) 0.0;
		for(int i=0;i<v.length;i++) {
			ssd+=(square(v[i]-mean));
		}
		if(v.length>1) sd = Math.sqrt(ssd / (v.length - 1.0D));
	}
	Numerics(double[] v) {
		for(int i=0;i<v.length;i++) {
			min = Math.min(min,v[i]);
			max = Math.max(max, v[i]);
			sum += v[i];
		}
		mean = sum / v.length;
		double ssd = 0D;
		for(int i=0;i<v.length;i++) {
			ssd+=(square(v[i]-mean));
		}
		if(v.length>1) sd = Math.sqrt(ssd / (v.length - 1.0D));
	}
	
	class NumericVector<T extends Number> {
		double sum(List<T> in) {
			double s = 0.0;
			for(Number n : in) s+=n.doubleValue();
			return s;
		}
	}	

	static double H(double[] v) {
		double s = 0.0;
		double prob = 1.0 / v.length;
		for (int i = 0; i < v.length; i++)
			s -= (prob) * Math.log(v[i]);
		return s;
	}
	
	static double log2(double v) {
		return Math.log(v)/Math.log(2.0D);
	}
	
	static double entropy(double[] v) { // Entropy computation
		double ntrop = 0.0;
		double t = 0.0;
		for(double d : v) t+=d;
		for(double d : v) {
			double prob = d/t;
			ntrop -= prob*Math.log(prob);
		}
		return ntrop;
	}
	public static double entropy(float[] v) { // Entropy computation
		double ntrop = 0.0;
		double t = 0.0;
		for(float d : v) t+=d;
		for(float d : v) {
			double prob = d/t;
			ntrop -= prob*Math.log(prob);
		}
		return ntrop;
	}
	
	public static Double entropy(Collection<Double> v) { // Entropy computation
		Double ntrop = 0.0;
		Double t = 0.0;
		for(Double d : v) t+=d;
		for(Double d : v) {
			Double prob = d.doubleValue()/t;
			ntrop -= prob*Math.log(prob);
		}
		return ntrop;
	}
	
	static double entropy(int[] v) {
		int tot=0;
		for(int i=0;i<v.length;i++)tot+=v[i];
		double[] fv = new double[v.length];
		for(int i=0;i<v.length;i++) fv[i]=(double)v[i]/tot;
		System.out.println("intEnt: "); for(int i=0;i<v.length;i++)System.out.print("p(n="+i+")="+fv[i]+"; ");System.out.println();
		return entropy(fv);
	}
	
	static double square(double v) {
		return v * v;
	}
	
	static int sumInt(int[] iv) {
		int s = 0;
		for(int i=0;i<iv.length;i++)s+=iv[i];
		return s;
	}
	
	static float sumFloat(float [] v) {
		//TODO:numerically stable sum
		float s = (float)0.0;
		for(int i=0;i<v.length;i++)s+=v[i];
		return s;
	}
	
	static double sumDouble(double[] v)  {
		//TODO:numerically stable sum
		double s = 0.0D;
		for(int i=0;i<v.length;i++)s+=v[i];
		return s;
	}
	
	static String printVec(int[] ivec) {
		return printVec(ivec, "%-3d ");
	}
	
	static String printVec(int[] ivec, String fmt) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < ivec.length; i++)
			sb.append(String.format(fmt, ivec[i]));
		return sb.toString();
	}
	
	static String printVec(double[] fvec) {
		return printVec(fvec, "%-3.1f ");
	}
	
	static String printVec(double[] fvec, String fmt) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < fvec.length; i++)
			sb.append(String.format(fmt, fvec[i]));
		return sb.toString();
	}
	
	/**
	 * @param v1
	 * @param v2
	 * @return relative error
	 */
	public static float relativeError(Float v1, Float v2) {
		float d1 = Math.abs(v1);
		float d2 = Math.abs(v2);
		return (float) (Math.abs(d1-d2)/(d1+d2)/2D);
	}
	
	/**
	 * @param v1
	 * @param v2
	 * @return relative error
	 */
	public static Double relativeError(Double v1, Double v2) {
		if(v1==null&&v2==null)return null;
		Double d1 = Math.abs(v1);
		Double d2 = Math.abs(v2);
		return (Math.abs(d1-d2)/(d1+d2)/2D);
	}
	
	/**
	 * Get min of the elements that are neither NaN nor Null..
	 * @param vlist
	 * @return  min of the items that are neither NaN nor Null.
	 */
	public static Number nmin(Number ... vlist) { return nmin(Arrays.asList(vlist)); }
	public static Number nmin(List<Number>vlist) {
		Double result = Double.NaN;
		for(Number d : vlist) {
			Double dd=d.doubleValue();
			if(dd==null||dd.isNaN())continue;
			if(result==null||result.isNaN())result=dd;
			else result=Math.min(result,dd);
		}
		return result;
	}
	
	/**
	 * Get max of the elements that are neither NaN nor Null..
	 * @param vlist
	 * @return  max of the items that are neither NaN nor Null.
	 */
	public Number nmax(Number ... vlist) { return nmax(Arrays.asList(vlist)); }
	public Number nmax(List<Number>vlist) {
		Double result = Double.NaN;
		for(Number d : vlist) {
			Double dd=d.doubleValue();
			if(dd==null||dd.isNaN())continue;
			if(result==null||result.isNaN())result=dd;
			else result=Math.max(result,dd);
		}
		return result;
	}


	public static Collection<Number> toNumberList(String[]args) {
		List<Number>vals=new ArrayList<Number>();
		for(String s : args) {
			if(!s.matches("([0-9]+(\\.[0-9]*))|(\\.[0-9]+)")) vals.add(Double.NaN);
			else vals.add(Double.parseDouble(s));
		}
		return vals;
	}
	
	public static Collection<Integer>range(int from, int to) {
		List<Integer>result=new ArrayList<Integer>();
		for(Integer i=from;i<to;i++)result.add(i);
		return result;
	}
	
}
