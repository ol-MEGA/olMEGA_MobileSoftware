package com.fragtest.android.pa;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.R.attr.id;
import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

/**
 * Created by ulrikkowalk on 28.02.17.
 */

public class Questionnaire {


    private String mRawInput;
    private String[] mQuestionnaire;
    private int mNumPages;
    private List<String> mQuestionList;
    private LinearLayout mLinearContainer;
    // Context of QuestionnairePageAdapter for Visibility
    private QuestionnairePagerAdapter mContextQPA;
    private int mPosition;

    private AnswerIDs mAnswerIDs;
    // Performs Calculations
    public Calculations mCalculations;

    public ArrayList<QuestionInfo> mQuestionInfo;

    Context mContext;

    public Questionnaire(Context context, QuestionnairePagerAdapter contextQPA) {

        mContext = context;
        mContextQPA = contextQPA;
        mQuestionInfo = new ArrayList<>();
        mRawInput = readRawTextFile(mContext, R.raw.question_single);
        mQuestionnaire = mRawInput.split("<question|</question>");
        mQuestionList = stringArrayToListString(mQuestionnaire);
        mQuestionList = thinOutList(mQuestionList);
        mNumPages = mQuestionList.size();
        mCalculations = new Calculations(mContext);
        // Contains all IDs of checked Elements and Methods of Logic
        mAnswerIDs = new AnswerIDs(mContextQPA);
        mLinearContainer = new LinearLayout(mContext);
    }

    // Generate a Layout for Question at desired Position based on String Blueprint
    public Question createQuestion(int position) {
        mPosition = position;
        String sQuestionBlueprint = mQuestionList.get(mPosition);
        Question question = new Question(sQuestionBlueprint);
        mQuestionInfo.add(new QuestionInfo(question.getQuestionId(),
                question.getFilterId(), question.getFilterCondition(), position));

        return question;
    }

    // Builds the Layout of each Question
    public LinearLayout generateView(Question question) {

        // Are the answers to this specific Question grouped as Radio Button Group?
        boolean bRadio = false;

        mLinearContainer = new LinearLayout(mContext);
        LinearLayout.LayoutParams linearContainerParams =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
        mLinearContainer.setOrientation(LinearLayout.VERTICAL);
        mLinearContainer.setLayoutParams(linearContainerParams);

        // TextView carrying the Question
        QuestionText questionText = new QuestionText(mContext, question.getQuestionId(),
                question.getQuestionText(), mLinearContainer);
        questionText.addQuestion();

        // Creates a Canvas for the Question Layout
        AnswerLayout answerLayout = new AnswerLayout(mContext);
        mLinearContainer.addView(answerLayout.scrollContent);

        // Format of Answer e.g. "radio", "checkbox", ...
        String sType = question.getTypeAnswer();

        // Answer Buttons of type "radio" are grouped and handled together
        // Only needed in case of Radio Buttons
        final RadioGroup answerRadioGroup = new RadioGroup(mContext);
        answerRadioGroup.setOrientation(RadioGroup.VERTICAL);
        final List<Integer> listOfRadioIds = new ArrayList<Integer>();

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

            switch (sType) {
                case "radio": {
                    bRadio = true;
                    AnswerTypeRadio answer = new AnswerTypeRadio(mContext, nAnswerID, sAnswer, answerRadioGroup);
                    answer.addAnswer();
                    listOfRadioIds.add(nAnswerID);
                    break;
                }
                case "checkbox": {
                    bRadio = false;
                    AnswerTypeCheckBox answer = new AnswerTypeCheckBox(mContext, nAnswerID, sAnswer, answerLayout);
                    answer.addAnswer();
                    mAnswerIDs = answer.addClickListener(mAnswerIDs);
                    break;
                }
                default: {
                    bRadio = false;
                    Log.e("strange","Wat nu?");
                    break;
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

                    // Toggle Visiblity of suited/unsuited frames
                    checkVisibility(checkedID);
                }
            });
            answerLayout.layoutAnswer.addView(answerRadioGroup);
        }
        return mLinearContainer;
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
    private void checkVisibility(int checkedID) {

        for (int iPos = 0; iPos < mQuestionInfo.size(); iPos++) {

            QuestionInfo qI = mQuestionInfo.get(iPos);

            if ((qI.getFilterId() != -1) && (qI.getCondition())) {
                if (mAnswerIDs.contains(qI.getFilterId())) {
                    if (!qI.isActive()) {
                        Log.e("page not active", "inactive -> create");
                        mContextQPA.setNumPages(mContextQPA.getCount()+1);
                        Log.e("page not active", "num set to "+mContextQPA.getCount());
                        mContextQPA.notifyDataSetChanged();
                        Log.e("page not active", "notified");
                        mQuestionInfo.get(iPos).setActive();
                        Log.e("page not active","setActive");
                        Question newQuestion = createQuestion(iPos);
                        Log.e("page not active","Quetsion created");
                        LinearLayout newLayout = generateView(newQuestion);
                        Log.e("page not active","View generated");
                        mContextQPA.addView(newLayout, iPos);
                        Log.e("page not active","View added");
                        mContextQPA.notifyDataSetChanged();
                        Log.e("page not active","notified");
                    }
                }
                if (!mAnswerIDs.contains(qI.getFilterId())) {
                    if (qI.isActive()) {
                        Log.e("page active", "active -> destroy");
                        mQuestionInfo.get(iPos).setInactive();
                        Log.e("page active","setInactive");
                        mContextQPA.removeView(mContextQPA.mViewPager, iPos);
                        Log.e("page active","View removed");
                        mContextQPA.setNumPages(mContextQPA.getCount()-1);
                        Log.e("page active", "num set to "+mContextQPA.getCount());
                        mContextQPA.notifyDataSetChanged();
                        Log.e("page active","notified");
                    }
                }
            }

        }
    }


    /**
     * ----------------------------------- Useful Methods -------------------------------------
     **/


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

    private static String readRawTextFile(Context ctx, int resId) {
        InputStream inputStream = ctx.getResources().openRawResource(resId);

        InputStreamReader inputReader = new InputStreamReader(inputStream);
        BufferedReader buffReader = new BufferedReader(inputReader);
        String line;
        StringBuilder text = new StringBuilder();

        try {
            while ((line = buffReader.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
        } catch (IOException e) {
            return null;
        }
        return text.toString();
    }
}
