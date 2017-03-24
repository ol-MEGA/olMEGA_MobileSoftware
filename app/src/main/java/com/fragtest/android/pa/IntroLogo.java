package com.fragtest.android.pa;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.VideoView;

/**
 * Created by ulrikkowalk on 23.03.17.
 */

public class IntroLogo extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
/*
        setContentView(R.layout.intro);
        VideoView videoView = (VideoView) findViewById(R.id.introVideo);
        // Set the URI to local Video File
        Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.logo);
        videoView.setVideoURI(uri);
        videoView.start();

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
            */
                Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(mainIntent);
        /*
            }
        });
        */
    }

}
