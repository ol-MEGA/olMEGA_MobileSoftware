package com.fragtest.android.pa.Processing;

import android.os.Bundle;
import android.os.Messenger;

import com.fragtest.android.pa.Processing.Features.Loop;
import com.fragtest.android.pa.Processing.Features.PSD;
import com.fragtest.android.pa.Processing.Features.RMS;
import com.fragtest.android.pa.Processing.Features.ZCR;

/**
 * MainProcessingThread overloads mainRoutine() in BasicProcessingThread to start runnables
 * corresponding to selected features.
 *
 * Implement new features:
 * 
 * 1) create a new class by extending BasicProcessRunnable (use e.g. RMS.java as template)
 * 2) configure, instantiate and start the runnable class here in MainProcessingThread.java (see below)
 * 3) add the feature to res/values/features.xml
 * 
 * Note: 	The names of the class file (w/o java suffix), the string in checkForProcess() 
 * 			and the name in features.xml must match.    
 * 
 * */

public class MainProcessingThread extends BasicProcessingThread{

	public MainProcessingThread(Messenger messenger, Bundle b) {
		super(messenger, b);
	}

	@Override
	public void mainRoutine() {
		super.mainRoutine();

		int nProcSamples, nHop, nFeatures, nProcOutSamples;
		double nProcSeconds, nOutSeconds;

		if (isActiveFeature("PSD")) {
		
			// CPSD takes nProcSeconds of audio data, recursively computes and averages
			// the cross and auto power spectral densities every 25 ms and returns a
			// matrix where every row is one average after an nOutSeconds increment.
			// one row contains the cross power spectral density (complex-valued)
			// followed by the auto power spectral densities (real-valued)
			nProcSeconds = blocklengthInS;
			nOutSeconds = 0.125; // must be a multiple of 0.025 s

			int nFrameSize = (int) (0.025 * samplerate);
			
			nProcSamples = (int) (nProcSeconds * samplerate);
			nProcOutSamples = (int) (nOutSeconds * samplerate);
			
			nHop = nProcSamples;
            // next power of two;
			int NFFT = 1 << (32 - Integer.numberOfLeadingZeros(nFrameSize - 1));
			nFeatures = 2 * (NFFT + 2); 			
			PSD cpsdRunnable = new PSD(audioData, nProcSamples, nHop, nProcOutSamples,
                    nFeatures, processMessenger);

			cpsdRunnable.run();
		}
		
		if (isActiveFeature("RMS")) {
			nProcSamples = (int) (0.025f * samplerate); // 25ms
			nHop = nProcSamples / 2;
			nProcOutSamples = nProcSamples;		
			nFeatures = 2; // [left right]
			RMS rmsRunnable = new RMS(audioData, nProcSamples, nHop, nProcOutSamples,
                    nFeatures, processMessenger);

			rmsRunnable.run();
		}

		if (isActiveFeature("ZCR")) {
			nProcSamples = (int) (0.025f * samplerate); // 25ms
			nHop = nProcSamples / 2;
			nProcOutSamples = nProcSamples;		
			nFeatures = 4; // [left right delta_left delta_right] 
			ZCR zcRunnable = new ZCR(audioData, nProcSamples, nHop, nProcOutSamples,
                    nFeatures, processMessenger);

			zcRunnable.run();
		}				
	
		if (isActiveFeature("Loop")) {
			nProcSeconds = 5;
			nProcSamples = (int) (nProcSeconds * samplerate);
			nHop = nProcSamples;
			nProcOutSamples = nProcSamples;
			nFeatures = nProcSamples * 2;
			Loop loopRunnable = new Loop(audioData, nProcSamples, nHop, nProcOutSamples,
                    nFeatures, processMessenger);

			loopRunnable.run();
		}

	}
}
