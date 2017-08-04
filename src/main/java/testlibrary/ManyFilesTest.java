package testlibrary;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import org.apache.poi.hssf.usermodel.HSSFSheet;

import com.amazonaws.services.s3.model.S3Object;

public class ManyFilesTest {
	
	/**
	 * Uploads 100 files each ~36MB in size to the designated region's bucket.
	 * NOTE: These files are not hashed.
	 * @param n This designates which region to use.
	 */
	public static void uploadFiles(int n) throws IOException{
		File file;
		String key;
		for(int i = 0; i < GlobalValues.numSplits;i++){
			key = FileCreator.createKey(i + GlobalValues.splitKey);
			file = FileCreator.createSplitFile();
			GlobalValues.s3s[n].putObject(GlobalValues.buckets[n], key, file);
		}
	}
	/**
	 * Uploads 100 files each ~3.6MB in size to the designated region's bucket.
	 * NOTE: These files are not hashed.
	 * @param n This designates which region to use.
	 * @param x This indicates how many splits
	 */
	public static void uploadFiles(int n, int x) throws IOException{
		File file;
		String key;
		for(int i = 0; i < x; i++){
			key = FileCreator.createKey(i + GlobalValues.splitKey);
			file = FileCreator.createSplitFile();
			GlobalValues.s3s[n].putObject(GlobalValues.buckets[n], key, file);
		}
		
	}
	
	/**
	 * Downloads the designated files from the bucket and writes results to an excel sheet
	 * @param n - Designates which region to use.
	 * @param segments - This array designates which segments to pull from the bucket.
	 * @param set - Object containing information for the test to use.
	 * @throws IOException 
	 */
	public static void run(int n, int[] segments, S3TestSetup set) throws IOException{
		int index;
		Object [] times;
		long totalTime = 0;
		long endTime = 0;
		long startTime = 0;
		long firstTime = 0;
		String key;
		S3Object obj;
		char[] buf = new char[set.getBufferSize()];
		
		//Create the first sheet.
		HSSFSheet sheet = set.getExcel().getSheet(GlobalValues.multipleSingleFileDownloadName);
		if(sheet != null){
			index = set.getExcel().getSheetIndex(GlobalValues.multipleSingleFileDownloadName);
			set.getExcel().removeSheetAt(index);
		}
		sheet = set.getExcel().createSheet(GlobalValues.multipleSingleFileDownloadName);
		
		//Create the sheet holding the raw data.
		HSSFSheet sheet2 = set.getExcel().getSheet(GlobalValues.multipleSingleFileDownloadRawName);
		if(sheet2 != null){
			index = set.getExcel().getSheetIndex(GlobalValues.multipleSingleFileDownloadRawName);
			set.getExcel().removeSheetAt(index);
		}	
		sheet2 = set.getExcel().createSheet(GlobalValues.multipleSingleFileDownloadRawName);
		
		//Create labels for the first sheet.
    	TreeMap<String, Object[]> tree = new TreeMap<String,Object[]>();
    	tree.put("0", new Object [] {"Segment Number" , "First Time", "Average Time", "Standard Deviation",
    			"50%", "60%", "70%", "80%", "90%","95%","99%","100%"});
    	TreeMap<String, Object[]> treeRaw = new TreeMap<String,Object[]>();
    	
       	//Create labels for the raw data sheet.
    	Object[] rawHeader = new Object[set.getNumTests() + 1];
    	rawHeader[0] = "Segment Number";
    	for(int i = 0; i < set.getNumTests(); i++){
    		rawHeader[i + 1] = "Test " + (i+1);
    	}
    	treeRaw.put("0", rawHeader);
    	
    	//perform the tests for each individual segment.
    	for(int i = 0; i < segments.length; i++){
    		key = set.getSplitKeys()[segments[i]];
    		times = new Object[set.getNumTests() + 1];
	    	times[0] = segments[i];
	    	totalTime = 0;
			System.out.println("Testing download speed for segment: " + (segments[i]));
			
			//For each segment, perform the tests and record the data.
	    	for(int j = 0; j < set.getNumTests();j++){
	    		startTime = System.nanoTime();
	    		obj = set.getS3(n).getObject(set.getBucket(n), key);
	    		BufferedReader in = new BufferedReader(new InputStreamReader(obj.getObjectContent()));
		        while (in.read(buf) != -1){}
		        in.close();
	    		endTime = System.nanoTime();
				startTime = startTime / GlobalValues.convertNanoToMilli;
				endTime = endTime / GlobalValues.convertNanoToMilli;
	    		if(j == 0){
	    			firstTime = endTime - startTime;
	    			System.out.println("The first download took " + firstTime);
	    		}
	    		times[j + 1] = endTime - startTime;
	    		totalTime += endTime - startTime;	
	    	}
	    	
	    	//Put the data into the tree.
	    	treeRaw.put("" + (i+1), times);
	    	tree.put("" + (i+1), Stats.getStats(times, "" + segments[i]));
	    	System.out.println("Average time was " + 1.0 * totalTime/set.getNumTests());
	    	//Put a delay between successive tests if necessary.
			try {
				if(set.getDelay()){
					TimeUnit.SECONDS.sleep(10);
				}
			} catch (InterruptedException probablyImpossible) {
			}
    	}
    	
    	//Create the excel files.
    	FileCreator.createExcel(sheet, tree);
		FileCreator.createExcel(sheet2, treeRaw);
	}
	
	/**
	 * Downloads the designated files from the bucket and writes results to an excel sheet
	 * @param n - Designates which region to use.
	 * @param segments - This array designates which segments to pull from the bucket.
	 * @throws IOException 
	 */	
	public static void run(int n, int[] segments) throws IOException{
		int index;
		Object [] times;
		long totalTime = 0;
		long endTime = 0;
		long startTime = 0;
		long firstTime = 0;
		char[] buf = new char[70000000];
		String key;
		S3Object obj;
		
		//Create the excel sheet which will hold the basic statistics on the raw data.
		HSSFSheet sheet = GlobalValues.excel.getSheet(GlobalValues.multipleSingleFileDownloadName);
		if(sheet != null){
			index = GlobalValues.excel.getSheetIndex(GlobalValues.multipleSingleFileDownloadName);
			GlobalValues.excel.removeSheetAt(index);
		}
		sheet = GlobalValues.excel.createSheet(GlobalValues.multipleSingleFileDownloadName);
		
		//Create the excel sheet which will hold the raw data generated by these tests.
		HSSFSheet sheet2 = GlobalValues.excel.getSheet(GlobalValues.multipleSingleFileDownloadRawName);
		if(sheet2 != null){
			index = GlobalValues.excel.getSheetIndex(GlobalValues.multipleSingleFileDownloadRawName);
			GlobalValues.excel.removeSheetAt(index);
		}	
		sheet2 = GlobalValues.excel.createSheet(GlobalValues.multipleSingleFileDownloadRawName);
		
		//Create labels for the first sheet.
    	TreeMap<String, Object[]> tree = new TreeMap<String,Object[]>();
    	tree.put("0", new Object [] {"Segment Number" , "First Time", "Average Time", "Standard Deviation",
    			"50%", "60%", "70%", "80%", "90%","95%","99%","100%"});
    	TreeMap<String, Object[]> treeRaw = new TreeMap<String,Object[]>();
    	
       	//Create labels for the second sheet.
    	Object[] rawHeader = new Object[GlobalValues.numTests + 1];
    	rawHeader[0] = "Segment Number";
    	for(int i = 0; i < GlobalValues.numTests; i++){
    		rawHeader[i + 1] = "Test " + (i+1);
    	}
    	treeRaw.put("0", rawHeader);
    	
    	//Perform a series of tests on each segment tested.
    	for(int i = 0; i < segments.length; i++){
    		key = FileCreator.createKey(GlobalValues.splitKey + segments[i]);
    		times = new Object[GlobalValues.numTests + 1];
	    	times[0] = segments[i];
	    	totalTime = 0;
			System.out.println("Testing download speed for segment: " + (segments[i]));
			
			//Perform a number of tests and record the results.
	    	for(int j = 0; j < GlobalValues.numTests;j++){
	    		startTime = System.nanoTime();
	    		obj = GlobalValues.s3s[n].getObject(GlobalValues.buckets[n], key);
				BufferedReader in = new BufferedReader(new InputStreamReader(obj.getObjectContent()));
		        while (in.read(buf) != -1) {} //Read through the entire stream. 
		        in.close();
	    		endTime = System.nanoTime();
				startTime = startTime / GlobalValues.convertNanoToMilli;
				endTime = endTime / GlobalValues.convertNanoToMilli;
	    		if(j == 0){
	    			firstTime = endTime - startTime;
	    			System.out.println("The first download took " + firstTime);
	    		}
	    		times[j + 1] = endTime - startTime;
	    		totalTime += endTime - startTime;
				try {
					if(GlobalValues.delay){
						TimeUnit.SECONDS.sleep(10);
					}
				} catch (InterruptedException e) {
				}
	    	}
	    	treeRaw.put("" + (i+1), times);
	    	tree.put("" + (i+1), Stats.getStats(times, "" + segments[i]));
	    	System.out.println("Average time was " + 1.0 * totalTime/GlobalValues.numTests);
    	}
    	
    	//Create the excel sheets in the HSSFWorkbook
    	FileCreator.createExcel(sheet, tree);
		FileCreator.createExcel(sheet2, treeRaw);
	}
}
