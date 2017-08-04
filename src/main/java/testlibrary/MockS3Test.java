package testlibrary;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Properties;
import java.util.UUID;


import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;


public class MockS3Test {
	public static void main(String[] args) throws IOException{

		String endpoint = "http://127.0.0.1:8080";
		Properties properties = new Properties();
		properties.setProperty("s3proxy.authorization", "none");
		properties.setProperty("s3proxy.endpoint", endpoint);
		properties.setProperty("jclouds.provider", "filesystem");
		properties.setProperty("jclouds.filesystem.basedir", "/tmp/s3proxy");




		
		AmazonS3 client = AmazonS3ClientBuilder.standard().disableChunkedEncoding().withEndpointConfiguration(
				new EndpointConfiguration(endpoint, "us-west-1")).build();


		// Should Throw AWS Client Exception as Bucket / Key does not exist!
		String name = UUID.randomUUID() + "1";

		client.createBucket(name);
		System.out.println("created bucked called " + name);
		System.out.println(client.doesBucketExist(name));
		

		File file = createSmallFile();
		
		//client.putObject(new PutObjectRequest(name,"thisName", file));
		client.putObject(name, "Hit", file);
		System.out.println("uploaded file");
		S3Object obj = client.getObject(name,"Hit");
		System.out.println(obj.getKey());
		
	}
	/**
	 * This class creates a small (1 byte) file and returns it to the caller.
	 * @return A small 1-byte file called "temp.txt"
	 * @throws IOException It should never throw this exception.
	 */
	public static File createSmallFile() throws IOException{
		File smallFile = File.createTempFile("TempFile", ".txt");
		smallFile.deleteOnExit();
		
		Writer writer = new OutputStreamWriter(new FileOutputStream(smallFile));
		writer.write("0");
		writer.close();
		return smallFile;
				
	}
}
