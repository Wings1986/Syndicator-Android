package com.starclub.syndicator.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.aviary.android.feather.library.Constants;
import com.aviary.android.feather.sdk.FeatherActivity;
import com.starclub.syndicator.R;
import com.starclub.syndicator.customcontrol.CustomButtonTouchListener;
import com.starclub.syndicator.customcontrol.CustomStarsiteFontTextView;
import com.starclub.syndicator.widget.Camera2BasicFragment;
import com.starclub.syndicator.widget.CameraPreview;
import com.starclub.syndicator.widget.OnTakePictureCallBack;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SSComposeActivity extends FragmentActivity {

    LinearLayout navlayout;

    private CustomStarsiteFontTextView tvTitle;


    private static final int SELECT_PICTURE = 1;
    private static final int SELECT_VIDEO = 2;
    private static final int SELECT_AVIARY_PICTURE = 3;


    public enum CaptureType {

        SSPHOTO(0),
        SSVIDEO(1);

        private int _value;

        CaptureType(int Value) {
            this._value = Value;
        }

        public int getValue() {
            return _value;
        }

        public static CaptureType fromInt(int i) {
            for (CaptureType b : CaptureType.values()) {
                if (b.getValue() == i) { return b; }
            }
            return null;
        }
    }

    /*
           Camera
        */
    private Camera camera;
    private CameraPreview preview;

    Camera2BasicFragment camera2;

    FrameLayout cameraFrameLayout;

    ImageView btnGallery;
    CaptureType captureType = CaptureType.SSPHOTO;
    boolean isLightOn = true;
    int mCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;

    Button btnPhoto, btnVideo;
    ImageView arrowImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose);

        initView();
    }

    private void initView() {

        // header
        navlayout = (LinearLayout) findViewById(R.id.navlayout);

        tvTitle = (CustomStarsiteFontTextView) findViewById(R.id.nav_title);

        CustomStarsiteFontTextView  btnSetting = (CustomStarsiteFontTextView) findViewById(R.id.nav_back);
        btnSetting.setOnTouchListener(CustomButtonTouchListener.getInstance());
        btnSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SSComposeActivity.this, SSSettingActivity.class));
            }
        });
        btnSetting.setText("y"); // setting

        CustomStarsiteFontTextView btnAction = (CustomStarsiteFontTextView) findViewById(R.id.nav_action);
        btnAction.setOnTouchListener(CustomButtonTouchListener.getInstance());
        btnAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SSComposeActivity.this, SSDashboardActivity.class));
            }
        });


        //////////////////////////////////
        cameraFrameLayout = (FrameLayout) findViewById(R.id.fragment_camera_frame_layout);

        if (isOverAPI21()) {
            openCameraFragment(Camera2BasicFragment.CAMERA_FACING_BACK);
        }


        final CustomStarsiteFontTextView btnTorch = (CustomStarsiteFontTextView) findViewById(R.id.btnTorch);
        btnTorch.setOnTouchListener(CustomButtonTouchListener.getInstance());
        btnTorch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isLightOn = !isLightOn;
                btnTorch.setTextColor(!isLightOn ? ContextCompat.getColor(SSComposeActivity.this, R.color.COLOR_GRAY) : ContextCompat.getColor(SSComposeActivity.this, R.color.White));

                if (isOverAPI21()) {
                    camera2.changeTorch(isLightOn);
                } else {
                    preview.changeTorch(isLightOn);
                }

            }
        });

        PackageManager pm = getPackageManager();
        // if device support camera?
        if (!pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            Log.e("err", "Device has no camera!");
            btnTorch.setVisibility(View.GONE);
            return;
        }


        CustomStarsiteFontTextView btnToggle = (CustomStarsiteFontTextView) findViewById(R.id.btnToggle);
        btnToggle.setOnTouchListener(CustomButtonTouchListener.getInstance());
        btnToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isOverAPI21()) {
                    String newCameraID;
                    if (camera2.getCameraID().equals(Camera2BasicFragment.CAMERA_FACING_BACK)) {
                        newCameraID = Camera2BasicFragment.CAMERA_FACING_FRONT;
                        btnTorch.setVisibility(View.GONE);
                    } else {
                        newCameraID = Camera2BasicFragment.CAMERA_FACING_BACK;
                        btnTorch.setVisibility(View.VISIBLE);
                    }

                    openCameraFragment(newCameraID);
                } else {
                    if (mCameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
                        mCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
                        btnTorch.setVisibility(View.GONE);
                    } else {
                        mCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
                        btnTorch.setVisibility(View.VISIBLE);
                    }

                    safeCameraOpenInView();
                }
            }
        });

        btnGallery = (ImageView) findViewById(R.id.btnPhotoGallery);
        btnGallery.setOnTouchListener(CustomButtonTouchListener.getInstance());
        btnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,
                        "Select Picture"), SELECT_PICTURE);
            }
        });

        LinearLayout btnCurated = (LinearLayout) findViewById(R.id.btnCurated);
        btnCurated.setOnTouchListener(CustomButtonTouchListener.getInstance());
        btnCurated.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SSComposeActivity.this, SSCurateActivity.class));
            }
        });

        final ImageView btnCamera = (ImageView) findViewById(R.id.btnCamera);
        btnCamera.setOnTouchListener(CustomButtonTouchListener.getInstance());
        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (captureType == CaptureType.SSPHOTO) {
                    makeAPhoto();
                } else {
                    makeVideo();
                }
            }
        });

        btnPhoto = (Button) findViewById(R.id.btnPhoto);
        btnPhoto.setOnTouchListener(CustomButtonTouchListener.getInstance());
        btnPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnPhoto.setTextColor(ContextCompat.getColor(SSComposeActivity.this, R.color.CameraButtonSelectColor));
                btnVideo.setTextColor(ContextCompat.getColor(SSComposeActivity.this, R.color.CameraButtonNormalColor));

                btnCamera.setBackgroundResource(R.drawable.button_camera_photo_bg);

                if (captureType != CaptureType.SSPHOTO) {
                    captureType = CaptureType.SSPHOTO;
                    setAnimationArrow(500);
                }
            }
        });

        btnVideo = (Button) findViewById(R.id.btnVideo);
        btnVideo.setOnTouchListener(CustomButtonTouchListener.getInstance());
        btnVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnVideo.setTextColor(ContextCompat.getColor(SSComposeActivity.this, R.color.CameraButtonSelectColor));
                btnPhoto.setTextColor(ContextCompat.getColor(SSComposeActivity.this, R.color.CameraButtonNormalColor));

                btnCamera.setBackgroundResource(R.drawable.button_camera_video_bg);

                if (captureType != CaptureType.SSVIDEO) {
                    captureType = CaptureType.SSVIDEO;
                    setAnimationArrow(500);
                }
            }
        });

        btnPhoto.setTextColor(ContextCompat.getColor(SSComposeActivity.this, R.color.CameraButtonNormalColor));
        btnVideo.setTextColor(ContextCompat.getColor(SSComposeActivity.this, R.color.CameraButtonNormalColor));
        if (captureType == CaptureType.SSPHOTO) {
            btnPhoto.setTextColor(ContextCompat.getColor(SSComposeActivity.this, R.color.CameraButtonSelectColor));
        } else {
            btnVideo.setTextColor(ContextCompat.getColor(SSComposeActivity.this, R.color.CameraButtonSelectColor));
        }

        arrowImage = (ImageView) findViewById(R.id.arrowImage);
        arrowImage.setVisibility(View.INVISIBLE);

        btnPhoto.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mPhotoX = (int) btnPhoto.getX();
                mPhotoY = (int) btnPhoto.getY();
                mPhotoWidth = btnPhoto.getWidth();
                mPhotoHeight = btnPhoto.getHeight();

                setAnimationArrow(0);
            }
        });
        btnVideo.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mVideoX = (int) btnVideo.getX();
                mVideoY = (int) btnVideo.getY();
                mVideoWidth = btnVideo.getWidth();
                mVideoHeight = btnVideo.getHeight();

                setAnimationArrow(0);
            }
        });
        arrowImage.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mArrowWidth = arrowImage.getWidth();
                mArrowHeight = arrowImage.getHeight();

                setAnimationArrow(0);
            }
        });

    }

    @Override
    public void onBackPressed() {

        if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }

    }

    static String TAG_FRAGMENT = "fragment_camera";

    private void openCameraFragment(String cameraId) {
        closeCameraFragment();

        camera2 = Camera2BasicFragment.newInstance(new OnTakePictureCallBack() {
            @Override
            public void onTakenPicture(File file) {
                Uri imageUri = Uri.fromFile(file);
                setAviaryImage(imageUri);
            }
        });
        camera2.setCameraID(cameraId);

        getFragmentManager().beginTransaction()
                .replace(R.id.fragment_camera_frame_layout, camera2, TAG_FRAGMENT)
                .commit();
    }
    private void closeCameraFragment() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_FRAGMENT);
        if (fragment != null)
        getSupportFragmentManager().beginTransaction().
                remove(fragment).commit();

    }
    /*
        navigation header
     */
    public void showNavigationBar(boolean bShow) {
        navlayout.setVisibility(bShow ? View.VISIBLE : View.GONE);
    }

    public void setTitle(String _title) {
        tvTitle.setText(_title);
    }





    @Override
    public void onResume() {
        safeCameraOpenInView();
        super.onResume();
    }

    @Override
    public void onPause() {
        releaseCameraAndPreview();
        super.onPause();
    }


    int mPhotoX = 0, mPhotoY = 0, mPhotoWidth = 0, mPhotoHeight = 0;
    int mVideoX = 0, mVideoY = 0, mVideoWidth = 0, mVideoHeight = 0;
    int mArrowWidth = 0, mArrowHeight = 0;

    private void setAnimationArrow(int duration) {
        if (mPhotoWidth == 0 || mVideoWidth == 0 || mArrowWidth == 0)
            return;

        arrowImage.setVisibility(View.VISIBLE);

        float start_x, start_y, end_x, end_y;

        if (captureType == CaptureType.SSPHOTO) {
            start_x = mVideoX + mVideoWidth/2 - mArrowWidth/2;
            start_y = end_y = mVideoY + mVideoHeight/2;
            end_x = mPhotoX + mPhotoWidth/2 - mArrowWidth/2;
        } else {
            start_x = mPhotoX + mPhotoWidth/2 - mArrowWidth/2;
            start_y = end_y = mVideoY + mVideoHeight/2;
            end_x = mVideoX + mVideoWidth/2 - mArrowWidth/2;
        }

        TranslateAnimation animation = new TranslateAnimation(start_x, end_x, start_y, end_y);
        animation.setDuration(duration);
        animation.setFillAfter(true);
        arrowImage.startAnimation(animation);

    }

    /**
     * A safe way to get an instance of the Camera object.
     */

    public static Camera getCameraInstance(int cameraId) {
        Camera c = null;
        try {
            c = Camera.open(cameraId); // attempt to get a Camera instance
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    private boolean safeCameraOpenInView() {
        if (isOverAPI21())
            return false;

        boolean qOpened = false;
        releaseCameraAndPreview();
        camera = getCameraInstance(mCameraId);
        qOpened = (camera != null);
        preview = new CameraPreview(getBaseContext(), camera);

        cameraFrameLayout.removeAllViews();

        FrameLayout.LayoutParams param = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.CENTER);
        cameraFrameLayout.addView(this.preview, param);

        return qOpened;
    }

    private void releaseCameraAndPreview() {
        if (isOverAPI21())
            return;

        if (preview == null) {
            return;
        }
        preview.setCamera(null);
        if (camera != null) {
            camera.release();
            camera = null;
        }
    }

    private void makeVideo() {

        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        takeVideoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 30);
        takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, Environment.getExternalStorageDirectory().getPath()+"videocapture_example.mp4");

        startActivityForResult(takeVideoIntent, SELECT_VIDEO);
    }

    private void makeAPhoto() {

        if (camera2 != null) {
            camera2.takePicture();
        }

        if (camera != null) {

            camera.takePicture(new Camera.ShutterCallback() {
                                   @Override
                                   public void onShutter() {

                                   }
                               },
                    new Camera.PictureCallback() {
                        @Override
                        public void onPictureTaken(byte[] data, Camera camera) {

                        }
                    }, new Camera.PictureCallback() {
                        @Override
                        public void onPictureTaken(byte[] data, Camera camera) {

                            String filePath = generalFilePath();
                            if (writeBitmapToFile(data, filePath)) {

                                if (camera != null) {
                                    camera.stopPreview();
                                    camera.startPreview();
                                }

                                //
                                Uri imageUri = Uri.fromFile(new File(filePath));
                                setAviaryImage(imageUri);
                            }
                        }
                    });

        }
    }

    private static String generalFilePath() {
        // Create a media file name
        SimpleDateFormat forPictureFileDateFormat = new SimpleDateFormat("dd_MM_yyyy_HH:mm:ss");
        String timeStamp = forPictureFileDateFormat.format(new Date());

        String filepath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + File.separator
                + "IMG_" + timeStamp + ".jpg";

        return filepath;
    }
    private boolean writeBitmapToFile(byte[] data, String filepath) {

        File pictureFile = new File(filepath);

        if (pictureFile != null) {
            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return false;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }

            return true;
        }

        return false;

    }

    private boolean isOverAPI21() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    private void setAviaryImage(Uri mImageUri) {

        String aviary_secret_key = getResources().getString(R.string.AVIARY_SECRET_KEY);

        Intent newIntent = new Intent(getBaseContext(), FeatherActivity.class );
        newIntent.setData( mImageUri );
        newIntent.putExtra( Constants.EXTRA_IN_API_KEY_SECRET, aviary_secret_key);
        startActivityForResult( newIntent, SELECT_AVIARY_PICTURE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                Uri selectedImageUri = data.getData();

//                String selectedImagePath = getPath(selectedImageUri);
//                Bitmap yourSelectedImage = BitmapFactory.decodeFile(selectedImagePath);
//                btnGallery.setImageBitmap(yourSelectedImage);
//
//                gotoPostViewWithImage(yourSelectedImage, null, null);

                setAviaryImage(selectedImageUri);
            }
            else if (requestCode == SELECT_VIDEO) {

                try {
                    Uri selectedVideoUri = data.getData();

//                    String selectedImagePath = SSUtils.getVideoPathInfo(selectedVideoUri, this).get("filepath");
//
//                    List<Bitmap> thumbs = new ArrayList<Bitmap>();
//
//                    int interval = 3;
//
//                    MediaMetadataRetriever retriever = new MediaMetadataRetriever();
//                    retriever.setDataSource(filePath);
//                    for (int i = 0 ; i < duration ; i += interval) {
//                        Bitmap bmp = retriever.getFrameAtTime(i * 1000000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
//                        thumbs.add(bmp);
//                    }

//                    thumb = SSUtils.getResize(thumb);

                    gotoPostViewWithImage(null, selectedVideoUri);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else if (requestCode == SELECT_AVIARY_PICTURE) {

                Uri selectedImageUri = data.getData();
                Bundle extra = data.getExtras();

                if( null != extra ) {
                    // image has been changed by the user?
//                        boolean changed = extra.getBoolean( Constants.EXTRA_OUT_BITMAP_CHANGED );

//                    String selectedImagePath = SSUtils.getImagePath(selectedImageUri, this);
//                    Bitmap yourSelectedImage = BitmapFactory.decodeFile(selectedImagePath);

                    gotoPostViewWithImage(selectedImageUri, null);

                }


            }
        }
    }



    private void gotoPostViewWithImage(Uri imageUri, Uri videoUri) {

        final Intent intent = new Intent(SSComposeActivity.this, SSPostActivity.class);

        intent.putExtra("isVideo", captureType == CaptureType.SSVIDEO);

        if (imageUri != null) {
            intent.putExtra("image", imageUri.toString());
        }

        if (videoUri!= null) {
            intent.putExtra("video", videoUri.toString());
        }

        startActivity(intent);

    }
}
