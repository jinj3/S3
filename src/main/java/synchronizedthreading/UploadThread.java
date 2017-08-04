package synchronizedthreading;

import java.nio.file.Path;
import java.util.NoSuchElementException;

import com.amazonaws.services.s3.AmazonS3;

public class UploadThread extends Thread{
	SynchronizedUpload upload;
	AmazonS3 s3;
	String bucket;
	
	
	/**
	 * Creates a thread which will individually upload selected information
	 * @param up - Gives the next file to be uploaded
	 * @param client - an AmazonS3 object storing information like credentials and region for uploading.
	 * @param bucketname - name of the bucket onto which the files will be loaded.
	 */
	public UploadThread(SynchronizedUpload up, AmazonS3 client, String bucketname){
		upload = up;
		s3 = client;
		bucket = bucketname;
	}
	
	/**
	 * This overrides the Thread run method to behave the way an UploadThread should.
	 */
	public void run(){
	
		
		/*
		 * While there are files to be uploaded, upload the next file.
		 */
		try{
			Path curr = upload.getFile();
		
			while(true){
				System.out.println("Uploading " + curr.toString());
			
				//s3.putObject(bucket, curr.toString(), curr.toFile());
				curr = upload.getFile();
			}
		}catch(NoSuchElementException e){
			
		}
		
		
	}
	

}
