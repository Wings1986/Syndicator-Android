package com.starclub.syndicator.data;

import org.json.JSONObject;

/**
 * Created by iGold on 10/16/15.
 */
public class SSFBPageAccess {

    public String accessToken = "";
    public String category = "";
    public String pid = "";
    public String name = "";

    public void initWithDictionary(JSONObject dict) {

        try {
            accessToken = dict.getString("access_token");
        } catch (Exception e) {e.printStackTrace();}

        try {
            category = dict.getString("category");
        } catch (Exception e) {e.printStackTrace();}

        try {
            pid = dict.getString("id");
        } catch (Exception e) {e.printStackTrace();}

        try {
            name = dict.getString("name");
        } catch (Exception e) {e.printStackTrace();}

    }
}
