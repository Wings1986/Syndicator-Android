package com.starclub.syndicator.widget;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Path;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.LinearLayout;


import java.text.SimpleDateFormat;
import java.util.Calendar;

@SuppressLint("DrawAllocation")
public class GraphView extends LinearLayout {

	float sliderValRounded = -1f;
	float currentRevenuePerWeekRate;

	Typeface font;


	public GraphView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		initView(context);
	}

	public GraphView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		initView(context);
	}

	public GraphView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		initView(context);
	}

	private void initView(Context context)
	{
		font = Typeface.createFromAsset(context.getAssets(), "fonts/HelveticaNeue.ttf");
		setWillNotDraw(false);
	}

	final int TOTAL_BARS = 12;

	public void setValue(float sliderValRounded, float currentRevenuePerWeekRate) {

		this.sliderValRounded = sliderValRounded;
		this.currentRevenuePerWeekRate = currentRevenuePerWeekRate;

		invalidate();
	}

	final int OFFSET_TITLE = 40;

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		
		Paint paint = new Paint();

		int width = getWidth();
		int height = getHeight();

		if (sliderValRounded != -1) {
			paint.setTypeface(font);
			paint.setTextSize(20.0f);

			// interval segment
			float intervalWidth = width / TOTAL_BARS;

			for (int i = 0; i < TOTAL_BARS; i++) {
				// title
				paint.setStyle(Paint.Style.STROKE);
				paint.setColor(Color.WHITE);

				Calendar cal = Calendar.getInstance();
				cal.add(Calendar.MONTH, i);

				SimpleDateFormat sdf = new SimpleDateFormat("MMM");
				String monthStr = sdf.format(cal.getTime());

				paint.setColor(Color.WHITE);
				paint.setTextAlign(Align.CENTER);

				float centerX = intervalWidth * i + (intervalWidth - 2) / 2;
				canvas.drawText(monthStr, centerX, height - 5, paint);

				// stick
				paint.setStyle(Paint.Style.FILL);
				paint.setColor(adjustAlpha(Color.parseColor("#1DAEEC"), 0.1f + (0.1f * i)));

				float fHeight = (height - OFFSET_TITLE * 2) * sliderValRounded * (i + 1) / TOTAL_BARS;

				float left = intervalWidth * i;
				float top = OFFSET_TITLE + (height - OFFSET_TITLE * 2 - fHeight);
				float right = intervalWidth * (i + 1) - 2;
				float bottom = height - OFFSET_TITLE;

				canvas.drawRect(left, top, right, bottom, paint);

				// title
				paint.setStyle(Paint.Style.STROKE);
				paint.setColor(Color.WHITE);

				String formattedString = String.format("%,f", fHeight * currentRevenuePerWeekRate);

				String[] parts = formattedString.split(",");
				String[] partsLabel = {"k","m","b","t"};

				int totalParts = parts.length;

				String cleanStr;
				if(totalParts > 1){
					String afterDecimal = parts[1];
					afterDecimal = afterDecimal.substring(0, 1);

					if(totalParts <= 2){
						afterDecimal = "0";
					}

					cleanStr = parts[0] + ((afterDecimal.equalsIgnoreCase("0")) ? "" : "."+afterDecimal) + partsLabel[totalParts-2];
				}else{
					cleanStr = parts[0];
				}

				canvas.drawText(cleanStr, centerX, top-5, paint);

			}

			// round line
//			paint.reset();
			paint.setColor(Color.parseColor("#FFB300"));
			paint.setStrokeWidth(2);
			final Path path = new Path();
			path.moveTo(0, height - OFFSET_TITLE); // start point
			path.quadTo(width / 2, OFFSET_TITLE + (height - OFFSET_TITLE * 2) * 2 / 3, width, OFFSET_TITLE);
			canvas.drawPath(path, paint);

			// dot line
			paint.setStyle(Paint.Style.STROKE);
			paint.setColor(Color.WHITE);
			paint.setPathEffect(new DashPathEffect(new float[]{5, 2, 2}, 0));
			canvas.drawLine(0, height - OFFSET_TITLE, width, height - OFFSET_TITLE, paint);
		}

		super.onDraw(canvas);
	}


	public int adjustAlpha(int color, float factor) {
		if (factor >= 1.f) {
			factor = 1.f;
		}

		int alpha = Math.round(Color.alpha(color) * factor);
		int red = Color.red(color);
		int green = Color.green(color);
		int blue = Color.blue(color);
		return Color.argb(alpha, red, green, blue);
	}
}
