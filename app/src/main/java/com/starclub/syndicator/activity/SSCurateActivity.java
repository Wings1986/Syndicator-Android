package com.starclub.syndicator.activity;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.squareup.picasso.Picasso;
import com.starclub.syndicator.R;
import com.starclub.syndicator.SSAPIRequestBuilder;
import com.starclub.syndicator.SSAppController;
import com.starclub.syndicator.SSConstants;
import com.starclub.syndicator.SSUtils;
import com.starclub.syndicator.customcontrol.CustomButtonTouchListener;
import com.starclub.syndicator.customcontrol.CustomFontTextView;
import com.starclub.syndicator.customcontrol.CustomStarsiteFontTextView;
import com.starclub.syndicator.customcontrol.DialogEditCallBack;
import com.starclub.syndicator.customcontrol.DialogHelper;
import com.starclub.syndicator.customcontrol.SSImageView;
import com.starclub.syndicator.data.SCItem;
import com.turbomanage.httpclient.AsyncCallback;
import com.turbomanage.httpclient.HttpResponse;
import com.turbomanage.httpclient.ParameterMap;
import com.turbomanage.httpclient.android.AndroidHttpClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SSCurateActivity extends FragmentActivity {

    private MyCustomAdapter mAdapter;
    PullToRefreshListView mListView;

    CustomFontTextView titleVideo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_curate);

        initView();
    }

    @Override
    public void onBackPressed() {

        if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }

    }


    private void initView() {

        // header
        CustomStarsiteFontTextView  btnSetting = (CustomStarsiteFontTextView) findViewById(R.id.nav_back);
        btnSetting.setOnTouchListener(CustomButtonTouchListener.getInstance());
        btnSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        btnSetting.setText("7"); // back

        CustomStarsiteFontTextView btnAction = (CustomStarsiteFontTextView) findViewById(R.id.nav_action);
        btnAction.setVisibility(View.INVISIBLE);

        titleVideo = (CustomFontTextView) findViewById(R.id.titleVideo);

        // ListView
        mListView = (PullToRefreshListView) findViewById(R.id.listview);
        mListView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
        mListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                fetchData();
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {

            }
        });


        mAdapter = new MyCustomAdapter(this);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                SCItem selectedItem = mAdapter.getItem((int) l);

                playVideo(selectedItem);

            }
        });

        fetchData();
    }

    private void fetchData() {
        final Dialog waitDialog = DialogHelper.getWaitDialog(this, "Loading !!");
        waitDialog.show();

        AndroidHttpClient httpClient = new AndroidHttpClient(SSConstants.WEB_SERVICE_ROOT);
        httpClient.setMaxRetries(3);
        ParameterMap params = SSAPIRequestBuilder.APIDictionary(httpClient);

        String url = SSAPIRequestBuilder.APIForGetCuratedData();

        httpClient.post(url, params, new AsyncCallback() {

            @Override
            public void onComplete(HttpResponse httpResponse) {

                waitDialog.dismiss();

                try {
                    JSONObject result = new JSONObject(httpResponse.getBodyAsString());

                    if (SSUtils.isResponseSuccessful(result)) {

                        final List<SCItem> items = new ArrayList<SCItem>();

                        JSONArray arrayData = result.getJSONArray("data");
                        for (int i = 0; i < arrayData.length(); i++) {
                            JSONObject dict = arrayData.getJSONObject(i);

                            SCItem item = new SCItem();
                            item.initWithDictionary(dict);

                            items.add(item);
                        }


                        //Check if they need to pair - welcome screen
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // TODO Auto-generated method stub
                                mAdapter.setItem(items);
                                mAdapter.notifyDataSetChanged();

                                updateTotals(items);
                            }
                        });

                    } else {

                        SSAppController.sharedInstance().alertWithServerResponse(SSCurateActivity.this, result);

                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mListView.onRefreshComplete();
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                super.onError(e);

                waitDialog.dismiss();

                DialogHelper.getDialog(SSCurateActivity.this, "Connection Failed", "Unable to make request, please try again.", "OK", null, null).show();
            }
        });
    }

    private void playVideo(SCItem item) {
        String urlVideo = item.fileURL.substring(0, item.fileURL.lastIndexOf('.'))  + "/master.m3u8";

        Intent intent = new Intent(SSCurateActivity.this, SSVideoPlayer.class);
        intent.putExtra("url_video", urlVideo);
        startActivity(intent);
    }


    private void updateTotals(List<SCItem> items) {
        int totalVideos = items.size();

        if(totalVideos == 0){
            titleVideo.setText("");
        }else{
            titleVideo.setText(getString(R.string.media) + ": " + totalVideos);
        }

    }

    public class MyCustomAdapter extends BaseAdapter {

        private Context mContext;
        private List<SCItem> items = new ArrayList<>();
        private LayoutInflater mInflater;


        public MyCustomAdapter(Context context) {
            mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mContext = context;
        }

        public void setItem(List<SCItem> array) {
            items = array;
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return items.size();
        }

        @Override
        public SCItem getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;


            if (convertView == null) {
                holder = new ViewHolder();

                convertView = mInflater.inflate(R.layout.cell_curate, null);

                holder.imageView = (SSImageView) convertView.findViewById(R.id.ivAvatar);
                holder.lbTitle = (CustomFontTextView)convertView.findViewById(R.id.tvTitle);
                holder.lbDate = (CustomFontTextView)convertView.findViewById(R.id.tvDate);

                holder.btnSwipeOpt = (CustomFontTextView) convertView.findViewById(R.id.btnSwipeOpt);

                holder.rightButtons = (LinearLayout)convertView.findViewById(R.id.rightButtons);
                holder.btnView = (LinearLayout)convertView.findViewById(R.id.btnView);
                holder.btnShare = (LinearLayout)convertView.findViewById(R.id.btnShare);
                holder.textView = (CustomFontTextView)convertView.findViewById(R.id.textView);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder)convertView.getTag();
            }


            try {

                final SCItem item = items.get(position);


                try {
//                    Picasso.with(mContext)
//                            .load(item.thumbnail)
//                            .into(holder.imageView);
                    holder.imageView.setImageBitmap(null);

                    String url = SSAPIRequestBuilder.getURLEncoded(item.thumbnail);

                    final ViewHolder finalHolder = holder;
                    ImageLoader.getInstance().displayImage(url, holder.imageView.mImageView, new ImageLoadingListener() {
                        @Override
                        public void onLoadingStarted(String imageUri, View view) {

                        }

                        @Override
                        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

                        }

                        @Override
                        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                            finalHolder.imageView.setImageBitmap(loadedImage);
                        }

                        @Override
                        public void onLoadingCancelled(String imageUri, View view) {

                        }
                    });

                } catch (Exception e) {e.printStackTrace();}

                try {

                    holder.lbTitle.setText(item.message);

//                    holder.lbDate.setText(item.ss_videoLengthHuman);
//                    if (item.ss_videoLengthHuman.equalsIgnoreCase("00:00:00")) {
//
//
//                        NSURL *videoURL=[NSURL URLWithString:item.fileURL];
//                        AVURLAsset *asset = [[AVURLAsset alloc] initWithURL:videoURL options:nil];
//
//                        NSTimeInterval durationInSeconds = 0.0;
//                        if (asset ) {
//                            durationInSeconds = CMTimeGetSeconds(asset.duration);
//
//
//                            int seconds = (int) durationInSeconds % 60;
//                            int minutes = (int) (durationInSeconds / 60) % 60;
//                            int hours = (int) durationInSeconds / 3600;
//
//                            _itemDate.text = [NSString stringWithFormat:@"%02d:%02d:%02d",hours, minutes, seconds];
//
//                        }
//                    }
                } catch (Exception e) {e.printStackTrace();}

                // event
                if (holder.rightButtons.getVisibility() == View.VISIBLE) {
                    closeRightButtons(holder.rightButtons);
                }

                final ViewHolder finalHolder = holder;
                holder.btnSwipeOpt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (finalHolder.rightButtons.getVisibility() == View.VISIBLE) {
                            closeRightButtons(finalHolder.rightButtons);
                        } else {
                            openRightButtons(finalHolder.rightButtons);
                        }

                    }
                });


                holder.btnShare.setOnTouchListener(CustomButtonTouchListener.getInstance());
                holder.btnShare.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        final Intent intent = new Intent(SSCurateActivity.this, SSPostActivity.class);

                        intent.putExtra("isPostCurated", true);
                        intent.putExtra("isVideo", true);
                        intent.putExtra("image_url", item.thumbnail);
                        intent.putExtra("video_url", item.fileURL);
                        intent.putExtra("viralContentId", item.itemId);
                        intent.putExtra("caption", item.message);


                        SSCurateActivity.this.startActivity(intent);

                    }
                });

                holder.btnView.setOnTouchListener(CustomButtonTouchListener.getInstance());
                holder.btnView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        playVideo(item);
                    }
                });

                if (item.isPhoto) {
                    holder.btnShare.setBackgroundColor(Color.parseColor("#75BF6B"));
                    holder.textView.setText(getResources().getString(R.string.view));
                } else {
                    holder.btnShare.setBackgroundColor(ContextCompat.getColor(SSCurateActivity.this, R.color.COLOR_TEAL));
                    holder.textView.setText(getResources().getString(R.string.play));
                }


            } catch (Exception e) {e.printStackTrace();}


            return convertView;
        }

    }

    public static class ViewHolder {

        public SSImageView imageView;
        public CustomFontTextView lbTitle;
        public CustomFontTextView lbDate;

        CustomFontTextView btnSwipeOpt;

        LinearLayout rightButtons;
        LinearLayout btnView, btnPlay, btnShare;
        CustomFontTextView textView;

    }



    private void openRightButtons(final LinearLayout rightMenu) {
        rightMenu.setVisibility(View.VISIBLE);
    }
    private void closeRightButtons(final LinearLayout rightMenu) {
        rightMenu.setVisibility(View.GONE);
    }

}
