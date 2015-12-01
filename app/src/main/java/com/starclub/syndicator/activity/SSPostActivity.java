package com.starclub.syndicator.activity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;


import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.starclub.syndicator.R;
import com.starclub.syndicator.SSAPIRequestBuilder;
import com.starclub.syndicator.SSAppController;
import com.starclub.syndicator.SSConstants;
import com.starclub.syndicator.SSUtils;
import com.starclub.syndicator.UIApplication;
import com.starclub.syndicator.customcontrol.CustomButtonTouchListener;
import com.starclub.syndicator.customcontrol.CustomFontButton;
import com.starclub.syndicator.customcontrol.CustomFontEdittext;
import com.starclub.syndicator.customcontrol.CustomFontTextView;
import com.starclub.syndicator.customcontrol.CustomStarsiteFontTextView;
import com.starclub.syndicator.customcontrol.DialogCallBack;
import com.starclub.syndicator.customcontrol.DialogHelper;
import com.starclub.syndicator.customcontrol.SSPostSocialButton;
import com.starclub.syndicator.data.SSPairingItem;
import com.starclub.syndicator.multipart.MultipartUtility;
import org.json.JSONArray;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.List;



public class SSPostActivity extends FragmentActivity {

    static String TAG = SSPostActivity.class.getName();

    boolean isVideo;
    Bitmap capturedImage;

    ImageView ivAvatar;
    CustomStarsiteFontTextView btnPlayVideo;
    CustomFontEdittext editCaption;
    LinearLayout groupSocialButtons;

    private static final int SELECT_THUMB_IMAGE = 4;

    Uri imageUri = null, videoUri = null;
    String videoUrl = "";

    boolean isPostCurated = false;
    String viralContentId = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

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


        btnPlayVideo = (CustomStarsiteFontTextView) findViewById(R.id.btnPlayVideo);

        isVideo = getIntent().getExtras().getBoolean("isVideo", false);
        if (!isVideo) {
            btnPlayVideo.setVisibility(View.GONE);
        } else {
            btnPlayVideo.setVisibility(View.VISIBLE);
        }

        try {
            isPostCurated = getIntent().getExtras().getBoolean("isPostCurated", false);
        } catch (Exception e) {e.printStackTrace();}

        try {
            viralContentId = getIntent().getStringExtra("viralContentId");
        } catch (Exception e) {e.printStackTrace();}

        ivAvatar = (ImageView) findViewById(R.id.ivAvatar);

        try {
            imageUri = Uri.parse(getIntent().getStringExtra("image"));
        } catch (Exception e) {e.printStackTrace();}

        if (imageUri != null) {
            String selectedImagePath = SSUtils.getImagePath(imageUri, this);
            capturedImage = BitmapFactory.decodeFile(selectedImagePath);
            ivAvatar.setImageBitmap(capturedImage);
        }

        try {

            String imageUrl = getIntent().getStringExtra("image_url");
            if (imageUrl != null && imageUrl.length() > 1) {
                String url = SSAPIRequestBuilder.getURLEncoded(imageUrl);
                ImageLoader.getInstance().displayImage(url, ivAvatar, new ImageLoadingListener() {
                    @Override
                    public void onLoadingStarted(String imageUri, View view) {

                    }

                    @Override
                    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

                    }

                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        capturedImage = loadedImage;
                        ivAvatar.setImageBitmap(capturedImage);
                    }

                    @Override
                    public void onLoadingCancelled(String imageUri, View view) {

                    }
                });
            }
        } catch (Exception e) {e.printStackTrace();}



        try {
            videoUrl = getIntent().getStringExtra("video_url");
        } catch (Exception e) {e.printStackTrace();}

        try {
            videoUri = Uri.parse(getIntent().getStringExtra("video"));
        } catch (Exception e) {e.printStackTrace();}

        if (isVideo && videoUri != null) { // get Capture, thumb
            String videoPath = SSUtils.getVideoPathInfo(videoUri, this).get("filepath");
            capturedImage = ThumbnailUtils.createVideoThumbnail(videoPath, MediaStore.Images.Thumbnails.MINI_KIND);
            ivAvatar.setImageBitmap(capturedImage);
        }

        ivAvatar.setOnTouchListener(CustomButtonTouchListener.getInstance());
        ivAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isVideo) {
                    Intent intent = new Intent(SSPostActivity.this, SSVideoPlayer.class);

                    if (!isPostCurated) {
                        intent.putExtra("url_video", SSUtils.getVideoPathInfo(videoUri, SSPostActivity.this).get("filepath"));
                    } else {
                        String urlVideo = videoUrl.substring(0, videoUrl.lastIndexOf('.'))  + "/master.m3u8";
                        intent.putExtra("url_video", urlVideo);
                    }
                    startActivity(intent);
                }
            }
        });


        CustomFontButton btnChangeThumb = (CustomFontButton) findViewById(R.id.btnChangeThumb);
        btnChangeThumb.setOnTouchListener(CustomButtonTouchListener.getInstance());
        btnChangeThumb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SSPostActivity.this, SSChangeThumbActivity.class);
                intent.putExtra("videopath", videoUri.toString());
                startActivityForResult(intent, SELECT_THUMB_IMAGE);
            }
        });

        if (isVideo && !isPostCurated) {
            btnChangeThumb.setVisibility(View.VISIBLE);
        } else {
            btnChangeThumb.setVisibility(View.GONE);
        }

        final CustomFontTextView charCount = (CustomFontTextView) findViewById(R.id.charCount);
        charCount.setText(getResources().getString(R.string.suggestion_100_characters));


        String capture = "";
        try {
            capture = getIntent().getStringExtra("caption");
        } catch (Exception e) {e.printStackTrace();}

        editCaption = (CustomFontEdittext) findViewById(R.id.edit_caption);
        editCaption.setText(capture);
        editCaption.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 0) {
                    charCount.setText(getResources().getString(R.string.suggestion_100_characters));
                } else {
                    charCount.setText(s.length() + " char");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        CustomFontButton btnShare = (CustomFontButton) findViewById(R.id.btnShare);
        btnShare.setOnTouchListener(CustomButtonTouchListener.getInstance());
        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doShare();
            }
        });


        groupSocialButtons = (LinearLayout) findViewById(R.id.groupSocialButtons);
        for (int i = 0 ; i < groupSocialButtons.getChildCount() ; i ++ ) {
            View child = groupSocialButtons.getChildAt(i);

            if (child instanceof SSPostSocialButton) {
                child.setVisibility(View.GONE);
            }
        }

        for (SSPairingItem item : SSAppController.sharedInstance().pairingItems) {

            if(SSConstants.isDemoApp || SSAppController.sharedInstance().isPinModeApp){
                item.isConnected = true;
            }

            int resID;
            switch (item.pairingTypeId) {
                case SSParingTypeFacebook:
                    resID = R.id.btnFacebook;
                    break;
                case SSParingTypeTwitter:
                    resID = R.id.btnTwitter;
                    break;
                case SSParingTypeTumblr:
                    resID = R.id.btnThumblr;
                    break;
                case SSParingTypeInstagram:
                    resID = R.id.btnInstagram;
                    break;
                case SSParingTypeGoogle:
                    resID = R.id.btnGoogle;
                    break;
                case SSParingTypePinterest:
                    resID = R.id.btnPinterest;
                    break;
                default:
                    resID = R.id.btnFacebook;
                    break;
            }

            final SSPostSocialButton btnSocial = (SSPostSocialButton) findViewById(resID);
            btnSocial.setVisibility(View.VISIBLE);
            btnSocial.setItem(item, isVideo);
            btnSocial.setOnTouchListener(CustomButtonTouchListener.getInstance());
            btnSocial.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    btnSocial.socialBtnPressed();
                }
            });

        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_THUMB_IMAGE) {
//                Uri thumbUri = null;
//                try {
//                    thumbUri = Uri.parse(getIntent().getStringExtra("thumb"));
//                } catch (Exception e) {e.printStackTrace();}
//                if (thumbUri != null) {
//                    String selectedImagePath = SSUtils.getImagePath(thumbUri, this);
//                    capturedImage = BitmapFactory.decodeFile(selectedImagePath);
//                    ivAvatar.setImageBitmap(capturedImage);
//                }
//                byte[] byteArray = getIntent().getExtras().getByteArray("thumb");

                String filepath = getIntent().getExtras().getString("thumb");
                if (filepath == null)
                    return;

                File f= new File(filepath);
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                try {
                    capturedImage = BitmapFactory.decodeStream(new FileInputStream(f), null, options);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                ivAvatar.setImageBitmap(capturedImage);
            }
        }
    }

    private void doShare() {
        if (editCaption.getText().toString().length() == 0) {
            DialogHelper.getDialog(this, "Missing Caption", "Please add a caption", "OK", null, null).show();
            return;
        }
        if(capturedImage == null && !isVideo) {
            DialogHelper.getDialog(this, "Image or Video required", "Please add a video or an image", "OK", null, null).show();
            return;
        }

        boolean sendToInstagram = false;
        //gather what we are sharing
        JSONArray arr = new JSONArray();
        for (int i = 0 ; i < groupSocialButtons.getChildCount() ; i ++ ) {
            View child = groupSocialButtons.getChildAt(i);
            if (child instanceof SSPostSocialButton) {
                SSPostSocialButton btn = (SSPostSocialButton) child;
                arr.put(btn.packageForServer());
                if (btn.getItem().pairingTypeId == SSPairingItem.SSParingTypeId.SSParingTypeInstagram
                        && btn.isSelected()) {
                    sendToInstagram = true;
                }
            }
        }

        final String jsonString = arr.toString();

        /*
        HashMap<String, Object> dict = new HashMap<String, Object>() {
            {
                put("caption", editCaption.getText().toString());
                put("image", capturedImage);
                put("videoURL", isVideo ? videoUri.toString() : "");
                put("is_video", isVideo ? "Y" : "N");
                put("sendToInstagram", sendToInstagram ? "Y" : "N");
                put("pairing_data", jsonString);
            }
        };
        */

        final boolean isPostInstagram = sendToInstagram;

        final Dialog waitDialog = DialogHelper.getWaitDialog(this, "Uploading...");
        waitDialog.show();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url;

                    if (!isPostCurated) {
                        url = new URL(SSConstants.WEB_SERVICE_ROOT + SSAPIRequestBuilder.APIForPostContent());
                    } else {
                        url = new URL(SSConstants.WEB_SERVICE_ROOT + SSAPIRequestBuilder.APIForPostCuratedContent());
                    }

                    MultipartUtility multipart = new MultipartUtility(url.toString(), "UTF-8");

//            multipart.addHeaderField("User-Agent", "CodeJava");
//            multipart.addHeaderField("Test-Header", "Header-Value");

                    if (SSAppController.sharedInstance().currentUser != null && !SSAppController.sharedInstance().currentUser.token.isEmpty()) {
                        multipart.addFormField("token", SSAppController.sharedInstance().currentUser.token);
                    }
                    if (SSAppController.sharedInstance().currentChannel != null && !SSAppController.sharedInstance().currentChannel.channelId.isEmpty()) {
                        multipart.addFormField("channel_id", SSAppController.sharedInstance().currentChannel.channelId);
                    }

                    multipart.addFormField("pairing_data", Base64.encodeToString(jsonString.getBytes(), Base64.DEFAULT));
                    multipart.addFormField("description", editCaption.getText().toString());
                    multipart.addFormField("type", isVideo ? "video" : "photo");

                    if (!isPostCurated) {
                        if (isVideo) {
                            // video data

                            multipart.addFilePart("data", new File(SSUtils.getVideoPathInfo(videoUri, SSPostActivity.this).get("filepath")), "video.mp4", "video/mov");

                            // thumb
                            multipart.addFilePart("thumb", SSUtils.convertTobyte(capturedImage), "photo.jpg", "image/jpeg");

                        } else { // image
                            multipart.addFilePart("data", SSUtils.convertTobyte(capturedImage), "photo.jpg", "image/jpeg");
                        }
                    }
                    else {
                        multipart.addFormField("viral_id", viralContentId);
                    }


                    List<String> response = multipart.finish();

                    System.out.println("SERVER REPLIED:");

                    for (String line : response) {
                        System.out.println(line);
                    }

                    waitDialog.dismiss();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            DialogHelper.getDialog(SSPostActivity.this, "Finished!", "Please allow a few minutes to see results", "OK", null, new DialogCallBack() {
                                @Override
                                public void onClick(int which) {

                                    if (isPostInstagram) {
                                        ((UIApplication) getApplication()).checkIfPostToInstagram(editCaption.getText().toString(), capturedImage);
                                    }

                                    SSPostActivity.this.finish();
                                }
                            }).show();

                        }
                    });


                } catch (Exception e) {
                    e.printStackTrace();

                    waitDialog.dismiss();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            DialogHelper.getDialog(SSPostActivity.this, "Connection Failed", "Unable to make request, please try again.", "OK", null, null).show();
                        }
                    });

                }
            }
        }).start();

    }


}
