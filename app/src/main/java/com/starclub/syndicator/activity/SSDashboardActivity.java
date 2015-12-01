package com.starclub.syndicator.activity;

import android.app.ActionBar;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
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
import com.starclub.syndicator.customcontrol.CountingTextView;
import com.starclub.syndicator.customcontrol.CustomButtonTouchListener;
import com.starclub.syndicator.customcontrol.CustomFontTextView;
import com.starclub.syndicator.customcontrol.CustomStarsiteFontTextView;
import com.starclub.syndicator.customcontrol.DialogCallBack;
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

public class SSDashboardActivity extends FragmentActivity {

    static String TAG = SSDashboardActivity.class.getName();

    private MyCustomAdapter mAdapter;
    PullToRefreshListView mListView;

    CustomFontTextView titleVideo;
    CountingTextView lbEarnings, lbImpressions, lbReach;
    CustomFontTextView lbEarningsSubtitle, lbImpressionsSubtitle, lbReachSubtitle;

    JSONObject totals;

    LinearLayout layoutTop, layoutBottom;
    FrameLayout layoutMiddle;
    LinearLayout layoutMapBottom;

    int originTopHeight, originBottomHeight, originMiddleHeight;
    CustomFontTextView lbMapPercent;

    boolean mapExpanded = false;

    GoogleMap mGoogleMap;
    HeatmapTileProvider mProvider;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        initView();

        MapsInitializer.initialize(this);
        initMapIfNecessary();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initMapIfNecessary();
    }

    protected void initMapIfNecessary() {
        if( mGoogleMap != null ) {
            return;
        }

        mGoogleMap = ( (MapFragment) getFragmentManager().findFragmentById(R.id.map) ).getMap();
    }

    private void addHeatMap(List<LatLng> locations/*, LatLng mCenterLocation*/) {
        mProvider = new HeatmapTileProvider.Builder().data( locations ).build();
//        mProvider.setRadius( HeatmapTileProvider.DEFAULT_RADIUS );
        mProvider.setRadius( 50 );
        mGoogleMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));


//        CameraPosition position = CameraPosition.builder()
//                .target( mCenterLocation )
//                .zoom( 10.f )
//                .build();
//
//        mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), null);
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

        lbEarnings = (CountingTextView) findViewById(R.id.lbEarning);
        lbEarnings.setText("- - -");
        lbEarningsSubtitle = (CustomFontTextView) findViewById(R.id.lbEarningSubtitle);
        lbEarningsSubtitle.setText("~Est Quarterly");

        lbImpressions = (CountingTextView) findViewById(R.id.lbImpression);
        lbImpressions.setText("- - -");
        lbImpressionsSubtitle = (CustomFontTextView) findViewById(R.id.lbImpressionSubtitle);
        lbImpressionsSubtitle.setText("Impressions");

        lbReach = (CountingTextView) findViewById(R.id.lbReach);
        lbReach.setText("- - -");
        lbReachSubtitle = (CustomFontTextView) findViewById(R.id.lbReachSubtitle);
        lbReachSubtitle.setText("Reach");

        CustomStarsiteFontTextView btnExpandMap = (CustomStarsiteFontTextView) findViewById(R.id.btnExpandMap);
        btnExpandMap.setOnTouchListener(CustomButtonTouchListener.getInstance());
        btnExpandMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mapExpanded) {
                    //
                    layoutTop.setVisibility(View.VISIBLE);
                    layoutBottom.setVisibility(View.VISIBLE);

                    layoutMiddle.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, originMiddleHeight));
                    layoutMapBottom.setVisibility(View.GONE);
                }
                else {
                    // expand

                    layoutTop.setVisibility(View.GONE);
                    layoutBottom.setVisibility(View.GONE);

                    layoutMiddle.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));

                    layoutMapBottom.setVisibility(View.VISIBLE);
                }

                mapExpanded = !mapExpanded;
            }
        });


        titleVideo = (CustomFontTextView) findViewById(R.id.titleVideo);

        // ListView
        mListView = (PullToRefreshListView) findViewById(R.id.listview);
        mListView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
        mListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                refreshPage();
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

                Intent intent = new Intent(SSDashboardActivity.this, SSDetailActivity.class);
                intent.putExtra("item", selectedItem);
                startActivity(intent);
            }
        });

        layoutTop = (LinearLayout) findViewById(R.id.layoutHeader);
//        layoutTop.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//            @Override
//            public void onGlobalLayout() {
//                layoutTop.getViewTreeObserver().removeOnGlobalLayoutListener(this);
//
//                originTopHeight = layoutTop.getMeasuredHeight();
//            }
//        });

        layoutBottom = (LinearLayout) findViewById(R.id.layoutBottom);
//        layoutBottom.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//            @Override
//            public void onGlobalLayout() {
//                layoutBottom.getViewTreeObserver().removeOnGlobalLayoutListener(this);
//
//                originBottomHeight = layoutBottom.getMeasuredHeight();
//            }
//        });

        layoutMiddle = (FrameLayout) findViewById(R.id.layoutMiddle);
        layoutMiddle.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                layoutMiddle.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                originMiddleHeight = layoutMiddle.getMeasuredHeight();
            }
        });

        layoutMapBottom = (LinearLayout) findViewById(R.id.layoutMapBottom);
        layoutMapBottom.setVisibility(View.GONE);

        lbMapPercent = (CustomFontTextView) findViewById(R.id.lbMapPercent);
        lbMapPercent.setText("");


        boolean doingCashOut = SSAppController.sharedInstance().showTheMoney;
        if (doingCashOut) {

        }

        refreshPage();
    }

    private void refreshPage() {
        fetchData();
        fetchHeatMapData();
    }

    private void fetchData() {
        final Dialog waitDialog = DialogHelper.getWaitDialog(this, "Loading Real-Time");
        waitDialog.show();

        AndroidHttpClient httpClient = new AndroidHttpClient(SSConstants.WEB_SERVICE_ROOT);
        httpClient.setMaxRetries(3);
        ParameterMap params = SSAPIRequestBuilder.APIDictionary(httpClient);

        String url = SSAPIRequestBuilder.APIForGetDashboardData();

        httpClient.post(url, params, new AsyncCallback() {

            @Override
            public void onComplete(HttpResponse httpResponse) {

                waitDialog.dismiss();

                try {
                    JSONObject result = new JSONObject(httpResponse.getBodyAsString());

                    if (SSUtils.isResponseSuccessful(result)) {

                        try {
                            totals = result.getJSONObject("totals");
                        } catch (Exception e) {e.printStackTrace();}


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

                                updateTotals(items.size());
                            }
                        });

                    } else {

                        SSAppController.sharedInstance().alertWithServerResponse(SSDashboardActivity.this, result);

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

                DialogHelper.getDialog(SSDashboardActivity.this, "Connection Failed", "Unable to make request, please try again.", "OK", null, null).show();
            }
        });
    }

    private void fetchHeatMapData() {

        AndroidHttpClient httpClient = new AndroidHttpClient(SSConstants.WEB_SERVICE_ROOT);
        httpClient.setMaxRetries(3);
        ParameterMap params = SSAPIRequestBuilder.APIDictionary(httpClient);

        String url = SSAPIRequestBuilder.APIForHeatMapData();

        httpClient.post(url, params, new AsyncCallback() {

            @Override
            public void onComplete(HttpResponse httpResponse) {


                try {
                    JSONObject result = new JSONObject(httpResponse.getBodyAsString());

                    if (SSUtils.isResponseSuccessful(result)) {

                        String percentDisplay = "";
                        try {
                            percentDisplay = result.getString("percents_display");
                        } catch (Exception e) {e.printStackTrace();}

                        final String mapPercent = percentDisplay;


                        final ArrayList<LatLng> locations = new ArrayList<LatLng>();

                        JSONArray arrayData;
                        try {
                            arrayData = result.getJSONArray("data");

                            for (int i = 0; i < arrayData.length(); i++) {

                                JSONObject dict = arrayData.getJSONObject(i);

                                double lat = 0.0f;
                                double lng = 0.0f;

                                try {
                                    lat = Double.parseDouble(dict.getString("lat"));
                                    lng = Double.parseDouble(dict.getString("lng"));
                                } catch (Exception e) {e.printStackTrace();}

                                locations.add(new LatLng(lat, lng));
                            }

                        } catch (Exception e) {e.printStackTrace();}

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // TODO Auto-generated method stub

                                addHeatMap(locations);

                                lbMapPercent.setText(mapPercent);

                            }
                        });

                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onError(Exception e) {
                super.onError(e);
            }
        });
    }

    private void updateTotals(int totalVideos) {

        float totalEarnings = 0.f;
        try {
            String s = totals.getString("earnings_avg");
            s = s.replace(",", ".");
            totalEarnings = Float.parseFloat(s);
        } catch (Exception e){e.printStackTrace();}

        String totalEarningsSymbol = "";
        try {
            totalEarningsSymbol = totals.getString("earnings_avg_symbol");
        } catch (Exception e){e.printStackTrace();}

        String EarningsSubtitle = "";
        try {
            EarningsSubtitle = totals.getString("earnings_subtitle");
        } catch (Exception e){e.printStackTrace();}

        lbEarningsSubtitle.setText(EarningsSubtitle);

        float totalImpressions = 0.f;
        try {
            totalImpressions = Float.parseFloat(totals.getString("impressions_avg"));
        } catch (Exception e){e.printStackTrace();}

        String totalImpressionsSymbol = "";
        try {
            totalImpressionsSymbol = totals.getString("impressions_avg_symbol");
        } catch (Exception e){e.printStackTrace();}

        String impressionsSubtitle = "";
        try {
            impressionsSubtitle = totals.getString("impressions_subtitle");
        } catch (Exception e){e.printStackTrace();}

        lbImpressionsSubtitle.setText(impressionsSubtitle);


        float totalReach = 0.f;
        try {
            totalReach = Float.parseFloat(totals.getString("reach_avg"));
        } catch (Exception e){e.printStackTrace();}

        String totalReachSymbol = "";
        try {
            totalReachSymbol = totals.getString("reach_avg_symbol");
        } catch (Exception e){e.printStackTrace();}

        String reachSubtitle = "";
        try {
            reachSubtitle = totals.getString("reach_subtitle");
        } catch (Exception e){e.printStackTrace();}

        lbReachSubtitle.setText(reachSubtitle);

        try {
            SSAppController.sharedInstance().currentChannel.weeklyPostingRate = totals.getInt("weekly_post_rate");
        } catch (Exception e) {e.printStackTrace();}

        if (totalEarnings > 0) {
            lbEarnings.countFrom(0, totalEarnings, 1000, totalEarningsSymbol);
        } else {
            lbEarnings.setText("- - -");
        }

        if (totalImpressions > 0) {
            lbImpressions.countFrom(0, totalImpressions, 1000, totalImpressionsSymbol);
        } else {
            lbImpressions.setText("- - -");
        }

        if (totalReach > 0) {
            lbReach.countFrom(0, totalReach, 1000, totalReachSymbol);
        } else {
            lbReach.setText("- - -");
        }


        if(totalVideos == 0){
            titleVideo.setText("");
        }else{
            titleVideo.setText(getResources().getString(R.string.posts) +  ": " + totalVideos);
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

        public List<SCItem> getItems() {
            return items;
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
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;


            if (convertView == null) {
                holder = new ViewHolder();

                convertView = mInflater.inflate(R.layout.cell_dashboard, null);

                holder.imageView = (SSImageView) convertView.findViewById(R.id.ivAvatar);
                holder.lbTitle = (CustomFontTextView)convertView.findViewById(R.id.tvTitle);
                holder.lbDate = (CustomFontTextView)convertView.findViewById(R.id.tvDate);

                holder.btnSwipeOpt = (CustomFontTextView) convertView.findViewById(R.id.btnSwipeOpt);

                holder.rightButtons = (LinearLayout)convertView.findViewById(R.id.rightButtons);
                holder.btnView = (LinearLayout)convertView.findViewById(R.id.btnView);
                holder.btnEmbed = (LinearLayout)convertView.findViewById(R.id.btnEmbed);
                holder.btnEdit = (LinearLayout)convertView.findViewById(R.id.btnEdit);
                holder.btnDelete = (LinearLayout)convertView.findViewById(R.id.btnDelete);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder)convertView.getTag();
            }


            try {

                final SCItem item = items.get(position);

                Log.d(TAG, "image url = " + item.thumbnail);

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

                    holder.lbDate.setText(SSUtils.timeAgo(item.createdDate));


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

                holder.btnView.setOnTouchListener(CustomButtonTouchListener.getInstance());
                holder.btnView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String urlString = "";
                        if (SSConstants.isDemoApp || SSAppController.sharedInstance().isPinModeApp) {
                            urlString = "http://sc.on.starsite.com";
                        } else {
                            urlString = item.url;
                        }
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(urlString)));
                    }
                });

                holder.btnEmbed.setOnTouchListener(CustomButtonTouchListener.getInstance());
                holder.btnEmbed.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if(item.isPhoto){
                            String urlString = "";
                            if (SSConstants.isDemoApp || SSAppController.sharedInstance().isPinModeApp) {
                                urlString = "http://sc.on.starsite.com";
                            } else {
                                urlString = item.url;
                            }
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(urlString)));
                        }else{
                            ClipboardManager clipboard = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
                            ClipData clip = ClipData.newPlainText("label", item.embed);
                            clipboard.setPrimaryClip(clip);

                            DialogHelper.getDialog(mContext, "Copied to Clipboard", "The embed code for this item is now in your clipboard, paste to share", "OK", null, null).show();
                        }

                    }
                });

                holder.btnEdit.setOnTouchListener(CustomButtonTouchListener.getInstance());
                holder.btnEdit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DialogHelper.getEditDialog(SSDashboardActivity.this, "Update Description", item.message, "Cancel", "Update", new DialogEditCallBack() {
                            @Override
                            public void onClick(int which, String text) {
                                if (which == 1) {
                                    saveEditedItem(position, text);
                                }
                            }
                        }).show();
                    }
                });

                holder.btnDelete.setOnTouchListener(CustomButtonTouchListener.getInstance());
                holder.btnDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        DialogHelper.getDialog(SSDashboardActivity.this, "", "Are you sure you want to delete this?", "Delete", null, new DialogCallBack() {
                            @Override
                            public void onClick(int which) {
                                deleteItem(position);
                            }
                        }).show();
                    }
                });

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
        LinearLayout btnView, btnEmbed, btnEdit, btnDelete;

    }

    private void openRightButtons(final LinearLayout rightMenu) {
        rightMenu.setVisibility(View.VISIBLE);
    }
    private void closeRightButtons(final LinearLayout rightMenu) {
        rightMenu.setVisibility(View.GONE);
    }

    private void deleteItem(final int position) {

        final List<SCItem> items = mAdapter.getItems();

        final SCItem item = items.get(position);

        AndroidHttpClient httpClient = new AndroidHttpClient(SSConstants.WEB_SERVICE_ROOT);
        httpClient.setMaxRetries(3);
        ParameterMap params = SSAPIRequestBuilder.APIDictionary(httpClient);

        params.add("video_id", item.videoId);
        params.add("item_type", item.postType);
        params.add("photo_id", item.videoId);

        String url = SSAPIRequestBuilder.APIForDeletePost();

        httpClient.post(url, params, new AsyncCallback() {

            @Override
            public void onComplete(HttpResponse httpResponse) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        items.remove(position);

                        mAdapter.setItem(items);
                        mAdapter.notifyDataSetChanged();

                        titleVideo.setText(getResources().getString(R.string.posts) + ": " + items.size());
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                super.onError(e);
            }
        });

    }

    private void saveEditedItem(final int position, final String msg) {

        final List<SCItem> items = mAdapter.getItems();

        final SCItem item = items.get(position);

        AndroidHttpClient httpClient = new AndroidHttpClient(SSConstants.WEB_SERVICE_ROOT);
        httpClient.setMaxRetries(3);
        ParameterMap params = SSAPIRequestBuilder.APIDictionary(httpClient);

        params.add("video_id", item.videoId);
        params.add("item_type", item.postType);
        params.add("photo_id", item.videoId);
        params.add("msg", msg);

        String url = SSAPIRequestBuilder.APIForUpdatePost();

        httpClient.post(url, params, new AsyncCallback() {

            @Override
            public void onComplete(HttpResponse httpResponse) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        items.get(position).message = msg;

                        mAdapter.setItem(items);
                        mAdapter.notifyDataSetChanged();

                    }
                });
            }

            @Override
            public void onError(Exception e) {
                super.onError(e);
            }
        });

    }


}
