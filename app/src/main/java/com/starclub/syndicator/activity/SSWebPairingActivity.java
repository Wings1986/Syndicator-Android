package com.starclub.syndicator.activity;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.MediaController;
import android.widget.VideoView;

import com.starclub.syndicator.R;
import com.starclub.syndicator.customcontrol.CustomButtonTouchListener;
import com.starclub.syndicator.customcontrol.CustomStarsiteFontTextView;

import java.util.Arrays;

/**
 * Created by iGold on 10/29/15.
 */
public class SSWebPairingActivity extends Activity{

    static String TAG = SSWebPairingActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview_pairing);

        // header
        CustomStarsiteFontTextView btnBack = (CustomStarsiteFontTextView) findViewById(R.id.nav_back);
        btnBack.setOnTouchListener(CustomButtonTouchListener.getInstance());
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


        String urlString = getIntent().getStringExtra("url_web");
        if (urlString == null)
            return;

        WebView webview = (WebView) findViewById(R.id.webview);

        WebSettings settings = webview.getSettings();
        settings.setJavaScriptEnabled(true);
        webview.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
        webview.loadUrl(urlString);
        webview.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
//                return super.shouldOverrideUrlLoading(view, url);

                Log.d(TAG, "url = " + url);

                if (url.startsWith("com.starsite.cms")
                        || url.startsWith("com.starclub.syndicator")) {
                    String[] queryPairs = url.split("/");
                    if (Arrays.asList(queryPairs).contains("tumblr-pair")
                            || Arrays.asList(queryPairs).contains("instagram-pair")) { // tumblr || instgram

                        String returnedId = queryPairs[queryPairs.length -1];

                        Intent returnIntent = new Intent();
                        returnIntent.putExtra("db_id", returnedId);
                        setResult(Activity.RESULT_OK, returnIntent);
                        finish();

                        return false;
                    }

                }

                view.loadUrl(url);
                return false;
            }
        });

    }

}
