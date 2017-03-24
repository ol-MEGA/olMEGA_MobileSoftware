package com.fragtest.android.pa;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static android.util.Log.e;

/**
 * Created by ulrikkowalk on 23.03.17.
 */

public class FileIO {

    private static final String DATA_FOLDER = "IHAB";
    private static final String FILE_NAME = "hoersituation-v0.xml";
    private static String FILE_IO = "FileIO";

    public FileIO() {
    }

    // Offline version reads XML Basis Sheet from Raw Folder
    public String readRawTextFile(Context ctx, int resId) {
        InputStream inputStream = ctx.getResources().openRawResource(resId);

        InputStreamReader inputReader = new InputStreamReader(inputStream);
        BufferedReader buffReader = new BufferedReader(inputReader);
        String line;
        StringBuilder text = new StringBuilder();

        try {
            while ((line = buffReader.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
        } catch (IOException e) {
            return null;
        }
        return text.toString();
    }

    // Create / Find main Folder
    private static String getFolderPath(){
        File baseDirectory = Environment.getExternalStoragePublicDirectory( DATA_FOLDER );
        if( !baseDirectory.exists() ){
            Log.e(FILE_IO,"Directory does not exist ->create");
            baseDirectory.mkdir();
        }
        return baseDirectory.getAbsolutePath();
    }


    public String readRawTextFile() {

        try {
            // Obtain working Directory
            File dir = new File (getFolderPath());
            // Address Basis File in working Directory
            File file = new File(dir, FILE_NAME);

            FileInputStream inputStream = new FileInputStream(file);
            InputStreamReader inputReader = new InputStreamReader(inputStream);
            BufferedReader buffReader = new BufferedReader(inputReader);
            String line;
            StringBuilder text = new StringBuilder();

            try {
                while ((line = buffReader.readLine()) != null) {
                    text.append(line);
                    text.append('\n');
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


    public boolean saveDataToFile(MetaData metaData, AnswerIDs answerIDs) {


        return true;
    }
}
