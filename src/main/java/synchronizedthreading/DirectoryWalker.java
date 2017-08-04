package synchronizedthreading;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Stack;

/**
 * This class iterates through a given directory recursively, allowing for multiple threads to access 
 * all of the files under the directory. This reads a directory tree via a depth first search.
 * In addition, it only reads files, not directories.
 * @author jerryzhiruijin
 *
 */
public class DirectoryWalker implements Iterator<Path>{
	
	//private Path start;
	private Path curr;
	private DirectoryStream<Path> dir;
	private Stack <DirectoryStream<Path>> dirs = new Stack<DirectoryStream<Path>>();
	private Iterator<Path> iterator;
	private Stack<Iterator<Path>> iterators = new Stack<Iterator<Path>>();
	
	/**
	 * Constructs a DirectoryWalker iterator.
	 * @param start - Root Directory to be searched
	 * @throws IOException - Unlikely to happen unless cache becomes full or similar.
	 */
	public DirectoryWalker(Path start) throws IOException{
		//this.start = start;
		dir = Files.newDirectoryStream(start);
		iterator = dir.iterator();	
	}	
	
	/**
	 * This gets the next file in the directory tree. 
	 */
	public synchronized Path next(){
		while(true){
			try{		
				curr = iterator.next();
				
				//Find the next File in the directory.
				while(curr.toFile().isDirectory()){
					dirs.push(dir);
					dir = Files.newDirectoryStream(curr);
					iterators.push(iterator);
					iterator = dir.iterator();
					curr = iterator.next();
				}
				return curr;
				
			/*
			 * If an iterator finds that it's been completely read, go to the next lowest iterator
			 * and continue with iteration.
			 */
			}catch(NoSuchElementException e){
				if(!iterators.isEmpty()){
					try {
						dir.close();
					} catch (IOException e1) {
					}
					dir = dirs.pop();
					iterator = iterators.pop();
				}
				else{
					return null;
				}
			}
			catch(IOException e){
				System.out.println(e);
			}
		}
	}

	
	/**
	 *  This gets the next file in the directory tree. 
	 * @param n - 
	 * @return
	 */
	public synchronized Path next(int n){
		while(true){
			try{
				
				if(iterator.hasNext()){
					curr = iterator.next();
				}
				
				/*
				 * Find the next File in the directory.
				 */
				while(curr.toFile().isDirectory()){
					dirs.push(dir);
					dir = Files.newDirectoryStream(curr);
					iterators.push(iterator);
					iterator = dir.iterator();
					
					curr = iterator.next();
				}
				return curr;
				
			/*
			 * If an iterator finds that it's been completely read, go to the next lowest iterator
			 * and continue with iteration.
			 */
			}catch(NoSuchElementException e){
				if(!iterators.isEmpty()){
					try {
						dir.close();
					} catch (IOException e1) {
					}
					dir = dirs.pop();
					iterator = iterators.pop();
				}
				else{
					return null;
				}
			}
			catch(IOException e){
				System.out.println(e);
			}
		}
	}
	
	/**
	 * This method is not implemented, and I don't know how it would be implemented.
	 */
	public boolean hasNext(){
		return true;
	}	
}
