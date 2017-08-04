package testlibrary;

import java.io.File;
import java.io.IOException;

public class S3FilesSetup {

	
	/**
	 * Uploads a small file to every region given to the S3TestSetup object.
	 * @param setup - Object which holds all important information for tests.
	 * @throws IOException - Thrown if an IOException occurs (This shouldn't happen in normal use).
	 */
	public static void setupSmall(S3TestSetup setup) throws IOException{
		File temp = FileCreator.createSmallFile();
		
		for(int i = 0; i < setup.getNumberOfRegions(); i++){
			setup.getS3(i).putObject(setup.getBucket(i), 
					FileCreator.createKey(GlobalValues.smallKey), temp);
		}
	}
	
	/**
	 * Uploads a small file to a single region in S3
	 * @param setup - Object which holds all important information for tests.
	 * @param region - integer specifying which region to use.
	 * @throws IOException - Thrown if an IO exception occurs while creating the small file.
	 */
	public static void setupSmall(S3TestSetup setup, int region) throws IOException{
		File temp = FileCreator.createSmallFile();
		setup.getS3(region).putObject(setup.getBucket(region), 
				FileCreator.createKey(GlobalValues.smallKey), temp);
	}
	
	
	/**
	 * Uploads a large file to all regions in S3
	 * @param setup - Object which holds all important information for tests.
	 * @throws IOException - Thrown if an IO exception occurs while creating the small file.
	 */
	public static void setupLarge(S3TestSetup setup) throws IOException{
		File temp = FileCreator.createLargeFile();
		
		for(int i = 0; i < setup.getNumberOfRegions(); i++){
			setup.getS3(i).putObject(setup.getBucket(i), 
					FileCreator.createKey(GlobalValues.largeKey), temp);
		}
	}
	
	/**
	 * Uploads a large file to a single region in S3
	 * @param setup - Object which holds all important information for tests.
	 * @param region - integer specifying which region to use.
	 * @throws IOException - Thrown if an IO exception occurs while creating the small file.
	 */
	public static void setupLarge(S3TestSetup setup, int region) throws IOException{
		File temp = FileCreator.createLargeFile();
		setup.getS3(region).putObject(setup.getBucket(region), 
				FileCreator.createKey(GlobalValues.largeKey), temp);
	}
	
	/**
	 * Uploads a number of files to all regions in S3
	 * @param setup - Object which holds all important information for tests.
	 * @param numSplits - the number of different sections a single large file is split into
	 * @throws IOException - Thrown if an IO exception occurs while creating the small file.
	 */
	public static void setupSplit(S3TestSetup setup, int numSplits) throws IOException{
		GlobalValues.numSplits = numSplits;
		File temp = FileCreator.createSplitFile();
		for(int i = 0; i < setup.getNumberOfRegions(); i++){
			for(int j = 0; j < numSplits; j++){
				setup.getS3(i).putObject(setup.getBucket(i), 
						FileCreator.createKey(GlobalValues.splitKey + j), temp);
			}
		}
	}
	
	/**
	 * Uploads a number of files to a single region in S3
	 * @param setup - Object which holds all important information for tests.
	 * @param numSplits - the number of different sections a single large file is split into
	 * @param region - the number representing which region the split files will be uploaded to.
	 * @throws IOException - Thrown if an IO exception occurs while creating the small file.
	 */	public static void setupSplit(S3TestSetup setup, int numSplits, int region) throws IOException{
		File temp = FileCreator.createSplitFile();
		
		for(int j = 0; j < numSplits; j++){
			setup.getS3(region).putObject(setup.getBucket(region), 
					FileCreator.createKey(GlobalValues.splitKey + j), temp);
		}
		
	}
	
	
}
