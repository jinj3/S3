package synchronizedthreading;



import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;

/**
 * This class implements a thread which allows for multithreaded downloading from Amazon S3
 * @author jerryzhiruijin
 *
 */
class DownloadThread extends Thread{
	SynchronizedDownload download;
	AmazonS3 s3;
	String bucket;
	SynchronizedCounter counter;
	Object thread;
	
	/**
	 * constructor for DownloadThread
	 * @param count - counter from which this thread will check whether all threads have finished 
	 * 				  execution
	 * @param down - Class which holds the list of key names and synchronously delivers them.
	 * @param client - The AmazonS3 object holding credentials and the required region
	 * @param bucketname - name of the bucket this thread will access from.
	 */
	public DownloadThread(SynchronizedCounter count, SynchronizedDownload down, AmazonS3 client, 
			String bucketname, Object thr){
		download = down;
		s3 = client;
		bucket = bucketname;
		counter = count;
		thread = thr;
	}
	
	/**
	 * This is the "main" method of this thread. It executes the downloads until there are no more
	 * files to download from AmazonS3. NOTE: Unsure if this is the most efficient method to benchmark
	 * parallel performance. Especially since reading a stream into a char array is IO heavy.
	 */
	public void run(){
		S3Object obj;
		char[] buf = new char [4096];
		String key = download.getKey();
		while(key != null){
			System.out.println(key + this.toString());
			obj = s3.getObject(bucket, key);
			key = download.getKey();
			try {
				BufferedReader in = new BufferedReader(new InputStreamReader(obj.getObjectContent()));

				//Read all the data. It seems that either S3
		        while (in.read(buf) != -1) {}
		        in.close();
			} catch (IOException e) {
				e.printStackTrace();
				break;
			}
			System.out.println("Read 1 more");
		}
		//Notify calling thread once all information is read.
		if(counter.lastThread()){
			synchronized(thread){
				System.out.println("trying to notify");
				thread.notify();
			}
		}
	}	
}
