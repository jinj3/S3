package testlibrary;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.TreeMap;
import org.apache.poi.hssf.usermodel.HSSFSheet;



public class RegionsTest {
	private static TreeMap<String, Object[]> tree;
	private static TreeMap<String, Object[]> treeRaw;
	/**
	 * This method tests the speed at which S3 can copy between buckets in different regions.
	 * @param n - int corresponding to the region that will be tested (This is so a single
	 *            call for inter-region testing doesn't take 182 * numTest puts just to 
	 *            perform this test 1.
	 * @throws FileNotFoundException - This should never happen. Eclipse just requires
	 * 								   either a try/catch or a throw for creating a new writer.
	 */
	public static void test(int n) throws FileNotFoundException{
		File file = new File(GlobalValues.regionCopyFile);		
		int index;
		PrintWriter writer = new PrintWriter(file);
		writer.write("Source\t\tDest\t\tfirst\tavg\n");
		
		HSSFSheet sheet = GlobalValues.excel.getSheet(GlobalValues.multiRegionName);
		if(sheet != null){
			index = GlobalValues.excel.getSheetIndex(GlobalValues.multiRegionName);
			GlobalValues.excel.removeSheetAt(index);

		}
		sheet = GlobalValues.excel.createSheet(GlobalValues.multiRegionName);
		
		HSSFSheet sheet2 = GlobalValues.excel.getSheet(GlobalValues.multiRegionRawName);
		if(sheet2 != null){
			index = GlobalValues.excel.getSheetIndex(GlobalValues.multiRegionRawName);
			GlobalValues.excel.removeSheetAt(index);
			
		}  
		sheet2 = GlobalValues.excel.createSheet(GlobalValues.multiRegionRawName);
    	tree = new TreeMap<String,Object[]>();
    	tree.put("0", new Object [] {"Initial/Destination" , "First Time", "Average Time", 
    			"Standard Deviation", "50%", "60%", "70%", "80%", "90%","95%","99%","100%"});
    	
    	treeRaw = new TreeMap<String,Object[]>();
    	
       	
    	Object[] rawHeader = new Object[GlobalValues.numTests + 1];
    	rawHeader[0] = "Initial/Destination";
    	
    	
    	
    	for(int i = 0; i < GlobalValues.numTests; i++){
    		rawHeader[i + 1] = "Test " + (i+1);
    	}
    	treeRaw.put("0", rawHeader);
 
		for(int i = 0; i < GlobalValues.numRegions; i++){
			
			if(i != n){
				System.out.println("Testing copy speed between " + GlobalValues.regions[i] + 
						" and " + GlobalValues.regions[n] + "\n");
				copy(i,n,writer);
				
			}
		}
		writer.close();
		FileCreator.createExcel(sheet, tree);
		FileCreator.createExcel(sheet2, treeRaw);
	}
	
	/**
	 * This method tests the speed of copying a file between 2 buckets in different regions
	 * @param x - Initial bucket
	 * @param y - destination bucket
	 * @param writer - Writes the results to a file.
	 */
	private static void copy(int x, int y, Writer writer){
		long startTime = 0;
		long endTime = 0;
		long firstTime = 0;
		long totalTime = 0;

		Object[] results = new Object[GlobalValues.numTests + 1];
		results[0] = GlobalValues.regions[x]+ "/" + GlobalValues.regions[y];
		for(int i = 0; i < GlobalValues.numTests; i++){
			String key = FileCreator.createKey(GlobalValues.splitKey);
			startTime = System.nanoTime();
			GlobalValues.s3s[y].copyObject(GlobalValues.buckets[x], key, 
					GlobalValues.buckets[y], key);
			
			
			
			endTime = System.nanoTime();
			startTime = startTime / GlobalValues.convertNanoToMilli;
			endTime = endTime / GlobalValues.convertNanoToMilli;
			
			results[i+1] = endTime - startTime;
			if(i == 0){
				firstTime = endTime - startTime;
			}
			totalTime += endTime - startTime;

		}
		treeRaw.put(""+tree.size(), results);
		
		try {
			writer.write(GlobalValues.regions[x] + "\t" + GlobalValues.regions[y] + 
					"\t" + firstTime + "\t" + totalTime/GlobalValues.numTests + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
		tree.put(""+tree.size(), Stats.getStats(results, (String)results[0]));
	}
}
