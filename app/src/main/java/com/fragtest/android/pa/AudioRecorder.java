package com.fragtest.android.pa;

import android.media.AudioDeviceInfo;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.fragtest.android.pa.Core.AudioFileIO;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Record audio using Android's AudioRecorder
 */

public class AudioRecorder {

    private final static int CHANNELS = 2;
    private final static int BITS = 16;

    final static String LOG = "AudioRecorder";

    private AudioRecord audioRecord;
    private Thread recordingThread;
    private boolean stopRecording = true;
    private boolean isWave;
    private int chunklengthInBytes, bufferSize;
    private Messenger messenger;
    private int samplerate;
    private ControlService mContext;


    public AudioRecorder(ControlService context, Messenger _messenger, int _chunklengthInS, int _samplerate, boolean _isWave) {

        this.mContext = context;
        this.messenger = _messenger;
        this.isWave = _isWave;
        this.samplerate = _samplerate;

        chunklengthInBytes = (_chunklengthInS * _samplerate * CHANNELS * BITS / 8);

        bufferSize = AudioRecord.getMinBufferSize(_samplerate,
                AudioFormat.CHANNEL_IN_STEREO,
                AudioFormat.ENCODING_PCM_16BIT
        );

        Log.e(LOG, "Creating new with values: " + chunklengthInBytes + ", " + samplerate + ", " + bufferSize);

        audioRecord = new AudioRecord(
                MediaRecorder.AudioSource.VOICE_RECOGNITION,
                samplerate,
                AudioFormat.CHANNEL_IN_STEREO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize
        );

        Log.e(LOG, "Done.");


        recordingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                recordAudio();
            }
        }, "AudioRecorder Thread");
    }


    public void start() {
        stopRecording = false;
        recordingThread.start();
    }

    public int getRecordingState() {
        return audioRecord.getRecordingState();
    }

    public void setPreferredDevice(AudioDeviceInfo device) {
        audioRecord.setPreferredDevice(device);
        Log.e(LOG, "Recording device set through AudioRecord: " + audioRecord.getPreferredDevice());
    }

    public void stop() {
        stopRecording = true;
        audioRecord.stop();
    }

    public void release() {
        audioRecord.release();
        Log.e(LOG, "AudioRecorder released");
    }

    private void recordAudio() {

        Log.e(LOG, "Starting to record.");

        ControlService.setIsRecording(true);
        mContext.messageClient(ControlService.MSG_START_RECORDING);
        mContext.getVibration().singleBurst();

        audioRecord.startRecording();

        byte[] buffer = new byte[bufferSize];
        int bytesToWrite = 0, bytesRemaining = 0;

        // recording loop
        while (!stopRecording &&
                (audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING)) {

            // get stream to write audio data to flash memory
            AudioFileIO fileIO = new AudioFileIO();
            DataOutputStream outputStream = fileIO.openDataOutStream(
                    audioRecord.getSampleRate(),
                    audioRecord.getChannelCount(),
                    audioRecord.getAudioFormat(),
                    isWave
            );

            // write remaining data from last block
            if (bytesRemaining > 0) {
                try {
                    outputStream.write(buffer, bytesToWrite, bytesRemaining);
                    bytesRemaining = 0;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // block loop
            int bytesWritten = 0;

            while (bytesWritten < chunklengthInBytes && !stopRecording) {

                int bytesRead = audioRecord.read(buffer, 0, bufferSize);

                if (bytesRead > 0) {

                    // check for
                    if (((bytesWritten + bytesRead) > chunklengthInBytes)) {
                        bytesToWrite = chunklengthInBytes - bytesWritten;
                        bytesRemaining = bytesWritten - chunklengthInBytes;
                    } else {
                        bytesToWrite = bytesRead;
                    }

                    try {
                        outputStream.write(buffer, 0, bytesToWrite);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    // total bytes written in this block
                    bytesWritten += bytesToWrite;
                }
            }

            String filename = fileIO.filename;
            fileIO.closeDataOutStream();

            // report back to service
            Message msg = Message.obtain(null, ControlService.MSG_CHUNK_RECORDED);
            if (msg != null) {
                Bundle data = new Bundle();
                data.putString("filename", filename);
                msg.setData(data);
                try {
                    messenger.send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        mContext.messageClient(ControlService.MSG_STOP_RECORDING);
        mContext.messageService(ControlService.MSG_RECORDING_STOPPED);
        ControlService.setIsRecording(false);

        Log.e(LOG, "Recording stopped.");
        mContext.getVibration().singleBurst();
    }

}
