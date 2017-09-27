package com.fragtest.android.pa.Processing.Preprocessing;

/**
 * Created by michael on 08.03.17.
 */

public class Filter {

    // filter states for Direct Form II
    private double state1_DF2; //memory for the filter state [z^-1]
    private double state2_DF2; //memory for the filter state [z^-2]

    //filter states for Direct Form I
    private double state1_DF1; //memory for the filter state [z^-1]
    private double state2_DF1; //memory for the filter state [z^-2]
    private double state3_DF1; //memory for the filter state [z^-1]
    private double state4_DF1; //memory for the filter state [z^-2]

    public Filter(){

        //define the first filter states as zeros
        this.state1_DF2 = 0;
        this.state2_DF2 = 0;

        this.state1_DF1 = 0;
        this.state2_DF1 = 0;
        this.state3_DF1 = 0;
        this.state4_DF1 = 0;

    }

    public double[] iirFilterSosDf2(double[] b, double[] a, double[]
            x_inSig, boolean clearStates) {

        // implementation of a Direct Form II SOS IIR filter
        // y(n) = 1/a0 * ( b0 * x(n) + b1 * x(n-1) + b2 * x(n-2) - a1 * y(n-1)
        // - y(n-2) )


        if(clearStates == true) {
            this.state1_DF2 = 0;
            this.state2_DF2 = 0;
        }

        double[] y0_outSig = new double[x_inSig.length];
        double b0 = b[0];
        double b1 = b[1];
        double b2 = b[2];
        double a0 = a[0];
        double a1 = a[1];
        double a2 = a[2];
        double state1 = this.state1_DF2;
        double state2 = this.state2_DF2;

        //filter the input signal
        for (int i = 0; i < x_inSig.length; i++) {

            y0_outSig[i] = (b0 * x_inSig[i] + state1);
            y0_outSig[i] = y0_outSig[i] / a0;
            state1 = b1 * x_inSig[i] - a1 * y0_outSig[i] + state2;
            state2 = b2 * x_inSig[i] - a2 * y0_outSig[i];

        }

        //store the filter states
        this.state1_DF2 = state1;
        this.state2_DF2 = state2;

        return y0_outSig;
    }
    
    public double[] iirFilterSosDf1(double[] b, double[] a, double[] x_inSig,
     boolean clearStates) {

        // implementation of a Direct Form I SOS IIR filter
        // y(n) = 1/a0 * ( b0 * x(n) + b1 * x(n-1) + b2 * x(n-2) - a1 * y(n-1)
        // - y(n-2) )

        if(clearStates == true) {
            this.state1_DF1 = 0;
            this.state2_DF1 = 0;
            this.state3_DF1 = 0;
            this.state4_DF1 = 0;
        }

        double[] y0_outSig = new double[x_inSig.length];
        double b0 = b[0];
        double b1 = b[1];
        double b2 = b[2];
        double a0 = a[0];
        double a1 = a[1];
        double a2 = a[2];
        double x1 = this.state1_DF1;
        double x2 = this.state2_DF1;
        double y1 = this.state3_DF1;
        double y2 = this.state4_DF1;


        //filter the input signal
        for (int i = 0; i < x_inSig.length; i++) {

            y0_outSig[i] = b0 * x_inSig[i] + b1 * x1 + b2 * x2
                    - a1 * y1 - a2 * y2;
            y0_outSig[i] = y0_outSig[i] / a0;

            x2 = x1;
            x1 = x_inSig[i];
            y2 = y1;
            y1 = y0_outSig[i];

        }

        //store the filter states
        this.state1_DF1 = x1;
        this.state2_DF1 = x2;
        this.state3_DF1 = y1;
        this.state4_DF1 = y2;

        return y0_outSig;
    }

}
