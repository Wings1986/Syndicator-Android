package com.starclub.syndicator.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import com.github.siyamed.shapeimageview.CircularImageView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.starclub.syndicator.R;
import com.starclub.syndicator.SSAPIRequestBuilder;
import com.starclub.syndicator.SSAppController;
import com.starclub.syndicator.SSConstants;
import com.starclub.syndicator.SSUtils;
import com.starclub.syndicator.customcontrol.CustomButtonTouchListener;
import com.starclub.syndicator.customcontrol.CustomFontTextView;
import com.starclub.syndicator.customcontrol.CustomStarsiteFontTextView;
import com.starclub.syndicator.customcontrol.DialogHelper;
import com.starclub.syndicator.data.SSPairingItem;
import com.starclub.syndicator.multipart.MultipartUtility;
import com.starclub.syndicator.widget.GraphView;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class SSSettingActivity extends FragmentActivity {

    static String TAG = SSSettingActivity.class.getName();

    CircularImageView ivAvatar;

    MyCustomAdapter mAdapter;

    int currentPerWeek;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        initView();
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

        CustomFontTextView btnLogout = (CustomFontTextView) findViewById(R.id.nav_logout);
        btnLogout.setVisibility(View.VISIBLE);
        btnLogout.setOnTouchListener(CustomButtonTouchListener.getInstance());
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SSAppController.sharedInstance().removeAllData();

                Intent intent = new Intent(SSSettingActivity.this, SSLoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
        if (SSConstants.isDemoApp) {
            btnLogout.setVisibility(View.GONE);
        }


        // avatar image
        ivAvatar = (CircularImageView) findViewById(R.id.ivAvatar);
        ivAvatar.setOnTouchListener(CustomButtonTouchListener.getInstance());
        ivAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImageIntent();
            }
        });

        try {
            String url = SSAPIRequestBuilder.getURLEncoded(SSAppController.sharedInstance().currentChannel.img);
            ImageLoader.getInstance().displayImage(url, ivAvatar);
        } catch (Exception e) {e.printStackTrace();}

        // user name
        CustomFontTextView lbUsername = (CustomFontTextView) findViewById(R.id.lbUsername);
        lbUsername.setText(SSAppController.sharedInstance().currentChannel.name);


        // Grid View
        GridView gridview = (GridView) findViewById(R.id.gridview);
        mAdapter = new MyCustomAdapter(this);
        mAdapter.setItem(SSAppController.sharedInstance().pairingItems);
        gridview.setAdapter(mAdapter);

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                SSPairingItem item = mAdapter.getItem(position);

                Intent intent = new Intent(SSSettingActivity.this, SSPairingActivity.class);
                SSPairingActivity.startingProperty = item;
                //intent.putExtra("startingProperty", item);
                startActivity(intent);

            }
        });

        final LinearLayout layoutArrow = (LinearLayout) findViewById(R.id.layoutArrow);
        final CustomFontTextView currentPerWeekLabel = (CustomFontTextView) findViewById(R.id.currentPerWeekLabel);

        //
        currentPerWeek = SSAppController.sharedInstance().currentChannel.weeklyPostingRate;
        if(currentPerWeek > 100)
            currentPerWeek = 100;
        if(currentPerWeek < 1)
            currentPerWeek = 1;

        final float currentRevenuePerWeekRate = SSAppController.sharedInstance().currentChannel.weeklyRevenueRate;

        final GraphView graphView = (GraphView) findViewById(R.id.graphview);

        final CustomFontTextView lbLowVal = (CustomFontTextView) findViewById(R.id.lbLowVal);

        final SeekBar seekBar = (SeekBar) findViewById(R.id.seekbar);
        seekBar.setMax(50);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                lbLowVal.setText("" + progress);
                lbLowVal.setShadowLayer(5 * progress / seekBar.getMax(), 5, 5, Color.GREEN);

                graphView.setValue((float) progress / seekBar.getMax(), currentRevenuePerWeekRate);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        seekBar.setProgress(currentPerWeek);

        seekBar.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int widthSlider = seekBar.getMeasuredWidth();

                int arrowX = widthSlider * currentPerWeek / 50 + 10;

                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) layoutArrow.getLayoutParams();
                params.leftMargin = arrowX;
                layoutArrow.setLayoutParams(params);

                currentPerWeekLabel.setText(String.format("YOU: %d Video%s", currentPerWeek, currentPerWeek==1?"":"s"));

            }
        });

        CustomFontTextView lbApiVersion = (CustomFontTextView) findViewById(R.id.lbApiVersion);
        lbApiVersion.setText(getResources().getString(R.string.api_version) + " " + SSAPIRequestBuilder.API_VERSION_NUMBER);


    }


    private void openImageIntent() {
        // Camera.
        final List<Intent> cameraIntents = new ArrayList<Intent>();
        final Intent captureIntent = new Intent(
                android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        final PackageManager packageManager = getPackageManager();
        final List<ResolveInfo> listCam = packageManager.queryIntentActivities(
                captureIntent, 0);
        for (ResolveInfo res : listCam) {
            final String packageName = res.activityInfo.packageName;
            final Intent intent = new Intent(captureIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName,
                    res.activityInfo.name));
            intent.setPackage(packageName);
            cameraIntents.add(intent);
        }

        // Filesystem.
        final Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

        // Chooser of filesystem options.
        final Intent chooserIntent = Intent.createChooser(galleryIntent,
                "Please Choose");

        // Add the camera options.
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS,
                cameraIntents.toArray(new Parcelable[] {}));

        startActivityForResult(chooserIntent, 100);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != Activity.RESULT_OK)
            return;
        if (data == null)
            return;

        if (requestCode == 100) {

            // output image path
            Uri mImageUri = data.getData();
            Bundle extra = data.getExtras();

            if( null != extra ) {

                try {

                    Bitmap bitmap = SSUtils.scaleImage(this, mImageUri);
                    ivAvatar.setImageBitmap(bitmap);

                    imagePickedForAvatar(bitmap);


                } catch (Exception e) {e.printStackTrace();}

            }
        }
    }

    private void imagePickedForAvatar(final Bitmap image) {

        final Dialog waitDialog = DialogHelper.getWaitDialog(this, "Uploading...");
        waitDialog.show();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(SSConstants.WEB_SERVICE_ROOT + SSAPIRequestBuilder.APIUpdateAvatar());

                    MultipartUtility multipart = new MultipartUtility(url.toString(), "UTF-8");

                    if (SSAppController.sharedInstance().currentUser != null && !SSAppController.sharedInstance().currentUser.token.isEmpty()) {
                        multipart.addFormField("token", SSAppController.sharedInstance().currentUser.token);
                    }
                    if (SSAppController.sharedInstance().currentChannel != null && !SSAppController.sharedInstance().currentChannel.channelId.isEmpty()) {
                        multipart.addFormField("channel_id", SSAppController.sharedInstance().currentChannel.channelId);
                    }

                    multipart.addFilePart("file", SSUtils.convertTobyte(image), "avatar.jpg", "image/jpeg");

                    List<String> response = multipart.finish();

                    System.out.println("SERVER REPLIED:");

                    for (String line : response) {
                        System.out.println(line);
                    }

                    waitDialog.dismiss();

                } catch (Exception e) {
                    e.printStackTrace();

                    waitDialog.dismiss();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            DialogHelper.getDialog(SSSettingActivity.this, "Connection Failed", "Unable to make request, please try again.", "OK", null, null).show();
                        }
                    });

                }
            }
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mAdapter != null) {
            mAdapter.setItem(SSAppController.sharedInstance().pairingItems);
            mAdapter.notifyDataSetChanged();
        }

    }


    public class MyCustomAdapter extends BaseAdapter {

        private Context mContext;
        private List<SSPairingItem> items = new ArrayList<>();
        private LayoutInflater mInflater;


        public MyCustomAdapter(Context context) {
            mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mContext = context;
        }

        public void setItem(List<SSPairingItem> array) {
            items = array;
        }

        public List<SSPairingItem> getItems() {
            return items;
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return items.size();
        }

        @Override
        public SSPairingItem getItem(int position) {
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

                convertView = mInflater.inflate(R.layout.grid_setting_connect, null);

                holder.lbIcon = (CustomStarsiteFontTextView)convertView.findViewById(R.id.lbIcon);
                holder.lbTitle = (CustomFontTextView)convertView.findViewById(R.id.lbTitle);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder)convertView.getTag();
            }


            try {

                final SSPairingItem item = items.get(position);

                try {
                    item.checkPairingStatus();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                holder.lbIcon.setText(item.icon);

                if (SSConstants.isDemoApp) {
                    item.atLeastOne = true;
                }

                if (!item.atLeastOne) {
                    holder.lbTitle.setText(mContext.getResources().getString(R.string.not_connected));
                    holder.lbTitle.setTextColor(Color.RED);
                }
                else {
                    holder.lbTitle.setText(mContext.getResources().getString(R.string.connected));
                    holder.lbTitle.setTextColor(Color.GREEN);
                }

            } catch (Exception e) {e.printStackTrace();}


            return convertView;
        }

    }

    public static class ViewHolder {

        public CustomStarsiteFontTextView lbIcon;
        public CustomFontTextView lbTitle;
    }
}
