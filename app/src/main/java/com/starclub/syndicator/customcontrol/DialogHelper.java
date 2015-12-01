package com.starclub.syndicator.customcontrol;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.starclub.syndicator.R;


/**
 * Created by iGold on 6/2/15.
 */

public class DialogHelper {

    public static Dialog getDialog(Context context, String title, String content,
                                               String firstText, String secondText,
                                               final DialogCallBack callback) {
        if (context != null) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(title);
            builder.setMessage(content);
            builder.setCancelable(true);
            builder.setNegativeButton(firstText, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // TODO Auto-generated method stub
                    dialog.dismiss();

                    if (callback != null) {
                        callback.onClick(0);
                    }
                }
            });

            if (secondText != null) {
                builder.setPositiveButton(secondText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (callback != null) {
                            callback.onClick(1);
                        }
                    }
                });
            }

            AlertDialog alertDialog = builder.create();
            alertDialog.requestWindowFeature((int) Window.FEATURE_NO_TITLE);
            return alertDialog;
        }
        return null;
    }

    public static Dialog getEditDialog(Context context, String title, String content,
                                   String firstText, String secondText,
                                   final DialogEditCallBack callback) {
        if (context != null) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
//            builder.setTitle(title);
            builder.setMessage(title);

            final EditText input = new EditText(context);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            input.setLayoutParams(lp);
            input.setText(content);

            builder.setView(input);

            builder.setCancelable(true);
            builder.setNegativeButton(firstText, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // TODO Auto-generated method stub

                    dialog.dismiss();
                    if (callback != null) {
                        callback.onClick(1, input.getText().toString());
                    }


                }
            });

            if (secondText != null) {
                builder.setPositiveButton(secondText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if (input.getText().toString().length() > 0) {
                            dialog.dismiss();

                            if (callback != null) {
                                callback.onClick(0, input.getText().toString());
                            }
                        }
                    }
                });
            }

            AlertDialog alertDialog = builder.create();
            alertDialog.requestWindowFeature((int) Window.FEATURE_NO_TITLE);
            return alertDialog;
        }
        return null;
    }


    public static void showToast (Context context, String title) {
        if (context != null) {
            Toast.makeText(context, title, Toast.LENGTH_LONG).show();
        }

    }


    public static ProgressDialog getProgressDialog(Context context){

        ProgressDialog waitDialog = new ProgressDialog(context);
        waitDialog.setMessage("Loading...");
        waitDialog.setCancelable(false);
        return waitDialog;
    }

    public static Dialog getWaitDialog(Activity context, String message){
//        AlertDialog.Builder builder = new AlertDialog.Builder(context);
//        LayoutInflater inflater = context.getLayoutInflater();
//
//
//        View view = inflater.inflate(R.layout.wait_dialog_layout, null);
//
//        CustomFontTextView tvMessage = (CustomFontTextView) view.findViewById(R.id.loading_title);
//        tvMessage.setText(message);
//
//        builder.setView(view);
//        builder.setCancelable(false);
//        return builder.create();

        Dialog alertDialog = new Dialog(context);
        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        LayoutInflater inflater = context.getLayoutInflater();
        View view = inflater.inflate(R.layout.wait_dialog_layout, null);
        CustomFontTextView tvMessage = (CustomFontTextView) view.findViewById(R.id.loading_title);
        tvMessage.setText(message);

        alertDialog.setContentView(view);
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        return alertDialog;
    }

}
