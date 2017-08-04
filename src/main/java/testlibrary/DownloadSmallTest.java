package testlibrary;



import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;


/**
 * This class tests the speed of a "GET" command from Amazon S3 using various regions.
 * @author Jerry Jin
 *
 */
public class DownloadSmallTest {
	
	private static Regions region;
	private static String bucketName;	
	private static String key = ""; //Name under which this file will be stored to S3.
	private static long beginTime; //Time right before an upload is made to S3.
	private static long endTime; //Time right after an upload is made to S3.
	private static long totalTime = 0;//Total Time (in millis) used to upload all files.
	private static long firstTime;
	private static AmazonS3 s3; //Account used to link with S3 database.	
	public static void run() throws FileNotFoundException{

		File file = new File(GlobalValues.regionDownloadFile);
		PrintWriter writer = new PrintWriter(file);
		
		writer.write("Region \t\t first \t average \n");
        for(int i = 0; i < GlobalValues.numRegions; i++){
        	region = GlobalValues.regions[i];
        	bucketName = GlobalValues.buckets[i];
        	totalTime = 0;
        	
        	s3 = GlobalValues.s3s[i];
        	
        	System.out.println("Downloading files from region: " + region.toString());
        	for(int j = 0; j < GlobalValues.numTests; j++){
        		key = FileCreator.createKey(GlobalValues.smallKey);
        
        		//System.out.println("Finding the file at key " + key);
        		beginTime = System.nanoTime();
        		s3.getObject(new GetObjectRequest(bucketName, key));
        		endTime = System.nanoTime();
        		//System.out.println("This file contains the value " + object.toString());
        		totalTime += endTime - beginTime;
        		beginTime = beginTime / GlobalValues.convertNanoToMilli;
    			endTime = endTime / GlobalValues.convertNanoToMilli;
        		if(j == 0){
        			firstTime = totalTime;
        			System.out.println("Time for first download: " + totalTime);
        		}
        		
        	}
        	System.out.println("Average download time after " + GlobalValues.numTests + 
        			" downloads is: " + 1.0*totalTime/GlobalValues.numTests);
        	
			
			

				

			FileCreator.createStats(writer, region.toString(), firstTime,
					1.0*totalTime/GlobalValues.numTests);

        }
		writer.close();

	}
}