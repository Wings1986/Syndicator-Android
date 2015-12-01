package com.starclub.syndicator.data;

import android.util.Base64;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by iGold on 10/16/15.
 */
public class SCItem implements Serializable{

    public String itemId = "";
    public String postId = "";
    public String thumbnail = "";
    public String message = "";
    public String postType = "";
    public String videoId = "";
    public String photoId = "";
    public String url = "";
    public String embed = "";
    public String created = "";

    // Facebook Insights
    public String fb_likes = "";
    public String fb_comments = "";
    public String fb_shares = "";
    public String fb_reaches = "";

    // Twitter Insights
    public String tw_retweets = "";
    public String tw_favorites = "";

    // Google + Insights
    public String gp_likes = "";
    public String gp_comments = "";

    // Tublr Insights
    public String tu_likes = "";
    public String tu_comments = "";

    // Starstats
    public String ss_impressions = "";
    public String ss_engagements = "";
    public String ss_earnings = "";
    public String ss_pageViews = "";
    public String ss_videoViews = "";
    public String ss_videoLengthHuman = "";
    public String ss_videoLengthAndPercent = "";
    public String ss_earningsTitle = "";

// Starsite CMS / Content Media
    public String fileURL = "";
    public double createdDate;
    public boolean isPhoto = false;


    public void initWithDictionary(JSONObject dict) {

        try {
            itemId = dict.getString("id");
        } catch (Exception e) {e.printStackTrace();}

        try {
            postId = dict.getString("postId");
        } catch (Exception e) {e.printStackTrace();}

        try {
            thumbnail = dict.getString("thumbnail");
        } catch (Exception e) {e.printStackTrace();}

        try {
            message = dict.getString("message");
        } catch (Exception e) {e.printStackTrace();}

        try {
            created = dict.getString("created");
        } catch (Exception e) {e.printStackTrace();}


        try {
            fb_likes = dict.getString("likes");
        } catch (Exception e) {e.printStackTrace();}

        try {
            fb_shares = dict.getString("shares");
        } catch (Exception e) {e.printStackTrace();}

        try {
            fb_comments = dict.getString("comments");
        } catch (Exception e) {e.printStackTrace();}

        try {
            fb_reaches = dict.getString("reaches");
        } catch (Exception e) {e.printStackTrace();}


        try {
            tw_retweets = dict.getString("tw_retweets");
        } catch (Exception e) {e.printStackTrace();}

        try {
            tw_favorites = dict.getString("tw_favorites");
        } catch (Exception e) {e.printStackTrace();}


        try {
            gp_likes = dict.getString("gp_likes");
        } catch (Exception e) {e.printStackTrace();}

        try {
            gp_comments = dict.getString("gp_comments");
        } catch (Exception e) {e.printStackTrace();}


        try {
            tu_likes = dict.getString("tu_likes");
        } catch (Exception e) {e.printStackTrace();}

        try {
            tu_comments = dict.getString("tu_comments");
        } catch (Exception e) {e.printStackTrace();}


        try {
            ss_impressions = dict.getString("impressions");
        } catch (Exception e) {e.printStackTrace();}

        try {
            ss_engagements = dict.getString("engagement");
        } catch (Exception e) {e.printStackTrace();}

        try {
            ss_earnings = dict.getString("earnings");
        } catch (Exception e) {e.printStackTrace();}

        try {
            ss_earningsTitle = dict.getString("earnings_title");
        } catch (Exception e) {e.printStackTrace();}

        try {
            ss_videoViews = dict.getString("video_views");
        } catch (Exception e) {e.printStackTrace();}

        try {
            ss_pageViews = dict.getString("ss_pageViews");
        } catch (Exception e) {e.printStackTrace();}


        try {
            url = dict.getString("url");
        } catch (Exception e) {e.printStackTrace();}

        try {
            String base64Decode = dict.getString("embed");
            byte[] byteData = Base64.decode(base64Decode, Base64.DEFAULT);
            embed = byteData.toString();
        } catch (Exception e) {e.printStackTrace();}

        try {
            fileURL = dict.getString("fileURL");
        } catch (Exception e) {e.printStackTrace();}


        try {
            ss_videoLengthHuman = dict.getString("video_length_human");
        } catch (Exception e) {e.printStackTrace();}

        try {
            ss_videoLengthAndPercent = dict.getString("video_engagement");
        } catch (Exception e) {e.printStackTrace();}


        try {
            postType = dict.getString("post_type");
        } catch (Exception e) {e.printStackTrace();}

        try {
            videoId = dict.getString("video_db_id");
        } catch (Exception e) {e.printStackTrace();}

        try {
            photoId = dict.getString("photo_db_id");
        } catch (Exception e) {e.printStackTrace();}

        if (!photoId.isEmpty() && photoId.length() > 0)  {
            isPhoto = true;
        }

        try {
            createdDate = dict.getDouble("created_time");
        } catch (Exception e) {e.printStackTrace();}
    }
}
