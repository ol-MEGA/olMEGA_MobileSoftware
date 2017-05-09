package com.fragtest.android.pa;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Created by ulrikkowalk on 28.02.17.
 */

public class Questionnaire {

    private static String CLASS_NAME = "Questionnaire";
    // Accumulator for ids checked in by given answers
    public AnswerIds mAnswerIds;
    // Number of pages in questionnaire (visible and hidden)
    private int mNumPages;
    // List containing all questions (including all attached information)
    private List<String> mQuestionList;
    // Context of QuestionnairePageAdapter for visibility
    private QuestionnairePagerAdapter mContextQPA;
    // Context of MainActivity()
    private Context mContext;
    // Accumulator for Text and id of text format answers
    private AnswerTexts mAnswerTexts;
    // Accumulator for question id and metric input
    private AnswerValues mAnswerValues;
    // Basic information about all available questions
    private ArrayList<QuestionInfo> mQuestionInfo;
    private MetaData mMetaData;
    private FileIO mFileIO;

    private EvaluationList mEvaluationList;

    // Flag: display forced empty vertical spaces
    private boolean acceptBlankSpaces = false;
    // Use on screen debug output
    private boolean isDebug = true;

    public Questionnaire(Context context, QuestionnairePagerAdapter contextQPA) {

        mContext = context;
        mContextQPA = contextQPA;
        mFileIO = new FileIO();
        mQuestionInfo = new ArrayList<>();

        // Contains all ids of checked elements
        mAnswerIds = new AnswerIds();
        // Contains all contents of text answers
        mAnswerTexts = new AnswerTexts();
        // Contains all metric answers
        mAnswerValues = new AnswerValues();

        mEvaluationList = new EvaluationList();
    }

    public void setUp(){
        //mRawInput = mFileIO.readRawTextFile();
        // offline version
        String mRawInput = mFileIO.readRawTextFile(mContext, R.raw.question_single);

        mMetaData = new MetaData(mContext, mRawInput);
        mMetaData.initialise();

        // Split basis data into question segments
        String[] questionnaire = mRawInput.split("<question|</question>|<finish>|</finish>");
        mQuestionList = stringArrayToListString(questionnaire);
        mQuestionList = thinOutList(mQuestionList);
        mNumPages = mQuestionList.size();
    }

    // Generate a Layout for Question at desired Position based on String Blueprint
    public Question createQuestion(int position) {

        String sQuestionBlueprint = mQuestionList.get(position);
        Question question = new Question(sQuestionBlueprint);
        mQuestionInfo.add(new QuestionInfo(question, question.getQuestionId(),
                question.getFilterId(), question.getFilterCondition(), position,
                question.isHidden(), question.getAnswerIds()));
        mMetaData.addQuestion(question);

        return question;
    }

    // Builds the Layout of each Stage Question
    public LinearLayout generateView(Question question) {

        // Are the answers to this specific Question grouped as Radio Button Group?
        boolean isRadio = false;
        boolean isSliderFix = false;
        boolean isSliderFree = false;
        boolean isEmoji = false;

        LinearLayout answerContainer = new LinearLayout(mContext);
        LinearLayout.LayoutParams linearContainerParams =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
        answerContainer.setOrientation(LinearLayout.VERTICAL);
        answerContainer.setLayoutParams(linearContainerParams);
        answerContainer.setBackgroundColor(Color.WHITE);

        // TextView carrying the Question
        QuestionText questionText = new QuestionText(mContext, question.getQuestionId(),
                question.getQuestionText(), answerContainer);
        questionText.addQuestion();

        // Creates a Canvas for the Answer Layout
        final AnswerLayout answerLayout = new AnswerLayout(mContext);
        answerContainer.addView(answerLayout.scrollContent);

        // Format of Answer e.g. "radio", "checkbox", ...
        String sType = question.getTypeAnswer();

        // Answer Buttons of type "radio" are grouped and handled together
        // Only needed in case of Radio Buttons
        final RadioGroup answerRadioGroup = new RadioGroup(mContext);
        answerRadioGroup.setOrientation(RadioGroup.VERTICAL);
        // Works with all single-option tasks e.g. radio buttons, slider, ...
        final List<Integer> listOfRadioIds = new ArrayList<>();

        // In case of emoji type
        final AnswerTypeEmoji answerTypeEmoji = new AnswerTypeEmoji(
                mContext, mContextQPA, answerLayout);

        // In case of sliderFix type
        final AnswerTypeSliderFix answerSliderFix = new AnswerTypeSliderFix(
                mContext, answerLayout, question.getQuestionId());

        // In case of sliderFree type
        final AnswerTypeSliderFree answerSliderFree = new AnswerTypeSliderFree(
                mContext, answerLayout, question.getQuestionId());

        // Number of possible Answers
        int nNumAnswers = question.getNumAnswers();
        // List carrying all Answers and Answer Ids
        List<Answer> answerList = question.getAnswers();

        // Iteration over all possible Answers attributed to current question
        for (int iAnswer = 0; iAnswer < nNumAnswers; iAnswer++) {

            /** ANSWER OBJECT **/
            // Obtain Answer specific Parameters
            Answer currentAnswer = answerList.get(iAnswer);
            String sAnswer = currentAnswer.Text;
            int nAnswerId = currentAnswer.Id;
            boolean isDefault = currentAnswer.isDefault();

            if (((nAnswerId == 66666) && (acceptBlankSpaces)) || (nAnswerId != 66666)) {

                switch (sType) {
                    case "radio": {
                        isRadio = true;
                        AnswerTypeRadio answer = new AnswerTypeRadio(mContext,
                                nAnswerId, sAnswer, answerRadioGroup);
                        if (isDefault) {
                            answer.setChecked();
                            mAnswerIds.add(nAnswerId);
                        }
                        answer.addAnswer();
                        listOfRadioIds.add(nAnswerId);
                        break;
                    }
                    case "checkbox": {
                        AnswerTypeCheckBox answer = new AnswerTypeCheckBox(mContext,
                                nAnswerId, sAnswer, answerLayout);
                        if (isDefault) {
                            answer.setChecked();
                            mAnswerIds.add(nAnswerId);
                        }
                        answer.addAnswer();
                        mAnswerIds = answer.addClickListener(mAnswerIds);
                        break;
                    }
                    case "text": {
                        AnswerTypeText answer = new AnswerTypeText(mContext,
                                nAnswerId, answerLayout, mAnswerTexts);
                        mAnswerTexts = answer.addClickListener(mAnswerTexts);
                        answer.addAnswer();
                        break;
                    }
                    case "finish": {
                        AnswerTypeFinish answer = new AnswerTypeFinish(mContext,
                                nAnswerId, answerLayout);
                        answer.addClickListener(mContext, mMetaData, mAnswerIds, mAnswerTexts);
                        answer.addAnswer();
                        break;
                    }
                    case "sliderFix": {
                        isSliderFix = true;
                        answerSliderFix.addAnswer(nAnswerId, sAnswer, isDefault);
                        break;
                    }
                    case "sliderFree": {
                        isSliderFree = true;
                        answerSliderFree.addAnswer(nAnswerId, sAnswer, isDefault);
                        break;
                    }
                    case "emoji": {
                        isEmoji = true;
                        answerTypeEmoji.addAnswer(nAnswerId, sAnswer, isDefault);
                        break;
                    }
                    default: {
                        isRadio = false;
                        Log.i(CLASS_NAME, "Weird object found. Id: " + question.getQuestionId());
                        break;
                    }
                }
            }
        }

        if (isEmoji) {
            mAnswerIds = answerTypeEmoji.buildView(mAnswerIds);
            mAnswerIds = answerTypeEmoji.addClickListener(mAnswerIds);
        }

        // In case of sliderFix, create View
        if (isSliderFix) {
            answerSliderFix.buildSlider();
            mEvaluationList = answerSliderFix.addClickListener(mEvaluationList);
        }

        // In case of sliderFix, create View
        if (isSliderFree) {
            answerSliderFree.buildSlider();
            mEvaluationList = answerSliderFree.addClickListener(mEvaluationList);
        }

        // In Case of Radio Buttons, additional RadioGroup is implemented
        if (isRadio) {
            answerRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    // In Case of Radio Buttons checking one means un-checking all other Elements
                    // Therefore onClickListening must be handled on Group Level
                    // listOfRadioIds contains all Ids of current Radio Group
                    mAnswerIds.removeAll(listOfRadioIds);
                    mAnswerIds.add(checkedId);
                    answerRadioGroup.check(checkedId);
                    // Toggle Visibility of suited/unsuited frames
                    checkVisibility();
                }
            });
            answerLayout.layoutAnswer.addView(answerRadioGroup);
        }

        return answerContainer;
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

        Log.i("Checking","visibility");

        for (int iPos = 0; iPos < mQuestionInfo.size(); iPos++) {

            QuestionInfo qI = mQuestionInfo.get(iPos);

            if (qI.getFilterId() != -1                                  // Specific Filter Id
                    && qI.getCondition()                                // which MUST exist
                    && mAnswerIds.contains(qI.getFilterId())            // DOES exist
                    && !qI.isActive())                                  // on an INACTIVE Layout
            {
                addQuestion(iPos);
            }

            if (qI.getFilterId() != -1                                  // Specific Filter Id
                    && qI.getCondition()                                // which MUST exist
                    && !mAnswerIds.contains(qI.getFilterId())           // does NOT exist
                    && qI.isActive())                                   // on an ACTIVE Layout
            {
                removeQuestion(iPos);
            }

            if (qI.getFilterId() != -1                                  // Specific Filter Id
                    && !qI.getCondition()                               // which MUST NOT exist
                    && mAnswerIds.contains(qI.getFilterId())            // DOES exist
                    && !qI.isActive())                                  // on an INACTIVE Layout
            {
                addQuestion(iPos);
            }

            if ((qI.getFilterId() != -1)                                // Specific Filter Id
                    && (!qI.getCondition())                             // which MUST NOT exist
                    && mAnswerIds.contains(qI.getFilterId())            // DOES exist
                    && qI.isActive())                                   // on an ACTIVE Layout
            {
                removeQuestion(iPos);
            }

            if (qI.getFilterId() != -1                                  // Specific Filter Id
                    && !qI.getCondition()                               // which MUST NOT exist
                    && !mAnswerIds.contains(qI.getFilterId())           // DOES NOT exist
                    && !qI.isActive())                                  // on an INACTIVE Layout
            {
                addQuestion(iPos);
            }

            if (qI.isHidden() && qI.isActive()) {
                qI.setInactive();
                removeQuestion(iPos);
            }
        }
        manageCheckedIds();
    }

    public void manageCheckedIds() {

        // Obtain outdated ids and prepare list for removal simultaneously
        ArrayList<Integer> listOfIdsToRemove = new ArrayList<>();
        // Iterate over all ids that have been checked before
        for (int iId = 0; iId < mAnswerIds.size(); iId++) {
            int nId = mAnswerIds.get(iId);
            boolean bFoundId = false;
            // Iterate over all currently visible/active Views
            for (int iView = 0; iView < mContextQPA.mListOfActiveViews.size(); iView++) {
                List<Answer> listOfAnswerIds =
                        mContextQPA.mListOfActiveViews.get(iView).getListOfAnswerIds();
                for (int iAnswer = 0; iAnswer < listOfAnswerIds.size(); iAnswer++) {
                    if (listOfAnswerIds.get(iAnswer).Id == nId) {
                        bFoundId = true;
                    }
                }
            }
            if (!bFoundId && nId != 111111) {
                listOfIdsToRemove.add(nId);
            }
        }
        // Remove all outdated ids
        if (listOfIdsToRemove.size() > 0) {
            mAnswerIds.removeAll(listOfIdsToRemove);
        }
        if (isDebug) {
            showListOfIds();
        }
    }

    // Adds the question to the displayed list
    private boolean addQuestion(int iPos) {
        mQuestionInfo.get(iPos).setActive();
        // View is fetched from Storage List and added to Active List
        mContextQPA.addView(mContextQPA.mListOfViewsStorage.get(iPos).getView(),
                mContextQPA.mListOfActiveViews.size(),
                mContextQPA.mListOfViewsStorage.get(iPos).getPositionInRaw(),
                mContextQPA.mListOfViewsStorage.get(iPos).getListOfAnswerIds());
        renewPositionsInPager();
        mContextQPA.notifyDataSetChanged();
        return true;
    }

    // Removes the question from the displayed list
    private boolean removeQuestion(int iPos) {
        mQuestionInfo.get(iPos).setInactive();

        // Remove checked answers on removed questions
        String sType = mQuestionInfo.get(iPos).getQuestion().getTypeAnswer();
        List<Integer> mListOfAnswerIds = mQuestionInfo.get(iPos).getAnswerIds();

        for (int iAnswer = 0; iAnswer < mListOfAnswerIds.size(); iAnswer++) {
            if (sType.equals("checkbox")) {
                CheckBox checkBox = (CheckBox) mContextQPA.mListOfViewsStorage.get(iPos).getView().
                        findViewById(mQuestionInfo.get(iPos).getAnswerIds().get(iAnswer));
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

    public boolean clearAnswerIds() {
        mAnswerIds = new AnswerIds();
        checkVisibility();
        return true;
    }

    // Clears all entered answer Ids in mAnswerIds
    public boolean clearAnswerTexts() {
        revertEnteredText();
        mAnswerTexts = new AnswerTexts();
        return true;
    }

    // Clears all text answer entries in mAnswerTexts
    public void revertEnteredText() {

        for (int iText = 0; iText < mAnswerTexts.size(); iText++) {
            int nTextId = mAnswerTexts.get(iText).getId();
            for (int iView = 0; iView < mContextQPA.mListOfActiveViews.size(); iView++) {
                if (mContextQPA.mListOfActiveViews.get(iView).getView().getId() == nTextId) {
                    TextView tV = (TextView) mContextQPA.mListOfActiveViews.get(iView).getView().findViewById(nTextId);
                    tV.setText("");
                }
            }
        }
    }

    public void showListOfIds() {
        if (mAnswerIds.size() > 0) {
            String LIST = "" + mAnswerIds.get(0);
            for (int iId = 1; iId < mAnswerIds.size(); iId++) {
                LIST += ",\n" + mAnswerIds.get(iId);
            }
            Toast.makeText(mContext, LIST, Toast.LENGTH_SHORT).show();
        }
    }
}
