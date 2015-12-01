package com.starclub.syndicator.customcontrol;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by hotomcev on 21.07.2014.
 */
public class CustomStarsiteFontTextView extends TextView {


    public CustomStarsiteFontTextView(Context context) {
        super(context);
        if(!isInEditMode()){
            init();
        }
    }

    public CustomStarsiteFontTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if(!isInEditMode()){
            init();
        }

    }

    public CustomStarsiteFontTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if(!isInEditMode()){
            init();
        }

    }

    private void init() {
        setPaintFlags(getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);
        if(!this.isInEditMode()) {
            super.setTypeface(Typeface.createFromAsset(getContext().getAssets(), "fonts/starsite.ttf"));
        }
    }

    public void setTypeface(Typeface tf, int style) {
        if(!this.isInEditMode()) {
            super.setTypeface(Typeface.createFromAsset(getContext().getAssets(), "fonts/starsite.ttf"));
        }

    }
}
