package synchronizedthreading;


/**
 * This class feeds the threads the next file that they need to download from 
 * @author jerryzhiruijin
 *
 */
public class SynchronizedDownload {
	private int iterator;
	private String[] keys;
	
	/**
	 * Constructor for the class. 
	 * @param keys - The list of all keys to be downloaded from Amazon S3.
	 */
	public SynchronizedDownload(String [] keys){
		this.keys = keys;
		iterator = 0;
	}
	
	/**
	 * This gets the next file that needs to be downloaded from S3. It's synchronized so that
	 * multiple threads won't try to access this method at the same time.
	 * @return The name of the next file to be downloaded from Amazon S3
	 */
	public synchronized String getKey(){
		if(iterator < keys.length){
			
			return keys[iterator++];
			
		}
		return null;
	}

	
}
