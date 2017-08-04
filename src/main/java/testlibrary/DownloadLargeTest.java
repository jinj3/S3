package testlibrary;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import org.apache.poi.hssf.usermodel.HSSFSheet;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;


/**
 * This class holds the method to test speeds on large downloads.
 * @author jerryzhiruijin
 *
 */
public class DownloadLargeTest {
	
	private static Regions region;
	private static String bucketName;
	static String key = ""; //Name under which this file will be stored to S3.
	private static long startTime; //Time right before an upload is made to S3.
	private static long endTime; //Time right after an upload is made to S3.
	private static long totalTime = 0;//Total Time (in millis) used to upload all files.
	private static AmazonS3 s3; //Account used to link with S3 database.
	
	/**
	 * Tests downloading a large file from S3. This allows the user to set up the necessary 
	 * variables without having to know all of the required variables beforehand.
	 * @param set - Object holding the required variables for run to work properly
	 * @param regions - An array of integers specifying which regions to test.
	 * @throws IOException - Should only happen if the stream created by the object behaves 
	 * 						 incorrectly
	 * @throws IndexOutOfBoundsException - This is thrown if regions is incorrectly set.
	 */
	public static void run(S3TestSetup set, int reg) throws IOException{
		String originalLarge = set.getLargeKey();
		char[] buf = new char[set.getBufferSize()];
		Object[] times;
		long firstTime = 0;
    	HSSFSheet sheet = set.getExcel().getSheet(GlobalValues.largeFileDownloadName);
    	if(sheet == null){
    		sheet = set.getExcel().createSheet(GlobalValues.largeFileDownloadName);
    	}
    	HSSFSheet sheet2 = set.getExcel().getSheet(GlobalValues.largeFileDownloadRawName);
    	if(sheet2 == null){
    		sheet2 = set.getExcel().createSheet(GlobalValues.largeFileDownloadRawName);
    	}
    	TreeMap<String, Object[]> tree = new TreeMap<String,Object[]>();
    	tree.put("0", new Object [] {"File Size (MB)" , "First Time", "Average Time",
    			"Standard Deviation", "50%", "60%", "70%", "80%", "90%","95%","99%","100%"});
    	TreeMap<String, Object[]> treeRaw = new TreeMap<String,Object[]>();
    	Object[] rawHeader = new Object[set.getNumTests() + 1];
    	rawHeader[0] = "File Size (MB)";
    	for(int i = 0; i < set.getNumTests(); i++){
    		rawHeader[i + 1] = "Test " + (i+1);
    	}
    	treeRaw.put("0", rawHeader);
    	if(reg >= set.getNumberOfRegions()){
    		throw new IndexOutOfBoundsException(region + " does not specify a proper region.");
    	}
    	region = set.getRegion(reg);
    	bucketName = set.getBucket(reg);
    	
    	for(int sizeIndex = 0; sizeIndex < GlobalValues.fileSizes.length; sizeIndex++){
    		totalTime = 0;
    		s3 = set.getS3(reg);
    		times = new Object[set.getNumTests() + 1];
    		times[0] = GlobalValues.fileSizes[sizeIndex] / 1000000;
    		set.setLargeFileName(GlobalValues.sizeKeys[sizeIndex]);
    		System.out.println("Downloading files from region: " + s3.getRegionName());
	    	
    		//Tests numTests times per region.
    		for(int j = 0; j < set.getNumTests(); j++){
    			key = set.getLargeKey();
    			startTime = System.nanoTime();
    			S3Object obj = s3.getObject(new GetObjectRequest(bucketName, key));
    			BufferedReader in = new BufferedReader(new InputStreamReader(obj.getObjectContent()));
		    	while (in.read(buf) != -1){
		    	}
	        	in.close();
    			endTime = System.nanoTime();
    			startTime = startTime / GlobalValues.convertNanoToMilli;
    			endTime = endTime / GlobalValues.convertNanoToMilli;
    			System.out.println("It took " + (endTime - startTime));
    			totalTime += endTime - startTime;
    			if(j == 0){    		
    				firstTime = totalTime;
    				System.out.println("Time for first download: " + firstTime);
    			}	    		
    			times[j+1] = endTime - startTime;
    		}
	    	System.out.println("Average download time after " + set.getNumTests() + 
	    			" downloads is: " + 1.0 * totalTime / set.getNumTests());		    	
	    	tree.put((sizeIndex+1) + "", Stats.getStats(times, 
	    			GlobalValues.fileSizes[sizeIndex] / 1000000));
	   		treeRaw.put((sizeIndex+1) + "", times);
	   		try {
				if(set.getDelay()){
					TimeUnit.SECONDS.sleep(GlobalValues.betweenTestDelay);
				}
			} catch (InterruptedException e) {
			}
    	}
	   	FileCreator.createExcel(sheet, tree);
	   	FileCreator.createExcel(sheet2, treeRaw);
	    set.setLargeFileName(originalLarge);
	}
	
	/**
	 * Tests downloading a large file from S3. Might not implement
	 * @param set - Holds the required values
	 * @throws IOException 
	 */
	public static void downloadFromAllRegions(S3TestSetup set) throws IOException{
		Object[] times;
		long firstTime = 0;
		char[] buf = new char[100000];
    	HSSFSheet sheet = set.getExcel().getSheet(GlobalValues.regionDownloadName);
    	if(sheet == null){
    		sheet = set.getExcel().createSheet(GlobalValues.regionDownloadName);
    	}
    	HSSFSheet sheet2 = set.getExcel().getSheet(GlobalValues.regionDownloadRawName);
    	if(sheet2 == null){
    		sheet2 = set.getExcel().createSheet(GlobalValues.regionDownloadRawName);
    	}
    	TreeMap<String, Object[]> tree = new TreeMap<String,Object[]>();
    	tree.put("0", new Object [] {"Region Name" , "First Time", "Average Time","Standard Deviation",
    			"50%", "60%", "70%", "80%", "90%","95%","99%","100%"});
    	TreeMap<String, Object[]> treeRaw = new TreeMap<String,Object[]>();
    	Object[] rawHeader = new Object[set.getNumTests() + 1];
    	rawHeader[0] = "Region Name";
    	for(int i = 0; i < set.getNumTests(); i++){
    		rawHeader[i + 1] = "Test " + (i+1);
    	}
    	treeRaw.put("0", rawHeader);
    	
		//Tests every region. 
	    for(int i = 0; i < set.getNumberOfRegions(); i++){
	    	region = set.getRegion(i);
	    	bucketName = set.getBucket(i);
	    	totalTime = 0;
	    	s3 = set.getS3(i);
	    	times = new Object[set.getNumTests() + 1];
	    	times[0] = region.toString();
	    	System.out.println("Downloading files from region: " + region.toString());
	    	
	    	//Tests numTests times per region.
	    	for(int j = 0; j < set.getNumTests(); j++){
	    		key = set.getLargeKey();
	    
	    		//System.out.println("Finding the file at key " + key);
	    		startTime = System.nanoTime();
	    		S3Object obj = s3.getObject(bucketName, key);
	    		BufferedReader in = new BufferedReader(new InputStreamReader(obj.getObjectContent()));
		        while (in.read(buf) != -1) {}
		        in.close();
	    		endTime = System.nanoTime();
	    		startTime = startTime / GlobalValues.convertNanoToMilli;
	    		endTime = endTime / GlobalValues.convertNanoToMilli;
	    		totalTime += endTime - startTime;
	    		if(j == 0){
	    			firstTime = totalTime;
	    			System.out.println("Time for first download: " + firstTime);
	    		}
	    		
	    		times[j+1] = endTime-startTime;
	    	}
	    	System.out.println("Average download time after " + GlobalValues.numTests + 
	    			" downloads is: " + 1.0*totalTime/GlobalValues.numTests);	
	    	tree.put((i+1) + "", Stats.getStats(times, ""+GlobalValues.regions[i]));
	    	treeRaw.put((i+1) + "", times);
	    	try {
				if(set.getDelay()){
					TimeUnit.SECONDS.sleep(GlobalValues.betweenTestDelay);
				}
			} catch (InterruptedException e) {
			}
	    }	    
	    FileCreator.createExcel(sheet, tree);
	    FileCreator.createExcel(sheet2, treeRaw);
	}	
	
	/**
	 * This method tests the speed of downloading 1 large file from AmazonS3
	 * @param n - integer representing which region to download the file from
	 * @throws IOException
	 */
	public static void run(int n) throws IOException{
		S3TestSetup set = new S3TestSetup(GlobalValues.regions, GlobalValues.regionsFile, 
				GlobalValues.numTests, "a", false).setExcel(GlobalValues.excel);
		DownloadLargeTest.run(set, n);
	}
}
