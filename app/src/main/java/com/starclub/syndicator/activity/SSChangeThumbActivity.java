package com.starclub.syndicator.activity;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.starclub.syndicator.R;
import com.starclub.syndicator.SSUtils;
import com.starclub.syndicator.customcontrol.CustomButtonTouchListener;
import com.starclub.syndicator.customcontrol.CustomFontButton;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SSChangeThumbActivity extends FragmentActivity {

    private static String TAG = SSChangeThumbActivity.class.getName();


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_thumb);

        CustomFontButton btnCancel = (CustomFontButton) findViewById(R.id.btnCancel);
        btnCancel.setOnTouchListener(CustomButtonTouchListener.getInstance());
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SSChangeThumbActivity.this.finish();
            }
        });


        Uri videoUri = Uri.parse(getIntent().getStringExtra("videopath"));
        HashMap<String, String> info = SSUtils.getVideoPathInfo(videoUri, this);
        String videoPath = info.get("filepath");
        long duration = Long.parseLong(info.get("duration"));

        final List<Bitmap> thumbs = new ArrayList<Bitmap>();
        int interval = 3;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(videoPath);
        for (int i = 0 ; i < duration ; i += interval) {
            Bitmap bmp = retriever.getFrameAtTime(i * 1000000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
            thumbs.add(bmp);
        }

/*
        final RecyclerView mRecyclerView = (RecyclerView)findViewById(R.id.rv);
        mRecyclerView.setHasFixedSize(false);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);


        final RVAdapter adapter = new RVAdapter(thumbs);
        mRecyclerView.setAdapter(adapter);

        mRecyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(this, new OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        // do whatever

                        Bitmap bmp = thumbs.get(position);
                        String filepath = storeImage(bmp);

                        Intent returnIntent = new Intent();
                        returnIntent.putExtra("thumb", filepath);
                        SSChangeThumbActivity.this.setResult(Activity.RESULT_OK, returnIntent);
                        SSChangeThumbActivity.this.finish();
                    }
                })
        );
*/
        ViewPager mViewPager = (ViewPager) findViewById(R.id.pager);
        MyPagerAdapter adapter = new MyPagerAdapter(this, thumbs, new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                Bitmap bmp = thumbs.get(position);
                String filepath = storeImage(bmp);

                Intent returnIntent = new Intent();
                returnIntent.putExtra("thumb", filepath);
                SSChangeThumbActivity.this.setResult(Activity.RESULT_OK, returnIntent);
                SSChangeThumbActivity.this.finish();

            }
        });
        mViewPager.setAdapter(adapter);

    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    class RecyclerItemClickListener implements RecyclerView.OnItemTouchListener {
        private OnItemClickListener mListener;

        GestureDetector mGestureDetector;

        public RecyclerItemClickListener(Context context, OnItemClickListener listener) {
            mListener = listener;
            mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }
            });
        }

        @Override public boolean onInterceptTouchEvent(RecyclerView view, MotionEvent e) {
            View childView = view.findChildViewUnder(e.getX(), e.getY());
            if (childView != null && mListener != null && mGestureDetector.onTouchEvent(e)) {
                mListener.onItemClick(childView, view.getChildPosition(childView));
                return true;
            }
            return false;
        }

        @Override public void onTouchEvent(RecyclerView view, MotionEvent motionEvent) { }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }

    class RVAdapter extends RecyclerView.Adapter<RVAdapter.PersonViewHolder>{

        public class PersonViewHolder extends RecyclerView.ViewHolder {

            ImageView mImgView;

            PersonViewHolder(View itemView) {
                super(itemView);
                mImgView = (ImageView) itemView.findViewById(R.id.image);
            }
        }

        List<Bitmap> thumbs;

        RVAdapter(List<Bitmap> thumbs){
            this.thumbs = thumbs;
        }

        @Override
        public int getItemCount() {
            return thumbs.size();
        }

        @Override
        public PersonViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cell_pick_thumb, viewGroup, false);
            PersonViewHolder pvh = new PersonViewHolder(v);
            return pvh;
        }

        @Override
        public void onBindViewHolder(PersonViewHolder personViewHolder, int i) {

            personViewHolder.mImgView.setImageBitmap(thumbs.get(i));

        }

        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
        }
    }


    private String storeImage(Bitmap image) {
        File pictureFile = getOutputMediaFile();
        if (pictureFile == null) {
            Log.d(TAG, "Error creating media file, check storage permissions: ");// e.getMessage());
            return "";
        }
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            image.compress(Bitmap.CompressFormat.PNG, 90, fos);
            fos.close();
        } catch (FileNotFoundException e) {
            Log.d(TAG, "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d(TAG, "Error accessing file: " + e.getMessage());
        }

        return pictureFile.getAbsolutePath();
    }

    private  File getOutputMediaFile(){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory()
                + "/"
                + getResources().getString(R.string.app_name));

        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                return null;
            }
        }

        // Create a media file name
//        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmm", Locale.US).format(Calendar.getInstance().getTime());
        File mediaFile;
        String mImageName="temp.jpg";
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + mImageName);
        return mediaFile;
    }

    /*
     page adapter
     */
    private class MyPagerAdapter extends PagerAdapter{

        List<Bitmap> thumbs;
        Context mContext;
        OnItemClickListener listener;

        MyPagerAdapter(Context context, List<Bitmap> thumbs, OnItemClickListener listener){
            mContext = context;
            this.thumbs = thumbs;
            this.listener = listener;
        }

        @Override
        public int getCount() {
            return thumbs.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {

            View layout = View.inflate(mContext, R.layout.cell_pick_thumb, null);

            ImageView imageView = (ImageView) layout.findViewById(R.id.image);

            imageView.setImageBitmap(thumbs.get(position));

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onItemClick(v, position);
                    }
                }
            });

            container.addView(layout);
            return layout;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((ViewGroup)object);
        }

    }
}

