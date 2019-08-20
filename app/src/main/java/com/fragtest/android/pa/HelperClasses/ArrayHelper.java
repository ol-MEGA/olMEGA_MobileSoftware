package com.fragtest.android.pa.HelperClasses;

/**
 * Created by michael on 14.02.17.
 */
public class ArrayHelper {

    public ArrayHelper(){

    }

    public static double[] getRange(double[] array,int from, int to){

        int len = (to-from)+1;

        double[] newArray = new double[len];

        for(int i=from, j=0;i<=to;i++,j++){
            newArray[j] = array[i];
        }
        return newArray;
    }

    public static double[] getValues(double[] array,int[] idx){

        double[] values = new double[idx.length];

        for(int i=0;i<idx.length;i++){
            values[i] = array[idx[i]];
        }

        return values;
    }

    public static double[] mergeArrays(double[] array1, double[] array2){

        double[] newArray = new double[array1.length+array2.length];

        for(int i=0;i<array1.length;i++){
            newArray[i] = array1[i];
        }

        for(int i=array1.length,j=0;i<newArray.length;i++,j++){
            newArray[i] = array2[j];
        }

        return newArray;

    }

    public static double[] sum(double[][] array){

        double[] temp = new double[array.length];

        for(int n=0;n<array.length;n++){

            double sum = 0;

            for(int k=0;k<array[0].length;k++){
                sum += array[n][k];
            }

            temp[n] = sum;

        }

        return temp;
    }

    public static double[][][] mean(double[][][] array, int dimension) {
        //this function calculates the mean of the 3D input array.
        //with the parameter dimension you pretend over which dimension the
        //mean was calculated.

        //getFromId array size;
        int numLayer = array.length;
        int numRow = array[0].length;
        int numColumn = array[0][0].length;
        double[][][] meanOut = new double[0][0][0];

        //compute the mean
        if (numRow != 1) {
            if(dimension==1) {
                meanOut = new double[numLayer][1][numColumn];
                double sumTemp = 0;

                for (int l = 0; l < numLayer; l++) {
                    for (int c = 0; c < numColumn; c++) {
                        for (int r = 0; r < numRow; r++) {
                            sumTemp += array[l][r][c];
                        }
                        meanOut[l][0][c] = sumTemp / numRow;
                        sumTemp = 0;
                    }
                }
            }
            else if(dimension==2){
                meanOut = new double[numLayer][numRow][1];
                double sumTemp = 0;

                for (int l = 0; l < numLayer; l++) {
                    for (int r = 0; r < numRow; r++) {
                        for (int c = 0; c < numColumn; c++) {
                            sumTemp += array[l][r][c];
                        }
                        meanOut[l][r][0] = sumTemp / numColumn;
                        sumTemp = 0;
                    }
                }

            }
            else if(dimension==3){
                meanOut = new double[1][numRow][numColumn];
                double sumTemp = 0;

                for (int r = 0; r < numRow; r++) {
                    for (int c = 0; c < numColumn; c++) {
                        for (int l = 0; l < numLayer; l++) {
                            sumTemp += array[l][r][c];
                        }
                        meanOut[0][r][c] = sumTemp / numLayer;
                        sumTemp = 0;
                    }
                }

            }
        }
        else if (numRow == 1) {
            if (dimension == 1 || dimension == 2) {
                meanOut = new double[numLayer][1][1];
                double sumTemp = 0;

                for (int l = 0; l < numLayer; l++) {
                    for (int c = 0; c < numColumn; c++) {
                        sumTemp += array[l][0][c];
                    }
                    meanOut[l][0][0] = sumTemp / numColumn;
                    sumTemp = 0;
                }
            }
            if (dimension == 3) {
                meanOut = new double[1][1][numColumn];
                double sumTemp = 0;

                for (int c = 0; c < numColumn; c++) {
                    for (int l = 0; l < numLayer; l++) {
                        sumTemp += array[l][0][c];
                    }
                    meanOut[0][0][c] = sumTemp / numLayer;
                    sumTemp = 0;
                }
            }
        }
        return meanOut;
    }

    public static double[][][] max(double[][][] array, int dimension){
        //this function find the maximum value of the 3D input array.
        //with the parameter dimension you pretend over which dimension the
        //maximum value was searched.

        //getFromId array size;
        int numLayer = array.length;
        int numRow = array[0].length;
        int numColumn = array[0][0].length;
        double[][][] maxOut = new double[0][0][0];


        if (numRow == 1 && numColumn !=1 && dimension != 1) {
            dimension = 2;
        }
        else if(numColumn == 1 && numRow != 1 && dimension != 2){
            dimension=1;
        }
        else if(numColumn == 1 && numRow == 1 && numLayer !=1 &&
                (dimension != 1 || dimension != 2)){
            dimension=3;
        }
        else if(numLayer == 1 && dimension != 3){
            dimension=1;
        }

            if (dimension == 1) {
                maxOut = new double[numLayer][1][numColumn];
                for (int l = 0; l < numLayer; l++) {
                    for (int c = 0; c < numColumn; c++) {
                        double maxValue = array[l][0][c];
                        for (int r = 0; r < numRow; r++) {
                            if (array[l][r][c] > maxValue) {
                                maxValue = array[l][r][c];
                            }
                        }
                        maxOut[l][0][c] = maxValue;
                    }
                }
            }
            else if (dimension == 2) {
                maxOut = new double[numLayer][numRow][1];
                for (int l = 0; l < numLayer; l++) {
                    for (int r = 0; r < numRow; r++) {
                        double maxValue = array[l][r][0];
                        for (int c = 0; c < numColumn; c++) {
                            if (array[l][r][c] > maxValue) {
                                maxValue = array[l][r][c];
                            }
                        }
                        maxOut[l][r][0] = maxValue;
                    }
                }
            }
            else if (dimension == 3) {
                maxOut = new double[1][numRow][numColumn];
                for (int r = 0; r < numRow; r++) {
                    for (int c = 0; c < numColumn; c++) {
                        double maxValue = array[0][r][c];
                        for (int l = 0; l < numLayer; l++) {
                            if (array[l][r][c] > maxValue) {
                                maxValue = array[l][r][c];
                            }
                        }
                        maxOut[0][r][c] = maxValue;
                    }
                }
            }


        return maxOut;
    }

    public static double[][][] max(double[][][] array){
        //this function find the maximum value of the 3D input array.

        //getFromId array size;
        int numLayer = array.length;
        int numRow = array[0].length;
        int numColumn = array[0][0].length;
        double[][][] maxOut = new double[0][0][0];
        int dimension;

        if (numRow == 1 && numColumn != 1) {
            dimension = 2;
        }
        else if(numColumn == 1 && numRow != 1){
            dimension=1;
        }
        else if(numRow == 1 && numColumn == 1 && numLayer !=1){
            dimension=3;
        }
        else if(numLayer == 1 && numRow == 1 && numColumn == 1){
            dimension=1;
        }
        else{
            dimension=1;
        }

        if (dimension == 1) {
            maxOut = new double[numLayer][1][numColumn];
            for (int l = 0; l < numLayer; l++) {
                for (int c = 0; c < numColumn; c++) {
                    double maxValue = array[l][0][c];
                    for (int r = 0; r < numRow; r++) {
                        if (array[l][r][c] > maxValue) {
                            maxValue = array[l][r][c];
                        }
                    }
                    maxOut[l][0][c] = maxValue;
                }
            }
        }
        else if (dimension == 2) {
            maxOut = new double[numLayer][numRow][1];
            for (int l = 0; l < numLayer; l++) {
                for (int r = 0; r < numRow; r++) {
                    double maxValue = array[l][r][0];
                    for (int c = 0; c < numColumn; c++) {
                        if (array[l][r][c] > maxValue) {
                            maxValue = array[l][r][c];
                        }
                    }
                    maxOut[l][r][0] = maxValue;
                }
            }
        }
        else if (dimension == 3) {
            maxOut = new double[1][numRow][numColumn];
            for (int r = 0; r < numRow; r++) {
                for (int c = 0; c < numColumn; c++) {
                    double maxValue = array[0][r][c];
                    for (int l = 0; l < numLayer; l++) {
                        if (array[l][r][c] > maxValue) {
                            maxValue = array[l][r][c];
                        }
                    }
                    maxOut[0][r][c] = maxValue;
                }
            }
        }


        return maxOut;
    }

    public static double[] sum(double[][] array, int dimension){

        //getFromId array size;
        int numRow = array.length;
        int numColumn = array[0].length;
        double[] sumOut = new double[0];

        double sumTemp = 0;


            if (dimension == 1) {
                sumOut = new double[numColumn];
                for (int c = 0; c < numColumn; c++) {
                    sumTemp = 0;
                    for (int r = 0; r < numRow; r++) {
                        sumTemp += array[r][c];
                    }
                    sumOut[c] = sumTemp;
                }
            }
             else if(dimension == 2){
                sumOut = new double[numRow];
                for (int r = 0; r < numRow; r++) {
                    sumTemp = 0;
                    for (int c = 0; c < numColumn; c++) {
                        sumTemp += array[r][c];
                    }
                    sumOut[r] = sumTemp;
                }
            }


        return sumOut;
    }

    public static double sum(double[] array){

        double sumTemp = 0;

        for (int i=0;i<array.length;i++){
            sumTemp += array[i];
        }

        return sumTemp;
    }

    public static double[] cumsum(double[] array) {
        double[] cumSumOut = new double[array.length];
        double sumTemp = 0;
        for (int i = 0; i < array.length; i++) {
            sumTemp += array[i];
            cumSumOut[i] = sumTemp;
        }
        return cumSumOut;
    }


}


