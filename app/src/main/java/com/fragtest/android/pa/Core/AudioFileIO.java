package com.fragtest.android.pa.Core;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

import android.os.Environment;

public class AudioFileIO {

    protected static final String LOG = "IOClass";
    public static final String MAIN_FOLDER = FileIO.MAIN_FOLDER;
    public static final String CACHE_FOLDER = MAIN_FOLDER + "/cache";

    String filename;

    int samplerate   = 0;
    int channels     = 0;
    int format       = 0;
    boolean isWave   = false;

    File file               = null;
    DataOutputStream stream = null;

    // create main folder
    public String getFolderPath(){
        File baseDirectory = Environment.getExternalStoragePublicDirectory(CACHE_FOLDER);
        if( !baseDirectory.exists() ){
            baseDirectory.mkdir();
        }
        return baseDirectory.getAbsolutePath();
    }

    // build filename
    public String getFilename( boolean wavHeader ) {

        String ext;

        if ( wavHeader ) {
            ext = ".wav";
        } else {
            ext = ".raw";
        }

        String _filename = new StringBuilder()
                .append( getFolderPath() )
                .append( File.separator )
                .append( Timestamp.getTimestamp(3) )
                .append( ext )
                .toString();

        return _filename;
    }

    // open output stream w/ filename
    public DataOutputStream openDataOutStream( String _filename, int _samplerate, int _channels, int _format, boolean _isWave ){

        samplerate  = _samplerate;
        channels    = _channels;
        format      = _format;
        isWave      = _isWave;

        filename = new StringBuilder()
                .append( getFolderPath() )
                .append( File.separator )
                .append( _filename )
                .toString();
        file = new File( filename );

        return openFileStream();
    }

    // open output stream w/o filename
    public DataOutputStream openDataOutStream( int _samplerate, int _channels, int _format, boolean _isWave ){

        samplerate  = _samplerate;
        channels    = _channels;
        format      = _format;
        isWave      = _isWave;

        filename = getFilename( isWave );
        file = new File( filename );

        return openFileStream();
    }

    public DataOutputStream openFileStream() {

        try {
            FileOutputStream os = new FileOutputStream( file, false );
            stream = new DataOutputStream( new BufferedOutputStream( os ));

            // Write zeros. This will be filed with a proper header on close.
            // Alternatively, FileChannel might be used.
            if ( isWave ) {
                int nBytes = 44; // length of the WAV (RIFF) header
                byte[] zeros = new byte[nBytes];
                for ( int i = 0; i < nBytes; i++) {
                    zeros[i] = 0;
                }
                stream.write( zeros );
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return stream;

    }

    // close the output stream
    public void closeDataOutStream(){
        try {
            stream.flush();
            stream.close();

            if  ( isWave ) {
                writeWavHeader();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // open input stream
    public BufferedInputStream openInStream( String filepath ){
        BufferedInputStream stream = null;
        try {
            FileInputStream is = new FileInputStream( filepath );
            stream = new BufferedInputStream( is );
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return stream;
    }

    // close the input stream
    public void closeInStream( BufferedInputStream stream ){
        try {
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // delete a file
    public boolean deleteFile( String filename ){

        boolean success;

        File file2delete = new File( filename );

        if ( file2delete.exists() ) {

            success = file2delete.delete();
            if( success ){
//				Log.d( LOG, "Deleted " + filename );
            }else{
//				Log.d( LOG, "Could not delete " + filename );
            }

        } else  {
//			Log.d( LOG, "Could not delete " + filename + " (File does not exist)" );
            success = false;
        }

        return success;
    }

    // write WAV (RIFF) header
    public void writeWavHeader() {

        byte[] GROUP_ID     = "RIFF".getBytes();
        byte[] RIFF_TYPE    = "WAVE".getBytes();
        byte[] FORMAT_ID    = "fmt ".getBytes();
        byte[] DATA_ID      = "data".getBytes();
        short FORMAT_TAG    = 1; // PCM
        int FMT_LENGTH      = 16;

        short bitsize       = 16; // TODO

        try {

            RandomAccessFile raFile = new RandomAccessFile(file, "rw");

            int fileLength = (int) raFile.length(); // [bytes]
            int chunkSize  = fileLength - 8;
            int dataSize   = fileLength - 44;
            short blockAlign  = (short) (( channels) * (bitsize % 8));
            int bytesPerSec = samplerate * blockAlign;

            // RIFF-Header
            raFile.write(GROUP_ID);
            raFile.writeInt(Integer.reverseBytes(chunkSize));
            raFile.write(RIFF_TYPE);

            // fmt
            raFile.write(FORMAT_ID);
            raFile.writeInt(Integer.reverseBytes(FMT_LENGTH));
            raFile.writeShort(Short.reverseBytes(FORMAT_TAG));
            raFile.writeShort(Short.reverseBytes((short) channels));
            raFile.writeInt(Integer.reverseBytes(samplerate));
            raFile.writeInt(Integer.reverseBytes(bytesPerSec));
            raFile.writeShort(Short.reverseBytes(blockAlign));
            raFile.writeShort(Short.reverseBytes(bitsize));

            // data
            raFile.write(DATA_ID);
            raFile.writeInt(Integer.reverseBytes(dataSize));

            raFile.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
