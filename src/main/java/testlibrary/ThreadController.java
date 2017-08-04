package testlibrary;

/**
 * This class is used so that waiting threads won't spuriously wake up.
 * @author jerryzhiruijin
 *
 */
public class ThreadController {

	private boolean isFinished;
	
	public ThreadController(){
		isFinished = false;
	}
	
	public void setFinished(boolean bool){
		isFinished = bool;
	}
	
	public boolean isFinished(){
		return isFinished;
	}
}
