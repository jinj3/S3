package com.amazonaws.samples;


import java.util.function.*;
import static java.lang.System.out;

// Tested with JDK 1.8.0-ea-b75
public class p1
{
	   public static void main(String[] args)
	   {
		   System.out.println(findMedianSortedArrays(new int[]{1,2},new int[] {3,4}));
	   }
	
	public static double findMedianSortedArrays(int[] nums1, int[] nums2) {
        if(nums1.length == 0){
            return(findMedianSortedArrays(nums2,nums2));
        }
        if(nums2.length == 0){
            return(findMedianSortedArrays(nums1,nums1));
        }
        //int random = nums1.length;
        //random = nums1[0];
        int middle = (nums1.length + nums2.length) / 2;
        
        int even = (nums1.length + nums2.length) % 2;
        int prevS;
        int start1 = 0;
        int start2 = 0;
        int end1 = nums1.length - 1;
        int end2 = nums2.length - 1;
        while(middle > 4){
            if(nums1[(start1 + end1) / 2] > nums2[(start2 + end2) / 2]){
                end1 = ((start1 + end1) / 2);
                prevS = start2;
                start2 = ((start2 + end2) / 2);
                middle -= start2 - prevS;
            }
            else{
                prevS = start1;
                start1 = ((start1 + end1) / 2);
                end2 = ((start2 + end2) / 2);
                middle -= start1 - prevS;
            }
        }
        System.out.println(middle);
        int [] lastFew = new int [5];
        int iterator1 = start1;
        int iterator2 = start2;
        for(int i = 0; i < lastFew.length;i++){
            
            if(nums1[iterator1] > nums2[iterator2]){
                lastFew[i] = nums2[iterator2];
                iterator2++;
                if(iterator2 >= nums2.length){
                    break;
                }
            }
            else{
                lastFew[i] = nums1[iterator1++];
                if(iterator1 >= nums1.length){
                    break;
                }
            }
            System.out.println(lastFew[i]);
        }
        
        if(even == 0){
            return (lastFew[middle - 1] + lastFew[middle]) / 2;
        }
        else{
            return lastFew[middle];
        }
    }

}
