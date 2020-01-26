package com.fragtest.android.pa.InputProfile;

import android.bluetooth.BluetoothAdapter;
import android.content.SharedPreferences;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;

import com.fragtest.android.pa.ControlService;
import com.fragtest.android.pa.Core.AudioFileIO;
import com.fragtest.android.pa.Core.LogIHAB;
import com.fragtest.android.pa.Core.RingBuffer;
import com.fragtest.android.pa.library.BluetoothSPP;
import com.fragtest.android.pa.library.BluetoothState;

import java.io.DataOutputStream;
import java.util.Arrays;

public class InputProfile_PHANTOM implements InputProfile {

    private String LOG = "InputProfile_PHANTOM";
    private final String INPUT_PROFILE = "PHANTOM";

    private BluetoothSPP bt;

    byte checksum = 0;

    private boolean run = false;

    private boolean isAudioRecorderRunning = false;
    private boolean isAdaptiveBitshift = false;

    private ControlService mContext;
    private Messenger mServiceMessenger;

    private BluetoothAdapter mBluetoothAdapter;

    private static final int block_size = 64;
    private static final int RECORDER_SAMPLERATE = 16000;
    private int AudioBufferSize = block_size * 4;
    private RingBuffer ringBuffer = new RingBuffer(AudioBufferSize * 4);
    private byte[] AudioBlock = new byte[AudioBufferSize];

    private int BufferElements2Rec = 2048; // block_size * 16; // want to play 2048 (2K) since 2 bytes we use only 1024
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_STEREO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private AudioTrack audioTrack = null;

    private int AudioVolume = 0;
    private long lostBlockCount = 0, completeBlockCount = 0, countBlocks = 0, corruptBlocks = 0;
    private int countSamples = 0, BlockCount, transmittedCheckSum, ProtocolVersion, additionalBytesCount = 0;

    private int mChunklengthInS, chunklengthInBytes, nBlockCount, chunklengthInSamples;
    private static int N_BITS = 16;
    private boolean mIsWave;

    DataOutputStream outputStream;
    AudioFileIO fileIO;

    private int nSamples;

    private Runnable stopRunnable = new Runnable() {
        @Override
        public void run() {
            run = false;
            bt.stopService();
            mContext.getVibration().singleBurst();
        }
    };


    public InputProfile_PHANTOM(ControlService context, Messenger serviceMessenger) {
        this.mContext = context;
        this.mServiceMessenger = serviceMessenger;
        this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    @Override
    public String getInputProfile() {
        return this.INPUT_PROFILE;
    }

    @Override
    public void setInterface() {

        Log.e(LOG, "Requested setInterface()");
        LogIHAB.log(LOG);

        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
        }
        mBluetoothAdapter.cancelDiscovery();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.mContext);
        mChunklengthInS = Integer.parseInt(sharedPreferences.getString("chunklengthInS", "60"));
        mIsWave = sharedPreferences.getBoolean("isWave", true);

        chunklengthInBytes = (mChunklengthInS * RECORDER_SAMPLERATE * RECORDER_CHANNELS * N_BITS / 8);
        chunklengthInSamples = RECORDER_SAMPLERATE * mChunklengthInS * RECORDER_CHANNELS;

        initBluetooth();
        initAudioTrack();

    }

    @Override
    public void cleanUp() {

        Log.e(LOG, "Cleaning up.");
        stopRecording();
        finishFile();
        bt = null;

    }

    @Override
    public void setDevice(String sDeviceName) {
    }

    private void initBluetooth()
    {
        bt = new BluetoothSPP(mContext);

        if (bt.isBluetoothEnabled()) {

            bt.setBluetoothStateListener(new BluetoothSPP.BluetoothStateListener() {
                public void onServiceStateChanged(int state) {

                    PowerManager powerManager = (PowerManager) mContext.getSystemService(mContext.POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyApp::MyWakelockTag");

                    Log.e(LOG, "Bluetooth State changed: STATE_CONNECTED");
                    if (state == BluetoothState.STATE_CONNECTED) {
                        if (!ControlService.getIsCharging()){
                            startRecording();
                        } else {
                            stopRecording();
                        }
                    } else if (state == BluetoothState.STATE_CONNECTING) {
                        Log.e(LOG, "Bluetooth State changed: STATE_CONNECTING");
                    } else if (state == BluetoothState.STATE_LISTEN) {

                        Log.e(LOG, "Bluetooth State changed: STATE_LISTEN");

                        if (wakeLock.isHeld()) {
                            wakeLock.release();
                        }
                    } else if (state == BluetoothState.STATE_NONE) {

                        Log.e(LOG, "Bluetooth State changed: STATE_NONE");

                        if (wakeLock.isHeld()) {
                            wakeLock.release();
                        }
                        //stopRecording();
                    } else
                    {
                        if (wakeLock.isHeld()) wakeLock.release();
                    }
                }
            });

            bt.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() {
                public void onDataReceived(byte[] data) {

                    isAudioRecorderRunning = true;

                    while (isAudioRecorderRunning && run) {

                        //int DataBlockSize, lastBlockCount;
                        for (int i = 0; i < data.length; i++) {
                            ringBuffer.addByte(data[i]);
                            countSamples++;
                            //nSamples++;
                            if (additionalBytesCount != 12)
                                Log.e(LOG, additionalBytesCount + "");

                            if (ringBuffer.getByte(-2) == (byte) 0x00 && ringBuffer.getByte(-3) == (byte) 0x80) {
                                switch (((ringBuffer.getByte(-4) & 0xFF) << 8) | (ringBuffer.getByte(-5) & 0xFF)) { // Check Protocol-Version
                                    case 1:
                                        additionalBytesCount = 12;
                                        break;
                                }
                                if (ringBuffer.getByte(2 - (AudioBufferSize + additionalBytesCount)) == (byte) 0xFF && ringBuffer.getByte(1 - (AudioBufferSize + additionalBytesCount)) == (byte) 0x7F) {
                                    if (ringBuffer.getByte(0) == checksum) {
                                        long tmpBlockCount = BlockCount;
                                        AudioBlock = Arrays.copyOf(ringBuffer.data(3 - (AudioBufferSize + additionalBytesCount), AudioBufferSize), AudioBufferSize);

                                        writeData(AudioBlock);

                                        AudioVolume = (short)(((ringBuffer.getByte(-8) & 0xFF) << 8) | (ringBuffer.getByte(-9) & 0xFF));
                                        BlockCount = ((ringBuffer.getByte(-6) & 0xFF) << 8) | (ringBuffer.getByte(-7) & 0xFF);
                                        if (tmpBlockCount < BlockCount) {
                                            lostBlockCount += BlockCount - (tmpBlockCount + 1);
                                            completeBlockCount += BlockCount - tmpBlockCount;
                                            if (BlockCount - (tmpBlockCount + 1) > 0)
                                                Log.e(LOG, tmpBlockCount + " - " + BlockCount + " = " + (BlockCount - (tmpBlockCount + 1)) + "\t" + completeBlockCount);
                                        }
                                    }
                                    countSamples = 0;
                                    checksum = data[i];
                                }
                            }
                            else if (additionalBytesCount > 0 && countSamples == AudioBufferSize + additionalBytesCount) {
                                countSamples = 0;
                                corruptBlocks++;
                            }
                            checksum ^= data[i];

                            if (countSamples == 0)
                            {
                                countBlocks++;
                                if (audioTrack != null)
                                    audioTrack.write(AudioBlock, 0, AudioBufferSize);
                                /*runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        //((TextView) findViewById(R.id.textViewVolume)).setText(String.format("%d", AudioVolume));
                                        //((TextView) findViewById(R.id.textCorruptValue)).setText(String.format("%.3f", ((double) corruptBlocks / (double) countBlocks) * 100.0));
                                        //((TextView) findViewById(R.id.textLostValue)).setText(String.format("%.3f", ((double) lostBlockCount / (double) completeBlockCount) * 100.0));
                                        //((TextView) findViewById(R.id.textCorruptRealNumbersLabel)).setText(String.format("%d", corruptBlocks));
                                        //((TextView) findViewById(R.id.textLostRealNumbersLabel)).setText(String.format("%d", lostBlockCount));
                                    }
                                });*/
                            }
                        }


                    }


                }
            });
            bt.setupService();
            bt.startService(BluetoothState.DEVICE_OTHER);

        }
        else {
            isAudioRecorderRunning = false;
            bt.enable();
            initBluetooth();
        }
    }

    private void initAudioTrack() {
        if (audioTrack == null) {
            audioTrack = new AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    RECORDER_SAMPLERATE,
                    RECORDER_CHANNELS,
                    RECORDER_AUDIO_ENCODING,
                    BufferElements2Rec,
                    AudioTrack.MODE_STREAM);
            audioTrack.play();
        }
    }

    private void finishFile() {

        try {
            outputStream.flush();
        } catch (Exception e) {}

        String filename = fileIO.filename;
        fileIO.closeDataOutStream();

        // report back to service
        Message msg = Message.obtain(null, ControlService.MSG_CHUNK_RECORDED);
        if (msg != null) {
            Bundle dataSend = new Bundle();
            dataSend.putString("filename", filename);
            msg.setData(dataSend);
            try {
                mServiceMessenger.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        ControlService.setIsRecording(false);
    }

    private void writeData(byte[] data) {

        nBlockCount++;

        if (nSamples == 0) {
            outputStream = fileIO.openDataOutStream(
                    RECORDER_SAMPLERATE,
                    RECORDER_CHANNELS,
                    RECORDER_AUDIO_ENCODING,
                    mIsWave
            );
        }

        nSamples += block_size * RECORDER_CHANNELS;

        if (nSamples < chunklengthInSamples) {
            try {
                //outputStream.write(data);
                for (int iSample = 0; iSample < data.length-1; iSample += 2) {

                    short int16 = (short) (((data[iSample + 1] & 0xFF) << 8) | (data[iSample] & 0xFF));

                    outputStream.write(int16);
                }
                outputStream.flush();
            } catch (Exception e) {e.printStackTrace();}

        } else {

            nSamples = 0;
            nBlockCount = 0;

            Log.e(LOG, "SUPER: Samples: " + nSamples);
            Log.e(LOG, "SUPER: Blocks: " + nBlockCount);
            Log.e(LOG, "SUPER: New chunk");
            Log.e(LOG, "SUPER: ChunkLengthInBytes: " + chunklengthInBytes);

            String filename = fileIO.filename;
            fileIO.closeDataOutStream();

            // report back to service
            Message msg = Message.obtain(null, ControlService.MSG_CHUNK_RECORDED);
            if (msg != null) {
                Bundle dataSend = new Bundle();
                dataSend.putString("filename", filename);
                msg.setData(dataSend);
                try {
                    mServiceMessenger.send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            /*outputStream = fileIO.openDataOutStream(
                    RECORDER_SAMPLERATE,
                    RECORDER_CHANNELS,
                    RECORDER_AUDIO_ENCODING,
                    mIsWave
            );*/

        }



    }

    /*private void setVolume()
    {
        AudioVolume = Math.max(Math.min(AudioVolume, 9), -9);
        byte[] bytes = Charset.forName("UTF-8").encode(CharBuffer.wrap("V+" + AudioVolume)).array();
        if (AudioVolume < 0)
            bytes = Charset.forName("UTF-8").encode(CharBuffer.wrap("V" + AudioVolume)).array();
        bt.send(bytes, false);
    }

    private void setAdaptiveBitShift()
    {
        byte[] bytes = Charset.forName("UTF-8").encode(CharBuffer.wrap("B0")).array();
        if (isAdaptiveBitshift)
            bytes = Charset.forName("UTF-8").encode(CharBuffer.wrap("B1")).array();
        bt.send(bytes, false);
    }*/


    @Override
    public boolean getIsAudioRecorderClosed() {
        Log.e(LOG, "CLOSED: " + !run);
        return !run;
    }

    @Override
    public void registerClient() {
        Log.e(LOG, "Client Registered");

    }

    @Override
    public void unregisterClient() {
        Log.e(LOG, "Client Unregistered");

        cleanUp();
    }

    @Override
    public void batteryCritical() {
        cleanUp();
    }

    @Override
    public void chargingOff() {
        Log.e(LOG, "CharginOff");
        startRecording();
    }

    @Override
    public void chargingOn() {
        Log.e(LOG, "CharginOn");
        cleanUp();
    }

    @Override
    public void chargingOnPre() {
        Log.e(LOG, "CharginOnPre");
        cleanUp();
    }

    @Override
    public void onDestroy() {
        cleanUp();
    }


    @Override
    public void applicationShutdown() {
        cleanUp();
    }

    private void startRecording() {

        Log.e(LOG, "Requested start recording");

        BlockCount = 0;
        completeBlockCount = 0;
        lostBlockCount = 0;
        countSamples = 0;
        checksum = 0;
        countBlocks = 0;
        corruptBlocks = 0;
        additionalBytesCount = 0;
        nSamples = 0;

        run = true;
        fileIO = new AudioFileIO();
        /*outputStream = fileIO.openDataOutStream(
                RECORDER_SAMPLERATE,
                RECORDER_CHANNELS,
                RECORDER_AUDIO_ENCODING,
                mIsWave
        );*/

        //initBluetooth();
        //initAudioTrack();

        ControlService.setIsRecording(true);
        mContext.messageClient(ControlService.MSG_START_RECORDING);
        mContext.getVibration().singleBurst();

    }

    private void stopRecording() {

        Log.e(LOG, "Requested stop recording");

        Handler handler = new Handler();
        handler.post(stopRunnable);

        if (fileIO != null && isAudioRecorderRunning) {
            String filename = fileIO.filename;
            fileIO.closeDataOutStream();

            // report back to service
            Message msg = Message.obtain(null, ControlService.MSG_CHUNK_RECORDED);
            if (msg != null) {
                Bundle dataSend = new Bundle();
                dataSend.putString("filename", filename);
                msg.setData(dataSend);
                try {
                    mServiceMessenger.send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        //audioTrack.stop();
        //audioTrack.release();

    }


}
