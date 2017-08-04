package testlibrary;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;

public class DownloadByBufferSizeTest {
	
	public static void main(String[] args) throws IOException{
		S3TestSetup set = new S3TestSetup(GlobalValues.regions, GlobalValues.regionsFile, 10, 
				"FakeData.xls", false);
		set.useBufferList(new int [] {1000,10000,100000,1000000,10000000, 70000000}).setNumTests(5);
		run(set,0,FileCreator.createKey(GlobalValues.largeKey));
	}
	
	public static void run(S3TestSetup set, int regionIndex, String key) throws IOException{
		long startTime;
		long endTime;
		char[] buf;
		AmazonS3 s3 = set.getS3(regionIndex);
		String bucket = set.getBucket(regionIndex);
		for(int bufferIterator = 0; bufferIterator < set.getBufferListSize(); bufferIterator++){
			buf = new char[set.getBufferFromList(bufferIterator)];
			System.out.println("Using buffer size " + buf.length);
			for(int testIterator = 0; testIterator < set.getNumTests(); testIterator++){
				
				
				startTime = System.nanoTime() / GlobalValues.convertNanoToMilli;
				S3Object obj = s3.getObject(bucket, key);
				BufferedReader in = new BufferedReader(new InputStreamReader(obj.getObjectContent()));
				while(in.read(buf) != -1){
					//System.out.println("New Cycle");
				}
				in.close();
				endTime = System.nanoTime() / GlobalValues.convertNanoToMilli;
				
				
				System.out.println("This took " + (endTime - startTime));
			}
			
		}
	}
}
