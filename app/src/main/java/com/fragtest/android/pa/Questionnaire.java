package com.fragtest.android.pa;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.R.attr.y;


/**
 * Created by ulrikkowalk on 28.02.17.
 */

public class Questionnaire {

    // Number of pages in questionnaire (visible and hidden)
    private int mNumPages;
    // Current element position in questionnaire
    private int mPosition;
    // String containing the whole raw basis XML input
    private String mRawInput;
    // String array that contains all question basis data
    private String[] mQuestionnaire;
    // List containing all questions (including all attached information)
    private List<String> mQuestionList;

    //private List<Integer> mAnswerList;
    // Context of QuestionnairePageAdapter for visibility
    private QuestionnairePagerAdapter mContextQPA;
    // Context of MainActivity()
    private Context mContext;
    // Accumulator for ids checked in by given answers
    private AnswerIDs mAnswerIDs;
    // Accumulator for Text and id of text format answers
    private AnswerTexts mAnswerTexts;
    // Basic information about all available questions
    private ArrayList<QuestionInfo> mQuestionInfo;

    private MetaData mMetaData;

    private FileIO mFileIO;

    // Flag: display forced empty vertical spaces
    private boolean acceptBlankSpaces = false;

    public Questionnaire(Context context, QuestionnairePagerAdapter contextQPA) {

        mContext = context;
        mContextQPA = contextQPA;
        mFileIO = new FileIO();
        mQuestionInfo = new ArrayList<>();
        mRawInput = mFileIO.readRawTextFile();

        mMetaData = new MetaData(mContext, mRawInput);
        mMetaData.initialise();

        // Split basis data into question segments
        mQuestionnaire = mRawInput.split("<question|</question>|<finish>|</finish>");
        mQuestionList = stringArrayToListString(mQuestionnaire);
        mQuestionList = thinOutList(mQuestionList);
        mNumPages = mQuestionList.size();

        //mMetaData.setNumQuestions(mNumPages);

        // Contains all ids of checked elements
        mAnswerIDs = new AnswerIDs();
        // Contains all contents of text answers
        mAnswerTexts = new AnswerTexts();

    }

    // Generate a Layout for Question at desired Position based on String Blueprint
    public Question createQuestion(int position) {
        mPosition = position;
        String sQuestionBlueprint = mQuestionList.get(mPosition);
        Question question = new Question(sQuestionBlueprint);
        mQuestionInfo.add(new QuestionInfo(question, question.getQuestionId(),
                question.getFilterId(), question.getFilterCondition(), position,
                question.isHidden(), question.getAnswerIDs()));
        mMetaData.addQuestion(question);

        return question;
    }

    // Builds the Layout of each Stage Question
    public LinearLayout generateView(Question question) {

        // Are the answers to this specific Question grouped as Radio Button Group?
        boolean bRadio = false;
        boolean bSlider = false;

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

        // In case of sliderFix type
        AnswerTypeSliderFix answerSlider = new AnswerTypeSliderFix(mContext, answerLayout);


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
                        AnswerTypeText answer = new AnswerTypeText(mContext,
                                nAnswerID, answerLayout, mAnswerTexts);
                        mAnswerTexts = answer.addClickListener(mAnswerTexts);
                        answer.addAnswer();
                        break;
                    }
                    case "finish": {
                        AnswerTypeFinish answer = new AnswerTypeFinish(mContext,
                                nAnswerID, answerLayout);
                        answer.addClickListener(mContext, mMetaData, mAnswerIDs, mAnswerTexts);
                        answer.addAnswer();
                        break;
                    }
                    case "sliderFix": {
                        bSlider = true;
                        answerSlider.addAnswer(nAnswerID,sAnswer);
                        break;
                    }
                    default: {
                        bRadio = false;
                        Log.i("Questionnaire", "Weird object found. ID: "+question.getQuestionId());
                        break;
                    }
                }
            }
        }

        // In case of sliderFix, create View
        if (bSlider) {
            answerSlider.buildSlider();
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

    // Function checks all available pages whether their filtering condition has been met and
    // toggles visibility by destroying or creating the views and adding them to the list of views
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
        manageCheckedIDs();
    }

    public void manageCheckedIDs() {

        // Obtain outdated ids
        ArrayList<Integer> listOfIDsToRemove = new ArrayList<>();
        // Iterate over all ids that have been checked before
        for (int iID = 0; iID < mAnswerIDs.size(); iID++) {
            int nID = mAnswerIDs.get(iID);
            boolean bFoundID = false;
            // Iterate over all currently visible/active Views
            for (int iView = 0; iView < mContextQPA.mListOfActiveViews.size(); iView++) {
                List<Answer> listOfAnswerIDs =
                        mContextQPA.mListOfActiveViews.get(iView).getListOfAnswerIDs();
                for (int iAnswer = 0; iAnswer < listOfAnswerIDs.size(); iAnswer++) {
                    if (listOfAnswerIDs.get(iAnswer).Id == nID){
                        bFoundID = true;
                    }
                }
            }
            if (!bFoundID && nID != 111111) {
                listOfIDsToRemove.add(nID);
            }
        }

        // Remove all outdated ids
        if (listOfIDsToRemove.size() > 0) {
            mAnswerIDs.removeAll(listOfIDsToRemove);
        }


    }

    public void revertEnteredText() {

        for (int iText = 0; iText < mAnswerTexts.size(); iText++) {
            int nTextId = mAnswerTexts.get(iText).getID();
            for (int iView = 0; iView < mContextQPA.mListOfActiveViews.size(); iView++) {
                if (mContextQPA.mListOfActiveViews.get(iView).getView().getId() == nTextId) {
                    TextView tV = (TextView) mContextQPA.mListOfActiveViews.get(iView).getView().findViewById(nTextId);
                    tV.setText("");
                }
            }
        }
    }


    /**
     * ----------------------------------- Useful Methods -------------------------------------
     **/


    // Adds the question to the displayed list
    private boolean addQuestion(int iPos) {
        mQuestionInfo.get(iPos).setActive();
        // View is fetched from Storage List and added to Active List
        mContextQPA.addView(mContextQPA.mListOfViewsStorage.get(iPos).getView(),
                mContextQPA.mListOfActiveViews.size(),
                mContextQPA.mListOfViewsStorage.get(iPos).getPositionInRaw(),
                mContextQPA.mListOfViewsStorage.get(iPos).getListOfAnswerIDs());
        renewPositionsInPager();
        mContextQPA.notifyDataSetChanged();

        return true;
    }

    // Removes the question from the displayed list
    private boolean removeQuestion(int iPos) {
        mQuestionInfo.get(iPos).setInactive();

        // Remove checked answers on removed questions
        String sType = mQuestionInfo.get(iPos).getQuestion().getTypeAnswer();
        List<Integer> mListOfAnswerIDs = mQuestionInfo.get(iPos).getAnswerIDs();

        for (int iAnswer = 0; iAnswer < mListOfAnswerIDs.size(); iAnswer++) {
            if (sType.equals("checkbox")) {
                CheckBox checkBox = (CheckBox) mContextQPA.mListOfViewsStorage.get(iPos).getView().
                        findViewById(mQuestionInfo.get(iPos).getAnswerIDs().get(iAnswer));
                if (checkBox != null) {
                    checkBox.setChecked(false);
                }
            }
        }


        // Remove View from ActiveList
        mContextQPA.removeView(mQuestionInfo.get(iPos).getPositionInPager());
        renewPositionsInPager();
        mContextQPA.notifyDataSetChanged();
        return true;
    }

    // Renews all the positions in information object gathered from actual order
    private void renewPositionsInPager() {
        for (int iItem = 0; iItem < mQuestionInfo.size(); iItem++) {
            int iId = mQuestionInfo.get(iItem).getId();
            mQuestionInfo.get(iItem).setPositionInPager(mContextQPA.getPositionFromId(iId));
        }
    }

    // Removes irrelevant data from question sheet
    private List<String> thinOutList(List<String> mQuestionList) {
        for (int iItem = mQuestionList.size() - 1; iItem >= 0; iItem = iItem - 2) {
            mQuestionList.remove(iItem);
        }
        return mQuestionList;
    }

    // Turns an array of Strings into a List of Strings
    private List<String> stringArrayToListString(String[] stringArray) {
        List<String> listString = new ArrayList<>();
        Collections.addAll(listString, stringArray);
        return listString;
    }

    public boolean clearAnswerIDs(){
        mAnswerIDs = new AnswerIDs();
        checkVisibility();
        return true;
    }

    public boolean clearAnswerTexts(){
        revertEnteredText();
        mAnswerTexts = new AnswerTexts();
        return true;
    }
}
