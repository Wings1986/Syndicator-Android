package com.starclub.syndicator;


import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.multidex.MultiDex;

import com.crashlytics.android.Crashlytics;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.starclub.syndicator.customcontrol.DialogHelper;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import io.fabric.sdk.android.Fabric;


/**
 * Created by iGold on 22.02.15.
 */
public class UIApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        TwitterAuthConfig authConfig = new TwitterAuthConfig(getString(R.string.TWITTER_KEY), getString(R.string.TWITTER_SECRET));
        Fabric.with(this, new Twitter(authConfig), new Crashlytics());

        SSAppController.sharedInstance().init(getBaseContext());

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                .memoryCacheExtraOptions(480, 800) // default = device screen dimensions
                .diskCacheExtraOptions(480, 800, null)
                .threadPriority(Thread.NORM_PRIORITY - 2) // default
                .tasksProcessingOrder(QueueProcessingType.FIFO) // default
                .denyCacheImageMultipleSizesInMemory()
                .memoryCache(new LruMemoryCache(2 * 1024 * 1024))
                .memoryCacheSize(2 * 1024 * 1024)
                .memoryCacheSizePercentage(13) // default
                .diskCacheSize(50 * 1024 * 1024)
                .diskCacheFileCount(100)
                .diskCacheFileNameGenerator(new HashCodeFileNameGenerator()) // default
                .imageDownloader(new BaseImageDownloader(this)) // default
                .defaultDisplayImageOptions(DisplayImageOptions.createSimple()) // default
//                .writeDebugLogs()
                .build();
        ImageLoader.getInstance().init(config);

    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public void checkIfPostToInstagram(String caption, Bitmap image) {
        boolean instalado = false;

        try {
            ApplicationInfo info = getPackageManager().getApplicationInfo("com.instagram.android", 0);
            instalado = true;
        } catch (PackageManager.NameNotFoundException e) {
            instalado = false;
        }

        if (instalado == false) {
            return;
        }

        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setPackage("com.instagram.android");
        if (image != null) {
            shareIntent.setType("image/png");
            String path = MediaStore.Images.Media.insertImage(getContentResolver(), image, "image", null);
            Uri uri = Uri.parse(path);
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        }

        shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(shareIntent);

    }
}
