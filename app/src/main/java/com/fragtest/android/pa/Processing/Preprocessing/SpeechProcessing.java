package com.fragtest.android.pa.Processing.Preprocessing;


import com.fragtest.android.pa.HelperClasses.ArrayHelper;

import static com.fragtest.android.pa.HelperClasses.Find.findGreaterIdx;
import static com.fragtest.android.pa.HelperClasses.Find.findMaxVal;

/**
 * Created by michael on 13.02.17.
 */
public class SpeechProcessing{


    public SpeechProcessing(){

    }

    public static double[] voiceActivityDetect(double[] audioData, long fsamp){
        // method for simple energy-thresholding voice activity detection.
        // remove silence parts of the speech signal
        //----------------------------------------------------------------------

        //pre calculations to find the energy threshold
        double[] audioData_sqr = new double[audioData.length];
        double[] audioData_abs = new double[audioData.length];

        for(int i=0;i<audioData.length;i++){
            audioData_sqr[i] = audioData[i] * audioData[i];
            audioData_abs[i] = Math.abs(audioData[i]);
        }

        double audioData_max = findMaxVal(audioData_abs);

        double energyThresh = 50; // energy threshold in dB

        //find indices from  samples they are greater than the threshold
        int[] indices = findGreaterIdx(audioData_sqr,(
                (audioData_max*audioData_max)/(Math.pow(10,energyThresh/10))));

        int[] diff = new int[indices.length-1];

        for(int i= 0;i<diff.length;i++){

            diff[i] = indices[i+1] - indices[i];
        }

        double thres = 0.05f*fsamp; //time threshold

        int[] idx1 = findGreaterIdx(diff,thres);

        for(int i= 0;i<idx1.length;i++){

            idx1[i] = indices[idx1[i]];
        }

        int[] idx2 = findGreaterIdx(diff,thres);

        for(int i= 0;i<idx2.length;i++){

            idx2[i] = indices[idx2[i]+1];
        }

        int[] indices2 = new int[idx1.length+idx2.length];

        for(int i=0,j=0;i<idx1.length;i++,j=j+2){

            indices2[j] = idx1[i];
            indices2[j+1] = idx2[i];
        }

        //creates the new signal without silence parts
        double[] audioData_new = null;

        if (indices2.length != 0){
            if (indices2.length>2) {
                 audioData_new = ArrayHelper.getRange(audioData,indices[0],
                         indices2[0]);

                 for(int i=1;i<indices2.length-2;i=i+2){

                     double[] tempArray = ArrayHelper.getRange(audioData,
                             indices2[i],indices2[i+1]);

                     audioData_new  = ArrayHelper.mergeArrays(audioData_new,
                             tempArray);
                 }

                 audioData_new = ArrayHelper.mergeArrays(audioData_new,
                         ArrayHelper.getRange(audioData,
                                 indices2[indices2.length-1],indices[indices
                                         .length-1]));
            }
            else {
                audioData_new = ArrayHelper.mergeArrays(ArrayHelper.getRange
                        (audioData,indices[0],indices2[0]),ArrayHelper
                        .getRange(audioData,indices2[0],indices[indices
                                .length-1]));
            }
        }
        else {
            audioData_new = ArrayHelper.getRange(audioData,indices[0],
                    indices[indices.length-1]);
        }
        return audioData_new;
    }

    public static double[] activeSpeechLevel(double[] audioData, long fsamp){
        //Active speech level measurement following ITU-T P.56
        //----------------------------------------------------------------------

        //normalize audio signal if the range grater than [-1,1], by divided
        // by 2^(15) (pcm data!)
        if (findMaxVal(audioData)>1){
            for(int i=0;i<audioData.length;i++){
                audioData[i] = audioData[i]/(Math.pow(2,15));
            }
        }

        // TODO: implement the ASL...




        return audioData;
    }

}
