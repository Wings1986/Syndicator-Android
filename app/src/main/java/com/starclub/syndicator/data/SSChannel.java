package com.starclub.syndicator.data;

import org.json.JSONObject;

/**
 * Created by iGold on 10/16/15.
 */
public class SSChannel {

    public String channelId = "";
    public String name = "";
    public String img = "";
    public String totalReachDisplay = "";
    public int weeklyPostingRate = -1;
    public float weeklyRevenueRate = -1.f;
    public boolean hasAtLeastOnePairing;


    public void initWithDictionary(JSONObject dict) {

        try {
            channelId = dict.getString("id");
        } catch (Exception e) {e.printStackTrace();}

        try {
            name = dict.getString("name");
        } catch (Exception e) {e.printStackTrace();}

        try {
            img = dict.getString("img");
        } catch (Exception e) {e.printStackTrace();}

        try {
            totalReachDisplay = dict.getString("total_reach_display");
        } catch (Exception e) {e.printStackTrace();}

        try {
            weeklyPostingRate = dict.getInt("weekly_post_rate");
        } catch (Exception e) {e.printStackTrace();}

        try {
            weeklyRevenueRate = (float) dict.getDouble("weekly_revenue_rate");
        } catch (Exception e) {e.printStackTrace();}

    }
}
