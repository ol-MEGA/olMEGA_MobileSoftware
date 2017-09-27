package com.fragtest.android.pa.HelperClasses;
import java.util.*;


/**
 * Created by michael on 13.02.17.
 */
public class Find {

    public Find(){

    }

    public static double findMaxVal(double[] data) {
        // method to get the maximum value of an array

        double maxVal = data[0];

        for (int i = 0; i < data.length; i++) {
            if (data[i] > maxVal) {
                maxVal = data[i];
            }
        }
        return maxVal;
    }

    public static double findMinVal(double[] data) {
        // method to get the minimum value of an array

        double minVal = data[0];

        for (int i = 0; i < data.length; i++) {
            if (data[i] < minVal) {
                minVal = data[i];
            }
        }
        return minVal;
    }


    public static int findMaxIdx(double[] data) {
        // method to get the index of the maximum value of an array

        double maxVal = findMaxVal(data);
        int maxIdx = 0;

        for (int i = 0; i < data.length; i++) {
            if (maxVal == data[i]) {
                maxIdx = i;
                break;
            }
        }

        return maxIdx;
    }

    public static int findMinIdx(double[] data) {
        // method to get the index of  the minimum value of an array

        double minVal = findMinVal(data);
        int minIdx = 0;

        for (int i = 0; i < data.length; i++) {
            if (minVal == data[i]) {
                minIdx = i;
                break;
            }
        }

        return minIdx;
    }

    public static int[] findGreaterIdx(double[] data, double data2){
        // method to get the index of the value of data that are bigger than
        // data2

        List<Integer> greaterIndices = new ArrayList<>();

        for(int i=0;i<data.length;i++){

            if (data[i] > data2){
                greaterIndices.add(i);
            }
        }

        int[] returnIndices = new int[greaterIndices.size()];

        // convert list to array
        for(int i = 0; i < greaterIndices.size(); i++){
            returnIndices[i] = greaterIndices.get(i);
        }

        return returnIndices;
    }

    public static int[] findGreaterIdx(int[] data, double data2){
        // method to get the index of the value of data that are bigger than
        // data2

        List<Integer> greaterIndices = new ArrayList<>();

        for(int i=0;i<data.length;i++){

            if (data[i] > data2){
                greaterIndices.add(i);
            }
        }

        int[] returnIndices = new int[greaterIndices.size()];

        // convert list to array
        for(int i = 0; i < greaterIndices.size(); i++){
            returnIndices[i] = greaterIndices.get(i);
        }

        return returnIndices;
    }

    public static int[] findGreaterVal(int[] data, double data2){
        // method to get the index of the value of data that are bigger than
        // data2

        List<Integer> greaterIndices = new ArrayList<>();

        for(int i=0;i<data.length;i++){

            if (data[i] > data2){
                greaterIndices.add(data[i]);
            }
        }

        int[] returnIndices = new int[greaterIndices.size()];

        // convert list to array
        for(int i = 0; i < greaterIndices.size(); i++){
            returnIndices[i] = greaterIndices.get(i);
        }
        return returnIndices;
    }
}