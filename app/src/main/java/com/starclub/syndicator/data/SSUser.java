package com.starclub.syndicator.data;

import org.json.JSONObject;

/**
 * Created by iGold on 10/16/15.
 */
public class SSUser {

    public String token = "";
    public String name = "";
    public String img = "";

    public void initWithDictionary(JSONObject dict) {

        try {
            token = dict.getString("token");
        } catch (Exception e) {e.printStackTrace();}

        try {
            name = dict.getString("name");
        } catch (Exception e) {e.printStackTrace();}

        try {
            img = dict.getString("img");
        } catch (Exception e) {e.printStackTrace();}

    }
}
