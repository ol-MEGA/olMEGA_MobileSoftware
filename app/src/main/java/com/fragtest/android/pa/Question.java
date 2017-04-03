package com.fragtest.android.pa;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.TimeZone;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;
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
    private boolean mHidden;
    private boolean mFilterCondition;
    private List<String> ListOfNonTypicalAnswerTypes = Arrays.asList("text", "date");
    private List<Integer> mListOfAnswerIDs = new ArrayList<>();

    // Public Constructor
    public Question(String sQuestionBlueprint) {

        mQuestionBlueprint = sQuestionBlueprint;

        if (isFinish()) {
            mQuestionId = 99999;
            mQuestionText = extractQuestionTextFinish();
            mFilterId = -1;
            mFilterCondition = true;
            mTypeAnswer = "finish";
            mNumAnswers = 1;
            mHidden = extractHidden();
            mAnswers = new ArrayList<>();
            mAnswers.add(new Answer("Abschließen", 99999));

        } else {
            // Obtain Question ID
            mQuestionId = extractQuestionId();
            // Obtain Question Text
            mQuestionText = extractQuestionText();
            // Obtain Filter ID
            mFilterId = extractFilterId();
            // Obtain Filter ID Condition ("if true" or "if false")
            mFilterCondition = extractFilterCondition();
            // Obtain Answer Type (e.g. Radio, Button, Slider,...)
            mTypeAnswer = extractTypeAnswers();
            // Obtain Number of Answers
            mNumAnswers = extractNumAnswers();
            // Create List of Answers
            mAnswers = extractAnswerList();
            // Determine whether Element is hidden
            mHidden = extractHidden();

            //Log.e("Type",""+mQuestionId+": "+mTypeAnswer+": "+mNumAnswers);
        }
    }

    private int extractQuestionId() {
        // Obtain Question ID from Questionnaire
        return Integer.parseInt(mQuestionBlueprint.split("\"")[1]);
    }

    private String extractQuestionText() {
            // Obtain Question Text from Questionnaire
            return (mQuestionBlueprint.split("\\r?\\n")[2].split("<text>|</text>")[1]);
    }

    private String extractQuestionTextFinish() {
        // Obtain Question Text from Questionnaire
        return (mQuestionBlueprint.split("\\r?\\n")[1].split("<text>|</text>")[1]);
    }

    private int extractFilterId() {

        if (mQuestionBlueprint.split("\"")[4].contains("filter")) {
            // String carrying the Filter ID terms
            String sFilterIDLine = mQuestionBlueprint.split("\"")[5];
            // Filter ID is extracted
            String[] sFilterID = sFilterIDLine.split("_");

            if (sFilterID.length > 1) {
                return Integer.parseInt(sFilterID[1]);
            } else {
                return -1;
            }
        }
        return -1;
    }

    private boolean extractFilterCondition() {

        if (mQuestionBlueprint.split("\"")[4].contains("filter")) {
            // String carrying the Filter ID terms
            String sFilterIDLine = mQuestionBlueprint.split("\"")[5];
            // '!' before Filter ID means the Question is shown ONLY if ID was not checked
            if (sFilterIDLine.charAt(0) == '!') {
                return false;
            }
        }
        return true;
    }

    private int extractNumAnswers() {
        //if (mQuestionBlueprint.contains("hidden=\"true\"")) {
        //    return 0;
        //} else
        if (nonTypicalAnswer(mTypeAnswer)) {
            return 1;
        } else {
            // String Array carrying  the whole Question with Answers etc
            String[] itemQuestionLines = mQuestionBlueprint.split("\\r?\\n");
            // Obtain Number of Answers
            return (itemQuestionLines.length - 4) / 3;
        }
    }

    private boolean extractHidden() {
        if (mQuestionBlueprint.contains("hidden=\"true\"")) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isFinish() {
        // String Array carrying introductory Line with ID, Type, Filter
        String[] introductoryLine = mQuestionBlueprint.split("\"");
        if (introductoryLine.length == 1) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isHidden() {
        return mHidden; }

    private boolean nonTypicalAnswer(String sTypeAnswer) {
        return ListOfNonTypicalAnswerTypes.contains(sTypeAnswer);}

    private String extractTypeAnswers() {
        // String Array carrying introductory Line with ID, Type, Filter
        String[] introductoryLine = mQuestionBlueprint.split("\"");
        // Obtain Answer Type (e.g. Radio, Button, Slider,...)
        return introductoryLine[3];
    }

    private List<Answer> extractAnswerList() {

        /**  Automatically given answer Ids
        //
        //  Answer ID:  Class:
        //
        //  11111       meta data (no answer id provided in Quest)
        //  33333       editable text (no answer id provided in Quest)
        //  66666       forced blank space
        //  99999       finish (handled in earlier stage)
        **/

        // List of Answers
        List<Answer> listAnswers = new ArrayList<>();
        // String Array carrying  the whole Question with Answers etc
        String[] itemQuestionLines = mQuestionBlueprint.split("\\r?\\n");

        switch (mTypeAnswer) {
            case "radio": case "checkbox":

                for (int iAnswer = 0; iAnswer < mNumAnswers; iAnswer++) {

                    int nAnswerID = -1;
                    // Obtain Answer ID
                    String sAnswerIDLine = itemQuestionLines[iAnswer * 3 + 1 + 3];
                    String[] sAnswerIDSplit = sAnswerIDLine.split("\"");

                    // Obtain answer id
                    if (sAnswerIDSplit.length < 3) {
                        nAnswerID = mQuestionId*2;
                    } else {
                        nAnswerID = Integer.parseInt(sAnswerIDSplit[1]);
                    }

                    // Obtain answer id
                    String sAnswerTextLine = itemQuestionLines[iAnswer * 3 + 2 + 3];
                    String[] answerParts = sAnswerTextLine.split("<text>|</text>");
                    if (answerParts.length > 1) {
                        if (sAnswerIDLine.contains("default")) {
                            listAnswers.add(new Answer(answerParts[1], nAnswerID, true));
                        } else {
                            listAnswers.add(new Answer(answerParts[1], nAnswerID));
                        }
                    } else {
                        if (sAnswerIDLine.contains("default")) {
                            listAnswers.add(new Answer("", nAnswerID, true));
                        } else {
                            listAnswers.add(new Answer("", nAnswerID));
                        }
                    }
                }
                break;

            case "text":case "date":

                if (itemQuestionLines.length < 6) {
                    // Real editable text -> unfortunately no provided id since no answers defined -
                    // therefore question id is used with prefix 333
                    int nAnswerID = mQuestionId + 33300000;
                    listAnswers.add(new Answer("", nAnswerID));
                } else {
                    // Text/date that is used to acquire meta data
                    int nAnswerID = 11111;
                    String sAnswerTextLine = itemQuestionLines[5];
                    String[] answerParts = sAnswerTextLine.split("<text>|</text>");
                    listAnswers.add(new Answer(answerParts[1], nAnswerID));
                }
                break;

            default:
                Log.e("CASE","Something doesn't fit.");
                break;

        }

        /*


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
            if (nAnswerID == 66666) {
                // An ID of 66666 means an empty vertical Space
                // 33333 is usually editable Text
                listAnswers.add(new Answer("", nAnswerID));
            } else if (nAnswerID == 33333) {
                // Obtain Answer Text
                String sAnswerTextLine = itemQuestionLines[iAnswer * 3 + 2 + 3];

                String[] answerParts = sAnswerTextLine.split("<text>|</text>");
                listAnswers.add(new Answer(answerParts[1], nAnswerID, true));
                Log.e("AP 33333",answerParts[1]);
            } else {
                // Obtain Answer Text
                String sAnswerTextLine = itemQuestionLines[iAnswer * 3 + 2 + 3];
                String[] answerParts = sAnswerTextLine.split("<text>|</text>");
                if (sAnswerIDLine.contains("default")) {
                    Log.e("ID","default");
                    listAnswers.add(new Answer(answerParts[1], nAnswerID, true));
                } else {
                    listAnswers.add(new Answer(answerParts[1], nAnswerID));
                }
            }

        }
        */
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

    public List<Integer> getAnswerIDs() {
        if (mNumAnswers > 0) {
            for (int iAnswer = 0; iAnswer < mNumAnswers; iAnswer++) {
                mListOfAnswerIDs.add(mAnswers.get(iAnswer).Id);
            }
        }
        return mListOfAnswerIDs;
    }
}
