package testlibrary;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import org.apache.poi.hssf.usermodel.HSSFSheet;


import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

public class DownloadSegmentTest {
	
	static String key;
	
	/**
	 * Tests downloading random portions of a large file and comparing
	 * @param reg - Region to download from
	 * @param segments - An array of integers specifying which segments to get and time.
	 * @throws IOException 
	 */
	public static void run(int reg, int[] segments) throws IOException{
		int index;
		long startTime = 0;
		long endTime = 0;
		long firstTime = 0;
		long totalTime;
		char[] buf = new char[70000000];
		Object [] times;
		key = FileCreator.createKey(GlobalValues.largeKey);
		GetObjectRequest req;
		S3Object obj;
	
		//Create a worksheet that holds the statistics
		HSSFSheet sheet = GlobalValues.excel.getSheet(GlobalValues.segmentSheetName);
		if(sheet != null){
			index = GlobalValues.excel.getSheetIndex(GlobalValues.segmentSheetName);
			GlobalValues.excel.removeSheetAt(index);	
		}
		sheet = GlobalValues.excel.createSheet(GlobalValues.segmentSheetName);
		
		//Create a worksheet that holds the raw data.
		HSSFSheet sheet2 = GlobalValues.excel.getSheet(GlobalValues.segmentSheetRawName);
		if(sheet2 != null){
			index = GlobalValues.excel.getSheetIndex(GlobalValues.segmentSheetRawName);
			GlobalValues.excel.removeSheetAt(index);
		}
		sheet2 = GlobalValues.excel.createSheet(GlobalValues.segmentSheetRawName);
		TreeMap<String, Object[]> tree = new TreeMap<String,Object[]>();
		tree.put("0", new Object [] {"Segment Number" , "First Time", "Average Time", "Standard Deviation",
				"50%", "60%", "70%", "80%", "90%","95%","99%","100%"});
		TreeMap<String, Object[]> treeRaw = new TreeMap<String,Object[]>();
		Object[] rawHeader = new Object[GlobalValues.numTests + 1];
		rawHeader[0] = "Segment Number";
		for(int i = 0; i < GlobalValues.numTests; i++){
			rawHeader[i + 1] = "Test " + (i+1);
		}
		treeRaw.put("0", rawHeader);
		for(int i = 0; i < segments.length; i++){
	    	times = new Object[GlobalValues.numTests + 1];
	    	times[0] = segments[i];
			totalTime = 0;
			System.out.println("Testing download speed for segment: " + (segments[i]));
			for(int j = 0; j < GlobalValues.numTests; j++){
				req = new GetObjectRequest(GlobalValues.buckets[reg], key);
				req.setRange(segments[i] * 36 * GlobalValues.fileSize/GlobalValues.numSplits,
						(segments[i] + 1) * 36 * GlobalValues.fileSize/GlobalValues.numSplits - 1);
				startTime = System.nanoTime();
				obj = GlobalValues.s3s[reg].getObject(req);
				BufferedReader in = new BufferedReader(new InputStreamReader(obj.getObjectContent()));
		        while (in.read(buf) != -1)  {}
		        in.close();
				endTime = System.nanoTime();
				startTime = startTime / GlobalValues.convertNanoToMilli;
				endTime = endTime / GlobalValues.convertNanoToMilli;
				if(j == 0){
					firstTime = endTime - startTime;
					System.out.println("First test took: " + firstTime);
				}
				totalTime += endTime - startTime;
				times[j+1] = endTime - startTime;
				try {
					if(GlobalValues.delay){
						TimeUnit.SECONDS.sleep(GlobalValues.betweenTestDelay);
					}
				} catch (InterruptedException e) {
				}
			}
			System.out.println("Average test time was: " + 1.0*totalTime/GlobalValues.numTests);
			tree.put((i+1) + "", Stats.getStats(times, ""+segments[i]));
			treeRaw.put((i+1) + "", times);
		}
		FileCreator.createExcel(sheet, tree);
		FileCreator.createExcel(sheet2, treeRaw);
	}
	
	/**
	 * Tests downloading portions of a large file
	 * @param reg - Region to download from
	 * @param segments - portions of the file to download (Integer between 0 and numSegments)
	 * @param set - An object containing all necessary information pertaining to this test.
	 * @throws IOException
	 */
	public static void run(int reg, int[] segments, S3TestSetup set) throws IOException{
		char[] buf = new char[set.getBufferSize()];
		int index;
		long startTime = 0;
		long endTime = 0;
		long firstTime = 0;
		long totalTime;
		Object [] times;
		key = set.getLargeKey();
		GetObjectRequest req;
		S3Object obj;
		HSSFSheet sheet = set.getExcel().getSheet(GlobalValues.segmentSheetName);
		if(sheet != null){
			index = set.getExcel().getSheetIndex(GlobalValues.segmentSheetName);
			set.getExcel().removeSheetAt(index);	
		}
		sheet = set.getExcel().createSheet(GlobalValues.segmentSheetName);
		HSSFSheet sheet2 = set.getExcel().getSheet(GlobalValues.segmentSheetRawName);
		if(sheet2 != null){
			index = set.getExcel().getSheetIndex(GlobalValues.segmentSheetRawName);
			set.getExcel().removeSheetAt(index);	
		}
		sheet2 = set.getExcel().createSheet(GlobalValues.segmentSheetRawName);
		TreeMap<String, Object[]> tree = new TreeMap<String,Object[]>();
		tree.put("0", new Object [] {"Segment Number" , "First Time", "Average Time", "Standard Deviation",
				"50%", "60%", "70%", "80%", "90%","95%","99%","100%"});
		TreeMap<String, Object[]> treeRaw = new TreeMap<String,Object[]>();
		
		//Create the labels for the Raw data sheet.
		Object[] rawHeader = new Object[set.getNumTests() + 1];
		rawHeader[0] = "Segment Number";
		for(int i = 0; i < set.getNumTests(); i++){
			rawHeader[i + 1] = "Test " + (i+1);
		}
		treeRaw.put("0", rawHeader);
		
		//Test download speeds for each identified segment.
		for(int i = 0; i < segments.length; i++){
	    	times = new Object[set.getNumTests() + 1];
	    	times[0] = segments[i];
			totalTime = 0;
			System.out.println("Testing download speed for segment: " + (segments[i]));
			for(int j = 0; j < set.getNumTests(); j++){
				req = new GetObjectRequest(set.getBucket(reg), key);
				req.setRange((long)segments[i] * 36 * ((long)set.getFileSize()/set.getNumSplits()),
						(long)(segments[i] + 1) * 36 * ((long)set.getFileSize()/set.getNumSplits()) - 1);
				startTime = System.nanoTime();
				obj = set.getS3(reg).getObject(req);
				BufferedReader in = new BufferedReader(new InputStreamReader(obj.getObjectContent()));
		        while (in.read(buf) != -1){}
		        in.close();
				endTime = System.nanoTime();
				startTime = startTime / GlobalValues.convertNanoToMilli;
				endTime = endTime / GlobalValues.convertNanoToMilli;
				if(j == 0){
					firstTime = endTime - startTime;
					System.out.println("First test took: " + firstTime);
				}
				totalTime += endTime - startTime;
				times[j+1] = endTime - startTime;
			}
			try {
				if(set.getDelay()){
					TimeUnit.SECONDS.sleep(GlobalValues.betweenTestDelay);
				}
			} catch (InterruptedException e) {
			}
			System.out.println("Average test time was: " + 1.0*totalTime/set.getNumTests());
			tree.put((i+1) + "", Stats.getStats(times, ""+segments[i]));
			treeRaw.put((i+1) + "", times);
		}
		FileCreator.createExcel(sheet, tree);
		FileCreator.createExcel(sheet2, treeRaw);
	}
}
