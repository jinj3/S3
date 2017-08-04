package synchronizedthreading;

import java.io.IOException;
import java.nio.file.Paths;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

public class TestSync {
	/**
	 * Expect
	 * 
	 * NOTE: testbucket126 is ncal
	 * @param args expects path, bucketName, region, numThreads
	 * 
	 * @throws IOException - Shouldn't happen generally
	 */
	public static void main(String[] args) throws IOException{

		
		
		AWSCredentials creds = new DefaultAWSCredentialsProviderChain().getCredentials();
		String path = args[0];
		String bucket = args[1];
		Regions region = Regions.fromName(args[2]);
		int numThreads = Integer.parseInt(args[3]);
		
		AmazonS3 s3 = AmazonS3ClientBuilder.standard().withRegion
				(region).withCredentials(new AWSStaticCredentialsProvider
				(creds)).build();

		SyncUpload(s3, path, bucket,numThreads);


		
	}
	
	
	/**
	 * Uploads a all files and subfiles of a directory in parallel
	 * @param s3 - AmazonS3 holding credentials and the endpoint
	 * @param path - Path to the desired directory inside of the local machine
	 * @param bucket - Name of the bucket that these files should be uploaded to
	 * @param numThreads - Number of concurrent threads to be executed. Note: Unsure if these threads
	 * 					   would actually save any time since the putobject requset is blocked.
	 * @throws IOException - This should only happen if the directory structure is extremely deep
	 * 						 and so saturates the memory allocated to S3
	 */
	public static void SyncUpload(AmazonS3 s3, String path, String bucket, int numThreads) throws IOException{
		SynchronizedUpload sync = new SynchronizedUpload(Paths.get(path));
		for(int i = 0; i < numThreads; i++){
			new UploadThread(sync, s3, bucket).start();
		}
		
	}
}
