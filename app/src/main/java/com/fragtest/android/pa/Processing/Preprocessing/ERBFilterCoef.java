package com.fragtest.android.pa.Processing.Preprocessing;
import java.io.*;
import java.math.BigInteger;

/**
 * Created by michael on 11.03.17.
 */
public class ERBFilterCoef {

    public double[] A0 = new double[23];
    public double[] A11 = new double[23];
    public double[] A12 = new double[23];
    public double[] A13 = new double[23];
    public double[] A14 = new double[23];
    public double[] A2 = new double[23];
    public double[] B0 = new double[23];
    public double[] B1 = new double[23];
    public double[] B2 = new double[23];
    public double[] gain = new double[23];


    public ERBFilterCoef(){


    }

    public void readCoef(long fs){

        //read ERB filter coefficients in hex format from txt file and convert
        // in to double values. Coefficients only available for 8 or 16 kHz!

        int i =0;

        FileReader fr = null;

        try {

            if(fs==8000) {
                fr = new FileReader("erb_coeff_fs8k.txt");
            }
            else if(fs==16000){
                fr = new FileReader("erb_coeff_fs16k.txt");
            }
            else{
                System.err.println("wrong fs!, default 8kHz");
                fr = new FileReader("erb_coeff_fs8k.txt");
            }

            BufferedReader br = new BufferedReader(fr);
            String line = null;


            while((line = br.readLine()) != null){
                String[] values = line.split(",");

                if(i==0) {

                    for (int j = 0; j < values.length; j++) {
                        A0[j] = Double.longBitsToDouble(new BigInteger(
                                values[j], 16).longValue());
                    }
                }

                if(i==1) {

                    for (int j = 0; j < values.length; j++) {
                        A11[j] = Double.longBitsToDouble(new BigInteger
                                (values[j], 16).longValue());
                    }
                }

                if(i==2) {

                    for (int j = 0; j < values.length; j++) {
                        A12[j] = Double.longBitsToDouble(new BigInteger
                                (values[j], 16).longValue());
                    }
                }

                if(i==3) {

                    for (int j = 0; j < values.length; j++) {
                        A13[j] = Double.longBitsToDouble(new BigInteger
                                (values[j], 16).longValue());
                    }
                }

                if(i==4) {

                    for (int j = 0; j < values.length; j++) {
                        A14[j] = Double.longBitsToDouble(new BigInteger
                                (values[j], 16).longValue());
                    }
                }

                if(i==5) {

                    for (int j = 0; j < values.length; j++) {
                        A2[j] = Double.longBitsToDouble(new BigInteger
                                (values[j], 16).longValue());
                    }
                }

                if(i==6) {

                    for (int j = 0; j < values.length; j++) {
                        B0[j] = Double.longBitsToDouble(new BigInteger
                                (values[j], 16).longValue());
                    }
                }

                if(i==7) {

                    for (int j = 0; j < values.length; j++) {
                        B1[j] = Double.longBitsToDouble(new BigInteger
                                (values[j], 16).longValue());
                    }
                }

                if(i==8) {

                    for (int j = 0; j < values.length; j++) {
                        B2[j] = Double.longBitsToDouble(new BigInteger
                                (values[j], 16).longValue());
                    }
                }

                if(i==9) {

                    for (int j = 0; j < values.length; j++) {
                        gain[j] = Double.longBitsToDouble(new BigInteger
                                (values[j], 16).longValue());
                    }
                }

                i++;
            }

            br.close();
            fr.close();


        } catch (Exception e) {

            System.out.println(e);
        }

    }


}
