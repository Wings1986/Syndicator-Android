package com.starclub.syndicator.data;

import android.util.Log;

import com.starclub.syndicator.SSAppController;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by iGold on 10/16/15.
 */
public class SSPairingItem implements Serializable{

    public static final String TAG = SSPairingItem.class.getName();

    public enum SSParingTypeId {

        SSParingTypeNone(0),
        SSParingTypeFacebook(1),
        SSParingTypeTwitter(2),
        SSParingTypeInstagram(3),
        SSParingTypeTumblr(4),
        SSParingTypeGoogle(5),
        SSParingTypePinterest(6);

        private int _value;

        SSParingTypeId(int Value) {
            this._value = Value;
        }

        public int getValue() {
            return _value;
        }

        public static SSParingTypeId fromInt(int i) {
            for (SSParingTypeId b : SSParingTypeId.values()) {
                if (b.getValue() == i) { return b; }
            }
            return null;
        }
    }


    public String token = "";
    public String pairingId = "";
    public String title = "";
    public String authURL = "";
    public String colorHex = "";
    public String img = "";
    public String icon = "";
    public JSONArray connectedPages;

    public boolean isConnected;
    public boolean atLeastOne;
    public boolean hideCaptionOption;

    public SSParingTypeId pairingTypeId = SSParingTypeId.SSParingTypeNone;


    public void initWithDictionary(JSONObject dict) {

        try {
            token = dict.getString("token");
        } catch (Exception e) {e.printStackTrace();}

        try {
            pairingId = dict.getString("id");
        } catch (Exception e) {e.printStackTrace();}

        try {
            title = dict.getString("title");
        } catch (Exception e) {e.printStackTrace();}

        try {
            img = dict.getString("img");
        } catch (Exception e) {e.printStackTrace();}

        try {
            icon = dict.getString("icon");
        } catch (Exception e) {e.printStackTrace();}

        try {
            colorHex = dict.getString("color");
        } catch (Exception e) {e.printStackTrace();}

        try {
            authURL = dict.getString("auth_url");
        } catch (Exception e) {e.printStackTrace();}

        try {
            isConnected = dict.getString("connected").equalsIgnoreCase("Y");
        } catch (Exception e) {e.printStackTrace();}

        atLeastOne = false;
        if (isConnected) {
            try {
                connectedPages = dict.getJSONArray("connected_pages");
            } catch (Exception e) {e.printStackTrace();}

            checkPairingStatus();
        }

        try {
            hideCaptionOption = dict.getString("hide_caption").equalsIgnoreCase("Y");
        } catch (Exception e) {e.printStackTrace();}

        if (pairingId.equalsIgnoreCase("1")) {
            pairingTypeId = SSParingTypeId.SSParingTypeFacebook;
        } else if (pairingId.equalsIgnoreCase("2")) {
            pairingTypeId = SSParingTypeId.SSParingTypeTwitter;
        } else if (pairingId.equalsIgnoreCase("3")) {
            pairingTypeId = SSParingTypeId.SSParingTypeInstagram;
        } else if (pairingId.equalsIgnoreCase("4")) {
            pairingTypeId = SSParingTypeId.SSParingTypeTumblr;
        } else if (pairingId.equalsIgnoreCase("6")) {
            pairingTypeId = SSParingTypeId.SSParingTypeGoogle;
        } else if (pairingId.equalsIgnoreCase("7")) {
            pairingTypeId = SSParingTypeId.SSParingTypePinterest;
        }

    }

    public void checkPairingStatus() {
        atLeastOne = false;
        for (int i = 0 ; i < connectedPages.length() ; i ++) {
            try {
                JSONObject page = connectedPages.getJSONObject(i);
                if (page.getString("active").equalsIgnoreCase("Y")) {
                    atLeastOne = true;
                }
            } catch (Exception e) {e.printStackTrace();}

        }
    }

    public static void findAndReplacePairingItemActiveStateWithDictionary(JSONObject dict) {

        Log.d(TAG, "Item passed : " + dict);


        for(SSPairingItem i : SSAppController.sharedInstance().pairingItems){
            Log.d(TAG, "Item :" + i);

            if (i.connectedPages != null) {
                for (int j = 0 ; j < i.connectedPages.length() ; j ++) {
                    try {
                        JSONObject d = i.connectedPages.getJSONObject(j);

                        Log.d(TAG, "Item D :" + d);

                        if (d.getString("id").equals(dict.getString("id"))) {
                            Log.d(TAG, "MATCH----> ");

                            JSONArray newPagesData = new JSONArray();

                            for (int k = 0 ; k < i.connectedPages.length() ; k ++) {
                                try {
                                    JSONObject subD = i.connectedPages.getJSONObject(k);
                                    JSONObject newMD = subD;

                                    if (newMD.getString("id").equals(dict.getString("id"))) {
                                        newMD = dict;
                                    }
                                    newPagesData.put(newMD);

                                } catch (Exception e) {e.printStackTrace();}
                            }

                            i.connectedPages = newPagesData;
                        }
                    } catch ( Exception e) {e.printStackTrace();}
                }
            }

        }

    }
}
