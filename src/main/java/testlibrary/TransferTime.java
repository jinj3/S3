package testlibrary;



import java.io.File;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.LogManager;


import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;

/**
 * Main class for testing S3 performance.
 * @author jerryzhiruijin
 *
 */
public class TransferTime {
	static boolean bucketsFlag = false;
	static boolean deleteFlag = true;
	static boolean regionsFlag = false;
	static boolean rangeFlag = false;
	static int regionNum = -1;
	static int largeNum = -1;
	static boolean sizeFlag = false;
	static boolean directoryFlag = false;
	static boolean largeFlag = false;
	static char flag;
	static S3TestSetup set;
	/**
	 * Takes in user input to perform various tests on Amazon S3.
	 * @param args - list of arguments given on method call.
	 * @throws IOException - These errors shouldn't happen
	 */
	public static void main (String[] args) throws IOException{
		LogManager.getLogManager().reset();
		if(parseArgs(args) == -1){
			return;
		}
		set = new S3TestSetup(GlobalValues.regions, GlobalValues.regionsFile,
				10, GlobalValues.excelName, false);
		if(bucketsFlag){
			if(deleteFlag){
				Buckets.deleteBuckets();
			}
			Buckets.createBuckets();
			Buckets.readBuckets();
			UploadSmallTest.run();
		}
		
		//Read the bucket names from file and update GlobalValues.
		try {
			Buckets.readBuckets();
		} 
		
		//If no buckets were previously created, then create buckets and read buckets.
		catch (IOException e) {
			System.out.println("No buckets have been created yet. Creating buckets automatically");
			Buckets.createBuckets();
			Buckets.readBuckets();
			UploadSmallTest.run();
		}
		
		//If the user wants to test inter-region copying, then use these methods.
		if(regionsFlag){
			try{
				for(int i = 0; i < GlobalValues.numRegions; i++){
					if(!GlobalValues.s3s[i].doesObjectExist(GlobalValues.buckets[i],
							FileCreator.createKey(GlobalValues.splitKey))){
						ManyFilesTest.uploadFiles(i, 1);
					}
				}
			}catch(Exception e){}
			RegionsTest.test(regionNum);
		}
		
		//If any of the "Large" file tests are needed, upload them to S3. 
		if(rangeFlag || largeFlag){	
			try{
				if(!GlobalValues.s3s[largeNum].doesObjectExist(GlobalValues.buckets[largeNum],
						FileCreator.createKey(GlobalValues.largeKey))){
					System.out.println("Uploading single large file to " + GlobalValues.regions[largeNum]);
					File file = FileCreator.createLargeFile();
					UploadLarge.createSingle(largeNum, file);
				}
				if(rangeFlag && !GlobalValues.s3s[largeNum].doesObjectExist(GlobalValues.buckets[largeNum],
						FileCreator.createKey(GlobalValues.splitKey + 1))){
					System.out.println("Uploading many files to " + GlobalValues.regions[largeNum]);
					ManyFilesTest.uploadFiles(largeNum);
				}
			}catch (AmazonServiceException ase) {
		        System.out.println("Caught an AmazonServiceException, which means your request made it "
		              + "to Amazon S3, but was rejected with an error response for some reason.");
		        System.out.println("Error Message:    " + ase.getMessage());
		        System.out.println("HTTP Status Code: " + ase.getStatusCode());
		        System.out.println("AWS Error Code:   " + ase.getErrorCode());
		        System.out.println("Error Type:       " + ase.getErrorType());
		        System.out.println("Request ID:       " + ase.getRequestId());
		    } catch (AmazonClientException ace) {
		        System.out.println("Caught an AmazonClientException, which means the client encountered "
		              + "a serious internal problem while trying to communicate with S3, "
		              + "such as not being able to access the network.");
		        System.out.println("Error Message: " + ace.getMessage());
	        }
		}
		
		//This uploads the large file required if it hasn't already been uploaded elsewhere
		if(sizeFlag){
			try{
				File file = null;
				for(int i = 0; i < GlobalValues.numRegions; i++){
					if(!GlobalValues.s3s[i].doesObjectExist(GlobalValues.buckets[i],
							FileCreator.createKey(GlobalValues.largeKey))){
						System.out.println("Uploading single large file to " + 
							GlobalValues.regions[i]);
						if(file != null){
							file = FileCreator.createLargeFile();
						}
						UploadLarge.createSingle(i, file);
					}
				}
			}catch(Exception e){}
			DownloadLargeTest.downloadFromAllRegions(set);
		}
		
		/*
		 * This checks how much time it takes to download a chunk vs. a file from S3.
		 * Note: Some skewing can be found as all later requests tend to average less time.
		 */
		if(rangeFlag){
			DownloadSegmentTest.run(largeNum,GlobalValues.segments,set);
			ManyFilesTest.run(largeNum, GlobalValues.segments,set);	
		}
		if(largeFlag){
			DownloadLargeTest.run(set, largeNum);
		}
		set.write();
	}
	
	/**
	 * Parses the arguments given to this program.
	 * @param args
	 */
	public static int parseArgs(String[] args){
		
		
		ArrayList<String> arg = new ArrayList<String>();
		/*
		 * parses through the argument list and identifies the flags.
		 */
		for(int i = 0; i < args.length; i++){
			if(args[i].startsWith("-")){
				arg.add(args[i]);
			}
			else{
				
				/*
				 * Sets numTests. Default value if not set is 10.
				 */
				try{
					GlobalValues.numTests = Integer.parseInt(args[i]);
					set.setNumTests(GlobalValues.numTests);
				}catch(NumberFormatException e){
					System.out.println("This program expects an integer argument. To see "
							+ "Usage, type --help");
					return -1;
				}
			}
		}
		
		//checks that there is at least 1 flag set.
		if(arg.size() == 0){
			System.out.println("This program expects at least one flag to be set. To see "
					+ "Usage, type --help");
			return -1;
		}
		
		/*
		 * Checks for --help or --regions flags and prints their respective information
		 * if found.
		 */
		for(int i = 0; i < arg.size(); i++){
			if(arg.get(i).equals("--help")){
				System.out.println("Usage: TransferTime NUMTESTS");
				System.out.println("\nAn Amazon S3 performance Tester \n");
				System.out.println("Options:");
				System.out.println("\t--help\tPrint usage");
				System.out.println("\t--regions\tPrint the list of regions");
				System.out.println("-B \t\tCreate new buckets (automatically deletes previous "
						+ "buckets)");
				System.out.println("-d \t\tKeep the previous buckets(Only useful if -B was "
						+ "called)");
				System.out.println("-R \t\tTest performance in different regions");
				System.out.println("-D \t\tTest performance when using different directory "
						+ "structures");
				System.out.println("-r \t\tTest performance when reading ranges "
						+ "from a large file");
				System.out.println("-F \t\tTest performance when downloading from different regions");
				System.out.println("-L \t\tTest performance when downloading"
						+ " a large file from one region.");
				System.out.println("The Integer represents the number of times to run each test.");
				return -1;
			}
			if(arg.get(i).equals("--regions")){
				for(int j = 0; j < GlobalValues.regions.length; j++){
					System.out.println(Integer.toHexString(j) + "\t-\t" + 
							GlobalValues.regions[j]);
				}
				return -1;
			}
		}
		
		//Parses through the flags and sets variables.
		for(int i = 0; i < arg.size(); i++){
			for(int j = 1; j < arg.get(i).length(); j++){
				flag = arg.get(i).charAt(j);
				switch (flag){
				
				/*
				 * This flag makes the program create new buckets
				 */
				case 'B':
					bucketsFlag = true;
					
					/*
					 * If there is a d flag immediately after then is means that
					 * this program should not delete the previous set of buckets.
					 */
					if(arg.get(i).length() > ++j && arg.get(i).charAt(j) == 'd'){
						deleteFlag = false;
					}
					else{
						--j;
					}
					break;
					
				/*
				 * This flag means that the program will check the S3 copy function
				 * between different regions.
				 */
				case 'R':
					regionsFlag = true;
					
					/*
					 * Finds the region for the test to go out of.
					 */
					try{
						regionNum = Integer.parseInt(arg.get(i).charAt(++j) + "", 16);
						if(regionNum >= GlobalValues.regions.length || regionNum < 0){
							System.out.println("Expected an integer from 0 to " + 
									GlobalValues.regions.length);
							System.out.println("For the list of regions, type --regions");
							return -1;
						}
						
						
					/*
					 * Catches invalid values after the R flag and prints a statement.
					 */
					}catch(Exception e){
						System.out.println("Expected an integer after 'R' flag. For the list "
								+ "of regions, type --regions");
						return -1;
					}
					break;
					
				/*
				 * Flag is set if the user wants to check upload/download on a large ~36MB file.
				 */
				case 'F':
					sizeFlag = true;
					break;
					
				/*
				 * TODO: This has no functionality right now. Probably no reason to implement 
				 * 		 since the website says key distribution only has an impact on high
				 * 		 request volume buckets
				 */
				case 'D':
					directoryFlag = true;
					break;
					
				/*
				 * This flag is set if the user wants to check how downloading different
				 * chunks from a large file affects S3 performance.
				 */
				case 'r':
					rangeFlag = true;
					try{
						largeNum = Integer.parseInt(arg.get(i).charAt(++j) + "",16);
						if(largeNum >= GlobalValues.regions.length || largeNum < 0){
							System.out.println("Expected an integer from 0 to " + 
									GlobalValues.regions.length);
							System.out.println("For the list of regions, type --regions");
							return-1;	
						}
					}catch(Exception e){
						System.out.println("Expected an integer after 'L' flag. For the list "
								+ "of regions, type --regions");
						return-1;
					}
					break;
				
				case 'L':
					largeFlag = true;
					try{
						largeNum = Integer.parseInt(arg.get(i).charAt(++j) + "",16);
						if(largeNum >= GlobalValues.regions.length || largeNum < 0){
							System.out.println("Expected an integer from 0 to " + 
									GlobalValues.regions.length);
							System.out.println("For the list of regions, type --regions");
							return-1;	
						}
					}catch(Exception e){
						System.out.println("Expected an integer after 'L' flag. For the list "
								+ "of regions, type --regions");
						return-1;
					}
					break;
				default:
					System.out.println("The flag " + flag +" is invalid, use --help for usage");
					return-1;
				}	
			}
		}
		return 0;
	}
}
