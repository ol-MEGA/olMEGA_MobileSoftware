package com.fragtest.android.pa.Questionnaire;

import android.content.Context;
import android.content.res.ColorStateList;
import android.net.ConnectivityManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.CompoundButtonCompat;
import android.text.Layout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.fragtest.android.pa.Core.Units;
import com.fragtest.android.pa.DataTypes.StringAndInteger;
import com.fragtest.android.pa.R;


/**
 * Created by ulrikkowalk on 17.02.17.
 */

public class AnswerTypeWebsite extends AnswerType {

    private static String LOG_STRING = "AnswerTypeWebsite";
    private String url;
    private String clientID;
    private Button button;
    private LayoutInflater inflater;
    private Context context;


    public AnswerTypeWebsite(Context context, Questionnaire questionnaire, AnswerLayout qParent,
                             int nQuestionId, boolean isImmersive) {

        super(context, questionnaire, qParent, nQuestionId);

        this.context = context;
        inflater = LayoutInflater.from(context);

    }

    public void addAnswer(String url, String clientID) {
        this.url = url;
        this.clientID = clientID;
    }

    public void buildView() {

        if (isNetworkAvailable()) {

            WebView webView = new WebView(mContext);
            webView.setWebViewClient(new WebViewClient());
            webView.getSettings().setJavaScriptEnabled(true);
            webView.loadUrl(this.url + this.clientID);

            this.button = new Button(mContext);

            button.setText(R.string.buttonTextOkay);
            button.setTextColor(ContextCompat.getColor(mContext, R.color.TextColor));
            button.setBackground(ContextCompat.getDrawable(mContext, R.drawable.button));
            LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            buttonParams.topMargin = 96;
            buttonParams.bottomMargin = 48;

            LinearLayout.LayoutParams webViewParams = new LinearLayout.LayoutParams(
                    Units.getScreenWidth(),
                    Units.getScreenHeight() - buttonParams.bottomMargin - buttonParams.topMargin - 650
            );

            //Log.e(LOG, "URL: " + webView.getUrl());
            //Log.e(LOG, "SCREENHEIGHT: " + Units.getScreenHeight());

            mParent.layoutAnswer.setBackgroundColor(ContextCompat.getColor(mContext, R.color.WebGray));
            mParent.scrollContent.setBackgroundColor(ContextCompat.getColor(mContext, R.color.WebGray));

            mParent.layoutAnswer.addView(webView, webViewParams);
            mParent.layoutAnswer.addView(button, buttonParams);

        } else {

            Toast.makeText(this.context, "No network available", Toast.LENGTH_SHORT).show();
            Log.e(LOG, "NO NETWORK AVAILABLE");

        }

    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }

    public void addClickListener() {

        //button.

    }

    public boolean isNetworkAvailable()
    {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) this.context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }
}
