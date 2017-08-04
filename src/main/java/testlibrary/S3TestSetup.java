package testlibrary;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;


/**
 * This class creates objects which can be used for testing various S3 performance benchmarks
 * and writing the results to the specified file.
 * @author jerryzhiruijin
 *
 */
public class S3TestSetup {
	private AmazonS3[] s3s;
	private Regions[] regions;
	private String[] buckets;
	private AWSCredentials credentials;
	private int numRegions;
	private int numTests;
	private String excelName;
	private HSSFWorkbook excel;
	private int numSplits = GlobalValues.numSplits;
	private long fileSize = GlobalValues.fileSize;
	private boolean delay;
	private String largeFile = FileCreator.createKey(GlobalValues.largeKey);
	private String smallFile = FileCreator.createKey(GlobalValues.smallKey);
	private String[] splitFiles = findSplitFiles();
	private int[] segments = {0};
	private int bufferSize = 10000000;
	private int[] buffers;
	private ClientConfiguration config;
	private int maxConnections = 64;//TODO: Handle changing this value by others
	boolean useHttp;
	
	/**
	 * Creates an object which holds the variables used in various tests
	 * @param regionArray - Array of regions
	 * @param bucks - Array of bucket names. The names must correspond with the regions.
	 * @param numTests - Number of tests to be performed for each benchmark.
	 * @param excelName - The name of the excel file in which the results will be written.
	 * @param delay - True if the user wants to make every test wait 1 minute between each request.
	 */
	public S3TestSetup(Regions[] regionArray, String[] bucks, int numTests, String excelName,
			boolean delay){
		this.delay = delay;
		credentials = new DefaultAWSCredentialsProviderChain().getCredentials();
		regions = new Regions[regionArray.length];
		buckets = new String[bucks.length];
		s3s = new AmazonS3[regionArray.length];
		
		//Print an error if regions and buckets are incorrectly configured.
		if(regions.length != buckets.length){
			System.err.println("regions and buckets don't match");
			return;
		}
		config = new ClientConfiguration();
		config.setMaxConnections(maxConnections);
		
		//create the AmazonS3 objects and save the fields to instance variables
		for(int i = 0; i < regionArray.length; i++){
			regions[i] = regionArray[i];
			buckets[i] = bucks[i];
			s3s[i] = AmazonS3ClientBuilder.standard().withRegion
					(regions[i]).withCredentials(new AWSStaticCredentialsProvider
					(credentials)).withClientConfiguration(config).build();
 		}
		this.numTests = numTests;
		numRegions = regionArray.length;
		this.excelName = excelName;
		GlobalValues.s3s = s3s;
		GlobalValues.numRegions = numRegions;
		GlobalValues.buckets = buckets;
		GlobalValues.regions = regions;
	
		//Create a new Excel worksheet with the specified name.
		try{
			POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(excelName));
			excel = new HSSFWorkbook(fs);
			for(int i = excel.getNumberOfSheets() - 1; i >= 0 ; i--){
				excel.removeSheetAt(i);
			}
		}catch (IOException e){
			excel = new HSSFWorkbook();
		}	
	}
	
	
	/**
	 * holds the variables for the various tests to use
	 * @param regs - array of regions
	 * @param bucketsFile - Name of the file holding all of the bucket names. These names MUST
	 * 						correspond with the regions array.
	 * @param numTests - Number of tests to be performed on each performance benchmark
	 * @param excelName - Name of the excel file that all of these test results will be written to
	 * @param delay - Indicates true if you want to implement a 1 minute delay between each
	 * 				  request.
	 * @throws IOException - This should only happen if the file specified is invalid
	 */
	public S3TestSetup(Regions[] regs, String bucketsFile, int numTests, String excelName,
			boolean delay) throws IOException{
		this.delay = delay;
		credentials = new DefaultAWSCredentialsProviderChain().getCredentials();
		numRegions = regs.length;
		buckets = new String[numRegions];
		regions = new Regions[numRegions];
		s3s = new AmazonS3[numRegions];
		FileReader fr = new FileReader(bucketsFile);
		BufferedReader reader = new BufferedReader(fr);
		String line;
		config = new ClientConfiguration();
		config.setMaxConnections(maxConnections);
		
		//Build the fields for regions buckets and AmazonS3s 
		for(int i = 0; i < numRegions && (line = reader.readLine()) != null;i++){
			buckets[i] = line;
			regions[i] = regs[i];
			s3s[i] = AmazonS3ClientBuilder.standard().withRegion
					(regions[i]).withCredentials(new AWSStaticCredentialsProvider
					(credentials)).withClientConfiguration(config).build();
		}
		reader.close();
		this.excelName = excelName;
		GlobalValues.s3s = s3s;
		GlobalValues.numRegions = numRegions;
		GlobalValues.buckets = buckets;
		GlobalValues.regions = regions;
		this.numTests = numTests;
		
		//Create a new excel workbook 
		try{
			POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(excelName));
			excel = new HSSFWorkbook(fs);
			for(int i = excel.getNumberOfSheets() - 1; i >= 0 ; i--){
				excel.removeSheetAt(i);
			}
		}catch (IOException e){
			excel = new HSSFWorkbook();
		}
	}
	
	/**
	 * Returns the file names of all the files. 
	 * @return
	 */
	private String[] findSplitFiles(){
		String [] keys = new String[100];
		for(int i = 0; i < 100; i++){
			keys[i] = FileCreator.createKey(GlobalValues.splitKey + i);
		}
		return keys;
	}
	
	/**
	 * Writes the excel data into a file and closes the file.
	 * @throws IOException - Throws this exception if the file has already been closed? idk.
	 */
	public void write() throws IOException{
		FileOutputStream fileOut = new FileOutputStream(excelName);
		excel.write(fileOut);
		fileOut.close();
	}
	
	/**
	 * Gives the name of the AmazonS3 connecting to a specific region.
	 * @param idx - index in the regions array that you want the s3 to point to.
	 * @return Returns the AmazonS3 object which corresponds to this index.
	 */
	public AmazonS3 getS3(int idx){
		return s3s[idx];
	}
	
	/**
	 * Gives the name of the Bucket corresponding to the index.
	 * @param idx - the index in the array that you want the value from.
	 * @return
	 */
	public String getBucket(int idx){
		return buckets[idx];
	}
	
	/**
	 * Gives the Region that corresponds to the index.
	 * @param idx - index in the array that the value should be taken from.
	 * @return - region corresponding to that index.
	 */
	public Regions getRegion(int idx){
		return regions[idx];
	}
	
	/**
	 * Returns the number of regions
	 * @return 
	 */
	public int getNumberOfRegions(){
		return numRegions;
	}
	
	/**
	 * Gives the index for which a specific region is inside this class.
	 * @param region - The region for which an index is found.
	 * @return the index corresponding to the region it is. -1 if region isn't found.
	 */
	public int getRegionNum(Regions region){
		for(int i = 0; i < numRegions; i++){
			if(region.equals(regions[i])){
				return i;
			}
		}
		return -1;
	}
	

	/**
	 * Returns the HSSFWorkbook reference so that it can be modified by the testing method.
	 * @return HSSFWorkbook that will be written to stream.
	 */
	public HSSFWorkbook getExcel(){
		return excel;
	}
	
	public S3TestSetup setExcel(HSSFWorkbook exc){
		excel = exc;
		return this;
	}
	
	/**
	 * Returns the number of tests for each individual benchmark
	 * @return number of tests.
	 */
	public int getNumTests(){
		return numTests;
	}
	
	/**
	 * Returns the number of times the file is split (Default 100)
	 * @return returns number of splits in the file
	 */
	public int getNumSplits(){
		return numSplits;
	}
	
	public void setNumSplits(int value){
		numSplits = value;
	}
	
	/**
	 * Gets the value of 1/36 of the file size of the current file
	 * @return represents (But not directly) the file size of the "large" file.
	 */
	public long getFileSize(){
		return fileSize;
	}
	
	public void setFileSize(long value){
		fileSize = value;
	}
	
	public void setDelay(boolean value){
		delay = value;
	}
	
	/**
	 * Indicates whether to have a delay between consecutive gets.
	 * @return boolean indicating whether a pause should be used.
	 */
	public boolean getDelay(){
		return delay;
	}
	
	/**
	 * Sets the name used in TestDownloadLarge and large segment downloads.
	 * @param fileName
	 * @return
	 */
	public S3TestSetup setLargeFileName(String fileName){
		largeFile = fileName;
		return this;
	}
	
	public String getLargeFileName(){
		return largeFile;
	}
	
	public S3TestSetup setSmallFileName(String fileName){
		smallFile = fileName;
		return this;
	}
	
	public String getSmallFileName(){
		return smallFile;
	}
	
	/**
	 * Sets the list of file names that will be used when benchmarking single file download
	 * versus ranges from larger files.
	 * @param fileNames - Array of file names to be downloaded and benchmarked 
	 * @return - A reference to this object for the Builder design pattern.
	 */
	public S3TestSetup setSplitFileNames(String [] fileNames){
		splitFiles = fileNames;
		return this;
	}
	
	/**
	 * Returns the list of all files that are going to be tested in the large file range versus
	 * smaller file get timing test.
	 * @return
	 */
	public String[] getSplitFileNames(){
		return splitFiles;
	}
	
	public S3TestSetup setSegmentValues(int[] values){
		segments = values;
		return this;
	}
	public int[] getSegmentValues(){
		return segments;
	}
	
	public S3TestSetup setNumTests(int n){
		numTests = n;
		return this;
	}
	
	public int getBufferSize(){
		return bufferSize;
	}
	
	public S3TestSetup setBufferSize(int n){
		bufferSize = n;
		return this;
	}
	public S3TestSetup useLargeKey (String key){
		largeFile = key;
		return this;
	}
	public String getLargeKey(){
		return largeFile;
	}
	
	public S3TestSetup useSmallKey(String key){
		smallFile = key;
		return this;
	}
	
	public String getSmallKey(){
		return smallFile;
	}
	
	public S3TestSetup useSplitKeys(String[] keys){
		splitFiles = keys;
		return this;
	}
	
	public String[] getSplitKeys(){
		return splitFiles;
	}
	
	/**
	 * Returns the buffer size at an index in the array
	 * @param idx - index of the buffers array
	 * @return - Buffer size corresponding to that index
	 */
	public int getBufferFromList(int idx){
		return buffers[idx];
	}
	
	/**
	 * Sets the list of buffers to be used in testing by buffer size.
	 * @param bufs - list of buffer sizes.
	 * @return - this S3TestSetup for further configuration.
	 */
	public S3TestSetup useBufferList(int[] bufs){
		buffers = bufs;
		return this;
	}
	
	/**
	 * Returns the buffer size when reading from file
	 */
	public int getBufferListSize(){
		return buffers.length;
	}

	/**
	 * Returns the credentials used to access Amazon S3
	 */
	public AWSCredentials getCredentials(){
		return credentials;
	}
	
	/**
	 * Returns the ClientConfiguration used to create the AmazonS3 clients.
	 */
	public ClientConfiguration getClientConfiguration(){
		return config;
	}
	
	/**
	 * Changes the maxConnections parameter for the AmazonS3 clients.
	 * @param i - New max connection count.
	 */
	public void setMaxConnections(int i){
		maxConnections = i;
		config.setMaxConnections(maxConnections);
		for(int index = 0; index < regions.length; index++){
			s3s[index] = AmazonS3ClientBuilder.standard().withRegion
					(regions[index]).withCredentials(new AWSStaticCredentialsProvider
					(credentials)).withClientConfiguration(config).build();
		}
	}
	
	/**
	 * Changes the maxConnections parameter for the AmazonS3 clients.
	 * @param i - New max connection count.
	 * @return - Returns this object for more changes.
	 */
	public S3TestSetup withMaxConnections(int i){
		maxConnections = i;
		
		config.setMaxConnections(maxConnections);
		for(int index = 0; index < regions.length; index++){
			s3s[index] = AmazonS3ClientBuilder.standard().withRegion
					(regions[index]).withCredentials(new AWSStaticCredentialsProvider
					(credentials)).withClientConfiguration(config).build();
		}
		return this;
	}
	
	/**
	 * Gets the max connections set for AmazonS3
	 */
	public int getMaxConnections(){
		return maxConnections;
	}
	
	/**
	 * Changes the protocol type for the AmazonS3 client.
	 * @param value - true indicates that the user wants to use HTTP, false indicates user wants
	 *                to use HTTPS.
	 * @return - A pointer to this object.
	 */
	public S3TestSetup withProtocol(boolean value){
		useHttp = value;
		if(useHttp){
			config.setProtocol(Protocol.HTTP);
		}
		else{
			config.setProtocol(Protocol.HTTPS);
		}
		for(int index = 0; index < regions.length; index++){
			s3s[index] = AmazonS3ClientBuilder.standard().withRegion
					(regions[index]).withCredentials(new AWSStaticCredentialsProvider
					(credentials)).withClientConfiguration(config).build();
		}
		return this;
	}
}
