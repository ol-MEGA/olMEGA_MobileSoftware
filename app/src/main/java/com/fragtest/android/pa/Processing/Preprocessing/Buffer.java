package com.fragtest.android.pa.Processing.Preprocessing;

/**
 * Created by michael on 16.03.17.
 */
public class Buffer {

    public Buffer(){

    }

    public static double[][] buffer(double[] inSig, int winLen, int overlap){
        // This buffer function separated the input signal in frames with the
        // desired length and overlap. For no overlap, overlap=0.
        // Output: outBuffer[0][] = first frame,
        //         outBuffer[1][] = second frame, ...

        int inSigLen = inSig.length;

        //computes the number of frames
        int numOfFrames = (int)Math.ceil(
                (double)inSigLen / ( (double)winLen - (double)overlap) );

        double[][] outBuffer = new double[numOfFrames][winLen];

        int h = 0;

        //computes the first frame
        for(int j=overlap;j<winLen;j++){
            outBuffer[0][j] = inSig[h];
            h++;
        }

        //computes the remaining frames
        for(int i=1;i<numOfFrames;i++){

            //overlap values from previous frame
            for(int j=0;j<overlap;j++){
                outBuffer[i][j] = outBuffer[i-1][(winLen-overlap)+j];
            }

            //add values from the input Signal
            for(int j=overlap;j<winLen && h<inSigLen;j++){
                outBuffer[i][j] = inSig[h];
                h++;
            }

        }

        return outBuffer;
    }

}
