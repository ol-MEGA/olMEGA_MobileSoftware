
package com.fragtest.android.pa.Processing;

import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.fragtest.android.pa.Core.AudioFileIO;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * BasicProcessRunnable.java
 *
 * Passes cached audio data to the respective processing algorithms and writes the feature-data
 * to storage.
 */

public class BasicProcessRunnable implements Runnable {

	protected static final String LOG = "Processing";
	private static final String EXTENSION = ".feat";
	private static String feature = "basicProcess";// name of the output file (will be overwritten by children)

	private float[][] audioData;
	private float[][] frameData;
	protected Messenger messenger = null;           // instance of processHandler

	private AudioFileIO ioClass = null;
	private RandomAccessFile featureRAF = null;
    private File featureFile = null;
	private String timestamp;
	private int procFrameSize;
	private int procHopSize;
	private int nProcFrames;
	protected int samplingrate;
	protected int nFeatures;
	private float procHopDuration;
	private float[] procTimestamp;
	private int procFrameCount;
	private long posFrames;

	// added for when procFrameSize != procOutFrameSize,
	// i.e. when one feature output represents a smaller time interval then one input frame
	private int procOutFrameSize; // one feature output represents this many input samples
	private float procOutDuration;

	public BasicProcessRunnable(float[][] audioData, int procFrameSize, int procHopSize,
                                int procOutFrameSize, int nFeatures, Messenger messenger) {

		// TODO how to handle this parameter-mess properly?

		this.audioData = audioData;
		this.procFrameSize = procFrameSize;
		this.procHopSize = procHopSize;
		this.nFeatures = nFeatures;
		this.messenger = messenger;

        // TODO: while at it, check this, too!
        samplingrate = BasicProcessingThread.samplerate;
        timestamp = BasicProcessingThread.timestamp;

		ioClass = new AudioFileIO();

		nProcFrames = (int) Math.floor((audioData[0].length - procFrameSize) / procHopSize) + 1;
		frameData = new float[2][procFrameSize];                                // processing frame

		procTimestamp = new float[2];

		// some features return smaller frames then the input frame size (and therefore more than 1 per input frame; e.g. CPSD)
		this.procOutFrameSize = procOutFrameSize;
		this.procOutDuration = procOutFrameSize * samplingrate;
	}

	protected void setFeature(String s) {
		feature = s;
	}

	@Override
	public void run() {

		if (audioData[0].length >= procFrameSize) {

			// we need a global flag to indicate a new recording session started.. new: why?

			openFeatureFile();

			// calling processing runnables for each frame..

			for (int iFrame = 1; iFrame <= nProcFrames; iFrame++) {

				for (int kk = 0; kk < procFrameSize; kk++) {
					for (int ii = 0; ii < 2; ii++) {
						frameData[ii][kk] = audioData[ii][kk + (iFrame - 1) * procHopSize];
					}
				}

				process(frameData, iFrame);
			}

			closeFeatureFile();
		}

		// tell processThread we're finished
		Message msg = Message.obtain(null, BasicProcessingThread.DONE);
		Bundle b = new Bundle();
		b.putString("featureFile", featureFile.getAbsolutePath());
		msg.setData(b);
		msg.obj = feature;
		try {
			messenger.send(msg);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}


	// has to be implemented in each child process
	public void process(float[][] frameData, int blocknr) {
	}


	// feature file handling
	// TODO - separate class, overload appendFeature() 
	private void openFeatureFile() {

        File directory = Environment.getExternalStoragePublicDirectory(AudioFileIO.FEATURE_FOLDER);
        if( !directory.exists() ){
            directory.mkdir();
        }

		try {

            featureFile = new File(directory +
                    "/" + feature + "_" + timestamp + EXTENSION);
			featureRAF = new RandomAccessFile(featureFile,
                    "rw");

			posFrames = featureRAF.length();  	// starting position (frame count for each block)
			featureRAF.seek(posFrames);        // skip to starting position

			// TODO: consider writing the header only once (minus timestamp)  

			featureRAF.writeInt(0);                        // frame count will be written on close
			featureRAF.writeInt(nFeatures + 2);            // feature dimension count + timestamps (relative)
			if (procFrameSize == procOutFrameSize) {
				featureRAF.writeInt(procFrameSize);        // [samples]
				featureRAF.writeInt(procHopSize);          // [samples]
			}
			if (procFrameSize > procOutFrameSize) {
				featureRAF.writeInt(procOutFrameSize);     // [samples]
				featureRAF.writeInt(procOutFrameSize);     // [samples]
			}
			featureRAF.writeInt(samplingrate);

			featureRAF.writeBytes(timestamp);    // HHMMssSSS, 9 bytes (absolute timestamp)

			procFrameCount = 0;
			procTimestamp[0] = 0;

			if (procFrameSize == procOutFrameSize) {
				procHopDuration = (float) procHopSize / samplingrate;
				procTimestamp[1] = (float) procFrameSize / samplingrate;
			}
			if (procFrameSize > procOutFrameSize) {
				procHopDuration = (float) procOutFrameSize / samplingrate;
				procTimestamp[1] = (float) procOutFrameSize / samplingrate;
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected void appendFeature(float[] data) {

		try {

			featureRAF.writeFloat(procTimestamp[0]);
			featureRAF.writeFloat(procTimestamp[1]);

			for (int i = 0; i < data.length; i++) {
				featureRAF.writeFloat(data[i]);
			}

			procTimestamp[0] += procHopDuration;
			procTimestamp[1] += procHopDuration;
			procFrameCount += 1;

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// For cases where InFramesize != OutFramesize
	protected void appendFeature(float[][] data) {

		try {
			procOutDuration = (float) procOutFrameSize / samplingrate;

			for (int i = 0; i < data.length; i++) {
				featureRAF.writeFloat(procTimestamp[0]);
				featureRAF.writeFloat(procTimestamp[1]);
				for (int j = 0; j < data[0].length; j++) {
					featureRAF.writeFloat(data[i][j]);
				}
				procTimestamp[0] += procOutDuration;
				procTimestamp[1] += procOutDuration;
				procFrameCount += 1;
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	private void closeFeatureFile() {
		try {

			featureRAF.seek(posFrames);
			featureRAF.writeInt(procFrameCount);
			featureRAF.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
