package testlibrary;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import com.amazonaws.regions.Regions;


/**
 * This class provides a working jar which can test various aspects of Amazon S3 performance.
 * This class requires a "regions.txt" file which has bucket names corresponding to regions expressed
 * in GlobalValues.regions. 
 * @author jerryzhiruijin
 *
 */
public class S3Benchmarks {
	static int numTests;
	static int regionIndex;
	static int numArgs;
	static S3TestSetup testProperties;
	static boolean errorFound;
	static boolean helpFound;
	static boolean fileSizeFlag;
	static boolean concurrencyFlag;
	static boolean RTTFlag;
	static boolean rangeReadFlag;
	static boolean uploadFilesFlag;
	static boolean maxConnectionsFlag;
	static String EC2InstanceName = "";
	static boolean httpFlag;
	
	
	/**
	 * Executes the benchmarks specified by the user.
	 * @param args - String of arguments supplied by the user.
	 */
	public static void main(String[]args){
		try{
			readArgs(args);
			if(errorFound || helpFound){
				return;
			}
			if(numArgs < 2){
				throw new Exception("Invalid number of arguments. Expected 2");
			}
		}catch(Exception e){
			e.printStackTrace();
			printUsage();
			return;
		}
		try {
			if(Regions.getCurrentRegion() != null){
				GlobalValues.excelName = "Data-" + Regions.getCurrentRegion() + ".xls";
			}
			testProperties = new S3TestSetup(GlobalValues.regions, GlobalValues.regionsFile, 
					numTests, GlobalValues.excelName, GlobalValues.delay);
		} catch (IOException e) {
			printFileError();
			return;
		}
		try {
			executeTests();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		try {
			testProperties.write();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Reads through the arguments given to the main method.
	 * @param args - The list of arguments originally given to this class's main method
	 */
	private static void readArgs(String [] args){
		ArrayList<String> flags = new ArrayList<String>();
		numArgs = 0;
		for(int i = 0; i < args.length; i++){
			if(args[i].startsWith("--")){
				handleUsage(args[i]);
				return;
			}
			else if(args[i].startsWith("-")){
				flags.add(args[i]);
			}
			else{
				handleArgs(args[i]);
			}
		}
		readFlags(flags);
	}
	
	/**
	 * Reads the flags and sets the proper values 
	 * @param flags - List of flags called by the person executing the main method.
	 */
	private static void readFlags(ArrayList<String> flags){
		for(String flagIterator: flags){
			for(int i = 1; i < flagIterator.length(); i++){
				switch(flagIterator.charAt(i)){
					case 'F':
						fileSizeFlag = true;
						break;
					case 'C':
						concurrencyFlag = true;
						break;
					case 'R':
						RTTFlag = true;
						break;
					case 'S':
						rangeReadFlag = true;
						break;
					case 'U':
						uploadFilesFlag = true;
						break;
					case 'M':
						maxConnectionsFlag = true;
						break;
					case 'd':
						GlobalValues.delay = true;
						break;
					case 'h':
						httpFlag = true;
						break;
					default:
						errorFound = true;
						printUsage();
						return;
				}
			}
		}
	}
		
	/**
	 * Executes the tests called for by the user.
	 * TODO: Possibly refactor each of the tests into their own seperate methods?
	 * @throws InterruptedException 
	 */
	private static void executeTests() throws InterruptedException{
		if(httpFlag){
			testProperties.withProtocol(true);
		}
		if(uploadFilesFlag){
			FileCreator.uploadFiles(regionIndex, testProperties, 
					GlobalValues.fileSizes, GlobalValues.sizeKeys);
			try {
				ManyFilesTest.uploadFiles(regionIndex);
				FileCreator.uploadCustomFile(36 * GlobalValues.fileSize, 
						FileCreator.createKey(GlobalValues.largeKey), testProperties, regionIndex);
			} catch (IOException e) {
				e.printStackTrace();
			}
			if(testProperties.getDelay()){
				TimeUnit.SECONDS.sleep(GlobalValues.testTypeDelay);
			}
		}
		if(fileSizeFlag){
			try {
				DownloadLargeTest.run(testProperties, regionIndex);
			} catch (IOException e) {
				e.printStackTrace();
			}		
			if(testProperties.getDelay()){
				TimeUnit.SECONDS.sleep(GlobalValues.testTypeDelay);
			}
		}
		if(RTTFlag){
			try {
				testProperties.setLargeFileName(GlobalValues.sizeKeys[3]);
				DownloadLargeTest.downloadFromAllRegions(testProperties);
			} catch (IOException e) {
				e.printStackTrace();
			}
			testProperties.setLargeFileName(FileCreator.createKey(GlobalValues.largeKey));
			if(testProperties.getDelay()){
				TimeUnit.SECONDS.sleep(GlobalValues.testTypeDelay);
			}
		}
		if(rangeReadFlag){	
			try {
				DownloadSegmentTest.run(regionIndex,GlobalValues.segments,testProperties);
				if(testProperties.getDelay()){
					TimeUnit.SECONDS.sleep(GlobalValues.testTypeDelay);
				}
				ManyFilesTest.run(regionIndex, GlobalValues.segments,testProperties);
			} catch (IOException e) {
				// I'm pretty sure that these 2 tests should never throw an IOException 
				e.printStackTrace();
			}
			if(testProperties.getDelay()){
				TimeUnit.SECONDS.sleep(GlobalValues.testTypeDelay);
			}
		}
		if(concurrencyFlag){		
			testProperties.setMaxConnections(1);
			BenchmarkConcurrency benchmark = new BenchmarkConcurrency(4);
			GlobalValues.multiThreadedDownloadName += (GlobalValues.fileSizes[3] / 1000000) + "MB";
			GlobalValues.multiThreadedDownloadNameRaw += (GlobalValues.fileSizes[3] / 1000000) + "MB";
			benchmark.testThreadsToDownload(testProperties,regionIndex,GlobalValues.sizeKeys[3],
					GlobalValues.fileSizes[3]);
			if(testProperties.getDelay()){
				TimeUnit.SECONDS.sleep(GlobalValues.testTypeDelay);
			}
		}
		if(maxConnectionsFlag){
			try{
				MaxConnectionsTest.run(testProperties, regionIndex, 
						7, GlobalValues.sizeKeys[3]);
			} catch (IOException e){
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Prints the Usage for this jar.
	 */
	private static void printUsage(){
		System.out.println("Usage: S3BenchMarks REGIONINDEX NUMTESTS");
		System.out.println("\nAn Amazon S3 performance Tester \n");
		System.out.println("Options:");
		System.out.println("\t--help\tPrint usage");
		System.out.println("\t--regions\tPrint the list of regions");
		System.out.println("-R \t\tTests Round Trip Time when downloading a file from different regions");
		System.out.println("-S \t\tTests performance when reading ranges vs reading smaller files");
		System.out.println("-F \t\tTests performance when downloading files of different sizes");
		System.out.println("-C \t\tTests performance when downloading a file using multiple threads");
		System.out.println("-U \t\tUploads all files needed to S3");
		System.out.println("-M \t\tTests performance when using different maxConnections");
		System.out.println("-d \t\tAdd a delay between tests");
		System.out.println("-h \t\tUse HTTP Protocol instead of HTTPS");
	}
	
	/**
	 * Prints the list of regions and their respective numbers
	 */
	private static void printRegions(){
		for(int j = 0; j < GlobalValues.regions.length; j++){
			System.out.println(j + "\t-\t" + GlobalValues.regions[j]);
		}
	}
	
	/**
	 * Prints a message that there was no regions.txt file.
	 */
	private static void printFileError(){
		System.out.println("A regions.txt file is required for this program to execute properly");
	}
	
	/**
	 * Handles all of the arguments given by the user.
	 * @param arg - A single argument given as a string.
	 */
	private static void handleArgs(String arg){
		if(numArgs == 0){
			regionIndex = Integer.parseInt(arg);
		}
		else if(numArgs == 1){
			numTests = Integer.parseInt(arg);
		}
		else{
			printUsage();
			errorFound = true;
			return;
		}
		numArgs++;
	}
	
	/**
	 * Handles any calls for --regions or --usage by the executing method/user.
	 * @param arg - The command that starts with "--"
	 */
	private static void handleUsage(String arg){
		if(arg.equals("--regions")){
			printRegions();
		}
		else if(arg.equals("--help")){
			printUsage();
		}
		helpFound = true;
	}
}
