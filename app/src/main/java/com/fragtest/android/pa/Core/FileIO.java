package com.fragtest.android.pa.Core;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import com.fragtest.android.pa.BuildConfig;

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

    public static final String FOLDER_MAIN = "IHAB";
    public static final String FOLDER_DATA = "data";
    private static final String FOLDER_QUEST = "quest";
    //private static final String FILE_NAME = "hoersituation-v0.xml";
    private static final String FILE_NAME = "questionnairecheckboxgroup.xml";
    private static final String LOG = "FileIO";
    // File the system looks for in order to show preferences, needs to be in main directory
    private static final String FILE_CONFIG = "rules.ini";
    private static final String FILE_FIRST = "ihab.ini";
    private boolean isVerbose = false;
    private Context mContext;

    // Create / Find main Folder
    public static String getFolderPath() {
        final File baseDirectory = new File(Environment.getExternalStorageDirectory() +
                File.separator + FOLDER_MAIN);
        if (!baseDirectory.exists()) {
            baseDirectory.mkdir();
        }
        return baseDirectory.getAbsolutePath();
    }

    public boolean setupFirstUse(Context context) {

        mContext = context;

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        boolean isFirst = sharedPreferences.getBoolean("isFirst", true);
        Log.i(LOG, "First use detected: " + isFirst);

        if (isFirst) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("isFirst", false);
            editor.commit();
            File fileConfig = saveDataToFile(context, FILE_CONFIG, "This file may remain empty.");
            new SingleMediaScanner(mContext, fileConfig);
        }

        String[] string = scanQuestOptions();
        if (string == null) {
            return false;
        } else {
            return true;
        }
    }

    public boolean lockPreferences() {
        File file = new File(getFolderPath() + File.separator + FOLDER_DATA +
                File.separator + FILE_CONFIG);
        Log.i(LOG, "does file exist? "+file.exists());
        boolean deleted = file.delete();
        new SingleMediaScanner(mContext, file);
        Log.e(LOG, "Bridge burnt: " + file.getAbsolutePath() + ", successful: "+ deleted);
        return deleted;
    }

    // Check whether preferences unlock file is present in main directory
    public boolean scanConfigMode() {

        Log.i(LOG, "Scan config mode");
        File fileConfig = new File(getFolderPath() + File.separator + FOLDER_DATA +
                File.separator + FILE_CONFIG);
        if (fileConfig.exists()) {
            return true;
        } else {
            return false;
        }

    }

    // Scan "quest" directory for present questionnaires
    public String[] scanQuestOptions() {

        //TODO: Validate files
        // Obtain working Directory
        File dir = new File(getFolderPath() + "/" + FOLDER_QUEST);

        // Scan for files
        File[] files = dir.listFiles();
        try {
            String[] fileList = new String[files.length];

            if (fileList.length == 0) {
                return null;
            }

            for (int iFile = 0; iFile < files.length; iFile++) {
                fileList[iFile] = files[iFile].getName();
            }
            return fileList;

        } catch (Exception e) {
            Log.i(LOG,""+e.toString());
            return null;
        }
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
                    if (isVerbose) {
                        Log.i(LOG, "Dropping line: " + line.trim());
                    }
                }
                if (!line.trim().startsWith("//") && line.split(" //").length > 1) {
                    text.append(line.split(" //")[0].trim());
                    if (isVerbose) {
                        Log.i(LOG, "Dropping part: " + line.split(" //")[1].trim());
                    }
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

    // Online with dynamic filename
    public String readRawTextFile(String fileName) {

        try {
            // Obtain working Directory
            File dir = new File(getFolderPath() + "/" + FOLDER_QUEST);
            // Address Basis File in working Directory
            File file = new File(dir, fileName);

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

                    if (!line.trim().isEmpty() && !line.trim().startsWith("//") && !isComment) {
                        text.append(line);
                        text.append('\n');
                    } else {
                        if (isVerbose) {
                            Log.i(LOG, "Dropping line: " + line.trim());
                        }
                    }
                    if (!line.trim().startsWith("//") && line.split(" //").length > 1) {
                        text.append(line.split(" //")[0].trim());
                        if (isVerbose) {
                            Log.i(LOG, "Dropping part: " + line.split(" //")[1].trim());
                        }
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

    // Online with predefined filename
    public String readRawTextFile() {

        try {
            // Obtain working Directory
            File dir = new File(getFolderPath() + "/" + FOLDER_QUEST);
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

                    if (!line.trim().isEmpty() && !line.trim().startsWith("//") && !isComment) {
                        text.append(line);
                        text.append('\n');
                    } else {
                        if (isVerbose) {
                            Log.i(LOG, "Dropping line: " + line.trim());
                        }
                    }
                    if (!line.trim().startsWith("//") && line.split(" //").length > 1) {
                        text.append(line.split(" //")[0].trim());
                        if (isVerbose) {
                            Log.i(LOG, "Dropping part: " + line.split(" //")[1].trim());
                        }
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

    public File saveDataToFile(Context context, String filename, String data) {

        //MediaScannerConnection mMs = new MediaScannerConnection(context, this);
        //mMs.connect();

        String sFileName = filename;

        // Obtain working Directory
        File dir = new File(getFolderPath() + "/" + FOLDER_DATA + "/");
        // Address Basis File in working Directory
        File file = new File(dir, sFileName);

        Log.e(LOG, "" + dir);

        // Make sure the path directory exists.
        if (!dir.exists()) {
            dir.mkdirs();
            if (BuildConfig.DEBUG) {
                Log.i(LOG, "Directory created: " + dir);
            }
        }

        String stringToSave = data;

        if (BuildConfig.DEBUG) {
            Log.e(LOG, "writing to File: " + file.getAbsolutePath());
        }

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

            if (BuildConfig.DEBUG) {
                Log.i(LOG, "Data successfully written.");
            }
            return file;
        } catch (IOException e) {

            if (BuildConfig.DEBUG) {
                Log.e("Exception", "File write failed: " + e.toString());
            }
        }
        return null;
    }

    public boolean saveDataToFileOffline(Context context, String filename, String data) {

        String sFileName = filename;

        // Obtain working Directory
        File dir = new File("C:/Users/ul1021/Desktop/data");
        // Address Basis File in working Directory
        File file = new File(dir, sFileName);

        Log.i(LOG, file.getAbsolutePath());

        // Make sure the path directory exists.
        if (!dir.exists()) {
            if (dir.mkdirs()) {
                if (BuildConfig.DEBUG) {
                    Log.i(LOG, "Directory created: " + dir);
                }

                String stringToSave = data;

                if (BuildConfig.DEBUG) {
                    Log.i(LOG, "writing to File: " + file.getAbsolutePath());
                }

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

                    if (BuildConfig.DEBUG) {
                        Log.i(LOG, "Data successfully written.");
                    }
                    return true;
                } catch (IOException e) {
                    if (BuildConfig.DEBUG) {
                        Log.e("Exception", "File write failed: " + e.toString());
                    }
                }
            } else {
                if (BuildConfig.DEBUG) {
                    Log.e(LOG, "Unable to create directory. Shutting down.");
                }
            }
        }
        return false;
    }
}
