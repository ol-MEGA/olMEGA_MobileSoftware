package com.fragtest.android.pa.Processing.Preprocessing;

import org.jtransforms.fft.DoubleFFT_1D;
import java.util.Arrays;
/**
 * Created by michael on 14.03.17.
 */
public class Envelope {

    public Envelope(){

    }

    public static double[] envelope(double[] inSig){
        //computes hilbert envelope of the input signal

        double[] hilbert = hilbertTransform(inSig);
        double[] envelope = new double[hilbert.length/2];

        //compute the abs of hilbert
        for(int i=0,j=0;i<hilbert.length;i=i+2,j++){
            envelope[j] = Math.sqrt(Math.pow(hilbert[i],2)+Math.pow
                    (hilbert[i+1],2));
        }

        return envelope;
    }

    public static double[] hilbertTransform(double[] inSig){
        //computes the discrete hilbert transform of the input signal, and
        //return it in a complex matrix. outSig[0]=Re[0], outSig[1]=Im[0], ...

        int NFFT = inSig.length;
        DoubleFFT_1D fft = new DoubleFFT_1D(NFFT);
        double[] X = Arrays.copyOf(inSig, 2*NFFT);

        fft.realForwardFull(X); //computes the fft

        double[] H = new double[NFFT];
        int fs_half = (int) Math.ceil(NFFT/2);

        //creates the hilbert transform transfer function
        if(2*fs_half==NFFT) {
            H[0] = 1; // 0Hz
            H[fs_half] = 1; // fs/2
            for (int i = 1; i < fs_half; i++) {
                H[i] = 2;
            }
        }
        else{
            H[0] = 1; // 0Hz
            for (int i = 1; i < fs_half+1; i++) {
                H[i] = 2;
            }
        }

        //multiply the hilbert transfer function with the spectrum of the signal
        for(int i=0,j=0;i<X.length;i=i+2,j++){
            X[i] *= H[j]; // Re(i) * H(j)
            X[i+1] *= H[j]; // Im(i+1) * H(j)
        }

        double[] outSig = X;

        DoubleFFT_1D ifft = new DoubleFFT_1D(NFFT);
        ifft.complexInverse(outSig,true); //computes the ifft

        return outSig;
    }


}
