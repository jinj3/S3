package testlibrary;

import java.io.File;
import java.io.IOException;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.transfer.Download;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;

public class MultiPartTest {
	public static void main(String[] args) throws IOException{
		String bucket = "d7eafafe-947f-4dd0-9757-ff269ddf93b21";
		Regions region = Regions.US_WEST_2;
		AWSCredentials credentials = new DefaultAWSCredentialsProviderChain().getCredentials();
		AmazonS3 s3= AmazonS3ClientBuilder.standard().withRegion
		(region).withCredentials(new AWSStaticCredentialsProvider
		(credentials)).build();
		TransferManager transfer = TransferManagerBuilder.standard().withS3Client(s3).build();
		long startTime = System.nanoTime() / GlobalValues.convertNanoToMilli;
		Download down = transfer.download(bucket, FileCreator.createKey(GlobalValues.largeKey),
					File.createTempFile("temp", ".txt"));
		while(!down.isDone()){
		}
		long endTime = System.nanoTime() / GlobalValues.convertNanoToMilli;
		
		System.out.println("Total time taken was " + (endTime - startTime));
		
	}
}
