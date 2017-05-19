package com.fragtest.android.pa;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Created by ulrikkowalk on 28.02.17.
 */

public class Questionnaire {

    private static String LOG_STRING = "Questionnaire";
    // Accumulator for ids, values and texts gathered from user input
    public EvaluationList mEvaluationList;
    // Number of pages in questionnaire (visible and hidden)
    private int mNumPages;
    // List containing all questions (including all attached information)
    private List<String> mQuestionList;
    // Context of QuestionnairePageAdapter for visibility
    private QuestionnairePagerAdapter mContextQPA;
    // Context of MainActivity()
    private Context mContext;
    // Basic information about all available questions
    private ArrayList<QuestionInfo> mQuestionInfo;
    private MandatoryInfo mMandatoryInfo;
    private MetaData mMetaData;
    private FileIO mFileIO;
    // Flag: display forced empty vertical spaces
    private boolean acceptBlankSpaces = false;
    // Use on screen debug output
    private boolean isDebug = false;

    public Questionnaire(Context context, QuestionnairePagerAdapter contextQPA) {

        mContext = context;
        mContextQPA = contextQPA;
        mEvaluationList = new EvaluationList();
        mFileIO = new FileIO();
        mQuestionInfo = new ArrayList<>();
        mMandatoryInfo = new MandatoryInfo();

        if (isDebug) {
            Log.i(LOG_STRING, "Constructor successful.");
        }
    }

    public void setUp() {
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

        if (isDebug) {
            Log.i(LOG_STRING, "Setup was successful.");
        }
    }

    // Generate a Layout for Question at desired Position based on String Blueprint
    public Question createQuestion(int position) {

        if (isDebug) {
            Log.e(LOG_STRING, "========================================================");
            Log.e(LOG_STRING, "Creating view for position " + position);
        }

        String sQuestionBlueprint = mQuestionList.get(position);
        Question question = new Question(sQuestionBlueprint);
        mQuestionInfo.add(new QuestionInfo(question, question.getQuestionId(),
                question.getFilterId(), question.getFilterCondition(), position,
                question.isHidden(), question.isMandatory(), question.getAnswerIds()));
        mMetaData.addQuestion(question);
        mMandatoryInfo.add(question.getQuestionId(), question.isMandatory(), question.isHidden());

        if (isDebug) {
            Log.e(LOG_STRING, "QuestionId: " + question.getQuestionId() + " mandatory: " +
                    question.isMandatory());
        }

        return question;
    }

    // Builds the Layout of each Stage Question
    public LinearLayout generateView(Question question) {

        // Are the answers to this specific Question grouped as Radio Button Group?
        boolean isRadio = false;
        boolean isCheckBox = false;
        boolean isSliderFix = false;
        boolean isSliderFree = false;
        boolean isEmoji = false;
        boolean isText = false;
        boolean isFinish = false;

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

        final AnswerTypeRadio answerTypeRadio = new AnswerTypeRadio(
                mContext, this, answerLayout, question.getQuestionId());

        // In case of checkbox type
        final AnswerTypeCheckBox answerTypeCheckBox = new AnswerTypeCheckBox(
                mContext, this, answerLayout, question.getQuestionId());

        // In case of emoji type
        final AnswerTypeEmoji answerTypeEmoji = new AnswerTypeEmoji(
                mContext, this, answerLayout, question.getQuestionId());

        // In case of sliderFix type
        final AnswerTypeSliderFix answerSliderFix = new AnswerTypeSliderFix(
                mContext, this, answerLayout, question.getQuestionId());

        // In case of sliderFree type
        final AnswerTypeSliderFree answerSliderFree = new AnswerTypeSliderFree(
                mContext, this, answerLayout, question.getQuestionId());

        final AnswerTypeText answerTypeText = new AnswerTypeText(
                mContext, this, answerLayout, question.getQuestionId());

        final AnswerTypeFinish answerTypeFinish = new AnswerTypeFinish(
                mContext, this, answerLayout);

        final AnswerTypeDate answerTypeDate = new AnswerTypeDate(
                mContext, this, question.getQuestionId());

        // Number of possible Answers
        int nNumAnswers = question.getNumAnswers();
        // List carrying all Answers and Answer Ids
        List<Answer> answerList = question.getAnswers();

        /** Iteration over all possible Answers attributed to current question **/
        for (int iAnswer = 0; iAnswer < nNumAnswers; iAnswer++) {

            // Obtain Answer specific Parameters
            Answer currentAnswer = answerList.get(iAnswer);
            String sAnswer = currentAnswer.Text;
            int nAnswerId = currentAnswer.Id;
            boolean isDefault = currentAnswer.isDefault();

            if (((nAnswerId == 66666) && (acceptBlankSpaces)) || (nAnswerId != 66666)) {

                switch (sType) {
                    case "date": {
                        answerTypeDate.addAnswer(sAnswer);
                        break;
                    }
                    case "radio": {
                        isRadio = true;
                        answerTypeRadio.addAnswer(nAnswerId, sAnswer, isDefault);
                        break;
                    }
                    case "checkbox": {
                        isCheckBox = true;
                        answerTypeCheckBox.addAnswer(nAnswerId, sAnswer, isDefault);
                        break;
                    }
                    case "text": {
                        isText = true;
                        if (nNumAnswers > 0) {
                            answerTypeText.addQuestion(sAnswer);
                        }
                        break;
                    }
                    case "finish": {
                        isFinish = true;
                        answerTypeFinish.addAnswer();
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
                        if (isDebug) {
                            Log.i(LOG_STRING, "Weird object found. Id: " +
                                    question.getQuestionId());
                        }
                        break;
                    }
                }
            }
        }

        if (isText) {
            answerTypeText.buildView();
            answerTypeText.addClickListener();
        }

        if (isCheckBox) {
            answerTypeCheckBox.buildView();
            answerTypeCheckBox.addClickListener();
        }

        if (isEmoji) {
            answerTypeEmoji.buildView();
            answerTypeEmoji.addClickListener();
        }

        if (isSliderFix) {
            answerSliderFix.buildView();
            answerSliderFix.addClickListener();
        }

        if (isSliderFree) {
            answerSliderFree.buildView();
            answerSliderFree.addClickListener();
        }

        if (isRadio) {
            answerTypeRadio.buildView();
            answerTypeRadio.addClickListener();
        }

        if (isFinish) {
            answerTypeFinish.addClickListener();
        }

        return answerContainer;
    }

    public boolean addValueToEvaluationList(int questionId, float value) {
        mEvaluationList.add(questionId, value);
        return true;
    }

    public boolean addTextToEvaluationLst(int questionId, String text) {
        mEvaluationList.add(questionId, text);
        return true;
    }

    public boolean addIdToEvaluationList(int questionId, int id) {
        mEvaluationList.add(questionId, id);
        return true;
    }

    public boolean addIdListToEvaluationList(int questionId, List<Integer> listOfIds) {
        mEvaluationList.add(questionId, listOfIds);
        return true;
    }

    public boolean removeIdFromEvaluationList(int id) {
        mEvaluationList.removeAnswerId(id);
        return true;
    }

    public boolean removeQuestionIdFromEvaluationList(int questionId) {
        mEvaluationList.removeQuestionId(questionId);
        return true;
    }

    public boolean finaliseEvaluation() {
        mMetaData.finalise(mEvaluationList);
        mFileIO.saveDataToFile(mContext, mMetaData.getFileName(), mMetaData.getData());
        Toast.makeText(mContext, R.string.infoTextSave, Toast.LENGTH_SHORT).show();
        ((Activity) mContext).finish();
        return true;
    }

    public int getNumPages() {
        return mNumPages;
    }

    public int getId(Question question) {
        return question.getQuestionId();
    }

    public boolean checkVisibility() {
        // Function checks all available pages on whether their filtering condition has been met and
        // toggles visibility by destroying or creating the views and adding them to the list of
        // views which is handled by QuestionnairePagerAdapter

        if (isDebug) {
            Log.i(LOG_STRING, "Checking visibility");
        }

        for (int iPos = 0; iPos < mQuestionInfo.size(); iPos++) {

            QuestionInfo qI = mQuestionInfo.get(iPos);

            if (qI.getFilterId() != -255                                    // Specific Filter Id
                    && qI.getCondition()                                    // which MUST exist
                    && mEvaluationList.containsAnswerId(qI.getFilterId())   // DOES exist
                    && !qI.isActive())                                      // on an INACTIVE Layout
            {
                addQuestion(iPos);
            }

            if (qI.getFilterId() != -255                                    // Specific Filter Id
                    && qI.getCondition()                                    // which MUST exist
                    && !mEvaluationList.containsAnswerId(qI.getFilterId())  // does NOT exist
                    && qI.isActive())                                       // on an ACTIVE Layout
            {
                removeQuestion(iPos);
            }

            if (qI.getFilterId() != -255                                    // Specific Filter Id
                    && !qI.getCondition()                                   // which MUST NOT exist
                    && mEvaluationList.containsAnswerId(qI.getFilterId())   // DOES exist
                    && !qI.isActive())                                      // on an INACTIVE Layout
            {
                addQuestion(iPos);
            }

            if ((qI.getFilterId() != -255)                                  // Specific Filter Id
                    && (!qI.getCondition())                                 // which MUST NOT exist
                    && mEvaluationList.containsAnswerId(qI.getFilterId())   // DOES exist
                    && qI.isActive())                                       // on an ACTIVE Layout
            {
                removeQuestion(iPos);
            }

            if (qI.getFilterId() != -255                                    // Specific Filter Id
                    && !qI.getCondition()                                   // which MUST NOT exist
                    && !mEvaluationList.containsAnswerId(qI.getFilterId())  // DOES NOT exist
                    && !qI.isActive())                                      // on an INACTIVE Layout
            {
                addQuestion(iPos);
            }

            if (qI.isHidden() && qI.isActive()) {
                qI.setInactive();
                removeQuestion(iPos);
            }
        }
        return true;
    }

    private boolean addQuestion(int iPos) {
        // Adds the question to the displayed list
        mQuestionInfo.get(iPos).setActive();
        // View is fetched from Storage List and added to Active List
        mContextQPA.addView(mContextQPA.mListOfViewsStorage.get(iPos).getView(),
                mContextQPA.mListOfActiveViews.size(),
                mContextQPA.mListOfViewsStorage.get(iPos).getPositionInRaw(),
                mContextQPA.mListOfViewsStorage.get(iPos).isMandatory(),
                mContextQPA.mListOfViewsStorage.get(iPos).getListOfAnswerIds());
        renewPositionsInPager();
        mContextQPA.notifyDataSetChanged();
        return true;
    }

    private boolean removeQuestion(int iPos) {
        // Removes the question from the displayed list

        // If view is mandatory but declared hidden
        if (mMandatoryInfo.isMandatoryFromId(mQuestionInfo.get(iPos).getId()) &&
                mMandatoryInfo.isHiddenFromId(mQuestionInfo.get(iPos).getId())) {

            if (isDebug) {
                Log.i(LOG_STRING, "Making invisible: " + mQuestionInfo.get(iPos).getId());
            }
        }

        // If view is not mandatory -> can really be removed including entries in mEvaluationList
        if (!mMandatoryInfo.isMandatoryFromId(mQuestionInfo.get(iPos).getId())) {

            if (isDebug) {
                Log.i(LOG_STRING, "Removing: " + mQuestionInfo.get(iPos).getId());
            }

            mQuestionInfo.get(iPos).setInactive();
            mEvaluationList.removeQuestionId(mQuestionInfo.get(iPos).getId());
            // Remove checked answers on removed questions
            String sType = mQuestionInfo.get(iPos).getQuestion().getTypeAnswer();
            List<Integer> mListOfAnswerIds = mQuestionInfo.get(iPos).getAnswerIds();

            for (int iAnswer = 0; iAnswer < mListOfAnswerIds.size(); iAnswer++) {
                if (sType.equals("checkbox")) {
                    CheckBox checkBox = (CheckBox) mContextQPA.mViewPager.findViewById(
                            mQuestionInfo.get(iPos).getAnswerIds().get(iAnswer));
                    if (checkBox != null) {
                        checkBox.setChecked(false);
                    }
                }
            }
        }

        // Remove View from ActiveList
        mQuestionInfo.get(iPos).setInactive();
        mContextQPA.removeView(mQuestionInfo.get(iPos).getPositionInPager());
        renewPositionsInPager();
        mContextQPA.notifyDataSetChanged();

        return true;
    }

    private void renewPositionsInPager() {
        // Renews all the positions in information object gathered from actual order

        for (int iItem = 0; iItem < mQuestionInfo.size(); iItem++) {
            int iId = mQuestionInfo.get(iItem).getId();
            mQuestionInfo.get(iItem).setPositionInPager(mContextQPA.getPositionFromId(iId));
        }
    }

    private List<String> thinOutList(List<String> mQuestionList) {
        // Removes irrelevant data from question sheet

        for (int iItem = mQuestionList.size() - 1; iItem >= 0; iItem = iItem - 2) {
            mQuestionList.remove(iItem);
        }
        return mQuestionList;
    }

    private List<String> stringArrayToListString(String[] stringArray) {
        // Turns an array of Strings into a List of Strings
        List<String> listString = new ArrayList<>();
        Collections.addAll(listString, stringArray);
        return listString;
    }

    public boolean clearAnswerIds() {
        mEvaluationList.removeAllOfType("id");
        checkVisibility();
        return true;
    }

    public boolean clearAnswerTexts() {
        // Clears all entered answer Texts in mEvaluationList

        mEvaluationList.removeAllOfType("text");
        return true;
    }
}
