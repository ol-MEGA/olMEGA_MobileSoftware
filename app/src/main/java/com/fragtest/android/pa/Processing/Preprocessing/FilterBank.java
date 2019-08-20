package com.fragtest.android.pa.Processing.Preprocessing;


import com.fragtest.android.pa.HelperClasses.ArrayHelper;


/**
 * Created by michael on 15.02.17.
 */
public class FilterBank {

    public FilterBank(){

    }

    public static double[][] ERBFilterBank(double[] inSig, long fsamp){

        //this function filter a input signal with the ERB filter bank, and
        //returns the output signals as a matrix of 23 filtered signals.

        //load ERB filter coefficients
        ERBFilterCoef erbCoeff = new ERBFilterCoef();
        erbCoeff.readCoef(fsamp);

        double[] A0 = erbCoeff.A0;
        double[] A11 = erbCoeff.A11;
        double[] A12 = erbCoeff.A12;
        double[] A13 = erbCoeff.A13;
        double[] A14 = erbCoeff.A14;
        double[] A2 = erbCoeff.A2;
        double[] B0 = erbCoeff.B0;
        double[] B1 = erbCoeff.B1;
        double[] B2 = erbCoeff.B2;
        double[] gain = erbCoeff.gain;
        double[] y1;
        double[] y2;
        double[] y3;
        double[] y4;

        double[][] output = new double[gain.length][inSig.length];

        Filter filt = new Filter();

        for(int i=0;i<gain.length;i++){

            double[] aCoeff_y1 = {A0[i]/gain[i], A11[i]/gain[i],
                    A2[i]/gain[i]};
            double[] bCoeff_y1 = {B0[i],B1[i],B2[i]};

            y1 = filt.iirFilterSosDf2(aCoeff_y1,bCoeff_y1,inSig,true);

            double[] aCoeff_y2 = {A0[i], A12[i], A2[i]};
            double[] bCoeff_y2 = {B0[i],B1[i],B2[i]};

            y2 = filt.iirFilterSosDf2(aCoeff_y2,bCoeff_y2,y1,true);

            double[] aCoeff_y3 = {A0[i], A13[i], A2[i]};
            double[] bCoeff_y3 = {B0[i],B1[i],B2[i]};

            y3 = filt.iirFilterSosDf2(aCoeff_y3,bCoeff_y3,y2,true);

            double[] aCoeff_y4 = {A0[i], A14[i], A2[i]};
            double[] bCoeff_y4 = {B0[i],B1[i],B2[i]};

            y4 = filt.iirFilterSosDf2(aCoeff_y4,bCoeff_y4,y3,true);

            for(int j=0;j<y4.length;j++){
                output[i][j] = y4[j];
            }
        }

       return output;
    }

    public static double[] computeModulationsCFs(int minCF, int maxCF, int
            nModFilters){

        //Computes the center frequencies of the filters needed for the
        // modulation filterbank used on the temporal envelope (or modulation
        // spectrum) of the cochlear channels.

        // minCF: Center frequency of the first modulation filter
        // maxCF: Center frequency of the last modulation filter
        // nModFilters: Number of modulation filters between minCF and maxCF

        double maxCFDouble = (double)maxCF;
        double minCFDouble = (double)minCF;
        double nModfiltersDouble = (double)nModFilters;

        // Spacing factor between filters.  Assumes constant (logarithmic)
        // spacing.
        double spacingFactor = Math.pow((maxCFDouble/minCFDouble),(1/
                (nModfiltersDouble-1)));

        // Computes the center frequencies
        double[] cfs = new double[nModFilters];
        cfs[0] = minCF;
        for(int i=1;i<nModFilters;i++){
            cfs[i] = cfs[i-1]*spacingFactor;
        }

        return cfs;
    }

    public static double[][] modulationFilterBank(double[] inSig, double[]
            modFilterCFs, long fsamp, int qFactor){

        double[][] outModFilterbank = new double[modFilterCFs.length][inSig.length];

        Filter filt = new Filter();
        double[] temp;

        for(int i=0;i<modFilterCFs.length;i++){

            double w0 = (2*Math.PI*modFilterCFs[i])/fsamp;
            double[] filtCoeff = makeModulationFilter(w0,qFactor);

            //getFromId the filter coefficients
            double[] b = {filtCoeff[0],filtCoeff[1],filtCoeff[2]};
            double[] a = {filtCoeff[3],filtCoeff[4],filtCoeff[5]};

            //filter the input signal
            temp = filt.iirFilterSosDf2(b,a,inSig,true);

            //store the filtered signal in the output matrix
            for(int j=0;j<outModFilterbank[0].length;j++){
                outModFilterbank[i][j] = temp[j];
            }
        }

        return outModFilterbank;
    }

    public static double[] makeModulationFilter(double w0, int qFactor){

        // w0 is cf of 2nd-order bandpass filter
        // Q is the Q of the filter

        double Q = (double)qFactor;
        double W0 = Math.tan(w0/2);
        double B0 = W0/Q;

        double[] b = {B0,0,-B0};
        double[] a = {1+B0+Math.pow(W0,2), 2*Math.pow(W0,2)-2, 1-B0+Math.pow
                (W0,2)};

        double a0 = a[0];

        for(int i=0;i<b.length;i++){
            b[i] = b[i]/a0;
            a[i] = a[i]/a0;
        }

        double[] coeff = ArrayHelper.mergeArrays(b,a);

        return coeff;
    }

    public static double[] calcERBs(double lowFreg, double fsamp, int
            nCochlearFilters){

        // computes the ERBs of the cochlear filterbank, designed with the
        // corresponding parameters.

        //Glasberg and Moore Parameters
        double EarQ = 9.26449;
        double minBW = 24.7;
        double order = 1;

        double[] cf = ERBSpace(lowFreg,fsamp/2,nCochlearFilters);

        double[] ERBs = new double[cf.length];

        for(int i=0;i<cf.length;i++){
            ERBs[(cf.length-1)-i] = Math.pow((Math.pow((cf[i]/EarQ),order)+Math
                    .pow(minBW,
                    order)), (1/order));
        }

        return ERBs;
    }

    public static double[] ERBSpace(double lowFreq, double highFreq, int
            nCochlearFilters){

        // Change the following three parameters if you wish to use a different
        // ERB scale.  Must change in MakeERBCoeffs too.
        double EarQ = 9.26449;				// Glasberg and Moore Parameters
        double minBW = 24.7;


        // All of the followFreqing expressions are derived in Apple TR #35, "An
        // Efficient Implementation of the Patterson-Holdsworth Cochlear
        // Filter Bank."  See pages 33-34.

        double[] centerFreq = new double[nCochlearFilters];

        for(int i=0,j=1;i<nCochlearFilters;i++,j++){

            centerFreq[i] = -(EarQ * minBW) + Math.exp(((double)j) * (-Math
                    .log(highFreq + EarQ*minBW) + Math.log(lowFreq +
                    EarQ*minBW))/nCochlearFilters) * (highFreq + EarQ*minBW);

        }

        return centerFreq;
    }

    public static double[] calcCutoffs(double[] cf, double fsamp, double
            QFactor){

        //Computes cutoff frequencies (3 dB) for 2nd order bandpass

        double[] cutoffs = new double[cf.length];
        double[] w0 = new double[cf.length];
        double[] B0 = new double[cf.length];

        for(int i=0;i<w0.length;i++){
            w0[i] = (2*Math.PI*cf[i])/fsamp;
        }

        for(int i=0;i<B0.length;i++){
            B0[i] = Math.tan(w0[i]/2)/QFactor;
        }

        for(int i=0;i<cutoffs.length;i++){
            cutoffs[i] = cf[i] - (B0[i]*fsamp / (2*Math.PI)); //L (left)
            // cutoffs[i] = cf[i] + (B0[i]*fsamp / (2*Math.PI)); //R (right)
        }

        return cutoffs;

    }
}
