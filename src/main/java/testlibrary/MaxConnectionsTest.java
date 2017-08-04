package testlibrary;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import org.apache.poi.hssf.usermodel.HSSFSheet;

import com.amazonaws.services.s3.model.S3Object;

public class MaxConnectionsTest {
	
	public static void run (S3TestSetup set, int regionIndex, int maxConnections, String key) 
			throws IOException{
		
		int originalConnections = set.getMaxConnections();
		int maxThreads = 1;
		for(int i = 0; i < maxConnections; i++){
			maxThreads = 2 * maxThreads;
		}
	
    	HSSFSheet sheet = set.getExcel().getSheet(GlobalValues.connectionCountName);
    	if(sheet == null){
    		sheet = set.getExcel().createSheet(GlobalValues.connectionCountName);
    	}
    	HSSFSheet sheet2 = set.getExcel().getSheet(GlobalValues.connectionCountNameRaw);
    	if(sheet2 == null){
    		sheet2 = set.getExcel().createSheet(GlobalValues.connectionCountNameRaw);
    	}
    	TreeMap<String, Object[]> tree = new TreeMap<String,Object[]>();
    	tree.put("0", new Object [] {"Connection Count" , "First Time", "Average Time",
    			"Standard Deviation","50%", "60%", "70%", "80%", "90%","95%","99%","100%"});
    	
    	TreeMap<String, Object[]> treeRaw = new TreeMap<String,Object[]>();
    	
    	Object[] rawHeader = new Object[set.getNumTests() + 1];
    	rawHeader[0] = "Connection Count";
    	for(int i = 0; i < set.getNumTests(); i++){
    		rawHeader[i + 1] = "Test " + (i+1);
    	}
    	treeRaw.put("0", rawHeader);
    	
    	Object[] times;
    	char [] buf = new char[set.getBufferSize()];
    	long startTime;
    	long endTime;
    	int connectionIndex = 0;
    	for(int numConnections = 1; numConnections <= maxThreads; numConnections *= 2){
    		System.out.println("Testing with " + numConnections + " Connections");
    		set.setMaxConnections(numConnections);
    		times = new Object[set.getNumTests() + 1];
	    	times[0] = numConnections;
    		for (int testNum = 0; testNum < set.getNumTests(); testNum++){
    			startTime = System.nanoTime() / GlobalValues.convertNanoToMilli;
    			S3Object obj = set.getS3(regionIndex).getObject(set.getBucket(regionIndex), key);
    			BufferedReader in = new BufferedReader(new InputStreamReader(obj.getObjectContent()));
    			while(in.read(buf) != -1){}
    			endTime = System.nanoTime() / GlobalValues.convertNanoToMilli;
    			times[testNum + 1] = endTime - startTime;
    		}
	    	tree.put((connectionIndex+1) + "", Stats.getStats(times, numConnections));
	   		treeRaw.put((connectionIndex+1) + "", times);
	   		connectionIndex++;
	   		try {
				if(set.getDelay()){
					TimeUnit.SECONDS.sleep(GlobalValues.betweenTestDelay);
				}
			} catch (InterruptedException e) {

			}
    	}
    	set.setMaxConnections(originalConnections);
    	FileCreator.createExcel(sheet, tree);
	   	FileCreator.createExcel(sheet2, treeRaw);
	}
}
