package com.starclub.syndicator.activity;

import android.app.Activity;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.MediaController;
import android.widget.VideoView;

import com.starclub.syndicator.R;
import com.starclub.syndicator.customcontrol.CustomButtonTouchListener;
import com.starclub.syndicator.customcontrol.CustomStarsiteFontTextView;

/**
 * Created by iGold on 10/29/15.
 */
public class SSVideoPlayer extends Activity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        // header
        CustomStarsiteFontTextView btnBack = (CustomStarsiteFontTextView) findViewById(R.id.nav_back);
        btnBack.setOnTouchListener(CustomButtonTouchListener.getInstance());
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });



        String urlString = getIntent().getStringExtra("url_video");
        if (urlString == null)
            return;

        VideoView vid = (VideoView) findViewById(R.id.videoView);
        Uri vidUri = Uri.parse(urlString);
        vid.setVideoURI(vidUri);
        vid.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                onBackPressed();
            }
        });

        vid.setMediaController(new MediaController(this));
        vid.start();
    }

}
