package com.starclub.syndicator.activity;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.starclub.syndicator.R;
import com.starclub.syndicator.SSAPIRequestBuilder;
import com.starclub.syndicator.SSAppController;
import com.starclub.syndicator.SSConstants;
import com.starclub.syndicator.SSUtils;
import com.starclub.syndicator.customcontrol.CountingTextView;
import com.starclub.syndicator.customcontrol.CustomButtonTouchListener;
import com.starclub.syndicator.customcontrol.CustomFontTextView;
import com.starclub.syndicator.customcontrol.CustomStarsiteFontTextView;
import com.starclub.syndicator.customcontrol.DialogCallBack;
import com.starclub.syndicator.customcontrol.DialogEditCallBack;
import com.starclub.syndicator.customcontrol.DialogHelper;
import com.starclub.syndicator.data.SCItem;
import com.turbomanage.httpclient.AsyncCallback;
import com.turbomanage.httpclient.HttpResponse;
import com.turbomanage.httpclient.ParameterMap;
import com.turbomanage.httpclient.android.AndroidHttpClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SSDetailActivity extends FragmentActivity {

    static String TAG = SSDetailActivity.class.getName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        initView();
    }

    @Override
    public void onBackPressed() {

        if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }

    }


    private void initView() {

        // header
        CustomStarsiteFontTextView  btnSetting = (CustomStarsiteFontTextView) findViewById(R.id.nav_back);
        btnSetting.setOnTouchListener(CustomButtonTouchListener.getInstance());
        btnSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        btnSetting.setText("7"); // back

        CustomStarsiteFontTextView btnAction = (CustomStarsiteFontTextView) findViewById(R.id.nav_action);
        btnAction.setVisibility(View.INVISIBLE);


        final SCItem selectedItem = (SCItem) getIntent().getSerializableExtra("item");

        ImageView ivAvatar = (ImageView) findViewById(R.id.ivAvatar);

        String url = SSAPIRequestBuilder.getURLEncoded(selectedItem.thumbnail);
        ImageLoader.getInstance().displayImage(url, ivAvatar);

        ivAvatar.setOnTouchListener(CustomButtonTouchListener.getInstance());
        ivAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!selectedItem.isPhoto) {
                    String urlVideo = selectedItem.fileURL.substring(0, selectedItem.fileURL.lastIndexOf('.'))  + "/master.m3u8";
                    Intent intent = new Intent(SSDetailActivity.this, SSVideoPlayer.class);
                    intent.putExtra("url_video", urlVideo);
                    startActivity(intent);
                }
            }
        });

        CustomFontTextView lbTitle = (CustomFontTextView) findViewById(R.id.lbTitle);
        lbTitle.setText(selectedItem.message);

        CustomFontTextView lbPosted = (CustomFontTextView) findViewById(R.id.lbPosted);
        lbPosted.setText(SSUtils.timeAgo2(selectedItem.createdDate));

        CountingTextView lbTotal = (CountingTextView) findViewById(R.id.lbTotal);
        CustomFontTextView lbTotalTitle = (CustomFontTextView) findViewById(R.id.lbTotalTitle);

        if (SSAppController.sharedInstance().showTheMoney) {

            float endingNumber = 0f;
            try {
                endingNumber = Float.parseFloat(selectedItem.ss_earnings);
            } catch (Exception e) {e.printStackTrace();}

            lbTotal.countFrom(0, endingNumber, 1000, "$%.1f");

            lbTotalTitle.setText(selectedItem.ss_earningsTitle);

        } else {
            lbTotal.setVisibility(View.GONE);
            lbTotalTitle.setVisibility(View.GONE);
        }


        /*
            setting
         */
        // starclub
        CustomFontTextView lbSCRevenus = (CustomFontTextView) findViewById(R.id.lbSCRevenus);
        CustomFontTextView lbSCViews = (CustomFontTextView) findViewById(R.id.lbSCViews);

        lbSCRevenus.setText(selectedItem.ss_earnings);
        lbSCViews.setText(selectedItem.ss_impressions);

        // facebook
        CustomFontTextView lbFBLikes = (CustomFontTextView) findViewById(R.id.lbFBLikes);
        CustomFontTextView lbFBShares = (CustomFontTextView) findViewById(R.id.lbFBShares);

        lbFBLikes.setText(selectedItem.fb_likes);
        lbFBShares.setText(selectedItem.fb_reaches);

        // twitter
        CustomFontTextView lbTWFavourite = (CustomFontTextView) findViewById(R.id.lbTWFavourite);
        CustomFontTextView lbTwRetweet = (CustomFontTextView) findViewById(R.id.lbTwRetweet);

        lbTWFavourite.setText(selectedItem.tw_favorites);
        lbTwRetweet.setText(selectedItem.tw_retweets);

        // google
        LinearLayout layoutGoogle = (LinearLayout) findViewById(R.id.layoutGoogle);
        CustomFontTextView lbGGLikes = (CustomFontTextView) findViewById(R.id.lbGGLikes);
        CustomFontTextView lbGGComments = (CustomFontTextView) findViewById(R.id.lbGGComments);

        if (selectedItem.isPhoto) {
            layoutGoogle.setVisibility(View.GONE);
        } else {
            lbGGLikes.setText(selectedItem.gp_likes);
            lbGGComments.setText(selectedItem.gp_comments);
        }

        // Thumblr
        LinearLayout layoutTumblr = (LinearLayout) findViewById(R.id.layoutTumblr);
        CustomFontTextView lbTRLikes = (CustomFontTextView) findViewById(R.id.lbTRLikes);
        CustomFontTextView lbTRComments = (CustomFontTextView) findViewById(R.id.lbTRComments);

        if (selectedItem.isPhoto) {
            layoutTumblr.setVisibility(View.GONE);
        } else {
            lbTRLikes.setText(selectedItem.tu_likes);
            lbTRComments.setText(selectedItem.tu_comments);
        }


    }

}
