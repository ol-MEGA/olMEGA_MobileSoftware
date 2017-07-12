package com.fragtest.android.pa;

/**
 * Record audio using Android's AudioRecorder
 */

public class AudioRecorder {

    private Thread recordingThread;

    AudioRecorder() {


        recordingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                recordAudio();
            }
        }, "AudioRecorder Thread");

    }

    public void start() {

    }

    public void stop() {

    }

    public void close() {

    }

    public void recordAudio() {

    }

}
