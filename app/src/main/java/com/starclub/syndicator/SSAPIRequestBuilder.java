package com.starclub.syndicator;

import com.turbomanage.httpclient.ParameterMap;
import com.turbomanage.httpclient.android.AndroidHttpClient;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * Created by iGold on 10/16/15.
 */
public class SSAPIRequestBuilder {

    public static String API_VERSION_NUMBER = "3.4";

    public static String URL_TERMS   = "http://darwin.on.starsite.com/template/_terms.php";
    public static String URL_PRIVACY = "http://darwin.on.starsite.com/template/_privacy.php";

    public static String getURLEncoded(String url) {
        try {
            url = URLDecoder.decode(url, "UTF-8");

            url = url.replaceAll("\\+", "%20");
            url = url.replace(" ", "%20");

        } catch(UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return url;
    }

    public static ParameterMap APIDictionary(AndroidHttpClient httpClient) {

        ParameterMap params = httpClient.newParams();

        if (SSAppController.sharedInstance().currentUser != null && !SSAppController.sharedInstance().currentUser.token.isEmpty()) {
            params.add("token", SSAppController.sharedInstance().currentUser.token);
        }
        else {
            params.add("token", SSAppController.sharedInstance().getUserToken());
        }

        if (SSAppController.sharedInstance().currentChannel != null && !SSAppController.sharedInstance().currentChannel.channelId.isEmpty()) {
            params.add("channel_id", SSAppController.sharedInstance().currentChannel.channelId);
        }
        else {
            params.add("channel_id", SSAppController.sharedInstance().getUserChannelId());
        }

        return params;
    }

    public static String APISuffix() {

        return "?apiversion=" + API_VERSION_NUMBER + "&nd=" + ((!SSConstants.isDemoApp) ? "1" : "0") +
                "&smm=" + ((SSAppController.sharedInstance().shouldShowMeTheMoney) ? "Y" : "0") + "&pm=" + ((SSAppController.sharedInstance().isPinModeApp) ? "Y" : "0");

    }

    public static String APIForLogin() {
        return "/login/" + APISuffix();
    }

    public static String APIForStoreSocialKeys() {
        return "/storeSocialKeys/" + APISuffix();
    }

    public static String APIForUpdateSocialKeyStatus() {
        return "/updateSocialKeyStatus/" + APISuffix();
    }

    public static String APIForHeatMapData() {
        return "/heatMapData/" + APISuffix();
    }

    public static String APIForGetDashboardData() {
        return "/dashboardData/" + APISuffix();
    }

    public static String APIForGetCuratedData() {
        return "/curateData/" + APISuffix();
    }

    public static String APIForPostContent() {
        return "/post/" + APISuffix();
    }

    public static String APIForPostCuratedContent() {
        return "/curatePost/" + APISuffix();
    }

    public static String APIUpdateAvatar() {
        return "/updateAvatar/" + APISuffix();
    }

    public static String APIForDeletePost() {
        return "/deletePost/" + APISuffix();
    }

    public static String APIForUpdatePost() {
        return "/updatePost/" + APISuffix();
    }

    public static String APIForUpdateDeviceId() {
        return "/updatePushDeviceId/" + APISuffix();
    }

}
