package com.fragtest.android.pa;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by ulrikkowalk on 28.02.17.
 */

public class Question extends AppCompatActivity {

    private String LOG_STRING = "Question";
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
    private List<Integer> mListOfAnswerIds = new ArrayList<>();

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
            // Obtain Question Id
            mQuestionId = extractQuestionId();
            // Obtain Question Text
            mQuestionText = extractQuestionText();
            // Obtain Filter Id
            mFilterId = extractFilterId();
            // Obtain Filter Id Condition ("if true" or "if false")
            mFilterCondition = extractFilterCondition();
            // Obtain Answer Type (e.g. Radio, Button, Slider,...)
            mTypeAnswer = extractTypeAnswers();

            if (!mTypeAnswer.equals("text")) {
                // Create List of Answers
                mAnswers = extractAnswerList();
            } else {
                mAnswers = new ArrayList<>();
                mAnswers.add(new Answer("", 33333, false));
            }
            // Obtain Number of Answers
            mNumAnswers = extractNumAnswers();
            // Determine whether Element is hidden
            mHidden = extractHidden();
        }
    }

    private int extractQuestionId() {
        // Obtain Question Id from Questionnaire
        return Integer.parseInt(mQuestionBlueprint.split("id=\"")[1].split("\"")[0]);
    }

    private String extractQuestionText() {
            // Obtain Question Text from Questionnaire
        return (mQuestionBlueprint.split("<label>|</label>")[1].split("<text>|</text>")[1]);
    }

    private String extractQuestionTextFinish() {
        // Obtain Question Text from Questionnaire
        return (mQuestionBlueprint.split("\\r?\\n")[1].split("<text>|</text>")[1]);
    }

    private int extractFilterId() {
        if (mQuestionBlueprint.split("\"")[4].contains("filter")) {
            return Integer.parseInt(
                    mQuestionBlueprint.split("filter=\"")[1].split("\"")[0].split("_")[1]);
        }
        return -1;
    }

    private boolean extractFilterCondition() {

        if (mQuestionBlueprint.split("\"")[4].contains("filter")) {
            // String carrying the Filter Id terms
            String sFilterIdLine = mQuestionBlueprint.split("\"")[5];
            // '!' before Filter Id means the Question is shown ONLY if Id was not checked
            if (sFilterIdLine.charAt(0) == '!') {
                return false;
            }
        }
        return true;
    }

    private int extractNumAnswers() {
        if (nonTypicalAnswer(mTypeAnswer)) {
            return 1;
        } else {
            // Obtain Number of Answers
            return mAnswers.size();
        }
    }

    private String extractTypeAnswers() {
        // Obtain Answer Type (e.g. Radio, Button, Slider,...)
        return mQuestionBlueprint.split("type=\"")[1].split("\"")[0];
    }

    private List<Answer> extractAnswerList() {

        // List of Answers
        List<Answer> listAnswers = new ArrayList<>();
        String[] stringArray = mQuestionBlueprint.split("<option|<default");

        String answerString = "";
        int answerId = -1;
        boolean isDefault = false;

        for (int iA = 1; iA < stringArray.length; iA++) {
            //Log.i(LOG_STRING, "" + iA + " " + stringArray[iA]);


            if (stringArray[iA].contains("option")) {
                //Log.e(LOG_STRING,"option");
                isDefault = false;
                if (stringArray[iA].contains("id=") && stringArray[iA].split("id=\"|\"").length > 1) {
                    //Log.e(LOG_STRING,stringArray[iA].split("id=\"|\"")[1]);
                    answerId = Integer.parseInt(stringArray[iA].split("id=\"|\"")[1]);
                }
                if (stringArray[iA].split("<text>|</text>").length > 1) {
                    //Log.e(LOG_STRING, stringArray[iA].split("<text>|</text>")[1]);
                    answerString = stringArray[iA].split("<text>|</text>")[1];
                }

                listAnswers.add(new Answer(
                        answerString,
                        answerId,
                        isDefault
                ));
            }

            if (stringArray[iA].contains("default")) {
                isDefault = true;
                //Log.e(LOG_STRING,"default");
                if (stringArray[iA].contains("id=") && stringArray[iA].split("id=\"|\"").length > 1) {
                    //Log.e(LOG_STRING,stringArray[iA].split("id=\"|\"")[1]);
                    answerId = Integer.parseInt(stringArray[iA].split("id=\"|\"")[1]);
                }
                if (stringArray[iA].split("<text>|</text>").length > 1) {
                    //Log.e(LOG_STRING, stringArray[iA].split("<text>|</text>")[1]);
                    answerString = stringArray[iA].split("<text>|</text>")[1];
                }

                listAnswers.add(new Answer(
                        answerString,
                        answerId,
                        isDefault
                ));
            }
/*
            if (stringArray[iA].contains("option")) {
                if (stringArray[iA].contains("id=")) {
                    listAnswers.add(new Answer(
                            stringArray[iA + 1].split("text>")[1],
                            Integer.parseInt(stringArray[iA].split("id=\"|\"")[1])
                    ));
                } else {
                    listAnswers.add(new Answer(
                            stringArray[iA + 1].split("text>")[1],
                            11111
                    ));
                }

            } else if (stringArray[iA].contains("default")) {
                if (stringArray[iA].contains("id=")) {
                    if (stringArray[iA].contains("id=")) {
                        listAnswers.add(new Answer(
                                stringArray[iA + 1].split("text>")[1],
                                Integer.parseInt(stringArray[iA].split("id=\"|\"")[1]),
                                true
                        ));
                    }
                } else {
                    listAnswers.add(new Answer(
                            stringArray[iA + 1].split("text>")[1],
                            11111,
                            true
                    ));
                }
            }
            */
        }
        return listAnswers;
    }


    private List<Answer> FextractAnswerList() {

        /**  Automatically given answer Ids
         //
         //  Answer Id:  Class:
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
            case "radio": case "checkbox": case "sliderFix": case "sliderFree": case "emoji":

                for (int iAnswer = 0; iAnswer < mNumAnswers; iAnswer++) {

                    int nAnswerId = -1;
                    // Obtain Answer Id
                    String sAnswerIdLine = itemQuestionLines[iAnswer * 3 + 1 + 3];
                    String[] sAnswerIdSplit = sAnswerIdLine.split("\"");

                    // Obtain answer id
                    if (sAnswerIdSplit.length < 3) {
                        nAnswerId = mQuestionId*2;
                    } else {
                        nAnswerId = Integer.parseInt(sAnswerIdSplit[1]);
                    }

                    // Obtain answer id
                    String sAnswerTextLine = itemQuestionLines[iAnswer * 3 + 2 + 3];
                    String[] answerParts = sAnswerTextLine.split("<text>|</text>");
                    if (answerParts.length > 1) {
                        if (sAnswerIdLine.contains("default")) {
                            listAnswers.add(new Answer(answerParts[1], nAnswerId, true));
                        } else {
                            listAnswers.add(new Answer(answerParts[1], nAnswerId));
                        }
                    } else {
                        if (sAnswerIdLine.contains("default")) {
                            listAnswers.add(new Answer("", nAnswerId, true));
                        } else {
                            listAnswers.add(new Answer("", nAnswerId));
                        }
                    }
                }
                break;

            case "text": case "date":

                if (itemQuestionLines.length < 6) {
                    // Real editable text -> unfortunately no provided id since no answers defined -
                    // therefore question id is used with prefix 333
                    int nAnswerId = mQuestionId + 33300000;
                    listAnswers.add(new Answer("", nAnswerId));
                } else {
                    // Text/date that is used to acquire meta data
                    int nAnswerId = 11111;
                    String sAnswerTextLine = itemQuestionLines[5];
                    Log.i(LOG_STRING, sAnswerTextLine);
                    String[] answerParts = sAnswerTextLine.split("<text>|</text>");
                    listAnswers.add(new Answer(answerParts[1], nAnswerId));
                }
                break;

            default:
                Log.e("CASE","Something doesn't fit.");
                break;
        }
        return listAnswers;
    }


    private boolean extractHidden() {
        if (mQuestionBlueprint.contains("hidden=\"true\"")) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isFinish() {
        // String Array carrying introductory Line with Id, Type, Filter
        String[] introductoryLine = mQuestionBlueprint.split("\"");
        if (introductoryLine.length == 1) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isHidden() {
        return mHidden;
    }

    private boolean nonTypicalAnswer(String sTypeAnswer) {
        return ListOfNonTypicalAnswerTypes.contains(sTypeAnswer);
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

    public List<Integer> getAnswerIds() {
        Log.e("num",""+mNumAnswers);
        if (mNumAnswers > 0) {
            for (int iAnswer = 0; iAnswer < mNumAnswers; iAnswer++) {
                mListOfAnswerIds.add(mAnswers.get(iAnswer).Id);
            }
        }
        return mListOfAnswerIds;
    }
}
