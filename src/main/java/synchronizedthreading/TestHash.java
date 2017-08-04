package synchronizedthreading;

import testlibrary.FileCreator;

public class TestHash {
	public static void main(String[] args){
		int[] values = new int[10];
		String temp;
		
		for(int i = 0; i < 1000000; i++){
			temp = FileCreator.keyHash(FileCreator.createKey(i));
			values[Integer.parseInt("" +temp.charAt(0))]++;
			System.out.println(temp);
		}
		for(int i = 0; i < 10; i++){
			System.out.println(values[i]);
		}
	}
}
