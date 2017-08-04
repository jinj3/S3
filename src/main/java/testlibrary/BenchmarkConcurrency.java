package testlibrary;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import org.apache.poi.hssf.usermodel.HSSFSheet;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

import synchronizedthreading.SynchronizedCounter;

/**
 * This class benchmarks the time it takes to download a file using a specific number of threads.
 * @author jerryzhiruijin
 *
 */
public class BenchmarkConcurrency {
	private AmazonS3[] s3s;

	private ThreadController lock1 = new ThreadController();
	private SynchronizedCounter threadsLeft;
	private int maxThread = 5;
	
	public BenchmarkConcurrency(int threadPower){
		maxThread = threadPower;
	}
	
	public BenchmarkConcurrency() {
	}

	/**
	 * This method showcases benchmarking the data.
	 * @param args
	 */
	public static void main(String[] args){
		
		Regions [] regions = {Regions.US_WEST_2};
		String [] buckets = {"d7eafafe-947f-4dd0-9757-ff269ddf93b21"};
		S3TestSetup set = new S3TestSetup(regions, buckets, 5, args[0], false);
		int [] fileSizes = {1000000,5000000,10000000,50000000,100000000,500000000};
		String [] keys = {"222", "223", "224", "225", "226", "227"};
		//FileCreator.uploadFiles(0,set, fileSizes, keys);
		//System.out.println("Uploading done");
	
		BenchmarkConcurrency conc = new BenchmarkConcurrency();
		conc.testConcurrency(set, 0, keys[5],fileSizes[5],Integer.parseInt(args[1]));
		
	}

	/**
	 * This method creates and populates excel sheets holding the results of downloading a
	 * file with various numbers of threads. This method delegates to testConcurrency for the
	 * actual data collection.
	 * @param set - Object holding all necessary information for this test.
	 * @param region - Index inside set corresponding to the region.
	 * @param key - name of the key to be downloaded.
	 * @param fileSize - size of the key inside S3.
	 */
	public void testThreadsToDownload(S3TestSetup set, int region, String key, int fileSize){
		Object[] data;
		int maxThreads = 2;
		for(int i = 0; i < maxThread; i++){
			maxThreads = 2 * maxThreads;
		}
    	HSSFSheet sheet = set.getExcel().getSheet(GlobalValues.multiThreadedDownloadName);
    	if(sheet == null){
    		sheet = set.getExcel().createSheet(GlobalValues.multiThreadedDownloadName);
    	}
    	HSSFSheet sheet2 = set.getExcel().getSheet(GlobalValues.multiThreadedDownloadNameRaw);
    	if(sheet2 == null){
    		sheet2 = set.getExcel().createSheet(GlobalValues.multiThreadedDownloadNameRaw);
    	}
    	TreeMap<String, Object[]> tree = new TreeMap<String,Object[]>();
    	tree.put("0", new Object [] {"Thread Count" , "First Time", "Average Time","Standard Deviation",
    			"50%", "60%", "70%", "80%", "90%","95%","99%","100%"});
    	
    	TreeMap<String, Object[]> treeRaw = new TreeMap<String,Object[]>();
    	
    	Object[] rawHeader = new Object[set.getNumTests() + 1];
    	rawHeader[0] = "Thread Count";
    	for(int i = 0; i < set.getNumTests(); i++){
    		rawHeader[i + 1] = "Test " + (i+1);
    	}
    	treeRaw.put("0", rawHeader);
    	int iteration = 0;
    	for (int numThreads = 1; numThreads < maxThreads; numThreads *= 2){
    		data = new Object[set.getNumTests() + 1];
    		data[0] = numThreads;
    		for(int testNumber = 0; testNumber < set.getNumTests(); testNumber++){
    			data[testNumber + 1] = testConcurrency(set,region,key,fileSize,numThreads);
    		}
    		treeRaw.put((iteration + 1) + "", data);
    		tree.put((iteration + 1) + "", Stats.getStats(data, numThreads));
    		iteration++;
    		try {
				if(set.getDelay()){
					TimeUnit.SECONDS.sleep(GlobalValues.betweenTestDelay);
				}
			} catch (InterruptedException e) {
			}
    	}
	    FileCreator.createExcel(sheet, tree);
	    FileCreator.createExcel(sheet2, treeRaw);
	}
	
	/**
	 * This method does the actual testing for multithreaded downloads
	 * @param set - Object containing pertinent information when running various tests
	 * @param region - Specifies the index in set which represents the region to be used.
	 * @param key - The key of the file to be downloaded
	 * @param fileSize - The size of the file in bytes
	 * @param numThreads - The number of threads to be used concurrently.
	 * @return Time elapsed between starting and ending.
	 */
	public long testConcurrency(S3TestSetup set, int region, String key, int fileSize,
			int numThreads){
		DownloadThread [] threads = new DownloadThread [numThreads]; 
		s3s = new AmazonS3[numThreads];
		for(int i = 0; i < numThreads; i++){
			s3s[i] = AmazonS3ClientBuilder.standard().withRegion
					(set.getRegion(region)).withCredentials(new AWSStaticCredentialsProvider
					(set.getCredentials())).withClientConfiguration(set.getClientConfiguration()
					).build();
			
			//s3s[i] = set.getS3(region);
		}
		lock1 = new ThreadController();
		long endTime;
		long startTime;
		threadsLeft = new SynchronizedCounter(numThreads);
		for(int i = 0; i < numThreads; i++){
			threads[i] = new DownloadThread(set,region,key,(i*fileSize)/numThreads,
					( (i+1)*fileSize/numThreads)-1,i);
		}
		startTime = System.nanoTime() / GlobalValues.convertNanoToMilli;
		for(int i = 0; i < numThreads; i++){
			threads[i].start();
		}
		try {
			synchronized(lock1){
				while(!lock1.isFinished()){
					lock1.wait();
				}
			}
			endTime = System.nanoTime() / GlobalValues.convertNanoToMilli;
		} catch (InterruptedException e) {
			endTime = System.nanoTime() / GlobalValues.convertNanoToMilli;
		}
		System.out.println("Total time was " + (endTime - startTime));
		return endTime - startTime;
	}
	
	/**	
	 * This class makes a thread which downloads a specified region from a file and then notifies
	 * the calling class just before the last thread dies.
	 * @author jerryzhiruijin
	 *
	 */
	private class DownloadThread extends Thread{
		S3TestSetup set;
		int regionIndex;
		String key;
		int startRange; 
		int endRange;
		char [] buf = new char[4096];
		int thread;
		
		/**
		 * This constructs a new DownloadThread
		 * @param set - This object contains all pertinent information that extends across all 
		 * 			 	test types in this library.
		 * @param region - The index inside set for which the region corresponds to.
		 * @param key - The key that will be concurrently downloaded from S3
		 * @param startRange - the lower bound of the range that will be downloaded by this thread.
		 * @param endRange - Upper bound of the range to be downloaded by this thread.
		 */
		public DownloadThread(S3TestSetup set, int region, String key, int startRange, int endRange,
				int threadCounter){
			this.set = set;
			regionIndex = region;
			this.key = key;
			this.startRange = startRange;
			this.endRange = endRange;
			thread = threadCounter;
		}
		
		/**
		 * Tells the thread to first get an object from S3, read the object, and then check
		 * if it's the last thread left before notifying the calling method.
		 */
		public void run(){
			GetObjectRequest req = new GetObjectRequest(set.getBucket(regionIndex), key);
			req.setRange(startRange, endRange);
			S3Object obj = s3s[thread].getObject(req);
		
			BufferedReader in = new BufferedReader(new InputStreamReader(obj.getObjectContent()));
			try {
				while(in.read(buf) != -1){}
				in.close();
				if(threadsLeft.lastThread()){
					synchronized (BenchmarkConcurrency.this.lock1){
						BenchmarkConcurrency.this.lock1.setFinished(true);
						BenchmarkConcurrency.this.lock1.notifyAll();
					}
				}	
			} catch (IOException e) {
				e.printStackTrace();
			}
			catch(Exception e){
				if(threadsLeft.lastThread()){
					synchronized (BenchmarkConcurrency.this.lock1){
						BenchmarkConcurrency.this.lock1.setFinished(true);
						BenchmarkConcurrency.this.lock1.notifyAll();
					}
					
				}
			}
		}
	}
}

