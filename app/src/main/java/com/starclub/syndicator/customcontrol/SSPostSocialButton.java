package com.starclub.syndicator.customcontrol;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.starclub.syndicator.R;
import com.starclub.syndicator.data.SSPairingItem;

import org.json.JSONObject;

/**
 * Created by iGold on 11/2/15.
 */
public class SSPostSocialButton extends FrameLayout {

    Context mContext;

    CustomStarsiteFontTextView lbIcon;
    CustomStarsiteFontTextView lbChecked;

    SSPairingItem item;
    boolean isPostingVideo;

    boolean bSelected = false;

    public SSPostSocialButton(Context context) {
        super(context);
        init(context);
    }

    public SSPostSocialButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SSPostSocialButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void init(Context context) {

        mContext = context;

        View v = View.inflate(context, R.layout.post_social_button_layout, null);

        lbIcon = (CustomStarsiteFontTextView) v.findViewById(R.id.lbIcon);
        lbChecked = (CustomStarsiteFontTextView) v.findViewById(R.id.lbCheck);

        addView(v);
    }

    public JSONObject packageForServer() {
        JSONObject obj = new JSONObject();
        try {

            obj.put("property_id", item.pairingId);
            obj.put("property", item.title);
            obj.put("active", bSelected ? "Y" : "N");
            obj.put("message", "");
            obj.put("message_custom", "N");

        } catch (Exception e) {e.printStackTrace();}

        return obj;
    }

    public boolean isSelected() {
        return bSelected;
    }

    public SSPairingItem getItem() {
        return item;
    }

    public void setItem(SSPairingItem _item, boolean isVideo) {
        item = _item;
        isPostingVideo = isVideo;

        lbIcon.setText(item.icon);
        lbIcon.setTextColor(ContextCompat.getColor(mContext, R.color.COLOR_GRAY));
        lbIcon.setBackgroundResource(R.drawable.button_post_social_nor_bg);
        lbChecked.setVisibility(View.GONE);

        bSelected = false;

        if (item.isConnected) {
            bSelected = true;
            makeSocialStateOn(true);
        }
    }
    private void makeSocialStateOn(boolean val) {

        if(item.pairingTypeId == SSPairingItem.SSParingTypeId.SSParingTypeInstagram){
            boolean instalado = false;

            try {
                ApplicationInfo info = mContext.getPackageManager().getApplicationInfo("com.instagram.android", 0);
                instalado = true;
            } catch (PackageManager.NameNotFoundException e) {
                instalado = false;
            }

            if (instalado == false) {
                val = false;
            }

            if(isPostingVideo)
                val = false;
        }

        if(val){
            bSelected = true;
            lbIcon.setTextColor(ContextCompat.getColor(mContext, R.color.COLOR_GOLD));
            lbIcon.setBackgroundResource(R.drawable.button_post_social_sel_bg);
            lbChecked.setVisibility(View.VISIBLE);
        }else{
            bSelected = false;
            lbIcon.setTextColor(ContextCompat.getColor(mContext, R.color.COLOR_GRAY));
            lbIcon.setBackgroundResource(R.drawable.button_post_social_nor_bg);
            lbChecked.setVisibility(View.GONE);
        }

    }

    public void socialBtnPressed() {

        bSelected = !bSelected;
        makeSocialStateOn(bSelected);

        if(item.pairingTypeId == SSPairingItem.SSParingTypeId.SSParingTypeInstagram){

            if(isPostingVideo){

                DialogHelper.getDialog(mContext, "Video Limitation", "Instagram does now allow video posting at this time", "OK", null, null).show();

            }else{

                boolean instalado = false;

                try {
                    ApplicationInfo info = mContext.getPackageManager().getApplicationInfo("com.instagram.android", 0);
                    instalado = true;
                } catch (PackageManager.NameNotFoundException e) {
                    instalado = false;
                }

                if (!instalado) {
                    DialogHelper.getDialog(mContext, "Instagram not installed", "You will need to install Instagram on your device first", "OK", null, null).show();
                }
            }
        }
    }

}
