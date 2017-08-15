package com.fragtest.android.pa.Questionnaire;

import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by ulrikkowalk on 28.02.17.
 */

public class Question extends AppCompatActivity {

    private final String LOG_STRING = "Question";
    private final String mQuestionBlueprint;
    private final String mQuestionText;
    private final String mTypeAnswer;
    private final int mNumAnswers;
    private final int mQuestionId;
    private final boolean mHidden;
    //private boolean mFilterCondition;
    private final boolean mMandatory;
    private final List<String> ListOfNonTypicalAnswerTypes = Arrays.asList("text", "date");
    private final List<Integer> mListOfAnswerIds = new ArrayList<>();
    private List<Answer> mAnswers;
    private ArrayList<Integer> mFilterId;

    // Public Constructor
    public Question(String sQuestionBlueprint) {

        mQuestionBlueprint = sQuestionBlueprint;
        mFilterId = new ArrayList<>();

        if (isFinish()) {
            mQuestionId = 99999;
            mQuestionText = extractQuestionTextFinish();
            mMandatory = true;
            mTypeAnswer = "finish";
            mNumAnswers = 1;
            mHidden = false;
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
            //mFilterCondition = extractFilterCondition();
            // Obtain Answer Type (e.g. Radio, Button, Slider,...)
            mTypeAnswer = extractTypeAnswers();
            // Obtain information whether question is mandatory
            mMandatory = extractMandatory();

            // Create List of Answers
            mAnswers = extractAnswerList();
            // In case of real text input no answer text is given
            if (mAnswers.size() == 0) {
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

    private ArrayList<Integer> extractFilterId() {
        ArrayList<Integer> listOfFilterIds = new ArrayList<>();

        if (mQuestionBlueprint.split("filter=\"").length > 1) {
            String[] arrayTmp = mQuestionBlueprint.split("filter=\"")[1].split(",");
            for (int iId = 0; iId < arrayTmp.length; iId++) {

                // Negative factor represents EXCLUSION filter
                int nFactor = 1;
                if (arrayTmp[iId].startsWith("!")) {
                    nFactor = -1;
                }
                listOfFilterIds.add(Integer.parseInt(
                        arrayTmp[iId].split("_")[1].split("\"|>")[0]) * nFactor);
            }
        }
        return listOfFilterIds;
    }

    private boolean extractMandatory() {
        return mQuestionBlueprint.contains("mandatory=\"true\"");
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

            if (stringArray[iA].contains("option")) {
                isDefault = false;
                if (stringArray[iA].contains("id=") && stringArray[iA].split("id=\"|\"").length > 1) {
                    answerId = Integer.parseInt(stringArray[iA].split("id=\"|\"")[1]);
                }
                if (stringArray[iA].split("<text>|</text>").length > 1) {
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
                if (stringArray[iA].contains("id=") && stringArray[iA].split("id=\"|\"").length > 1) {
                    answerId = Integer.parseInt(stringArray[iA].split("id=\"|\"")[1]);
                }
                if (stringArray[iA].split("<text>|</text>").length > 1) {
                    answerString = stringArray[iA].split("<text>|</text>")[1];
                }
                listAnswers.add(new Answer(
                        answerString,
                        answerId,
                        isDefault
                ));
            }
        }
        return listAnswers;
    }

    private boolean extractHidden() {
        return mQuestionBlueprint.contains("hidden=\"true\"");
    }

    public boolean isFinish() {
        // String Array carrying introductory Line with Id, Type, Filter
        String[] introductoryLine = mQuestionBlueprint.split("\"");
        return introductoryLine.length == 1;
    }

    public boolean isHidden() {
        return mHidden;
    }

    public boolean isMandatory() {
        return mMandatory;
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

    public ArrayList<Integer> getFilterId() {
        return mFilterId;
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
        if (mNumAnswers > 0) {
            for (int iAnswer = 0; iAnswer < mNumAnswers; iAnswer++) {
                mListOfAnswerIds.add(mAnswers.get(iAnswer).Id);
            }
        }
        return mListOfAnswerIds;
    }
}
