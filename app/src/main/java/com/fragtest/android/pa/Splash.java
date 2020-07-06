package com.fragtest.android.pa;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.VideoView;

public class Splash extends AppCompatActivity {

    /**
     * Splash Screen displayed while the application is starting up.
     **/

    private final String LOG = "Splash";
    // Decide whether to show still image or video
    private boolean showStill = true;
    // Duration of wait
    private int SPLASH_DISPLAY_LENGTH = 0;

    // Called when the activity is first created.
    @Override
    public void onCreate(Bundle bundle) {

        super.onCreate(bundle);

        View decorView = getWindow().getDecorView();
        // Hide both the navigation bar and the status bar.
        // SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
        // a general rule, you should design your app to hide the status bar whenever you
        // hide the navigation bar.
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        try {
            if (showStill) {
                SPLASH_DISPLAY_LENGTH = 2500;
                setContentView(R.layout.splashscreen);

                /* New Handler to start the Menu-Activity
                 * and close this Splash-Screen after some seconds.*/
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        /* Create an Intent that will start the Menu-Activity. */
                        Intent mainIntent = new Intent(Splash.this, MainActivity.class);
                        Splash.this.startActivity(mainIntent);
                        overridePendingTransition(R.anim.fadeout, R.anim.fadein);
                        Splash.this.finish();
                        overridePendingTransition(R.anim.fadeout, R.anim.fadein);
                    }
                }, SPLASH_DISPLAY_LENGTH);

            } else {
                SPLASH_DISPLAY_LENGTH = 3000;
                setContentView(R.layout.splashvideo);
                VideoView videoView = (VideoView) findViewById(R.id.videoView);
                Uri video = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.olmegalogo);
                videoView.setVideoURI(video);
                videoView.setZOrderOnTop(true);
                videoView.start();

                /* New Handler to start the Menu-Activity
                 * and close this Splash-Screen after some seconds.*/
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        /* Create an Intent that will start the Menu-Activity. */
                        Intent mainIntent = new Intent(Splash.this, MainActivity.class);
                        Splash.this.startActivity(mainIntent);
                        overridePendingTransition(R.anim.hold, R.anim.fadein);
                        Splash.this.finish();
                        overridePendingTransition(R.anim.hold, R.anim.fadein);
                    }
                }, SPLASH_DISPLAY_LENGTH);

            }

        } catch (Exception ex) {
        }
    }
}