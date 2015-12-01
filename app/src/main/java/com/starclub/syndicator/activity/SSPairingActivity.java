package com.starclub.syndicator.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.starclub.syndicator.R;
import com.starclub.syndicator.SSAPIRequestBuilder;
import com.starclub.syndicator.SSAppController;
import com.starclub.syndicator.SSConstants;
import com.starclub.syndicator.SSUtils;
import com.starclub.syndicator.customcontrol.CustomButtonTouchListener;
import com.starclub.syndicator.customcontrol.CustomFontTextView;
import com.starclub.syndicator.customcontrol.CustomStarsiteFontTextView;
import com.starclub.syndicator.customcontrol.DialogHelper;
import com.starclub.syndicator.customcontrol.SSPairingBottomButton;
import com.starclub.syndicator.data.SSPairingItem;
import com.starclub.syndicator.utils.MyJSON;
import com.turbomanage.httpclient.AsyncCallback;
import com.turbomanage.httpclient.HttpResponse;
import com.turbomanage.httpclient.ParameterMap;
import com.turbomanage.httpclient.android.AndroidHttpClient;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class SSPairingActivity extends Activity {

    static String TAG = SSPairingActivity.class.getName();

    private static final int CALLBACK_SUCCESS = 5;

    CallbackManager callbackManager;
    TwitterAuthClient twitterAuthClient;

    final String kSSPairServiceFacebook     = "SSPairServiceFacebook";
    final String kSSPairServiceTwitter      = "SSPairServiceTwitter";
    final String kSSPairServiceTumblr       = "SSPairServiceTumblr";
    final String kSSPairServiceInstagram    = "SSPairServiceInstagram";
    final String kSSPairServiceGooglePlus   = "kSSPairServiceGooglePlus";
    final String kSSPairServicePinterest    = "SSPairServicePinterest";


    CustomFontTextView titleRowOne, titleRowTwo;
    CustomStarsiteFontTextView titleBtn;
    CustomFontTextView _btnSkip;


    boolean initialAppPairing = false;
    public static SSPairingItem startingProperty;

    List<HashMap<String, String>> stepConfig = new ArrayList<>();
    int stepIdx, _totalConnectedProperties;
    String currentService = "";
    SSPairingItem _currentPropertyInView;

    ListView connectListView;
    MyCustomAdapter mAdapter;


    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == CALLBACK_SUCCESS && data != null) {

                String db_id = data.getStringExtra("db_id");
                if (db_id == null)
                    return;

                String pairing_service = "";

                if (currentService.equalsIgnoreCase(kSSPairServiceInstagram)) {
                    pairing_service = "" + SSPairingItem.SSParingTypeId.SSParingTypeInstagram;
                } else if (currentService.equalsIgnoreCase(kSSPairServiceTumblr)) {
                    pairing_service = "" + SSPairingItem.SSParingTypeId.SSParingTypeTumblr;
                }

                sendPairingToServer(pairing_service, null, null, null, db_id);

            }
        }

        if (callbackManager != null) {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }

        if (twitterAuthClient != null) {
            twitterAuthClient.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pairing);


        /*
         Facebook
          */
        loginFB(this);


        stepConfig.add(new HashMap<String, String>() {
            {
                put("service", kSSPairServiceFacebook);
                put("propertyId", "1");
                put("titleOne", "Step 1");
                put("titleTwo", "Tap to Connect Facebook");
                put("icon", "9");
                put("color", "#6079B5");
            }
        });

        stepConfig.add(new HashMap<String, String>() { {
            put("service", kSSPairServiceTwitter);
            put("propertyId", "2");
            put("titleOne", "Step 2");
            put("titleTwo", "Tap to Connect Twitter");
            put("icon", "8");
            put("color", "#1DAEEC");
        }});

        stepConfig.add(new HashMap<String, String>() { {
            put("service", kSSPairServiceInstagram);
            put("propertyId", "3");
            put("titleOne", "Step 3");
            put("titleTwo", "Tap to Connect Instagram");
            put("icon", "0");
            put("color", "#EDE6DB");
        }});

        stepConfig.add(new HashMap<String, String>() { {
            put("service", kSSPairServiceTumblr);
            put("propertyId", "4");
            put("titleOne", "Step 4");
            put("titleTwo", "Tap to Connect Tumblr");
            put("icon", "T");
            put("color", "#FFFFFF");
        }});

        if (SSConstants.isDemoApp || SSAppController.sharedInstance().isPinModeApp) {
            stepConfig.add(new HashMap<String, String>() {
                {
                    put("service", kSSPairServiceGooglePlus);
                    put("propertyId", "5");
                    put("titleOne", "Step 5");
                    put("titleTwo", "Tap to Connect Google+");
                    put("icon", "J");
                    put("color", "#DF4A32");
                }
            });

            stepConfig.add(new HashMap<String, String>() {
                {
                    put("service", kSSPairServicePinterest);
                    put("propertyId", "6");
                    put("titleOne", "Step 6");
                    put("titleTwo", "Tap to Connect Pinterest");
                    put("icon", "H");
                    put("color", "#BF0112");
                }
            });
        }

// header
        CustomStarsiteFontTextView btnBack = (CustomStarsiteFontTextView) findViewById(R.id.nav_back);
        btnBack.setOnTouchListener(CustomButtonTouchListener.getInstance());
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        _btnSkip = (CustomFontTextView) findViewById(R.id.btnSkip);


        titleRowOne = (CustomFontTextView) findViewById(R.id.titleOne);
        titleRowTwo = (CustomFontTextView) findViewById(R.id.titleTwo);
        titleBtn = (CustomStarsiteFontTextView) findViewById(R.id.btnClick);
        titleBtn.setOnTouchListener(CustomButtonTouchListener.getInstance());
        titleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SSConstants.isDemoApp || SSAppController.sharedInstance().isPinModeApp) {
                    DialogHelper.getDialog(SSPairingActivity.this, "Demo", "Disabled for demo", "OK", null, null).show();
                    return;
                }

                if (currentService.equalsIgnoreCase(kSSPairServiceTwitter)) {
                    startTwitterPairing();
                } else if (currentService.equalsIgnoreCase(kSSPairServiceInstagram)) {
                    startInstagramPairing();
                } else if (currentService.equalsIgnoreCase(kSSPairServiceTumblr)) {
                    startTumblrPairing();
                } else {
                    startFacebookPairing();
                }
            }
        });


        connectListView = (ListView) findViewById(R.id.listview);
        mAdapter = new MyCustomAdapter(this);
        connectListView.setAdapter(mAdapter);
        connectListView.setVisibility(View.GONE);



// init value
        initialAppPairing = getIntent().getBooleanExtra("init_app", false);
        if(initialAppPairing){
            btnBack.setVisibility(View.GONE);
        }

//        startingProperty = (SSParingItem) getIntent().getSerializableExtra("startingProperty");

        stepIdx = 0;
        if(startingProperty != null){
            if(startingProperty.pairingTypeId == SSPairingItem.SSParingTypeId.SSParingTypeTwitter){
                stepIdx = 1;
            }else if(startingProperty.pairingTypeId == SSPairingItem.SSParingTypeId.SSParingTypeInstagram){
                stepIdx = 2;
            }else if(startingProperty.pairingTypeId == SSPairingItem.SSParingTypeId.SSParingTypeTumblr){
                stepIdx = 3;
            }
        }

        rebuildBottomButtons();

        bringInStep(stepIdx);
    }

    private void rebuildBottomButtons() {

        LinearLayout layoutButtons = (LinearLayout) findViewById(R.id.layoutButtons);
        for (int i = 0 ; i < layoutButtons.getChildCount() ; i ++ ) {
            View child = layoutButtons.getChildAt(i);

            if (child instanceof SSPairingBottomButton) {
                child.setVisibility(View.GONE);
            }
        }

        for (final HashMap<String, String> dict : stepConfig) {
            SSPairingBottomButton b = null;

            String service = dict.get("service");
            if (service.equalsIgnoreCase(kSSPairServiceFacebook)) {
                b = (SSPairingBottomButton) findViewById(R.id.btnFacebook);
            }
            else if (service.equalsIgnoreCase(kSSPairServiceTwitter)) {
                b = (SSPairingBottomButton) findViewById(R.id.btnTwitter);
            }
            else if (service.equalsIgnoreCase(kSSPairServiceInstagram)) {
                b = (SSPairingBottomButton) findViewById(R.id.btnInstagram);
            }
            else if (service.equalsIgnoreCase(kSSPairServiceTumblr)) {
                b = (SSPairingBottomButton) findViewById(R.id.btnThumblr);
            }
            else if (service.equalsIgnoreCase(kSSPairServiceGooglePlus)) {
                b = (SSPairingBottomButton) findViewById(R.id.btnGoogle);
            }
            else if (service.equalsIgnoreCase(kSSPairServicePinterest)) {
                b = (SSPairingBottomButton) findViewById(R.id.btnPinterest);
            }
            if (b == null)
                return;

            b.setVisibility(View.VISIBLE);

            b.setupWithDictionary(dict);

            b.setOnTouchListener(CustomButtonTouchListener.getInstance());
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    stepIdx = Integer.parseInt(dict.get("propertyId")) -1;
                    bringInStep(stepIdx);
                }
            });

            if(b.isConnected)
                _totalConnectedProperties++;

        }

    }

    private void bringInStep(int step) {

        HashMap<String, String> stepDict = stepConfig.get(step);

        currentService = stepDict.get("service");
        int propertyId = Integer.parseInt(stepDict.get("propertyId"));

        _currentPropertyInView = null;

        for(SSPairingItem i : SSAppController.sharedInstance().pairingItems){
            if(i.pairingTypeId.getValue() == propertyId){

                if(i.isConnected && !SSConstants.isDemoApp && !SSAppController.sharedInstance().isPinModeApp){
                    _currentPropertyInView = i;

                    titleRowOne.setText("Manage Connections");

                    if(_currentPropertyInView.pairingTypeId == SSPairingItem.SSParingTypeId.SSParingTypeFacebook){
                        titleRowTwo.setText("Found Facebook Connections");
                    }else if(_currentPropertyInView.pairingTypeId == SSPairingItem.SSParingTypeId.SSParingTypeTwitter){
                        titleRowTwo.setText("Found Twitter Connections");
                    }else if(_currentPropertyInView.pairingTypeId == SSPairingItem.SSParingTypeId.SSParingTypeInstagram){
                        titleRowTwo.setText("Found Instagram Connections");
                    }else if(_currentPropertyInView.pairingTypeId == SSPairingItem.SSParingTypeId.SSParingTypeTumblr){
                        titleRowTwo.setText("Found Tumblr Connections");
                    }

                    showActiveAccountsFoundForProperty(i);

                    connectListView.setVisibility(View.VISIBLE);
                    titleBtn.setVisibility(View.GONE);
                }
                else {
                    connectListView.setVisibility(View.GONE);
                    titleBtn.setVisibility(View.VISIBLE);
                }
                break;
            }
        }
        rebuildBottomButtons();

        // button active
        for (final HashMap<String, String> dict : stepConfig) {
            if (Integer.parseInt(dict.get("propertyId")) == step + 1) {
                SSPairingBottomButton b = null;

                String service = dict.get("service");
                if (service.equalsIgnoreCase(kSSPairServiceFacebook)) {
                    b = (SSPairingBottomButton) findViewById(R.id.btnFacebook);
                }
                else if (service.equalsIgnoreCase(kSSPairServiceTwitter)) {
                    b = (SSPairingBottomButton) findViewById(R.id.btnTwitter);
                }
                else if (service.equalsIgnoreCase(kSSPairServiceInstagram)) {
                    b = (SSPairingBottomButton) findViewById(R.id.btnInstagram);
                }
                else if (service.equalsIgnoreCase(kSSPairServiceTumblr)) {
                    b = (SSPairingBottomButton) findViewById(R.id.btnThumblr);
                }
                else if (service.equalsIgnoreCase(kSSPairServiceGooglePlus)) {
                    b = (SSPairingBottomButton) findViewById(R.id.btnGoogle);
                }
                else if (service.equalsIgnoreCase(kSSPairServicePinterest)) {
                    b = (SSPairingBottomButton) findViewById(R.id.btnPinterest);
                }
                if (b == null)
                    return;

                b.setAlpha(1.0f);
            }

        }


        // set title
        if(_currentPropertyInView != null){
            _btnSkip.setText(getString(R.string.next));

        }else{

            titleRowOne.setText(stepDict.get("titleOne"));
            titleRowTwo.setText(stepDict.get("titleTwo"));
            titleBtn.setTextColor(Color.parseColor(stepDict.get("color")));
            titleBtn.setText(stepDict.get("icon"));

        }

    }

    private void showActiveAccountsFoundForProperty(SSPairingItem propertyItem) {

        JSONArray items = MyJSON.copyJSONArray(propertyItem.connectedPages);

        if(propertyItem.pairingTypeId == SSPairingItem.SSParingTypeId.SSParingTypeInstagram
                || propertyItem.pairingTypeId == SSPairingItem.SSParingTypeId.SSParingTypeTumblr) {
            try {
                JSONObject obj = new JSONObject();
                obj.put("title", "Refresh...");
                obj.put("add", "Y");

                items.put(obj);
            } catch (Exception e) {e.printStackTrace();}
        }
        else {
            try {
                JSONObject obj = new JSONObject();
                obj.put("title", "Add More...");
                obj.put("add", "Y");

                items.put(obj);
            } catch (Exception e) {e.printStackTrace();}

        }

        if (mAdapter != null) {
            mAdapter.setItem(items);
            mAdapter.notifyDataSetChanged();
        }

    }
    /*
        account adapter
     */

    public class MyCustomAdapter extends BaseAdapter {

        private Context mContext;
        private JSONArray items = new JSONArray();
        private LayoutInflater mInflater;


        public MyCustomAdapter(Context context) {
            mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mContext = context;
        }

        public void setItem(JSONArray array) {
            items = array;
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return items.length();
        }

        @Override
        public JSONObject getItem(int position) {
            try {
                return items.getJSONObject(position);
            } catch (Exception e) {e.printStackTrace();}

            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }


        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;


            if (convertView == null) {
                holder = new ViewHolder();

                convertView = mInflater.inflate(R.layout.cell_pairing, null);

                holder.lbTitle = (CustomFontTextView)convertView.findViewById(R.id.lbTitle);
                holder.lbStatueFlag = (CustomStarsiteFontTextView)convertView.findViewById(R.id.lbStatueFlag);
                holder.lbStatue = (CustomFontTextView)convertView.findViewById(R.id.lbStatue);

                holder.cbEnable = (CheckBox) convertView.findViewById(R.id.swEnable);
                holder.cbEnable.setEnabled(false);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder)convertView.getTag();
            }


            try {

                final JSONObject item = items.getJSONObject(position);


                try {
                    holder.lbTitle.setText(item.getString("title"));
                } catch (Exception e) {e.printStackTrace();}

                if (position == items.length() - 1) {
                    holder.lbStatueFlag.setVisibility(View.GONE);
                    holder.lbStatue.setVisibility(View.GONE);
                    holder.cbEnable.setVisibility(View.INVISIBLE);
                }
                else {
                    holder.lbStatue.setVisibility(View.VISIBLE);
                    holder.cbEnable.setVisibility(View.VISIBLE);

                    try {
                        String active = item.getString("active");

                        if (active.equalsIgnoreCase("Y")) {
                            holder.lbStatueFlag.setVisibility(View.VISIBLE);
                            holder.lbStatueFlag.setTextColor(Color.GREEN);
                            holder.lbStatue.setText(" CONNECTED");
                            holder.lbStatue.setTextColor(Color.GREEN);
                            holder.cbEnable.setChecked(true);
                        } else {
                            holder.lbStatueFlag.setVisibility(View.GONE);
                            holder.lbStatue.setText("NOT CONNECTED");
                            holder.lbStatue.setTextColor(Color.RED);
                            holder.cbEnable.setChecked(false);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

//                holder.cbEnable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//                    @Override
//                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//
//                        try {
//                            item.put("active", isChecked ? "Y" : "N");
//                        } catch (Exception e) {e.printStackTrace();}
//
//                        notifyDataSetChanged();
//                    }
//                });

                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (position == items.length() - 1) {
                            // add
                            heardBtnAddMorePairing();
                        }
                        else {
                            try {
                                String active = item.getString("active");
                                item.put("active", active.equalsIgnoreCase("Y") ? "N" : "Y");
                                notifyDataSetChanged();

                                heardBtnPairingToggleChanged(item);

                            } catch (Exception e) {e.printStackTrace();}
                        }
                    }
                });


            } catch (Exception e) {e.printStackTrace();}

            return convertView;
        }

    }

    public static class ViewHolder {

        public CustomFontTextView lbTitle;
        public CustomStarsiteFontTextView lbStatueFlag;
        public CustomFontTextView lbStatue;

        CheckBox cbEnable;

    }

    private void heardBtnPairingToggleChanged(JSONObject dic) {


        AndroidHttpClient httpClient = new AndroidHttpClient(SSConstants.WEB_SERVICE_ROOT);
        httpClient.setMaxRetries(3);

        ParameterMap params = SSAPIRequestBuilder.APIDictionary(httpClient);

        String url = SSAPIRequestBuilder.APIForUpdateSocialKeyStatus();


//        JSONObject jsonParam = new JSONObject();
//        try {
//            jsonParam.put("token", params.get("token"));
//            jsonParam.put("channel_id", params.get("channel_id"));
//            jsonParam.put("property", dic);
//        } catch (Exception e) {e.printStackTrace();}

        try {

            params.put("property[id]", dic.getString("id"));
            params.put("property[active]", dic.getString("active"));
            params.put("property[title]", dic.getString("title"));
        } catch (Exception e) {e.printStackTrace();}


        try {

            httpClient.post(url, params, new AsyncCallback() {

                @Override
                public void onComplete(HttpResponse httpResponse) {

                }

                @Override
                public void onError(Exception e) {
                    super.onError(e);
                }
            });

        } catch (Exception e) {e.printStackTrace();}


    }

    /*
        Social pairing
     */
    private void heardBtnAddMorePairing() {

        if(_currentPropertyInView.pairingTypeId == SSPairingItem.SSParingTypeId.SSParingTypeFacebook){
            startFacebookPairing();
        }else if(_currentPropertyInView.pairingTypeId == SSPairingItem.SSParingTypeId.SSParingTypeTwitter){
            startTwitterPairing();
        }else if(_currentPropertyInView.pairingTypeId == SSPairingItem.SSParingTypeId.SSParingTypeInstagram){
            startInstagramPairing();
        }else if(_currentPropertyInView.pairingTypeId == SSPairingItem.SSParingTypeId.SSParingTypeTumblr){
            startTumblrPairing();
        }
    }

    private void startFacebookPairing() {

//        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("read_insights"));
        try {
            LoginManager.getInstance().logInWithPublishPermissions(this, Arrays.asList("manage_pages", "publish_actions", "publish_pages"));
        } catch (Exception e) {
            e.printStackTrace();

            DialogHelper.getDialog(SSPairingActivity.this, "No Permission", "Your account have not permission", "OK", null, null).show();
            return;
        }

        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {


            @Override
            public void onSuccess(LoginResult loginResult) {

                Profile profile = Profile.getCurrentProfile();
                final JSONObject jsonProfile = new JSONObject();
                try {
                    jsonProfile.put("id", profile.getId());
                    jsonProfile.put("first_name", profile.getFirstName());
                    jsonProfile.put("middle_name", profile.getMiddleName());
                    jsonProfile.put("last_name", profile.getLastName());
                    jsonProfile.put("name", profile.getName());
                    if (profile.getLinkUri() != null) {
                        jsonProfile.put("link_uri", profile.getLinkUri().toString());
                    }
                } catch (JSONException object) {

                }


                new GraphRequest(
                        AccessToken.getCurrentAccessToken(),
                        "/me/accounts",
                        null,
                        HttpMethod.GET,
                        new GraphRequest.Callback() {
                            public void onCompleted(GraphResponse response) {
                                /* handle the result */
                                JSONObject result = response.getJSONObject();
                                Log.d(TAG, "result = " + result.toString());

                                try {

                                    sendPairingToServer("facebook", result.toString(), jsonProfile.toString(), AccessToken.getCurrentAccessToken().getToken(), null);

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                            }
                        }
                ).executeAsync();

            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });

    }
    private void startTwitterPairing() {

        TwitterSession session = Twitter.getSessionManager().getActiveSession();
        if (session != null) {
            Twitter.getSessionManager().clearActiveSession();
            Twitter.logOut();
        }


        if (session  == null) {

            twitterAuthClient = new TwitterAuthClient();
            twitterAuthClient.authorize(this, new Callback<TwitterSession>() {
                @Override
                public void success(final Result<TwitterSession> result) {

                    Log.d("Twitter login", "success");
                    TwitterAuthToken authToken = result.data.getAuthToken();

                    String oauth_token = authToken.token;
                    String oauth_token_secret = authToken.secret;
                    String user_id = "" + result.data.getUserId();
                    String screen_name = result.data.getUserName();

                    JSONObject jsonUserInfo = new JSONObject();
                    try {
                        jsonUserInfo.put("oauth_token", oauth_token);
                        jsonUserInfo.put("oauth_token_secret", oauth_token_secret);
                        jsonUserInfo.put("user_id", user_id);
                        jsonUserInfo.put("screen_name", screen_name);
                        jsonUserInfo.put("x_auth_expires", "0");
                    } catch (Exception e) {e.printStackTrace();}

                    sendPairingToServer("twitter", jsonUserInfo.toString(), null, null, null);

                }

                @Override
                public void failure(final TwitterException e) {
                    // Do something on fail
                    Log.d("Twitter login", "fail = " + e.getMessage());
                }
            });
        }
        else {
            TwitterAuthToken authToken = session.getAuthToken();
        }
    }

    private void startInstagramPairing() {
        if (!currentService.equalsIgnoreCase(kSSPairServiceInstagram))
            return;

        SSPairingItem pairingItem = null;

        for(SSPairingItem pi : SSAppController.sharedInstance().pairingItems){
            if(pi.pairingTypeId == SSPairingItem.SSParingTypeId.SSParingTypeInstagram){
                pairingItem = pi;
                break;
            }
        }

        if (pairingItem == null)
            return;


        String authUrl = pairingItem.authURL;

        Intent intent = new Intent(SSPairingActivity.this, SSWebPairingActivity.class);
        intent.putExtra("url_web", authUrl);
        startActivityForResult(intent, CALLBACK_SUCCESS);

    }
    private void startTumblrPairing() {
        if (!currentService.equalsIgnoreCase(kSSPairServiceTumblr))
            return;

        SSPairingItem pairingItem = null;

        for(SSPairingItem pi : SSAppController.sharedInstance().pairingItems){
            if(pi.pairingTypeId == SSPairingItem.SSParingTypeId.SSParingTypeTumblr){
                pairingItem = pi;
                break;
            }
        }

        if (pairingItem == null)
            return;


        String authUrl = pairingItem.authURL;

        Intent intent = new Intent(SSPairingActivity.this, SSWebPairingActivity.class);
        intent.putExtra("url_web", authUrl);
        startActivityForResult(intent, CALLBACK_SUCCESS);
    }

    /*
        Facebook
     */
    private void loginFB(final Activity activity) {
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
    }


    private void sendPairingToServer(String pairing_service, String pairing_data, String user_data, String user_token, String db_id) {

        Log.d(TAG, "pairing_service = " + pairing_service);
        Log.d(TAG, "pairing_data = " + pairing_data);
        Log.d(TAG, "user_data = " + user_data);
        Log.d(TAG, "user_token = " + user_token);
        Log.d(TAG, "db_id = " + db_id);


        final Dialog waitDialog = DialogHelper.getWaitDialog(this, "Pairing");
        waitDialog.show();

        AndroidHttpClient httpClient = new AndroidHttpClient(SSConstants.WEB_SERVICE_ROOT);

        httpClient.setMaxRetries(3);
        final ParameterMap params = SSAPIRequestBuilder.APIDictionary(httpClient);

        String url = SSAPIRequestBuilder.APIForStoreSocialKeys();

        if (pairing_service != null) {
            params.add("pairing_service", pairing_service);
        }
        if (pairing_data != null) {
            params.add("pairing_data", pairing_data);
        }
        if (user_data != null) {
            params.add("user_data", user_data);
        }
        if (user_token != null) {
            params.add("user_token", user_token);
        }
        if (db_id != null) {
            params.add("db_id", db_id);
        }


        httpClient.post(url, params, new AsyncCallback() {

            @Override
            public void onComplete(HttpResponse httpResponse) {

                waitDialog.dismiss();

                try {
                    JSONObject result = new JSONObject(httpResponse.getBodyAsString());

                    if (SSUtils.isResponseSuccessful(result)) {

                        SSAppController.sharedInstance().pairingItems .clear();

                        JSONArray pairing = result.getJSONArray("pairing");
                        for (int i = 0 ; i < pairing.length() ; i ++) {
                            JSONObject d = pairing.getJSONObject(i);
                            SSPairingItem pi = new SSPairingItem();
                            pi.initWithDictionary(d);

                            SSAppController.sharedInstance().pairingItems.add(pi);
                        }

                        //Check if they need to pair - welcome screen
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // TODO Auto-generated method stub
                                bringInStep(stepIdx);
                            }
                        });

                    } else {

                        SSAppController.sharedInstance().alertWithServerResponse(SSPairingActivity.this, result);

                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onError(Exception e) {
                super.onError(e);

                waitDialog.dismiss();

                DialogHelper.getDialog(SSPairingActivity.this, "Connection Failed", "Unable to make request, please try again.", "OK", null, null).show();
            }
        });
    }

}

