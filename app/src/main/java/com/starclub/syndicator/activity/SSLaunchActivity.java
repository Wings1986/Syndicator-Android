package com.starclub.syndicator.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.starclub.syndicator.R;
import com.starclub.syndicator.SSAppController;

public class SSLaunchActivity extends Activity {

    private final int SPLASH_TIME = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!SSAppController.sharedInstance().haveSeenTutorial()) {
                    startActivity(new Intent(SSLaunchActivity.this, SSTutorialActivity.class));
                }
                else {
                    startActivity(new Intent(SSLaunchActivity.this, SSLoginActivity.class));
                }
                finish();

            }
        }, SPLASH_TIME);

    }
}

