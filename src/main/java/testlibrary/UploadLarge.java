package testlibrary;

import java.io.File;




public class UploadLarge {


	/**
	 * Creates a single large file and uploads it to S3
	 * @param n - represents the region that the file will be uploaded to
	 * @param file - the large file to be uploaded
	 */
	public static void createSingle(int n, File file){

		String key = FileCreator.createKey(GlobalValues.largeKey);
		long startTime = System.nanoTime();
		GlobalValues.s3s[n].putObject(GlobalValues.buckets[n], key, file);
		long endTime = System.nanoTime();
		
		System.out.println("It took "  + ((endTime-startTime)/ GlobalValues.convertNanoToMilli));
	}
	
	
	
}
