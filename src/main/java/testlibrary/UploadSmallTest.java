package testlibrary;



import java.io.*;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;


/**
 * This class times uploading a small file to S3. The files are uploaded under keys from
 * 00000001 to 000000010. May be used later to show the inefficiency of downloading files
 * with similar keys.
 * @author Jerry Jin
 *
 */
public class UploadSmallTest {
	private static Regions region = GlobalValues.regions[6];
	private static String bucket = GlobalValues.buckets[6];
	
	
	/**
	 * This method uploads multiple small files to Amazon S3 and finds the average
	 * upload time for small files to the specified server.
	 * @param args
	 * @throws FileNotFoundException 
	 */
	public static void run () throws FileNotFoundException{
		File smallFile; //Create a small file for testing Amazon upload speeds.
		String key; //Name under which this file will be stored to S3.
		long beginTime; //Time right before an upload is made to S3.
		long endTime; //Time right after an upload is made to S3.
		long totalTime = 0;//Total Time (in millis) used to upload all files.
		long firstTime = 0;
		AmazonS3 s3; //Account used to link with S3 database.
		
		
		/*
		 * create a small file that will be inserted into S3.
		 */
		try{
			smallFile = FileCreator.createSmallFile();
		}catch (IOException e){
			return;
		}
		File file = new File(GlobalValues.regionUploadsFile);		
		PrintWriter writer = new PrintWriter(file);  
		writer.write("Region \t\t first \t average \n");
		
		/*
		 * For every region, upload a small file multiple times and find the average 
		 * time taken for each upload.
		 */
        for(int j = 0; j < GlobalValues.numRegions; j++){
        	region = GlobalValues.regions[j];
        	bucket = GlobalValues.buckets[j];
        	endTime = 0;
        	totalTime = 0;
        	
	        s3 = GlobalValues.s3s[j];
	        System.out.println("Uploading files to region: " + region.toString());
     
	        /*
	         * find the average upload time over multiple "PUT" operations.
	         */
			for(int i = 0; i < GlobalValues.numTests; i++){
				key = FileCreator.createKey(GlobalValues.smallKey);
				
				//System.out.println("Uploading file number: " + (1 + i));
				beginTime = System.nanoTime();
				s3.putObject(new PutObjectRequest(bucket,key,smallFile));
				endTime = System.nanoTime();
				beginTime = beginTime / GlobalValues.convertNanoToMilli;
				endTime = endTime / GlobalValues.convertNanoToMilli;
				if(i == 0){
					firstTime = endTime - beginTime;
					System.out.println("First upload took " + 
								(firstTime) + " milliseconds");
				}
				totalTime += endTime - beginTime;
			}
			System.out.println("Total Time taken to run " + GlobalValues.numTests
					+ " is: " + totalTime);
			System.out.println("The average time for these small files was "
					+ 1.0 * totalTime/GlobalValues.numTests);
			



			FileCreator.createStats(writer, region.toString(), firstTime,
					1.0*totalTime/GlobalValues.numTests);

        }
        writer.close();
		
	}
	
	/**
	 * Uploads a single small file to the designated server
	 * @param n which server to use.
	 * @throws IOException 
	 */
	public static void uploadSingle(int n) throws IOException{
		String key;
		File file = FileCreator.createSmallFile();
	
		key = FileCreator.createKey(GlobalValues.smallKey);
		GlobalValues.s3s[n].putObject(GlobalValues.buckets[n], key, file);
		
	}

	

}