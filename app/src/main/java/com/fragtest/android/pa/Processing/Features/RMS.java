package com.fragtest.android.pa.Processing.Features;

import android.os.Messenger;

import com.fragtest.android.pa.Processing.BasicProcessRunnable;

public class RMS extends BasicProcessRunnable {

	public RMS(float[][] audioData, int procFrameSize, int nHop, int procOutFrameSize, int nFeatures, Messenger messenger) {
		super(audioData, procFrameSize, nHop, procOutFrameSize, nFeatures, messenger);
		setFeature( "RMS" );
	}

	@Override
	public void process(float[][] data, int iFrame) {
		super.process(data, iFrame);
		
		float[] result = new float[ data.length ];

		for ( int iChannel = 0; iChannel < data.length; iChannel++ ) { 

			result[iChannel] = function( data[iChannel] );
		
		}

		appendFeature( result );
		
	}
	
	protected float function(float[] data) {
		
		float fTemp = 0;
		float current;
		for( int kk = 0; kk < data.length; kk++) {
			current = data[ kk ];
			fTemp += current*current;
		}
		fTemp /= data.length;
		return (float) Math.sqrt(fTemp);
		
	}
}
