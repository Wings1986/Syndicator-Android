package com.starclub.syndicator.customcontrol;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by hotomcev on 21.07.2014.
 */
public class CustomFontTextView extends TextView {


    public CustomFontTextView(Context context) {
        super(context);
        if(!isInEditMode()){
            init();
        }
    }

    public CustomFontTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if(!isInEditMode()){
            init();
        }

    }

    public CustomFontTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if(!isInEditMode()){
            init();
        }

    }

    private void init() {
        setPaintFlags(getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);

        if (!this.isInEditMode()) {
            setTypeface(Typeface.createFromAsset(getContext().getAssets(), "fonts/HelveticaNeue-Light.ttf"));
        }

    }

    public void setTypeface(Typeface tf, int style) {
        if (!this.isInEditMode()) {
            if (style == Typeface.BOLD) {
                super.setTypeface(Typeface.createFromAsset(getContext().getAssets(), "fonts/HelveticaNeue-Medium.ttf"));
            } else if (style == Typeface.BOLD_ITALIC) {
                super.setTypeface(Typeface.createFromAsset(getContext().getAssets(), "fonts/HelveticaNeue-MediumItalic.ttf"));
            } else if (style == Typeface.ITALIC) {
                super.setTypeface(Typeface.createFromAsset(getContext().getAssets(), "fonts/HelveticaNeue-Italic.ttf"));
            } else {
                super.setTypeface(Typeface.createFromAsset(getContext().getAssets(), "fonts/HelveticaNeue-Light.ttf"));
            }
        }

    }
}
