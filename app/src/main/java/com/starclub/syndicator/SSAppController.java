package com.starclub.syndicator;

import android.content.Context;
import android.content.SharedPreferences;

import com.starclub.syndicator.customcontrol.DialogHelper;
import com.starclub.syndicator.data.SSChannel;
import com.starclub.syndicator.data.SSPairingItem;
import com.starclub.syndicator.data.SSUser;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by iGold on 22.02.15.
 */
public class SSAppController {
    public static final String PREFERENCES = "preferences";

    public static final String KEY_FIRST_LOADED = "firstTimeLoaded";
    public static final String KEY_SEEN_TUTORIAL = "seenTutorial";

    public static final String KEY_LOGIN_USER_NAME = "login_user_name";
    public static final String KEY_LOGIN_USER_PASSWORD = "login_user_password";
    public static final String KEY_LOGIN_USER_TOKEN = "login_user_token";
    public static final String KEY_LOGIN_USER_CHANNEL = "login_user_channel";

    public Context context;

    private static SSAppController instance;


    public SSUser currentUser;
    public SSChannel currentChannel;
    public List<SSPairingItem> pairingItems;

    public boolean showTheMoney;
    public boolean isPinModeApp;
    public boolean shouldShowMeTheMoney;


    public static SSAppController sharedInstance() {
        if (instance == null) {
            instance = new SSAppController();
        }
        return instance;
    }

    
    public void init(Context context) {
        this.context = context;
    }


    public boolean firstCrashed () {
        SharedPreferences sp = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        return sp.getBoolean("first_crashed", true);
    }
    public void saveFirstCrashed() {
        SharedPreferences sp = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();

        edit.putBoolean("first_crashed", false);

        edit.apply();
    }

    public boolean firstTimeLoaded () {
        SharedPreferences sp = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        return sp.getBoolean(KEY_FIRST_LOADED, true);
    }
    public void saveFirstTimeLoaded() {
        SharedPreferences sp = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();

        edit.putBoolean(KEY_FIRST_LOADED, false);

        edit.apply();
    }


    public boolean haveSeenTutorial () {
        SharedPreferences sp = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        if (!sp.contains(KEY_SEEN_TUTORIAL)) {
            return false;
        }
        else {
            return sp.getBoolean(KEY_SEEN_TUTORIAL, false);
        }

    }
    public void saveSeenTutorial() {
        SharedPreferences sp = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();

        edit.putBoolean(KEY_SEEN_TUTORIAL, true);

        edit.apply();
    }



    public Map<String, String> loadUserData() {

        SharedPreferences sp = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        if (sp.contains(KEY_LOGIN_USER_NAME) && sp.contains(KEY_LOGIN_USER_PASSWORD)) {

            Map <String,String> storedLogin =  new HashMap<>();

            storedLogin.put("username", sp.getString(KEY_LOGIN_USER_NAME, ""));
            storedLogin.put("pass", sp.getString(KEY_LOGIN_USER_PASSWORD, ""));
            return storedLogin;
        }

        return null;
    }

    public void saveUserLoginUsername(String username, String password) {

    	SharedPreferences sp = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();

        edit.putString(KEY_LOGIN_USER_NAME, username);
        edit.putString(KEY_LOGIN_USER_PASSWORD, password);

        edit.apply();

    }
    public void saveUserToken(String token, String channelId) {

        SharedPreferences sp = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();

        edit.putString(KEY_LOGIN_USER_TOKEN, token);
        edit.putString(KEY_LOGIN_USER_CHANNEL, channelId);

        edit.apply();
    }

    public String getUserToken() {
        SharedPreferences sp = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        if (sp.contains(KEY_LOGIN_USER_TOKEN)) {
            return sp.getString(KEY_LOGIN_USER_TOKEN, "");
        }
        return "";
    }
    public String getUserChannelId() {
        SharedPreferences sp = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        if (sp.contains(KEY_LOGIN_USER_CHANNEL)) {
            return sp.getString(KEY_LOGIN_USER_CHANNEL, "");
        }
        return "";
    }

    public void removeAllData() {
        SharedPreferences sp = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();

        edit.remove(KEY_LOGIN_USER_NAME);
        edit.remove(KEY_LOGIN_USER_PASSWORD);

        edit.apply();
    }

    public void setupUserAndChannelWithDictionary(JSONObject dict){

        String token = "", channelId = "";

        currentUser = new SSUser();
        try {
            currentUser.initWithDictionary(dict.getJSONObject("user"));
            token = currentUser.token;
        } catch (Exception e) {e.printStackTrace();}

        currentChannel = new SSChannel();
        try {
            currentChannel.initWithDictionary(dict.getJSONObject("channel"));
            channelId = currentChannel.channelId;
        } catch (Exception e) {e.printStackTrace();}

        saveUserToken(token, channelId);


        boolean atLeastOnePairing = false;

        pairingItems = new ArrayList<>();

        try {
            for (int i = 0; i < dict.getJSONArray("pairing").length(); i++) {

                SSPairingItem pi = new SSPairingItem();
                pi.initWithDictionary(dict.getJSONArray("pairing").getJSONObject(i));
                if (pi.isConnected) {
                    atLeastOnePairing = true;
                }
                pairingItems.add(pi);
            }
        } catch (Exception e) {e.printStackTrace();}

        currentChannel.hasAtLeastOnePairing = atLeastOnePairing;

        try {
            showTheMoney = dict.getString("showmethemoney").equalsIgnoreCase("Y");
        } catch (Exception e) {e.printStackTrace();}
    }

    public void alertWithServerResponse(Context context, JSONObject dict){

        try {

            if (dict.getString("status").equalsIgnoreCase("-3")) {

                String title = dict.getString("title");
                String message = dict.getString("message");
                String btn = dict.getString("btn");

                DialogHelper.getDialog(context, title, message, btn, null, null).show();

                return;
            }

            if (!dict.getString("message").isEmpty()) {

                if (dict.getString("status").equalsIgnoreCase("-1")) {

                }
                else {
                    String title = "Error";
                    try {
                        title = dict.getString("title");
                    } catch (Exception e) {e.printStackTrace();}

                    String message = dict.getString("message");

                    DialogHelper.getDialog(context, title, message, "OK", null, null).show();
                }
            }
        } catch (Exception e) {e.printStackTrace();}


    }

}
