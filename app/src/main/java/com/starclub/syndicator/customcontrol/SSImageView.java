package com.starclub.syndicator.customcontrol;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.starclub.syndicator.R;

/**
 * Created by iGold on 11/2/15.
 */
public class SSImageView extends FrameLayout {

    Context mContext;

    public ImageView mImageView;
    CustomStarsiteFontTextView lbIcon;

    public SSImageView(Context context) {
        super(context);
        init(context);
    }

    public SSImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SSImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void init(Context context) {

        mContext = context;

        View v = View.inflate(context, R.layout.custom_imageview_layout, null);

        lbIcon = (CustomStarsiteFontTextView) v.findViewById(R.id.lbIcon);
        mImageView = (ImageView) v.findViewById(R.id.imageview);

        addView(v);
    }

    public void setImageBitmap(Bitmap bitmap) {
        if (bitmap == null) {
            lbIcon.setVisibility(View.VISIBLE);
            mImageView.setVisibility(View.INVISIBLE);
        }
        else {
            lbIcon.setVisibility(View.INVISIBLE);
            mImageView.setVisibility(View.VISIBLE);
        }

        mImageView.setImageBitmap(bitmap);
    }


}
