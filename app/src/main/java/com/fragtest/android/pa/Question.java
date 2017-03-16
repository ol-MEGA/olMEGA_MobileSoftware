package com.fragtest.android.pa;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static android.os.Build.VERSION_CODES.M;
import static android.util.Log.i;

/**
 * Created by ulrikkowalk on 28.02.17.
 */

public class Question extends AppCompatActivity {

    private String mQuestionBlueprint;
    private String mQuestionText;
    private String mTypeAnswer;
    private List<Answer> mAnswers;
    private int mNumAnswers;
    private int mQuestionId;
    private int mFilterId;
    private boolean mFilterCondition;
    //private boolean mIsActive;
    private List<String> ListOfNonTypicalAnswerTypes = Arrays.asList("text", "date");

    // Public Constructor
    public Question(String sQuestionBlueprint) {

        mQuestionBlueprint = sQuestionBlueprint;
        // Set Question active
        //mIsActive = true;
        // Obtain Question Text
        mQuestionText = extractQuestionText();
        // Obtain Question ID
        mQuestionId = extractQuestionId();
        // Obtain Filter ID
        mFilterId = extractFilterId();
        //Log.i("Filter ID",""+mFilterId);
        // Obtain Filter ID Condition ("if true" or "if false")
        mFilterCondition = extractFilterCondition();
        //Log.i("Filter Condition",""+mFilterCondition);
        // Obtain Answer Type (e.g. Radio, Button, Slider,...)
        mTypeAnswer = extractTypeAnswers();
        //Log.i("Type of Answer",mTypeAnswer);
        // Obtain Number of Answers
        mNumAnswers = extractNumAnswers();
        //Log.i("Number of Answers",""+mNumAnswers);
        // Create List of Answers
        mAnswers = extractAnswerList();
        //Log.i("end","----------------------");
    }


    private int extractQuestionId() {
        // String Array carrying introductory Line with ID, Type, Filter
        String[] introductoryLine = mQuestionBlueprint.split("\"");
        // Obtain Question ID
        return Integer.parseInt(introductoryLine[1]);
    }

    private String extractQuestionText() {
        // String Array carrying  the whole Question with Answers etc
        String[] itemQuestionLines = mQuestionBlueprint.split("\\r?\\n");
        // Obtain Question Text
        return (itemQuestionLines[2].split("<text>|</text>")[1]);
    }

    private int extractFilterId() {

        // String Array carrying introductory Line with ID, Type, Filter
        String[] introductoryLine = mQuestionBlueprint.split("\"");

        if (introductoryLine[4].contains("filter")) {
            // String carrying the Filter ID terms
            String sFilterIDLine = introductoryLine[5];
            // Filter ID is extracted
            String[] sFilterID = sFilterIDLine.split("_");

            if (sFilterID.length > 1) {
                return Integer.parseInt(sFilterID[1]);
            }
        }
        return -1;
    }

    private boolean extractFilterCondition() {
        // String Array carrying introductory Line with ID, Type, Filter
        String[] introductoryLine = mQuestionBlueprint.split("\"");

        if (introductoryLine[4].contains("filter")) {
            // String carrying the Filter ID terms
            String sFilterIDLine = introductoryLine[5];
            // '!' before Filter ID means the Question is shown ONLY if ID was not checked
            if (sFilterIDLine.charAt(0) == '!') {
                return false;
            }
        }
        return true;
    }

    private int extractNumAnswers() {
        if (mQuestionBlueprint.contains("hidden=\"true\"")) {
            return 0;
        } else if (nonTypicalAnswer(mTypeAnswer)) {
            return 1;
        } else {
            // String Array carrying  the whole Question with Answers etc
            String[] itemQuestionLines = mQuestionBlueprint.split("\\r?\\n");
            // Obtain Number of Answers
            return (itemQuestionLines.length - 4) / 3;
        }
    }

    public boolean isHidden() {
        return mQuestionBlueprint.contains("hidden=\"true\""); }

    private boolean nonTypicalAnswer(String sTypeAnswer) {
        return ListOfNonTypicalAnswerTypes.contains(sTypeAnswer);}

    private String extractTypeAnswers() {
        // String Array carrying introductory Line with ID, Type, Filter
        String[] introductoryLine = mQuestionBlueprint.split("\"");
        // Obtain Answer Type (e.g. Radio, Button, Slider,...)
        return introductoryLine[3];
    }

    private List<Answer> extractAnswerList() {

        // List of Answers
        List<Answer> listAnswers = new ArrayList<Answer>();
        // String Array carrying  the whole Question with Answers etc
        String[] itemQuestionLines = mQuestionBlueprint.split("\\r?\\n");

        for (int iAnswer = 0; iAnswer < mNumAnswers; iAnswer++) {

            int nAnswerID;
            // Obtain Answer ID
            String sAnswerIDLine = itemQuestionLines[iAnswer * 3 + 1 + 3];
            String[] sAnswerIDSplit = sAnswerIDLine.split("\"");

            // Sort out common and uncommon IDs
            if (((sAnswerIDSplit.length < 2) && (!sAnswerIDLine.contains("default"))) ||
                    (nonTypicalAnswer(mTypeAnswer))) {
                // 33333 means no visible consequences but not Default value
                nAnswerID = 33333;
            } else if (sAnswerIDLine.contains("default")) {
                // 11111 means Default without visible consequences
                nAnswerID = 11111;
            } else {
                nAnswerID = Integer.parseInt(sAnswerIDSplit[1]);
            }

            // Create Answers based on their respective IDs
            if ((nAnswerID == 66666) || (nAnswerID == 33333)) {
                // An ID of 66666 means an empty vertical Space
                // 33333 is for editable Text
                listAnswers.add(new Answer("", nAnswerID));
            } else {
                // Obtain Answer Text
                String sAnswerTextLine = itemQuestionLines[iAnswer * 3 + 2 + 3];
                String[] answerParts = sAnswerTextLine.split("<text>|</text>");
                if (sAnswerIDLine.contains("default")) {
                    listAnswers.add(new Answer(answerParts[1], nAnswerID, true));
                } else {
                    listAnswers.add(new Answer(answerParts[1], nAnswerID));
                }
            }
        }
        return listAnswers;
    }

    public String getQuestionText() {
        return mQuestionText;
    }

    public int getQuestionId() {
        return mQuestionId;
    }

    public int getFilterId() {
        return mFilterId;
    }

    public boolean getFilterCondition() {
        return mFilterCondition;
    }

    public String getTypeAnswer() {
        return mTypeAnswer;
    }

    public int getNumAnswers() {
        return mNumAnswers;
    }

    public List<Answer> getAnswers() {
        return mAnswers;
    }

    /*
    public boolean isActive() {
        return mIsActive;
    }

    public void setActive() {
        mIsActive = true;
    }

    public void setInactive() {
        mIsActive = false;
    }
*/
}
