package synchronizedthreading;

import com.amazonaws.regions.Regions;

import testlibrary.FileCreator;
import testlibrary.S3TestSetup;

public class DownloadMain {
	
	/**
	 * Method demonstrating the functionality of the MultiThreaded Download Test.
	 * @param args - first argument is the region number, 2nd is the number of concurrent threads.
	 */
	public static void main(String[] args){
		if(args.length != 2){
			return;
		}
		int region = Integer.parseInt(args[0]);
		int numThreads = Integer.parseInt(args[1]);
		Regions [] regions = {Regions.US_WEST_1, Regions.US_WEST_2};
		String [] buckets = {"f1da2908-7b6b-4af0-8fe3-575dbba1a7a01",
							 "d7eafafe-947f-4dd0-9757-ff269ddf93b21"};
		String [] keys = new String [100];
		for(int i = 0; i < 100; i++){
			keys[i] = FileCreator.createKey(100+i);
		}
		S3TestSetup set = new S3TestSetup(regions, buckets, 1, "TestResults", false);
		TestMultiDown down = new TestMultiDown(set, region, keys, numThreads);
		down.testDownload();
	}
}
