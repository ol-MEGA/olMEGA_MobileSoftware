package com.fragtest.android.pa.Processing.Preprocessing;

/**
 * Created by michael on 15.03.17.
 */
public class Window {

    public Window(){

    }

    public static double[] hamming(int winLenght){
        //computes a hamming window
        //based on formula from https://en.wikipedia.org/wiki/Window_function

        double[] hammingWin = new double[winLenght];

        for(int i=0;i<winLenght;i++){
            hammingWin[i] = 0.54 - 0.46 * Math.cos( (2*Math.PI*i) /
                    (winLenght-1) );
        }

        return hammingWin;
    }
}
