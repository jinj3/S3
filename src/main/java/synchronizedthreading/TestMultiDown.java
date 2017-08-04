package synchronizedthreading;

import com.amazonaws.services.s3.AmazonS3;

import testlibrary.GlobalValues;
import testlibrary.S3TestSetup;


/**
 * This class tests an EC2 instance's ability to handle multithreaded download requests from S3.
 * @author jerryzhiruijin
 *
 */
public class TestMultiDown {
	private S3TestSetup set;
	private int n;
	private String[]keys;
	private int numThreads;
	Thread thread;
	public TestMultiDown(S3TestSetup set, int n, String[] keys, int numThreads){
		this.set = set;
		this.n = n;
		this.keys = keys;
		this.numThreads = numThreads;
	
	}
	
	/**
	 * Test the time it takes to download the files from S3 using some number of threads.
	 */
	public long testDownload(){
		long startTime = 0;
		long endTime = 0;
		SynchronizedDownload down = new SynchronizedDownload(keys);
		SynchronizedCounter counter = new SynchronizedCounter(numThreads);
		AmazonS3 s3 = set.getS3(n);
		String bucket = set.getBucket(n);
		startTime = System.nanoTime();
		for(int i = 0; i < numThreads; i++){
			new DownloadThread(counter, down, s3, bucket,this).start();
		}
		
		/*
		 * Wait for all of the threads to finish executing before figuring out the total time
		 * taken.
		 */
		try {
			synchronized (this){
			wait();
			endTime = System.nanoTime();
			}
		} catch (InterruptedException e) {
			endTime = System.nanoTime();
		}
		
		
		endTime = endTime / GlobalValues.convertNanoToMilli;
		startTime = startTime / GlobalValues.convertNanoToMilli;
		System.out.println("This multithreaded download using " + numThreads + " threads");
		System.out.println("to download " + keys.length + " files took " + (endTime-startTime) +"ms");
		return endTime - startTime;
	}
	
	/**
	 * Changes the number of threads that can be made concurrently
	 * @param i - new number of threads to make
	 */
	public void setThreads(int i){
		this.numThreads = i;
	}
}
