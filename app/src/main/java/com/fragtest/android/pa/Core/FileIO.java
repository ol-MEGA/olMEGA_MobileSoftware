package com.fragtest.android.pa.Core;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * Created by ulrikkowalk on 23.03.17.
 */

public class FileIO {

    private static final String MAIN_FOLDER = "IHAB";
    private static final String DATA_FOLDER = "data";
    private static final String FILE_NAME = "hoersituation-v0.xml";
    private static final String FILE_IO = "FileIO";


    public FileIO() {
    }

    // Create / Find main Folder
    private static String getFolderPath() {
        File baseDirectory = Environment.getExternalStoragePublicDirectory(MAIN_FOLDER);
        if (!baseDirectory.exists()) {
            Log.e(FILE_IO, "Directory does not exist ->create");
            baseDirectory.mkdir();
        }
        return baseDirectory.getAbsolutePath();
    }

    // Offline version reads XML Basis Sheet from Raw Folder
    public String readRawTextFile(Context ctx, int resId) {
        InputStream inputStream = ctx.getResources().openRawResource(resId);

        InputStreamReader inputReader = new InputStreamReader(inputStream);
        BufferedReader buffReader = new BufferedReader(inputReader);
        String line;
        StringBuilder text = new StringBuilder();
        boolean isComment = false;
        try {
            while ((line = buffReader.readLine()) != null) {

                if (line.trim().startsWith("/*")) {
                    isComment = true;
                }

                if (!line.trim().isEmpty() && !line.trim().startsWith("//") && !isComment) {
                    text.append(line);
                    text.append('\n');
                } else {
                    Log.i(FILE_IO,"Dropping line: "+line.trim());
                }

                if (line.trim().endsWith("*/")) {
                    isComment = false;
                }

            }
        } catch (IOException e) {
            return null;
        }
        return text.toString();
    }

    public String readRawTextFile() {

        try {
            // Obtain working Directory
            File dir = new File(getFolderPath());
            // Address Basis File in working Directory
            File file = new File(dir, FILE_NAME);

            FileInputStream inputStream = new FileInputStream(file);
            InputStreamReader inputReader = new InputStreamReader(inputStream);
            BufferedReader buffReader = new BufferedReader(inputReader);
            String line;
            StringBuilder text = new StringBuilder();
            boolean isComment = false;

            try {
                while ((line = buffReader.readLine()) != null) {

                    if (line.trim().startsWith("/*")) {
                        isComment = true;
                    }

                    if (!line.trim().isEmpty() && !line.trim().startsWith("//") && isComment) {
                        text.append(line);
                        text.append('\n');
                    } else {
                        Log.i(FILE_IO,"Dropping line: "+line.trim());
                    }

                    if (line.trim().endsWith("*/")) {
                        isComment = false;
                    }

                }
            } catch (IOException e) {
                return null;
            }

            inputStream.close();
            return text.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean saveDataToFile(Context context, String filename, String data) {

        //MediaScannerConnection mMs = new MediaScannerConnection(context, this);
        //mMs.connect();

        String sFileName = filename;

        // Obtain working Directory
        File dir = new File(getFolderPath() + "/" + DATA_FOLDER + "/");
        // Address Basis File in working Directory
        File file = new File(dir, sFileName);

        // Make sure the path directory exists.
        if (!dir.exists()) {
            dir.mkdirs();
            Log.i(FILE_IO, "Directory created: " + dir);
        }

        String stringToSave = data;

        Log.i(FILE_IO, "writing to File: " + file.getAbsolutePath());

        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream fOut = new FileOutputStream(file);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);

            myOutWriter.append(stringToSave);

            myOutWriter.close();

            fOut.flush();
            fOut.close();

            new SingleMediaScanner(context, file);
            Log.i(FILE_IO, "Data successfully written.");
            return true;
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
        return false;
    }

    public boolean saveDataToFileOffline(Context context, String filename, String data) {

        String sFileName = filename;

        // Obtain working Directory
        File dir = new File("C:/Users/ul1021/Desktop/data");
        // Address Basis File in working Directory
        File file = new File(dir, sFileName);

        Log.i(FILE_IO,file.getAbsolutePath());

        // Make sure the path directory exists.
        if (!dir.exists()) {
            if (dir.mkdirs()) {
                Log.i(FILE_IO, "Directory created: " + dir);

                String stringToSave = data;

                Log.i(FILE_IO, "writing to File: " + file.getAbsolutePath());

                try {
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    FileOutputStream fOut = new FileOutputStream(file);
                    OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);

                    myOutWriter.append(stringToSave);
                    myOutWriter.close();

                    fOut.flush();
                    fOut.close();

                    new SingleMediaScanner(context, file);
                    Log.i(FILE_IO, "Data successfully written.");
                    return true;
                } catch (IOException e) {
                    Log.e("Exception", "File write failed: " + e.toString());
                }
            } else {
                Log.e(FILE_IO, "Unable to create directory. Shutting down.");
            }
        }


        return false;
    }
}
