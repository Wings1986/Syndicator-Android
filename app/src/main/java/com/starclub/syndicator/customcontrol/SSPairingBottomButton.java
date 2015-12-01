package com.starclub.syndicator.customcontrol;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.starclub.syndicator.R;
import com.starclub.syndicator.SSAppController;
import com.starclub.syndicator.SSConstants;
import com.starclub.syndicator.data.SSPairingItem;

import java.util.HashMap;

/**
 * Created by iGold on 11/2/15.
 */
public class SSPairingBottomButton extends LinearLayout {

    CustomStarsiteFontTextView lbIcon;
    CustomFontTextView lbTitle;

    public boolean isConnected = false;

    public SSPairingBottomButton(Context context) {
        super(context);
        init(context);
    }

    public SSPairingBottomButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SSPairingBottomButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void init(Context context) {
        View v = View.inflate(context, R.layout.bottom_button_layout, null);

        lbIcon = (CustomStarsiteFontTextView) v.findViewById(R.id.lbIcon);
        lbTitle = (CustomFontTextView) v.findViewById(R.id.lbTitle);

        addView(v);
    }

    public void setupWithDictionary(HashMap<String, String> dic) {

        setAlpha(0.3f);
        isConnected = false;

        lbIcon.setText(dic.get("icon"));

        int propertyId = Integer.parseInt(dic.get("propertyId"));

        lbTitle.setText(getResources().getString(R.string.disabled));

        if (SSConstants.isDemoApp || SSAppController.sharedInstance().isPinModeApp) {
            lbTitle.setText(getResources().getString(R.string.enabled));
            isConnected = true;
        }

        for(SSPairingItem i : SSAppController.sharedInstance().pairingItems){
            if(i.pairingTypeId.getValue() == propertyId){
                if(i.isConnected){
                    lbTitle.setText(getResources().getString(R.string.enabled));
                    isConnected = true;
                }
            }
        }

    }

}
