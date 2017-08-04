package synchronizedthreading;

/**
 * This class is used to check whether all of the concurrent threads have finished running yet.
 * @author jerryzhiruijin
 *
 */
public class SynchronizedCounter {
	int count;
	
	/**
	 * Constructor for this object
	 * @param i - Total amount of threads being run concurrently
	 */
	public SynchronizedCounter(int i){
		count = i;
	}
	
	/**
	 * Checks if the current thread is the last thread. This method should only be called once
	 * by a thread.
	 * @return true if this is the last living thread in the pool, else return false.
	 */
	public synchronized boolean lastThread(){

		return --count == 0;
	}
}
