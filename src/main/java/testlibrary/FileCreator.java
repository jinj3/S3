package testlibrary;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

/**
 * This class contains methods which create files of different sizes.
 * @author Jerry Jin
 *
 */
public class FileCreator {
	
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
	
	/**
	 * Uploads a file of a custom size to a specified key
	 * @param numBytes - Size of the file (in bytes) that should be uploaded
	 * @param key - Key under which this object will be stored in Amazon S3
	 * @param set - Object containing all necessary information for uploading/downloading objects from S3
	 * @param regionIndex - Index corresponding to the region that will be used inside set.
	 * @throws IOException - Thrown if some IO exception occurs.
	 */
	public static void uploadCustomFile(long numBytes, String key, S3TestSetup set, int regionIndex) 
			throws IOException{
		File tempFile = File.createTempFile("TempFile", ".txt");
		tempFile.deleteOnExit();
		Writer writer = new OutputStreamWriter(new FileOutputStream(tempFile));
		for(int writeIteration = 0; writeIteration < numBytes / 36; writeIteration++){
			writer.write(UUID.randomUUID() + "");
		}
		for(int writeIteration = 0; writeIteration < numBytes % 36; writeIteration++){
			writer.write("2");
		}
		writer.close();
		System.out.println("Uploading file size " + numBytes + " to key " + key);
		set.getS3(regionIndex).putObject(set.getBucket(regionIndex), key, tempFile);
	}
	
	/**
	 * Creates a file that is 1/100 of the large file.
	 * @return Returns a file.
	 * @throws IOException - Shouldn't happen (I think)
	 */
	public static File createSplitFile() throws IOException{
		File medFile = File.createTempFile("TempFile", ".txt");
		medFile.deleteOnExit();
		Writer writer = new OutputStreamWriter(new FileOutputStream(medFile));
		for(int i = 0; i < GlobalValues.fileSize/GlobalValues.numSplits; i++){
			writer.write(UUID.randomUUID() + "");
		}
		writer.close();
		return medFile;
		
	}
	
	/**
	 * This class creates a "large" (~360MB) file and returns it to the caller.
	 * @return A small large file called "temp.txt"
	 * @throws IOException It should never throw this exception.
	 */
	public static File createLargeFile() throws IOException{
		File largeFile = File.createTempFile("TempFile", ".txt");
		largeFile.deleteOnExit();
		Writer writer = new OutputStreamWriter(new FileOutputStream(largeFile));
		for(int i = 0; i < GlobalValues.fileSize; i++){
			writer.write(UUID.randomUUID() + "\n");
		}
		writer.close();
		return largeFile;
				
	}
	
	/**
	 * Create the name under which a file will be stored given an integer i. This is meant to
	 * be a "lazy" key generation and shouldn't be used if actually storing large numbers
	 * of files into S3, since the key distribution will be bad.
	 * @param i This is generally the i-th file that will be inserted/retrieved from S3 for
	 *          small files.
	 * @return  Returns the key under which this file will be retrieved or sent.
	 */
	public static String createKey(int i){
		return "0000000" + i;
	}	
	
	/**
	 * Creates a key which better distributes the range of values for the first few chars.
	 * @param key - Key that will be hashed.
	 * @return A key with a better distribution to make S3 more efficient.
	 */
	public static String keyHash(String key){
		String temp;
		
		temp = new StringBuilder(key).reverse().toString();
		temp = (Math.abs(temp.hashCode()) % 1000)+"";
		if(temp.length() < 3){
			for(int i = temp.length() - 3; i < 0;i++){
				temp = "0"+ temp;
			}
		}
		return temp + key;
	}
	
	
	
	
	/**
	 * This method creates a single line in a table for showing various statistics.
	 * @param writer - Writes to the file needed
	 * @param tag - tag to be used
	 * @param firstTime - Time used on first put/get
	 * @param averageTime - Average time used over a number of put/gets.
	 */
	public static void createStats(PrintWriter writer, String tag, long firstTime, 
			double averageTime){

		writer.write(tag + '\t' + firstTime + '\t' + averageTime + '\n');

	}
	
	/**
	 * Creates an excel sheet which shows different results.
	 * @param sheet This is the Excel sheet that the method will write to
	 * @param data This TreeMap holds all the information that will be written.
	 */
	public static void createExcel(HSSFSheet sheet, TreeMap<String, Object[]> data){
		int rownum = 0;
		Set<String> keySet = data.keySet();
		
		//Iterate through the TreeMap, and convert every object to its respective type.
		for(String key : keySet){
			Row row = sheet.createRow(rownum++);
			Object[] obj = data.get(key);
			int cellnum = 0;
			for(Object o : obj){
				Cell cell = row.createCell(cellnum++);
				if(o instanceof String){
                    cell.setCellValue((String)o);
				}
                else if(o instanceof Long){
                    cell.setCellValue((Long)o);
                }
                else if(o instanceof Integer){
                	cell.setCellValue((Integer)o);
                }
                else if(o instanceof Double){
                	cell.setCellValue((Double) o);
                }
			}
		}
		for(int i = 0; i < sheet.getRow(0).getLastCellNum();i++){
			sheet.autoSizeColumn(i);
		}
	}

	/**
	 * Uploads all necessary files for this test. This method should only be called once.
	 */
	public static void uploadFiles(int regionIndex, S3TestSetup set, 
			int [] fileSizes, String [] keys){
		
		for(int fileIterator = 0; fileIterator < fileSizes.length; fileIterator++){
			try {
				uploadCustomFile(fileSizes[fileIterator], keys[fileIterator], set, regionIndex);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	
}