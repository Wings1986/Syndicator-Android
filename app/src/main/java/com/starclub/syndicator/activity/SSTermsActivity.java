package com.starclub.syndicator.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.starclub.syndicator.R;
import com.starclub.syndicator.customcontrol.CustomButtonTouchListener;
import com.starclub.syndicator.customcontrol.CustomFontButton;
import com.starclub.syndicator.customcontrol.CustomStarsiteFontTextView;

import java.util.Arrays;

/**
 * Created by iGold on 10/29/15.
 */
public class SSTermsActivity extends Activity{

    static String TAG = SSTermsActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms);

        CustomFontButton btnBack = (CustomFontButton) findViewById(R.id.button_close);
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

                view.loadUrl(url);
                return false;
            }
        });

    }

}
