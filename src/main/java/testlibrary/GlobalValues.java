package testlibrary;


import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;


/**
 * This class holds all of the constants to be used while testing the performance capabilities
 * of Amazon S3.
 * @author Jerry Jin
 *
 */
public class GlobalValues {
	
	public static HSSFWorkbook excel;
	
	public static String [] sizeKeys = {"222", "223", "224", "225", "226"};
	public static int [] fileSizes = {1000000,5000000,10000000,50000000,100000000};
	//public static String [] sizeKeys = {"222", "223", "224", "225", "226", "227"};
	//public static int [] fileSizes = {1000000,5000000,10000000,50000000,100000000,500000000};
	public static int[] segments = {0,5,9};
	
	
	public static boolean delay = false;
	public static String excelName = "Data.xls";
	public static long convertNanoToMilli = 1000000;
	public static long fileSize = 10000000; //actually it's 1/36 of the file size.	
	public static int largeKey = 1000;
	public static int splitKey = 100;
	public static int numSplits = 10;
	public static int smallKey = 1;
	public static int numTests = 10;//Number of consecutive requests to be timed.
	public static boolean mocked = false;
	public static long testTypeDelay = 120; // Delay in seconds between 2 tests of different types.
	public static long betweenTestDelay = 10; //Delay in seconds between 2 consecutive tests.

	
	public static String multiThreadedDownloadName = "Multithreaded Download size";
	public static String multiThreadedDownloadNameRaw = "Raw Multithreaded Download size";
	public static String multipleSingleFileDownloadName = "Single file Download Data";
	public static String multipleSingleFileDownloadRawName = "Single file Download Data Raw";
	public static String largeFileDownloadName = "Large Download Data";
	public static String largeFileDownloadRawName = "Large Download Data Raw";
	public static String regionDownloadName = "Inter-Region Get Data";
	public static String regionDownloadRawName = "Inter-Region get Data Raw";
	public static String segmentSheetName = "Chunked Download Data";
	public static String segmentSheetRawName = "Chunked Download Data Raw";
	public static String multiRegionName = "Inter-Region Copy Data";
	public static String multiRegionRawName = "Inter-Region Copy Data Raw";
	public static String bufferSizeName = "Buffer Size Download Data";
	public static String bufferSizeRawName = "Buffer Size Download Data Raw";
	public static String connectionCountName = "Connections Download Data";
	public static String connectionCountNameRaw = "Connections Download Data Raw";
	
	//Filenames which store various data from the tests. 
	public static String largeDownloadFile = "largedownstats.txt";
	public static String regionDownloadFile = "redownstats.txt";
	public static String regionUploadsFile = "regupstats.txt";
	public static String regionsFile = "regions.txt"; //Gives the bucket names for each region.
	public static String regionCopyFile = "regioncopy.txt";
	public static String regionsMock = "regionsMock.txt";

	//List of buckets and their respective regions.
	public static String bucketOregon;
	public static String bucketSydney;
	public static String bucketCali;
	public static String bucketVirg;
	public static String bucketOhio;
	public static String bucketCanada;
	public static String bucketIreland;
	public static String bucketLondon;
	public static String bucketGermany;
	public static String bucketMumbai;
	public static String bucketSingapore;
	public static String bucketTokyo;
	public static String bucketSeoul;
	public static String bucketSao;
	
	public static String[] buckets = {bucketOregon, bucketCali, bucketVirg,
            bucketOhio, bucketLondon, bucketSingapore,bucketTokyo,};

	public static int numRegions = buckets.length;
	
	//List of regions with their respective region symbol.
	public static Regions regionOregon = Regions.US_WEST_2;
	public static Regions regionSydney = Regions.AP_SOUTHEAST_2;
	public static Regions regionCali = Regions.US_WEST_1;
	public static Regions regionVirg = Regions.US_EAST_1;
	public static Regions regionOhio = Regions.US_EAST_2;
	public static Regions regionCanada = Regions.CA_CENTRAL_1;
	public static Regions regionIreland = Regions.EU_WEST_1;
	public static Regions regionLondon = Regions.EU_WEST_2;
	public static Regions regionGermany = Regions.EU_CENTRAL_1;
	public static Regions regionMumbai = Regions.AP_SOUTH_1;
	public static Regions regionSingapore = Regions.AP_SOUTHEAST_1;
	public static Regions regionTokyo = Regions.AP_NORTHEAST_1;
	public static Regions regionSeoul = Regions.AP_NORTHEAST_2;
	public static Regions regionSao = Regions.SA_EAST_1;
	
	public static Regions[] regions = {regionOregon, regionCali, regionVirg,
            regionOhio,regionLondon,regionSingapore,regionTokyo};


	public static AmazonS3 s3;


	public static AmazonS3[] s3s = new AmazonS3[numRegions];


	public static AWSCredentials credentials = null;

	
	
}
