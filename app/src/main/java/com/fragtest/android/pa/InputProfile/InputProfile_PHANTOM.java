package com.fragtest.android.pa.InputProfile;

import android.bluetooth.BluetoothAdapter;
import android.content.SharedPreferences;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
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
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.TimerTask;

public class InputProfile_PHANTOM implements InputProfile {

    private String LOG = "InputProfile_PHANTOM";
    private final String INPUT_PROFILE = "PHANTOM";

    private BluetoothSPP bt;

    byte checksum = 0;

    private boolean run = false;

    private boolean isAudioRecorderRunning = false;
    private boolean isAdaptiveBitshift = false;
    private boolean IsBluetoothConnectionPingNecessary = false;


    private ConnectedThread mConnectedThread;

    private ControlService mContext;
    private Messenger mServiceMessenger;

    private BluetoothAdapter mBluetoothAdapter;

    private static final int block_size = 64;
    private static final int RECORDER_SAMPLERATE = 16000;
    private int AudioBufferSize = block_size * 4;
    private RingBuffer ringBuffer = new RingBuffer(AudioBufferSize * 4);
    private byte[] AudioBlock = new byte[AudioBufferSize];

    private int BufferElements2Rec = 2048; // block_size * 16; // want to play 2048 (2K) since 2 bytes we use only 1024
    private static final int RECORDER_CHANNELS = 2; //AudioFormat.CHANNEL_IN_STEREO;
    private static final int PLAYBACK_CHANNELS = AudioFormat.CHANNEL_IN_STEREO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private AudioTrack audioTrack = null;

    private int AudioVolume = 0;
    private long lostBlockCount = 0, completeBlockCount = 0, countBlocks = 0, corruptBlocks = 0;
    private int countSamples = 0, BlockCount, transmittedCheckSum, ProtocolVersion, additionalBytesCount = 0;

    private int mChunklengthInS, chunklengthInBytes, nBlockCount, chunklengthInSamples;
    private static int N_BITS = 16;
    private boolean mIsWave;

    private byte[] samplesToWriteLater;
    private int writeLater;

    DataOutputStream outputStream;
    AudioFileIO fileIO;

    private int nSamples;

    public InputProfile_PHANTOM(ControlService context, Messenger serviceMessenger) {

        Log.d(LOG, "InputProfile_PHANTOM()");

        this.mContext = context;
        this.mServiceMessenger = serviceMessenger;
        this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        fileIO = new AudioFileIO();
        initBluetooth();
        //initAudioTrack();
    }

    @Override
    public String getInputProfile() {
        return this.INPUT_PROFILE;
    }

    @Override
    public void setState(String inputProfile) {

    }

    @Override
    public void setInterface() {

        Log.d(LOG, "setInterface()");
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

        if (bt.getServiceState() == 0){
            bt.startService(BluetoothState.DEVICE_OTHER);
        }

    }

    @Override
    public void cleanUp() {

        Log.d(LOG, "cleanUp()");
        stopRecording();
        try {
            bt.stopService();
            //bt.disconnect();
            //mConnectedThread = null;
        } catch(Exception e) {}
    }

    @Override
    public void setDevice(String sDeviceName) {
    }

    private void initBluetooth()
    {
        Log.d(LOG, "initBluetooth()");

        bt = new BluetoothSPP(mContext);
        if (bt.isBluetoothEnabled()) {

            bt.setBluetoothStateListener(new BluetoothSPP.BluetoothStateListener() {
                public void onServiceStateChanged(int state) {
                    PowerManager powerManager = (PowerManager) mContext.getSystemService(mContext.POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyApp::MyWakelockTag");
                    if (state == BluetoothState.STATE_CONNECTED) {
                        if (!wakeLock.isHeld()) {
                            wakeLock.acquire();
                        }
                        Log.d(LOG, "STATE: CONNECTED");
                        mConnectedThread = new ConnectedThread(bt.getBluetoothService().getConnectedThread().getInputStream());
                        mConnectedThread.setPriority(Thread.MAX_PRIORITY);
                        mConnectedThread.start();
                    } else {
                        if (mConnectedThread != null) {
                            mConnectedThread = null;
                        }
                        if (wakeLock.isHeld()) wakeLock.release();
                        if (state == BluetoothState.STATE_CONNECTING) {
                            Log.d(LOG, "STATE: CONNECTING.");
                        } else if (state == BluetoothState.STATE_LISTEN) {
                            Log.d(LOG, "STATE: LISTEN.");
                        } else if (state == BluetoothState.STATE_NONE) {
                            Log.d(LOG, "STATE: NONE.");
                        }
                        //mContext.messageClient(ControlService.MSG_STATE_CHANGE);
                    }
                }
            });
            bt.setupService();
            bt.startService(BluetoothState.DEVICE_OTHER);

        } else {
            bt.enable();
            initBluetooth();
        }
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (IsBluetoothConnectionPingNecessary) {
                    bt.send(" ", false);
                }
            }
        }, 0, 100);
    }

    private void initAudioTrack() {
        Log.d(LOG, "initAudioTrack()");
        if (audioTrack == null) {
            audioTrack = new AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    RECORDER_SAMPLERATE,
                    PLAYBACK_CHANNELS,
                    RECORDER_AUDIO_ENCODING,
                    BufferElements2Rec,
                    AudioTrack.MODE_STREAM);
            audioTrack.play();
        }
    }

    private void finishFile() {
        Log.d(LOG, "finishFile()");

        try {
            outputStream.flush();
        } catch (Exception e) {}

        try {
            String filename = fileIO.filename;
            fileIO.closeDataOutStream();

            // report back to service
            Log.d(LOG, "MSG_CHUNK_RECORDED");
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
        } catch (Exception e) {}

    }

    private void writeData(byte[] data) {
        Log.d(LOG, "writeData()");

        nBlockCount++;

        if (nSamples == 0) {
            outputStream = fileIO.openDataOutStream(
                    RECORDER_SAMPLERATE,
                    RECORDER_CHANNELS,
                    RECORDER_AUDIO_ENCODING,
                    mIsWave
            );
        }



        // In case more samples are arriving than there is space inside a chunk
        if (nSamples > chunklengthInSamples) {
            int writeNow = chunklengthInSamples - nSamples;
            writeLater = block_size * RECORDER_CHANNELS - writeNow;

            // Write samples that fit into chunk
            for (int iSample = 0; iSample < writeNow; iSample++) {
                try {
                    outputStream.write(data[iSample]);
                } catch (Exception e) {e.printStackTrace();}
            }

            // Store overflow samples for next write call
            samplesToWriteLater = new byte[writeLater];
            for (int iSample = 0; iSample < writeLater; iSample++) {
                samplesToWriteLater[iSample] = data[writeNow + iSample];
            }

            // Only technically correct since nSamples is set to zero at the bottom
            nSamples -= writeLater;
        }



        if (nSamples < chunklengthInSamples) {

            try {
                // In case samples were carried over from last block
                if (writeLater > 0) {
                    outputStream.write(samplesToWriteLater);
                    nSamples += writeLater;
                    samplesToWriteLater = new byte[0];
                    writeLater = 0;
                }

                outputStream.write(data);

                outputStream.flush();
            } catch (Exception e) {e.printStackTrace();}

            nSamples += block_size * RECORDER_CHANNELS;

        } else {

            nSamples = 0;
            //nBlockCount = 0;

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
        Log.d(LOG, "getIsAudioRecorderClosed()");
        return !run;
    }

    @Override
    public void registerClient() {
        Log.d(LOG, "registerClient()");

    }

    @Override
    public void unregisterClient() {
        Log.d(LOG, "unregisterClient()");

        cleanUp();
    }

    @Override
    public void batteryCritical() {
        cleanUp();
    }

    @Override
    public void chargingOff() {
        Log.d(LOG, "chargingOff()");
    }

    @Override
    public void chargingOn() {
        Log.d(LOG, "chargingOn()");
        try {
            bt.stopService();
            //bt.disconnect();
            //mConnectedThread = null;
        } catch(Exception e) {}
        mContext.setChargingProfile();
    }

    @Override
    public void chargingOnPre() {
        Log.d(LOG, "chargingOnPre()");
        try {
            bt.stopService();
            //bt.disconnect();
            //mConnectedThread = null;
        } catch(Exception e) {}
    }

    @Override
    public void onDestroy() {
        Log.d(LOG, "onDestroy()");
        cleanUp();
    }


    @Override
    public void applicationShutdown() {
        Log.d(LOG, "applicationShutdown()");
        cleanUp();
    }

    private void startRecording() {

        Log.d(LOG, "startRecording()");

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

        ControlService.setIsRecording(true);
        mContext.messageClient(ControlService.MSG_START_RECORDING);
        mContext.getVibration().singleBurst();

    }

    private void stopRecording() {

        Log.d(LOG, "stopRecording()");

        run = false;
        finishFile();
        mContext.messageClient(ControlService.MSG_STOP_RECORDING);
        mContext.getVibration().singleBurst();
    }

    private void AudioTransmissionStart() {
        Log.d(LOG, "AudioTransmissionStart()");
    }


    private void AudioTransmissionEnd() {
        Log.d(LOG, "AudioTransmissionEnd()");
        stopRecording();
    }

    enum initState {UNINITIALIZED, WAITING_FOR_CALIBRATION_VALUES, WAITING_FOR_AUDIOTRANSMISSION, INITIALIZED, STOP}

    class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        public initState initializeState;
        boolean isRunning = true;
        public boolean useCalib = true;
        private double[] calibValues = new double[]{Double.NaN, Double.NaN};
        private double[] calibValuesInDB = new double[]{Double.NaN, Double.NaN};

        ConnectedThread(InputStream stream) {
            mmInStream = stream;
        }

        public void run() {
            RingBuffer ringBuffer = new RingBuffer(AudioBufferSize * 2);
            int alivePingTimeout = 100, i, lastBlockNumber = 0, currBlockNumber = 0, additionalBytesCount = 0;
            byte[] data = new byte[1024], emptyAudioBlock = new byte[AudioBufferSize];
            byte checksum = 0;
            int timeoutBlockLimit = 500, millisPerBlock = block_size * 1000 / RECORDER_SAMPLERATE;
            BlockCount = 0;
            lostBlockCount = 0;
            initializeState = initState.UNINITIALIZED;
            Long lastBluetoothPingTimer = System.currentTimeMillis(), lastEmptyPackageTimer = System.currentTimeMillis(), lastStreamTimer = System.currentTimeMillis();
            try {
                while (isRunning) {
                    if (mmInStream.available() >= data.length) {
                        mmInStream.read(data, 0, data.length);
                        for (i = 0; i < data.length; i++) {
                            ringBuffer.addByte(data[i]);
                            checksum ^= ringBuffer.getByte(0);
                            if (ringBuffer.getByte(-2) == (byte) 0x00 && ringBuffer.getByte(-3) == (byte) 0x80) {
                                switch (initializeState) {
                                    case UNINITIALIZED:
                                        Log.d(LOG, "ConnectedThread::RUN::UNINITIALIZED");
                                        int protocollVersion = (((ringBuffer.getByte(-4) & 0xFF) << 8) | (ringBuffer.getByte(-5) & 0xFF));
                                        switch (protocollVersion) { // Check Protocol-Version
                                            case 1:
                                                calibValuesInDB[0] = 0.0;
                                                calibValuesInDB[1] = 0.0;
                                                calibValues[0] = 1.0;
                                                calibValues[1] = 1.0;
                                                additionalBytesCount = 12;
                                                initializeState = initState.WAITING_FOR_AUDIOTRANSMISSION;
                                                break;
                                            case 2:
                                                calibValuesInDB[0] = Double.NaN;
                                                calibValuesInDB[1] = Double.NaN;
                                                calibValues[0] = Double.NaN;
                                                calibValues[1] = Double.NaN;
                                                additionalBytesCount = 12;
                                                initializeState = initState.WAITING_FOR_CALIBRATION_VALUES;
                                                break;
                                            default:
                                                Log.d(LOG, "Unknown Protocoll-Version");
                                        }
                                        break;
                                    case WAITING_FOR_CALIBRATION_VALUES:
                                        Log.d(LOG, "ConnectedThread::RUN::WAITING_FOR_CALIBRATION_VALUES");
                                        if (ringBuffer.getByte(-15) == (byte) 0xFF && ringBuffer.getByte(-16) == (byte) 0x7F && ringBuffer.getByte(-14) == (byte) 'C' && (ringBuffer.getByte(-13) == (byte) 'L' || ringBuffer.getByte(-13) == (byte) 'R')) {
                                            byte[] values = new byte[8];
                                            byte ValuesChecksum = ringBuffer.getByte(-13);
                                            for (int count = 0; count < 8; count++) {
                                                values[count] = ringBuffer.getByte(-12 + count);
                                                ValuesChecksum ^= values[count];
                                            }
                                            if (ValuesChecksum == ringBuffer.getByte(-4)) {
                                                if (ringBuffer.getByte(-13) == 'L')
                                                    calibValuesInDB[0] = ByteBuffer.wrap(values).getDouble();
                                                else if (ringBuffer.getByte(-13) == 'R')
                                                    calibValuesInDB[1] = ByteBuffer.wrap(values).getDouble();
                                                if (!Double.isNaN(calibValuesInDB[0]) && !Double.isNaN(calibValuesInDB[1])) {
                                                    if (calibValuesInDB[0] <= calibValuesInDB[1]) {
                                                        calibValues[0] = Math.pow(10, (calibValuesInDB[0] - calibValuesInDB[1]) / 20.0);
                                                        calibValues[1] = 1;
                                                    } else {
                                                        calibValues[0] = 1;
                                                        calibValues[1] = Math.pow(10, (calibValuesInDB[1] - calibValuesInDB[0]) / 20.0);
                                                    }
                                                    Log.d(LOG, "START AUDIOTRANSMISSION");
                                                    initializeState = initState.WAITING_FOR_AUDIOTRANSMISSION;
                                                }
                                            }
                                        } else if (System.currentTimeMillis() - lastStreamTimer > 1000) {
                                            bt.send("GC", false);
                                            lastStreamTimer = System.currentTimeMillis();
                                            Log.d(LOG, "send GC");
                                        }
                                        break;
                                    case WAITING_FOR_AUDIOTRANSMISSION:
                                        Log.d(LOG, "ConnectedThread::RUN::WAITING_FOR_AUDIOTRANSMISSION");
                                        if (ringBuffer.getByte(2 - (AudioBufferSize + additionalBytesCount)) == (byte) 0xFF && ringBuffer.getByte(1 - (AudioBufferSize + additionalBytesCount)) == (byte) 0x7F) {
                                            if (ringBuffer.getByte(0) == (checksum ^ ringBuffer.getByte(0))) {
                                                startRecording();
                                                AudioTransmissionStart();
                                                initializeState = initState.INITIALIZED;
                                                currBlockNumber = ((ringBuffer.getByte(-6) & 0xFF) << 8) | (ringBuffer.getByte(-7) & 0xFF);
                                                lastBlockNumber = currBlockNumber;
                                            }
                                            checksum = 0;
                                        }
                                        break;
                                    case INITIALIZED:
                                        Log.d(LOG, "ConnectedThread::RUN::INITIALIZED");
                                        if (ringBuffer.getByte(2 - (AudioBufferSize + additionalBytesCount)) == (byte) 0xFF && ringBuffer.getByte(1 - (AudioBufferSize + additionalBytesCount)) == (byte) 0x7F) {
                                            if (ringBuffer.getByte(0) == (checksum ^ ringBuffer.getByte(0))) {
                                                AudioVolume = (short) (((ringBuffer.getByte(-8) & 0xFF) << 8) | (ringBuffer.getByte(-9) & 0xFF));
                                                currBlockNumber = ((ringBuffer.getByte(-6) & 0xFF) << 8) | (ringBuffer.getByte(-7) & 0xFF);
                                                if (currBlockNumber < lastBlockNumber && lastBlockNumber - currBlockNumber > currBlockNumber + (65536 - lastBlockNumber))
                                                    currBlockNumber += 65536;
                                                if (lastBlockNumber < currBlockNumber) {
                                                    BlockCount += currBlockNumber - lastBlockNumber;
                                                    lostBlockCount += currBlockNumber - lastBlockNumber - 1;
                                                    while (lastBlockNumber < currBlockNumber - 1) {
                                                        Log.d(LOG, "CurrentBlock: " + currBlockNumber + "\tLostBlocks: " + lostBlockCount);
                                                        writeData(emptyAudioBlock);
                                                        lastBlockNumber++;
                                                    }
                                                    lastBlockNumber = currBlockNumber % 65536;
                                                    for (int idx = 0; idx < AudioBufferSize / 2; idx++) {
                                                        if (useCalib)
                                                            ringBuffer.setShort((short) (ringBuffer.getShort(3 - (AudioBufferSize + additionalBytesCount) + idx * 2) * calibValues[idx % 2]), 3 - (AudioBufferSize + additionalBytesCount) + idx * 2);
                                                        else
                                                            ringBuffer.setShort((short) (ringBuffer.getShort(3 - (AudioBufferSize + additionalBytesCount) + idx * 2)), 3 - (AudioBufferSize + additionalBytesCount) + idx * 2);
                                                    }
                                                    writeData(ringBuffer.data(3 - (AudioBufferSize + additionalBytesCount), AudioBufferSize));
                                                    lastStreamTimer = System.currentTimeMillis();
                                                } else
                                                    Log.d(LOG, "CurrentBlock: " + currBlockNumber + "\tTOO SLOW!");
                                            }
                                            checksum = 0;
                                        }
                                        break;
                                    case STOP:
                                        Log.d(LOG, "ConnectedThread::RUN::STOP");
                                        if (initializeState == initState.INITIALIZED)
                                            AudioTransmissionEnd();
                                        initializeState = initState.UNINITIALIZED;
                                        bt.getBluetoothService().connectionLost();
                                        bt.getBluetoothService().start(false);
                                }
                            }
                        }
                        lastEmptyPackageTimer = System.currentTimeMillis();
                    } else if (initializeState == initState.INITIALIZED && System.currentTimeMillis() - lastEmptyPackageTimer > timeoutBlockLimit) {
                        for (long count = 0; count < timeoutBlockLimit / millisPerBlock; count++) {
                            BlockCount++;
                            lostBlockCount++;
                            lastBlockNumber++;
                            writeData(emptyAudioBlock);
                        }
                        Log.d(LOG, "Transmission Timeout\t");
                        lastEmptyPackageTimer = System.currentTimeMillis();
                    }
                    if (initializeState == initState.INITIALIZED) {
                        if (System.currentTimeMillis() - lastBluetoothPingTimer > alivePingTimeout) {
                            bt.send(" ", false);
                            lastBluetoothPingTimer = System.currentTimeMillis();
                        }
                        if (System.currentTimeMillis() - lastStreamTimer > 5 * 1000) // 5 seconds
                        {
                            if (initializeState == initState.INITIALIZED) AudioTransmissionEnd();
                            initializeState = initState.UNINITIALIZED;
                            bt.getBluetoothService().connectionLost();
                            bt.getBluetoothService().start(false);
                        }
                    }

                }
            } catch (IOException e) {
            }
            stopRecording();
        }
    }
}
