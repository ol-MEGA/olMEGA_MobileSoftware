package com.fragtest.android.pa;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ulrikkowalk on 28.02.17.
 */

public class Question extends AppCompatActivity {

    private String mQuestionText;
    private String mTypeAnswer;
    private List<Answer> mAnswers;
    private int mNumAnswers;
    private int mQuestionId;
    private int mFilterId;
    private boolean mFilterCondition;
    private boolean mIsActive;

    // Public Constructor
    public Question(String sQuestionBlueprint) {

        // Set Question active
        mIsActive = true;
        // Obtain Question Text
        mQuestionText = extractQuestionText(sQuestionBlueprint);
        // Obtain Question ID
        mQuestionId = extractQuestionId(sQuestionBlueprint);
        // Obtain Filter ID
        mFilterId = extractFilterId(sQuestionBlueprint);
        // Obtain Filter ID Condition ("if true" or "if false")
        mFilterCondition = extractFilterCondition(sQuestionBlueprint);
        // Obtain Number of Answers
        mNumAnswers = extractNumAnswers(sQuestionBlueprint);
        // Obtain Answer Type (e.g. Radio, Button, Slider,...)
        mTypeAnswer = extractTypeAnswers(sQuestionBlueprint);
        // Create List of Answers
        mAnswers = extractAnswerList(sQuestionBlueprint);
    }


    private int extractQuestionId(String sQuestionBlueprint) {
        // String Array carrying introductory Line with ID, Type, Filter
        String[] introductoryLine = sQuestionBlueprint.split("\"");
        // Obtain Question ID
        return Integer.parseInt(introductoryLine[1]);
    }

    private String extractQuestionText(String sQuestionBlueprint) {
        // String Array carrying  the whole Question with Answers etc
        String[] itemQuestionLines = sQuestionBlueprint.split("\\r?\\n");
        // Obtain Question Text
        return (itemQuestionLines[2].split("<text>|</text>")[1]);
    }

    private int extractFilterId(String sQuestionBlueprint) {
        // String Array carrying introductory Line with ID, Type, Filter
        String [] introductoryLine = sQuestionBlueprint.split("\"");
        // String carrying the Filter ID terms
        String sFilterIDLine = introductoryLine[5];
        // Filter ID is extracted
        String[] sFilterID = sFilterIDLine.split("_");
        if (sFilterID.length > 1) {
            return Integer.parseInt(sFilterID[1]);
        } else {
            return -1;
        }
    }

    private boolean extractFilterCondition(String sQuestionBlueprint) {
        // String Array carrying introductory Line with ID, Type, Filter
        String [] introductoryLine = sQuestionBlueprint.split("\"");
        // String carrying the Filter ID terms
        String sFilterIDLine = introductoryLine[5];
        // '!' before Filter ID means the Question is shown ONLY if ID was not checked
        if (sFilterIDLine.charAt(0) == '!') {
            return false;
        } else {
            return true;
        }
    }

    private int extractNumAnswers(String sQuestionBlueprint) {
        // String Array carrying  the whole Question with Answers etc
        String[] itemQuestionLines = sQuestionBlueprint.split("\\r?\\n");
        // Obtain Number of Answers
        return (itemQuestionLines.length - 7) / 3;
    }

    private String extractTypeAnswers(String sQuestionBlueprint) {
        // String Array carrying introductory Line with ID, Type, Filter
        String [] introductoryLine = sQuestionBlueprint.split("\"");
        // Obtain Answer Type (e.g. Radio, Button, Slider,...)
        return introductoryLine[3];
    }

    private List<Answer> extractAnswerList(String sQuestionBlueprint) {
        // String Array carrying  the whole Question with Answers etc
        String[] itemQuestionLines = sQuestionBlueprint.split("\\r?\\n");
        // List of Answers
        List<Answer> listAnswers = new ArrayList<Answer>();

        for (int iAnswer = 0; iAnswer < mNumAnswers; iAnswer++) {
            // Obtain Answer Text
            String sAnswerTextLine = itemQuestionLines[iAnswer * 3 + 2 + 6];
            String[] answerParts = sAnswerTextLine.split("<text>|</text>");
            // Obtain Answer ID
            String sAnswerIDLine = itemQuestionLines[iAnswer * 3 + 1 + 6];
            String[] sAnswerIDSplit = sAnswerIDLine.split("\"");
            int nAnswerID = Integer.parseInt(sAnswerIDSplit[1]);

            // Some Answers contain empty Text and are being discarded
            if (answerParts.length < 2) {
                Log.e("INFO","Invalid Answer was omitted.");
            } else {
                listAnswers.add(new Answer(answerParts[1],nAnswerID));
            }
        }
        return listAnswers;
    }

    public boolean isActive() { return mIsActive; }

    public void setActive() { mIsActive = true; }

    public void setInactive() {mIsActive = false;}

    public String getQuestionText() {
        return mQuestionText;
    }

    public int getQuestionId() { return mQuestionId; }

    public int getFilterId() { return mFilterId; }

    public boolean getFilterCondition() { return mFilterCondition; }

    public String getTypeAnswer() { return mTypeAnswer; }

    public int getNumAnswers() {return mNumAnswers; }

    public List<Answer> getAnswers() {
        return mAnswers;
    }


}
