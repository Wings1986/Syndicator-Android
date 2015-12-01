package com.starclub.syndicator.customcontrol;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by hotomcev on 21.07.2014.
 */
public class CountingTextView extends TextView {

    public String format;
    public int duration;

    private float startingValue, destinationValue;
    private int progress = 0;

    public CountingTextView(Context context) {
        super(context);
        if(!isInEditMode()){
            init();
        }
    }

    public CountingTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if(!isInEditMode()){
            init();
        }

    }

    public CountingTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if(!isInEditMode()){
            init();
        }

    }

    private void init() {
//        Typeface tf = getTypeface();
//        setTypeface(tf.getStyle());
        setPaintFlags(getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);
    }

    public void setTypeface(Typeface tf, int style) {
        if(!this.isInEditMode()) {
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

    public void countFrom(float startValue, float endValue, int duration, String format) {

        this.startingValue = startValue;
        this.destinationValue = endValue;
        this.format = format;

        if(format == null || format.equalsIgnoreCase(""))
            this.format = "%f";

        if (duration == 0) {
            setTextValue(endValue);
            return;
        }

        progress = 0;

        Timer timer = new Timer();

        timer.schedule(new TimerTask() {
            public void run() {
                progress++;


                handler.sendEmptyMessage(0);

                if (progress >= 100) {
                    cancel();
                }
            }
        }, 0, duration / 100);
    }

    private void setTextValue(float value) {

        if (format.contains("%(.*)d") || format.contains("%(.*)i")) {
            setText(String.format(format, (int) value));
        }
        else {
            setText(String.format(format, value));
        }
    }
    private void updateValue() {

        if (progress >= 100) {
            setTextValue(destinationValue);
            return;
        }

        float currentValue = startingValue + ((destinationValue - startingValue) * progress / 100.0f);
        setTextValue(currentValue);
    }

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {

            if (msg.what == 0) {
                updateValue();
            }

            return false;
        }
    });
}
