package testlibrary;

import java.util.Arrays;

public class Stats {

	/**
	 * Creates an object array that can be directly added to an HSSFWorksheet to show 
	 * various data values.
	 * @param obj - The array of raw results made from the test.
	 * @param name - Label for this row.
	 * @return - Returns an Object array which includes name and calculated statistical values
	 * 		     obtained from raw data.
	 */
	public static Object[] getStats(Object[] obj, Object name){
		
		long[] dataOrig = new long[obj.length - 1];
		long[] data = new long [dataOrig.length - 1];
		long first;
		double average;
		long sum = 0;
		double variance = 0;
		
		//Extract all of the data from the raw data array.
		for(int i = 0; i < obj.length - 1; i++){
			dataOrig[i] = (long)obj[i+1];
		}
		first = dataOrig[0];
		int length = dataOrig.length - 2;
		Object [] stats = new Object [12];
		Arrays.sort(dataOrig);
		
		//Delete the largest data point from the data. 
		for(int i = 0; i < data.length; i++){
			data[i] = dataOrig[i];
		}
		for(long d : data){
			sum += d;
		}
		average = 1.0 * sum / data.length;
		for(long d : data){
			variance += Math.pow(average - d, 2);
		}
		
		/*
		 * Put all of the found values into the array
		 * TODO: Stop hard coding the percentile values.
		 */
		stats[0] = name;
		stats[1] = first;
		stats[2] = average;
		stats[3] = Math.sqrt(variance/data.length);
		stats[4] = data[(int)Math.ceil(length * .5)];
		stats[5] = data[(int)Math.ceil(length * .6)];
		stats[6] = data[(int)Math.ceil(length * .7)];
		stats[7] = data[(int)Math.ceil(length * .8)];
		stats[8] = data[(int)Math.ceil(length * .9)];
		stats[9] = data[(int)Math.ceil(length * .95)];
		stats[10] = data[(int)Math.ceil(length * .99)];
		stats[11] = data[(int)Math.ceil(length)];
		
		return stats;
	}
}
