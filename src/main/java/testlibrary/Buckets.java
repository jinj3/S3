package testlibrary;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.UUID;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;

import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;



/**
 * This class contains all of the methods dealing with buckets in Amazon S3. 
 * There are methods to create buckets, read bucket names from file, write bucket names
 * to file, and delete buckets (Along with all of the files they hold).
 * @author jerryzhiruijin
 *
 */
public class Buckets {
	/**
	 * This method sets up the credentials and creates the AmazonS3s that will be used in testing
	 */
	public static void setup(){
		
		//Create the credentials used to connect with S3
		try {
            GlobalValues.credentials = new DefaultAWSCredentialsProviderChain().getCredentials();
        } catch (Exception e) {
            throw new AmazonClientException(
                    "Cannot load the credentials from the credential profiles file. ",
                    e);
        }
		
		//Create a S3 client for every region
		for(int i = 0; i < GlobalValues.numRegions; i++){
			GlobalValues.s3s[i] = AmazonS3ClientBuilder.standard().withRegion
					(GlobalValues.regions[i]).withCredentials(new AWSStaticCredentialsProvider
					(GlobalValues.credentials)).build();
		}
	}
	
	/**
	 * This method is used to set up the AmazonS3 in the mocked version of this program.
	 * @param n This is just to differentiate which setup to use. For the non-mocked tests
	 * 		  do not use any arguments
	 */
	public static void setup(int n){
		GlobalValues.mocked = true;//Sets this boolean to true so other methods know.
		String endpoint1 = "http://127.0.0.1:8080";//Find the URI of the localhost.
		String endpoint2 = "http://127.0.0.1:8081";//Find the URI of the localhost.
		/*
		 * Create a client between this program and the local host
		 * NOTE: chunking is disabled because it creates an error when authorization-none is used.
		 */
		GlobalValues.s3 = AmazonS3ClientBuilder.standard().disableChunkedEncoding().
				withEndpointConfiguration(new EndpointConfiguration(endpoint1, "local-1")).build();
		GlobalValues.s3s[0] = GlobalValues.s3;
		GlobalValues.s3s[1] = AmazonS3ClientBuilder.standard().disableChunkedEncoding().
				withEndpointConfiguration(new EndpointConfiguration(endpoint2, "local-2")).build();

		
		GlobalValues.numRegions = 2;
		GlobalValues.regionsFile = "regionsMock.txt";
		
	}
	
	/**
	 * This method creates the buckets and writes their names on a text file
	 * called 'regions.txt', separating names with a newline.
	 * @throws FileNotFoundException This should never happen (I think).  
	 */
	public static void createBuckets() throws FileNotFoundException{
		
		String bucketName;
		File file;
        
		/*
		 * Open the file containing the names of buckets from various regions.
		 */
		if(GlobalValues.mocked){
			file = new File(GlobalValues.regionsMock);
		}
		else{
			file = new File(GlobalValues.regionsFile);
		}
		PrintWriter writer = new PrintWriter(file);
		
		//Write each name into the file
		
        for(int i = 0; i < GlobalValues.numRegions; i++){
        	GlobalValues.s3 = GlobalValues.s3s[i];
        	bucketName = createBucket();
        	writer.println(bucketName);
        }
        
        
        writer.close();//close the writer
	}
	

	
	/**
	 * readBuckets reads the bucket names from the files specified and saves 
	 * those values to GlobalStrings.buckets. In addition, these buckets should be 
	 * read in the correct order so that each bucket corresponds with the region.
	 * @throws IOException This exception is thrown if an error occurs while reading the file
	 */
	public static void readBuckets() throws IOException{
		FileReader fr;
		
		if(!GlobalValues.mocked){
			fr = new FileReader(GlobalValues.regionsFile);
		}
		else{
			fr = new FileReader(GlobalValues.regionsMock);
		}
		BufferedReader reader = new BufferedReader(fr);
		String line;
		
		
		//Read the name of all of the buckets.
		for(int i = 0; i < GlobalValues.numRegions && (line = reader.readLine()) != null;i++){
			GlobalValues.buckets[i] = line;
		}
		reader.close();
	}	
	
	
	/**
	 * Deletes the buckets previously created in S3.
	 */
	public static void deleteBuckets(){
		
		
		/*
		 * Read the bucket names from file and save them to GlobalValues.
		 */
		try {
			readBuckets();
		} catch (IOException e) {
			return;
		}
		
		/*
		 * Deleting the buckets in all regions.
		 */
		for(int i = 0; i < GlobalValues.numRegions; i++){
			
			
			/*
			 * Prepare to list all keys in the bucket
			 */
			GlobalValues.s3 = GlobalValues.s3s[i];
			final ListObjectsRequest req = new ListObjectsRequest().
					withBucketName(GlobalValues.buckets[i]);
            ObjectListing result;
            
            
            /*
             * list and delete all keys in the bucket
             */
            do{
            	result = GlobalValues.s3.listObjects(req);
            	for(S3ObjectSummary objectSummary : result.getObjectSummaries()){
            		GlobalValues.s3.deleteObject(GlobalValues.buckets[i], objectSummary.getKey());
            		System.out.println("Deleted key " + objectSummary.getKey());
            	}
            	
            }while(result.isTruncated());
			
		}
	}
	
	/**
	 * Create a bucket in Amazon S3.
	 * @return The name of the bucket.
	 */
	private static String createBucket(){
		
		/*
		 * Here I thought I'd be smart and add an arbitrary constant to the UUID, not realizing
		 * that the chances of collision are nearly 0 anyways.
		 */
		String name = UUID.randomUUID() + "1";
		while(GlobalValues.s3.doesBucketExist(name)){
			name = UUID.randomUUID() + "1";
		}
		GlobalValues.s3.createBucket(name);
		return name;
	}



}
