package com.fragtest.android.pa;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import com.fragtest.android.pa.Core.AudioFileIO;


import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Record audio using Android's AudioRecorder
 */

public class AudioRecorder {

    public final static int CHANNELS = 2;
    public final static int BITS = 16;
    public final static int BLOCKLENGTH = 5000; // in ms

    final static String LOG = "AudioRecorder";

    private AudioRecord audioRecord;
    private Thread recordingThread;
    private boolean stopRecording = true;
    private int blocklengthInBytes, bufferSize;
    private Messenger messenger;


    AudioRecorder(Messenger _messenger, int samplerate) {

        messenger = _messenger;

        blocklengthInBytes = (int) (BLOCKLENGTH / 1000.0 * samplerate * CHANNELS * BITS / 8);

        bufferSize = AudioRecord.getMinBufferSize(samplerate,
                AudioFormat.CHANNEL_IN_STEREO,
                AudioFormat.ENCODING_PCM_16BIT
        );

        audioRecord = new AudioRecord(
                MediaRecorder.AudioSource.DEFAULT,
                samplerate,
                AudioFormat.CHANNEL_IN_STEREO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize
        );

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


    public void stop() {

        stopRecording = true;
    }


    public void close() {

        audioRecord.release();
    }


    private void recordAudio() {

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
                    false);

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

            while (bytesWritten < blocklengthInBytes) {

                int bytesRead = audioRecord.read(buffer, 0, bufferSize);

                // check for
                if (((bytesWritten + bytesRead) > blocklengthInBytes)) {
                    bytesToWrite = blocklengthInBytes - bytesWritten;
                    bytesRemaining = bytesWritten - blocklengthInBytes;
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

            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Message msg = Message.obtain(null, ControlService.MSG_BLOCK_RECORDED);
            try {
                messenger.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

        }

        audioRecord.stop();

    }

}
