package com.fragtest.android.pa;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.provider.MediaStore;
import com.fragtest.android.pa.Core.LogIHAB;


import com.fragtest.android.pa.Core.AudioFileIO;
import com.fragtest.android.pa.DataTypes.INPUT_CONFIG;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ResourceBundle;


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
    private int source;

    AudioRecorder(Messenger _messenger, int _chunklengthInS, int _samplerate, boolean _isWave) {

        messenger = _messenger;
        isWave = _isWave;
        samplerate = _samplerate;

        chunklengthInBytes = (_chunklengthInS * _samplerate * CHANNELS * BITS / 8);

        bufferSize = AudioRecord.getMinBufferSize(_samplerate,
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
        /*audioRecord = new AudioRecord(
                MediaRecorder.AudioSource.DEFAULT,
                samplerate,
                AudioFormat.CHANNEL_IN_STEREO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize
        );*/
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

        audioRecord.stop();
        Message msg = Message.obtain(null, ControlService.MSG_RECORDING_STOPPED);
        try {
            messenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

}
