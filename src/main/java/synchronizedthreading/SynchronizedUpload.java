package synchronizedthreading;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.util.NoSuchElementException;


public class SynchronizedUpload {
	DirectoryStream<Path> ds;
	//Iterator<Path> iterator;
	DirectoryWalker iterator;
	
	
	public SynchronizedUpload(Path dir) throws IOException{
		/*ds = Files.newDirectoryStream(dir);*/
		//iterator = ds.iterator();
		iterator = new DirectoryWalker(dir);
	}
	
	


	
	public synchronized Path getFile() throws NoSuchElementException{
	/*	Path temp = iterator.next();
		while(temp.toFile().isDirectory()){
			temp = iterator.next();
		}
		return temp;*/
		
		Path temp = iterator.next();
		if(temp == null){
			
			throw new NoSuchElementException();
		}
		return temp;
	}
}
