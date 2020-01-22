package com.fragtest.android.pa.InputProfile;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.os.Messenger;
import android.util.Log;

import com.fragtest.android.pa.ControlService;
import com.fragtest.android.pa.Core.LogIHAB;
import com.fragtest.android.pa.Core.RingBuffer;
import com.fragtest.android.pa.library.BluetoothSPP;
import com.fragtest.android.pa.library.BluetoothState;

import java.util.Arrays;
import java.util.Set;

public class InputProfile_PHANTOM implements InputProfile {

    private String LOG = "InputProfile_PHANTOM";
    private final String INPUT_PROFILE = "PHANTOM";

    private BluetoothSPP bt;

    private boolean isAudioRecorderRunning = false;
    private boolean isAdaptiveBitshift = false;

    private ControlService mContext;
    private Messenger mServiceMessenger;

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


    public InputProfile_PHANTOM(ControlService context, Messenger serviceMessenger) {
        this.mContext = context;
        this.mServiceMessenger = serviceMessenger;
        //this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    @Override
    public String getInputProfile() {
        return this.INPUT_PROFILE;
    }

    @Override
    public void setInterface() {

        LogIHAB.log(LOG);


        Log.e(LOG, "Requested setInterface()");
        initBluetooth();

    }

    @Override
    public void cleanUp() {

        Log.e(LOG, "Cleaning up.");

    }

    @Override
    public void setDevice(String sDeviceName) {

        // Get the local Bluetooth adapter
        BluetoothAdapter mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        // Get a set of currently paired devices
        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                //mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                if (device.getName().equals(sDeviceName)) {

                    Log.e(LOG, "Connecting to: " + device.getName() + ", Address: " + device.getAddress());
                } else {
                    Log.e(LOG, "NO DEVICES FOUND.");//lse {
                }
            }
        }
    }

    private void initBluetooth()
    {
        bt = new BluetoothSPP(this.mContext);
        if (bt.isBluetoothEnabled() == true) {
            bt.setBluetoothStateListener(new BluetoothSPP.BluetoothStateListener() {
                public void onServiceStateChanged(int state) {
                    if (state == BluetoothState.STATE_CONNECTED) {
                        BlockCount = 0;
                        Log.d("_IHA_", "Bluetooth Connected");
                    } else if (state == BluetoothState.STATE_CONNECTING) {
                        Log.e(LOG, "Bluetooth State changed: STATE_CONNECTING");
                    } else if (state == BluetoothState.STATE_LISTEN)
                    {
                        Log.e(LOG, "Bluetooth State changed: STATE_LISTEN");
                    }
                    else if (state == BluetoothState.STATE_NONE)
                        Log.e(LOG, "Bluetooth State changed: STATE_NONE"); }

            });
            bt.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() {
                public void onDataReceived(byte[] data) {

                    isAudioRecorderRunning = true;

                    int DataBlockSize, lastBlockCount;
                    for (int i = 0; i < data.length; i++)
                    {
                        ringBuffer.addByte(data[i]);
                        countSamples++;
                        if (ringBuffer.getByte(0) == (byte) 0x00 && ringBuffer.getByte(-1) == (byte) 0x80) {
                            ProtocolVersion = ((ringBuffer.getByte(-4) & 0xFF) << 8) | (ringBuffer.getByte(-5) & 0xFF);
                            if (ProtocolVersion == 1)
                                additionalBytesCount = 12;
                        }
                        DataBlockSize = AudioBufferSize + additionalBytesCount;
                        if (ringBuffer.getByte(2 - DataBlockSize) == (byte) 0xFF && ringBuffer.getByte(1 - DataBlockSize) == (byte) 0x7F && ringBuffer.getByte(0) == (byte) 0x00 && ringBuffer.getByte(-1) == (byte) 0x80) {
                            countSamples = 0;
                            lastBlockCount = BlockCount;
                            AudioBlock = Arrays.copyOf(ringBuffer.data(3 - DataBlockSize, AudioBufferSize), AudioBufferSize);
                            AudioVolume = ((ringBuffer.getByte(-8) & 0xFF) << 8) | (ringBuffer.getByte(-9) & 0xFF);
                            BlockCount = ((ringBuffer.getByte(-6) & 0xFF) << 8) | (ringBuffer.getByte(-7) & 0xFF);
                            transmittedCheckSum = ((ringBuffer.getByte(-2) & 0xFF) << 8) | (ringBuffer.getByte(-3) & 0xFF);
                            if (lastBlockCount < BlockCount) {
                                lostBlockCount += BlockCount - (lastBlockCount + 1);
                                completeBlockCount += BlockCount - lastBlockCount;
                            }
                        } else if (countSamples == AudioBufferSize + additionalBytesCount) {
                            countSamples = 0;
                            corruptBlocks++;
                        }
                        if (countSamples == 0)
                        {
                            if (audioTrack != null)
                                audioTrack.write(AudioBlock, 0, AudioBufferSize);
                            countBlocks++;
                        }
                    }
                }
            });
            bt.setupService();
            bt.startService(BluetoothState.DEVICE_OTHER);
        }
        else{
            isAudioRecorderRunning = false;
            bt.enable();
            initBluetooth();
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
        return isAudioRecorderRunning;
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

    }

    @Override
    public void chargingOn() {
        Log.e(LOG, "CharginOn");

    }

    @Override
    public void chargingOnPre() {
        Log.e(LOG, "CharginOnPre");
    }

    @Override
    public void onDestroy() {
        cleanUp();
    }


    @Override
    public void applicationShutdown() {

    }

    private void startRecording() {

        Log.e(LOG, "Requested start recording");

    }

    private void stopRecording() {

        Log.e(LOG, "Requested stop recording");

    }


}
