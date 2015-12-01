package com.starclub.syndicator.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.starclub.syndicator.R;
import com.starclub.syndicator.SSAPIRequestBuilder;
import com.starclub.syndicator.SSAppController;
import com.starclub.syndicator.SSConstants;
import com.starclub.syndicator.SSUtils;
import com.starclub.syndicator.customcontrol.CustomButtonTouchListener;
import com.starclub.syndicator.customcontrol.CustomFontButton;
import com.starclub.syndicator.customcontrol.CustomFontEdittext;
import com.starclub.syndicator.customcontrol.CustomFontTextView;
import com.starclub.syndicator.customcontrol.DialogHelper;
import com.turbomanage.httpclient.AsyncCallback;
import com.turbomanage.httpclient.HttpResponse;
import com.turbomanage.httpclient.ParameterMap;
import com.turbomanage.httpclient.android.AndroidHttpClient;

import org.json.JSONObject;

import java.util.Map;

public class SSLoginActivity extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final CustomFontEdittext etEmail = (CustomFontEdittext) findViewById(R.id.edit_email);
        final CustomFontEdittext etPass = (CustomFontEdittext) findViewById(R.id.edit_password);

        CustomFontTextView tvTerms = (CustomFontTextView) findViewById(R.id.text_terms);
        customTextView(tvTerms);

        CustomFontButton btnLogin = (CustomFontButton) findViewById(R.id.button_login);
        btnLogin.setOnTouchListener(CustomButtonTouchListener.getInstance());
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString();
                String password = etPass.getText().toString();

                if (email.length() < 1) {
                    DialogHelper.showToast(getApplicationContext(), "A valid email address is required");
                    return;
                }
                if (password.length() < 1) {
                    DialogHelper.showToast(getApplicationContext(), "Please enter your password");
                    return;
                }

                doLogin(email, password);

            }
        });

        if (SSConstants.isDemoApp) {
            String email = "sc@starsite.com";
            String password = "star";
            etEmail.setText(email);
            etPass.setText(password);

            doLogin(email, password);

            return;
        }

        Map<String, String> storedLogin =  SSAppController.sharedInstance().loadUserData();
        if (storedLogin != null) {
            String email = storedLogin.get("username");
            String password = storedLogin.get("pass");

            etEmail.setText(email);
            etPass.setText(password);

            doLogin(email, password);

            return;
        }

    }

    private void customTextView(CustomFontTextView view) {
        SpannableStringBuilder spanTxt = new SpannableStringBuilder(
                "By logging in, you agree to our \n");
        spanTxt.setSpan(new ForegroundColorSpan(Color.WHITE), 10, spanTxt.length(), 0);
        spanTxt.append("Terms of service");
        spanTxt.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
//                Toast.makeText(getApplicationContext(), "Terms of services Clicked",
//                        Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(SSLoginActivity.this, SSTermsActivity.class);
                intent.putExtra("url_web", SSAPIRequestBuilder.URL_TERMS);
                startActivity(intent);
            }
        }, spanTxt.length() - "Terms of service".length(), spanTxt.length(), 0);
        spanTxt.append(" & ");
//        spanTxt.setSpan(new ForegroundColorSpan(Color.BLACK), 32, spanTxt.length(), 0);
        spanTxt.append("Privacy Policy");
        spanTxt.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
//                Toast.makeText(getApplicationContext(), "Privacy Policy Clicked",
//                        Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(SSLoginActivity.this, SSTermsActivity.class);
                intent.putExtra("url_web", SSAPIRequestBuilder.URL_PRIVACY);
                startActivity(intent);
            }
        }, spanTxt.length() - "Privacy Policy".length(), spanTxt.length(), 0);

        view.setMovementMethod(LinkMovementMethod.getInstance());
        view.setText(spanTxt, TextView.BufferType.SPANNABLE);
    }

    private void doLogin(final String email, final String pass) {

        if (SSAppController.sharedInstance().firstCrashed()) {
            SSAppController.sharedInstance().saveFirstCrashed();

            String versionName = "", versionCode = "";
            try {
                versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
                versionCode = "" + getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
            } catch (Exception e) {e.printStackTrace();}

            throw new RuntimeException("This is a crash\n package name = " + getApplicationContext().getPackageName()
                + "build version = " + versionName + " " + versionCode);
        }


        final Dialog waitDialog = DialogHelper.getWaitDialog(this, "Logging In");
        waitDialog.show();

        AndroidHttpClient httpClient = new AndroidHttpClient(SSConstants.WEB_SERVICE_ROOT);
        httpClient.setMaxRetries(3);
        ParameterMap params = SSAPIRequestBuilder.APIDictionary(httpClient);

        params.add("email", email);
        params.add("password", pass);

        String url = SSAPIRequestBuilder.APIForLogin();

        httpClient.post(url, params, new AsyncCallback() {

            @Override
            public void onComplete(HttpResponse httpResponse) {

                waitDialog.dismiss();

                try {
                    JSONObject result = new JSONObject(httpResponse.getBodyAsString());

                    if (SSUtils.isResponseSuccessful(result)) {
                        SSAppController.sharedInstance().saveUserLoginUsername(email, pass);
                        SSAppController.sharedInstance().setupUserAndChannelWithDictionary(result);

                        //Check if they need to pair - welcome screen
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // TODO Auto-generated method stub
                                if (SSAppController.sharedInstance().currentChannel.hasAtLeastOnePairing) {

                                    startActivity(new Intent(SSLoginActivity.this, SSComposeActivity.class));
                                    finish();

                                } else {

                                    Intent intent = new Intent(SSLoginActivity.this, SSPairingActivity.class);
                                    intent.putExtra("initialAppPairing", true);
                                    SSPairingActivity.startingProperty = null;
                                    startActivity(intent);
                                    finish();
                                }

                            }
                        });

                    } else {

                        SSAppController.sharedInstance().alertWithServerResponse(SSLoginActivity.this, result);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onError(Exception e) {
                super.onError(e);

                waitDialog.dismiss();

                DialogHelper.getDialog(SSLoginActivity.this, "Connection Failed", "Unable to make request, please try again.", "OK", null, null).show();
            }
        });
    }
}

