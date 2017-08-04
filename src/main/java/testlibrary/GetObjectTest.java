package testlibrary;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

/**
 * This class implements tests for the speed of various get requests.
 * @author jerryzhiruijin
 *
 */
public class GetObjectTest {

	/**
	 * This method benchmarks the amount of time it takes a particular get request to be processed
	 * and return the object required from Amazon S3.
	 * @param client - AmazonS3 object holding credentials and other necessary information
	 * @param getRequest - object holding necessary information for a get request.
	 * @return - The time elapsed from the start of the getObject request until the object
	 * 			 has been completely read using a bufferedReader.
	 * @throws IOException - I really need to learn when a BufferedReader would throw this exception
	 */
	public static long getObjectTime(AmazonS3 client, GetObjectRequest getRequest) throws IOException{
		char [] buf = new char[1000000];
		S3Object obj;
		long startTime = System.nanoTime() / GlobalValues.convertNanoToMilli;
		obj  = client.getObject(getRequest);
		BufferedReader in = new BufferedReader(new InputStreamReader(obj.getObjectContent()));
		while(in.read(buf) != -1){	
		}
		in.close();
		long endTime = System.nanoTime() / GlobalValues.convertNanoToMilli;
		return endTime - startTime;
		
	}
}
