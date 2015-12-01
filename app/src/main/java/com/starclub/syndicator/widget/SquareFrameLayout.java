package com.starclub.syndicator.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class SquareFrameLayout extends FrameLayout {

	public SquareFrameLayout(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public SquareFrameLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public SquareFrameLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
	 
	    int width = MeasureSpec.getSize(widthMeasureSpec);
	    int height = MeasureSpec.getSize(heightMeasureSpec);
	 
	    if (width < height) {
	    	height = width;
	    } else {
	    	width = height;
	    }
	    
	    super.onMeasure(
	            MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
	            MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
	    );
	}
}
