package com.fragtest.android.pa;

import android.content.Context;
import android.graphics.Color;
import android.os.Environment;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.RadioGroup;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.os.Environment.getExternalStorageDirectory;
import static android.util.Log.e;


/**
 * Created by ulrikkowalk on 28.02.17.
 */

public class Questionnaire {

    // Number of Pages in Questionnaire (visible and hidden)
    private int mNumPages;
    // Current Element Position in Questionnaire
    private int mPosition;
    // Object containing System Information e.g. Time and Date
    private MetaData mMetaData;
    // String containing the whole raw basis XML Input
    private String mRawInput;
    // String Array that contains all Question Basis Data
    private String[] mQuestionnaire;
    private List<String> mQuestionList;
    // Context of QuestionnairePageAdapter for Visibility
    private QuestionnairePagerAdapter mContextQPA;
    // Context of MainActivity()
    private Context mContext;
    // Accumulator for IDs checked in by given Answers
    private AnswerIDs mAnswerIDs;

    // Performs general Calculations
    private Calculations mCalculations;
    // Basic Information about all available Questions
    private ArrayList<QuestionInfo> mQuestionInfo;

    private FileIO mFileIO;

    // Flag: Display forced empty vertical Spaces
    private boolean acceptBlankSpaces = false;

    public Questionnaire(Context context, QuestionnairePagerAdapter contextQPA) {

        mContext = context;
        mContextQPA = contextQPA;
        mFileIO = new FileIO();
        mQuestionInfo = new ArrayList<>();
        //mRawInput = readRawTextFile(mContext, R.raw.question_single);
        mRawInput = mFileIO.readRawTextFile();
        mQuestionnaire = mRawInput.split("<question|</question>|<finish>|</finish>");
        mQuestionList = stringArrayToListString(mQuestionnaire);
        mQuestionList = thinOutList(mQuestionList);
        mNumPages = mQuestionList.size();
        mCalculations = new Calculations(mContext);
        // Contains all IDs of checked Elements and Methods of Logic
        mAnswerIDs = new AnswerIDs(mContextQPA);
    }

    // Generate a Layout for Question at desired Position based on String Blueprint
    public Question createQuestion(int position) {
        mPosition = position;
        String sQuestionBlueprint = mQuestionList.get(mPosition);
        Question question = new Question(sQuestionBlueprint);
        mQuestionInfo.add(new QuestionInfo(question, question.getQuestionId(),
                question.getFilterId(), question.getFilterCondition(), position, question.isHidden()));

        return question;
    }

    // Builds the Layout of each Stage Question
    public LinearLayout generateView(Question question) {

        // Are the answers to this specific Question grouped as Radio Button Group?
        boolean bRadio = false;

        LinearLayout linearContainer = new LinearLayout(mContext);
        LinearLayout.LayoutParams linearContainerParams =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
        linearContainer.setOrientation(LinearLayout.VERTICAL);
        linearContainer.setLayoutParams(linearContainerParams);
        linearContainer.setBackgroundColor(Color.WHITE);

        // TextView carrying the Question
        QuestionText questionText = new QuestionText(mContext, question.getQuestionId(),
                question.getQuestionText(), linearContainer);
        questionText.addQuestion();

        // Creates a Canvas for the Answer Layout
        AnswerLayout answerLayout = new AnswerLayout(mContext);
        linearContainer.addView(answerLayout.scrollContent);

        // Format of Answer e.g. "radio", "checkbox", ...
        String sType = question.getTypeAnswer();

        // Answer Buttons of type "radio" are grouped and handled together
        // Only needed in case of Radio Buttons
        final RadioGroup answerRadioGroup = new RadioGroup(mContext);
        answerRadioGroup.setOrientation(RadioGroup.VERTICAL);
        final List<Integer> listOfRadioIds = new ArrayList<>();

        // Number of possible Answers
        int nNumAnswers = question.getNumAnswers();
        // List carrying all Answers and Answer IDs
        List<Answer> answerList = question.getAnswers();

        // Iteration over all possible Answers
        for (int iAnswer = 0; iAnswer < nNumAnswers; iAnswer++) {

            /** ANSWER OBJECT **/
            // Obtain Answer specific Parameters
            Answer currentAnswer = answerList.get(iAnswer);
            String sAnswer = currentAnswer.Text;
            int nAnswerID = currentAnswer.Id;
            boolean isDefault = currentAnswer.isDefault();

            if (((nAnswerID == 66666) && (acceptBlankSpaces)) || (nAnswerID != 66666)) {

                switch (sType) {
                    case "radio": {
                        bRadio = true;
                        AnswerTypeRadio answer = new AnswerTypeRadio(mContext,
                                nAnswerID, sAnswer, answerRadioGroup);
                        if (isDefault) {
                            answer.setChecked(); mAnswerIDs.add(nAnswerID);
                        }
                        answer.addAnswer();
                        listOfRadioIds.add(nAnswerID);
                        break;
                    }
                    case "checkbox": {
                        bRadio = false;
                        AnswerTypeCheckBox answer = new AnswerTypeCheckBox(mContext,
                                nAnswerID, sAnswer, answerLayout);
                        if (isDefault) {
                            answer.setChecked(); mAnswerIDs.add(nAnswerID);
                        }
                        answer.addAnswer();
                        mAnswerIDs = answer.addClickListener(mAnswerIDs);
                        break;
                    }
                    case "text": {
                        bRadio = false;
                        AnswerTypeText answer = new AnswerTypeText(mContext,
                                nAnswerID, answerLayout);
                        answer.addAnswer();
                        break;
                    }
                    case "finish": {
                        bRadio = false;
                        AnswerTypeFinish answer = new AnswerTypeFinish(mContext,
                                nAnswerID, answerLayout);
                        answer.addClickListener();
                        answer.addAnswer();
                        break;
                    }
                    default: {
                        bRadio = false;
                        e("strange", "Wat nu?");
                        break;
                    }
                }
            }
        }

        // In Case of Radio Buttons, additional RadioGroup is implemented
        if (bRadio) {
            answerRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedID) {
                    // In Case of Radio Buttons checking one means unchecking all other Elements
                    // Therefore onClickListening must be handled on Group Level
                    // listOfRadioIds contains all IDs of current Radio Group
                    mAnswerIDs.removeAll(listOfRadioIds);
                    mAnswerIDs.add(checkedID);
                    answerRadioGroup.check(checkedID);
                    // Toggle Visibility of suited/unsuited frames
                    checkVisibility();
                }
            });
            answerLayout.layoutAnswer.addView(answerRadioGroup);
        }
        return linearContainer;
    }

    public int getNumPages() {
        return mNumPages;
    }

    public int getId(Question question) {
        return question.getQuestionId();
    }

    // Function checks all available Pages whether their filtering Condition has been met and
    // toggles Visibility by destroying or creating the Views and adding them to the List of Views
    // which is handled by QuestionnairePagerAdapter
    public void checkVisibility() {

        for (int iPos = 0; iPos < mQuestionInfo.size(); iPos++) {

            QuestionInfo qI = mQuestionInfo.get(iPos);

            if (qI.getFilterId() != -1                              // Specific Filter ID
                    && qI.getCondition()                            // which MUST exist
                    && mAnswerIDs.contains(qI.getFilterId())        // DOES exist
                    && !qI.isActive())                              // on an INACTIVE Layout
            {
                addQuestion(iPos);
            }

            if (qI.getFilterId() != -1                              // Specific Filter ID
                    && qI.getCondition()                            // which MUST exist
                    && !mAnswerIDs.contains(qI.getFilterId())       // does NOT exist
                    && qI.isActive())                               // on an ACTIVE Layout
            {
                removeQuestion(iPos);
            }

            if (qI.getFilterId() != -1                                  // Specific Filter ID
                    && !qI.getCondition()                               // which MUST NOT exist
                    && mAnswerIDs.contains(qI.getFilterId())            // DOES exist
                    && !qI.isActive())                                  // on an INACTIVE Layout
            {
                addQuestion(iPos);
            }

            if ((qI.getFilterId() != -1)                                // Specific Filter ID
                    && (!qI.getCondition())                             // which MUST NOT exist
                    && mAnswerIDs.contains(qI.getFilterId())            // DOES exist
                    && qI.isActive())                                   // on an ACTIVE Layout
            {
                removeQuestion(iPos);
            }

            if (qI.getFilterId() != -1                                  // Specific Filter ID
                    && !qI.getCondition()                               // which MUST NOT exist
                    && !mAnswerIDs.contains(qI.getFilterId())           // DOES NOT exist
                    && !qI.isActive())                                  // on an INACTIVE Layout
            {
                addQuestion(iPos);
            }

            if (qI.isHidden() && qI.isActive()) {
                qI.setInactive();
                removeQuestion(iPos);
            }
        }
    }


    /**
     * ----------------------------------- Useful Methods -------------------------------------
     **/


    private boolean addQuestion(int iPos) {
        mQuestionInfo.get(iPos).setActive();
        // View is fetched from Storage List and added to Active List
        mContextQPA.addView(mContextQPA.mListOfViewsStorage.get(iPos).getView(),
                mContextQPA.mListOfActiveViews.size(),
                mContextQPA.mListOfViewsStorage.get(iPos).getPositionInRaw());
        renewPositionsInPager();
        mContextQPA.notifyDataSetChanged();
        return true;
    }

    private boolean removeQuestion(int iPos) {
        mQuestionInfo.get(iPos).setInactive();
        // Remove View from ActiveList
        mContextQPA.removeView(mQuestionInfo.get(iPos).getPositionInPager());
        renewPositionsInPager();
        mContextQPA.notifyDataSetChanged();
        return true;
    }

    private void renewPositionsInPager() {
        for (int iItem = 0; iItem < mQuestionInfo.size(); iItem++) {
            int iId = mQuestionInfo.get(iItem).getId();
            mQuestionInfo.get(iItem).setPositionInPager(mContextQPA.getPositionFromId(iId));
        }
    }

    private List<String> thinOutList(List<String> mQuestionList) {
        for (int iItem = mQuestionList.size() - 1; iItem >= 0; iItem = iItem - 2) {
            mQuestionList.remove(iItem);
        }
        return mQuestionList;
    }

    private List<String> stringArrayToListString(String[] stringArray) {
        List<String> listString = new ArrayList<>();
        Collections.addAll(listString, stringArray);
        return listString;
    }

}
