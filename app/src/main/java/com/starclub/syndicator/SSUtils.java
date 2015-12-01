package com.starclub.syndicator;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.provider.MediaStore;

import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Created by iGold on 10/16/15.
 */
public class SSUtils {

    public static boolean isResponseSuccessful(JSONObject dict) {

        try {
            if (dict.getString("status").equalsIgnoreCase("1")
                    || dict.getString("status").equalsIgnoreCase("true")) {
                return true;
            }
        } catch (Exception e) {e.printStackTrace();}

        return false;
    }

    @SuppressLint("DefaultLocale")
    public static String timeAgo(double time) {

        double seconds = System.currentTimeMillis()/1000 - time;

        String timeAgo = "";

        if (seconds < 5) {
            timeAgo = "just now";
        } else if (seconds < 55) {
            timeAgo = String.format(Locale.US, "%.0f seconds ago", seconds);
        } else if (seconds  >= 55 && seconds < 90) {
            timeAgo = "1 minute ago";
        } else if (seconds >= 90 && seconds <= 55*60) {
            timeAgo = String.format(Locale.US, "%.0f minutes ago", (seconds/60.f));
        } else if (seconds > 55*60 && seconds <= 90*60) {
            timeAgo = "1 hour ago";
        } else if (seconds > 90*60 && seconds <= 23.5*60*60) {
            timeAgo = String.format(Locale.US, "%.0f hours ago", (seconds/60.f/60.f));
        } else {
            SimpleDateFormat formatter = new SimpleDateFormat("dd MMM");
            timeAgo = formatter.format(new Date((long) time * 1000L));
        }

        return timeAgo;
    }

    public static String timeAgo2(double time) {

        SimpleDateFormat formatter = new SimpleDateFormat("MMM dd, yyyy, hh:mm a");
        String timeAgo = formatter.format(new Date((long) time * 1000L));

        return timeAgo;
    }


    /*
        bitmap setting
     */
    // bitmap
    public static Bitmap getThumbnail(Context context, Uri uri) throws IOException {
        final int THUMBNAIL_SIZE = 350;

        InputStream input = context.getContentResolver().openInputStream(uri);

        BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();
        onlyBoundsOptions.inJustDecodeBounds = true;
        onlyBoundsOptions.inDither = true;// optional
        onlyBoundsOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;// optional
        BitmapFactory.decodeStream(input, null, onlyBoundsOptions);
        input.close();
        if ((onlyBoundsOptions.outWidth == -1)
                || (onlyBoundsOptions.outHeight == -1))
            return null;

        int originalSize = (onlyBoundsOptions.outHeight > onlyBoundsOptions.outWidth) ? onlyBoundsOptions.outHeight
                : onlyBoundsOptions.outWidth;

        double ratio = (originalSize > THUMBNAIL_SIZE) ? (originalSize / THUMBNAIL_SIZE)
                : 1.0;

        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inSampleSize = getPowerOfTwoForSampleRatio(ratio);
        bitmapOptions.inDither = true;// optional
        bitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;// optional
        input = context.getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions);
        input.close();
        return bitmap;
    }

    private static int getPowerOfTwoForSampleRatio(double ratio) {
        int k = Integer.highestOneBit((int) Math.floor(ratio));
        if (k == 0)
            return 1;
        else
            return k;
    }

    public static Bitmap scaleImage(Context context, Uri photoUri) throws IOException {

        final int MAX_IMAGE_DIMENSION = 350;


        InputStream is = context.getContentResolver().openInputStream(photoUri);
        BitmapFactory.Options dbo = new BitmapFactory.Options();
        dbo.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(is, null, dbo);
        is.close();

        int rotatedWidth, rotatedHeight;
        int orientation = getOrientation(context, photoUri);

        if (orientation == 90 || orientation == 270) {
            rotatedWidth = dbo.outHeight;
            rotatedHeight = dbo.outWidth;
        } else {
            rotatedWidth = dbo.outWidth;
            rotatedHeight = dbo.outHeight;
        }

        Bitmap srcBitmap;
        is = context.getContentResolver().openInputStream(photoUri);
        if (rotatedWidth > MAX_IMAGE_DIMENSION || rotatedHeight > MAX_IMAGE_DIMENSION) {
            float widthRatio = ((float) rotatedWidth) / ((float) MAX_IMAGE_DIMENSION);
            float heightRatio = ((float) rotatedHeight) / ((float) MAX_IMAGE_DIMENSION);
            float maxRatio = Math.max(widthRatio, heightRatio);

            // Create the bitmap from file
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = (int) maxRatio;
            srcBitmap = BitmapFactory.decodeStream(is, null, options);
        } else {
            srcBitmap = BitmapFactory.decodeStream(is);
        }
        is.close();

        /*
         * if the orientation is not 0 (or -1, which means we don't know), we
         * have to do a rotation.
         */
        if (orientation > 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(orientation);

            srcBitmap = Bitmap.createBitmap(srcBitmap, 0, 0, srcBitmap.getWidth(),
                    srcBitmap.getHeight(), matrix, true);
        }

        String type = context.getContentResolver().getType(photoUri);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        if (type.equals("image/png")) {
            srcBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        } else if (type.equals("image/jpg") || type.equals("image/jpeg")) {
            srcBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        }
        byte[] bMapArray = baos.toByteArray();
        baos.close();
        return BitmapFactory.decodeByteArray(bMapArray, 0, bMapArray.length);
    }

    public static int getOrientation(Context context, Uri photoUri) {
        /* it's on the external media. */
        Cursor cursor = context.getContentResolver().query(photoUri,
                new String[] { MediaStore.Images.ImageColumns.ORIENTATION }, null, null, null);

        if (cursor.getCount() != 1) {
            return -1;
        }

        cursor.moveToFirst();
        return cursor.getInt(0);
    }

    /**
     * helper to retrieve the path of an image URI
     */
    public static String getImagePath(Uri uri, Activity activity) {
        // just some safety built in
        if( uri == null ) {
            // TODO perform some logging or show user feedback
            return null;
        }
        // try to retrieve the image from the media store first
        // this will only work for images selected from gallery
        String[] projection = { MediaStore.Images.Media.DATA };

        @SuppressWarnings("deprecation")
        Cursor cursor = activity.managedQuery(uri, projection, null, null, null);
        if( cursor != null ){
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
        // this is our fallback here
        return uri.getPath();
    }

    public static HashMap<String, String> getVideoPathInfo(Uri uri, Activity activity) {
        // just some safety built in
        if( uri == null ) {
            // TODO perform some logging or show user feedback
            return null;
        }
        String[] projection = { MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.SIZE, MediaStore.Video.Media.DURATION };

        @SuppressWarnings("deprecation")
        Cursor cursor = activity.managedQuery(uri, projection, null, null, null);
        cursor.moveToFirst();
        final String filePath = cursor.getString(cursor
                .getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
        final int fileSize = cursor.getInt(cursor
                .getColumnIndexOrThrow(MediaStore.Video.Media.SIZE));
        final long duration = TimeUnit.MILLISECONDS.toSeconds(cursor.getInt(cursor
                .getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)));
        System.out.println("size: " + fileSize);
        System.out.println("path: " + filePath);
        System.out.println("duration: " + duration);

        return  new HashMap<String, String>() {
            {
                put("filesize", "" + fileSize);
                put("filepath", "" + filePath);
                put("duration", "" + duration);
            }
        };
    }

    public static ByteArrayInputStream convertTobyte(Bitmap bitmap) {

        try  {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 75, bos);
            byte[] data = bos.toByteArray();

            ByteArrayInputStream inputStream = new ByteArrayInputStream(data);

            return inputStream;

        } catch (Exception e) {
            return null;
        }

    }
}
