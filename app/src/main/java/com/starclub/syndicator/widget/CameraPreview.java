package com.starclub.syndicator.widget;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.List;

/**
 * Surface on which the camera projects it's capture results.
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    SurfaceHolder mHolder;
    Camera mCamera;
    private List<Size> mSupportedPreviewSizes;

    private Size mPreviewSize;

    public CameraPreview(Context context, Camera camera) {
        super(context);
        setCamera(camera);
        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void setCamera(Camera camera) {
        if (mCamera == camera) {
            return;
        }

        stopPreviewAndFreeCamera();

        mCamera = camera;

        if (mCamera != null) {
            List<Size> localSizes = mCamera.getParameters().getSupportedPreviewSizes();
            mSupportedPreviewSizes = localSizes;
            requestLayout();

            
//            Camera.Parameters p = mCamera.getParameters();
//            int maxZoom = p.getMaxZoom();
//            int zoom = p.getZoom();
//            zoom = 15;
//            if (p.isZoomSupported()) {
//               p.setZoom(maxZoom);
//            }
            

            try {
                mCamera.setPreviewDisplay(mHolder);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Important: Call startPreview() to start updating the preview
            // surface. Preview must be started before you can take a picture.
            mCamera.startPreview();
            
            
        }
    }

    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
        	if (mCamera != null) {
        		mCamera.setPreviewDisplay(holder);
        		mCamera.startPreview();
        	}
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // Surface will be destroyed when we return, so stop the preview.
        if (mCamera != null) {
            // Call stopPreview() to stop updating the preview surface.
            mCamera.stopPreview();
        }
    }

    /**
     * When this function returns, mCamera will be null.
     */
    private void stopPreviewAndFreeCamera() {

        if (mCamera != null) {
            // Call stopPreview() to stop updating the preview surface.
            mCamera.stopPreview();

            // Important: Call release() to release the camera for use by other
            // applications. Applications should release the camera immediately
            // during onPause() and re-open() it during onResume()).
            mCamera.release();

            mCamera = null;
        }
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.
        if (mHolder.getSurface() == null) {
            // preview surface does not exist
            return;
        }
        if (mCamera == null)
            return;

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
        }
        // set preview size and make any resize, rotate or
        // reformatting changes here
        // start preview with new settings
        // Now that the size is known, set up the camera parameters and begin
        // the preview.
        //mCamera.setDisplayOrientation(90);
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.set("orientation", "portrait");
        if (mPreviewSize != null) {
            parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.width);
            parameters.setRotation(90);
        }
        //parameters.;
        requestLayout();
        mCamera.setParameters(parameters);
        mCamera.setDisplayOrientation(90);
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);

        setMeasuredDimension(width, height);

        if (mCamera != null) {
            if (mSupportedPreviewSizes != null) {
                mPreviewSize = getBestPreviewSize(width, height, mCamera.getParameters());
//                mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, width, height);
            }
        }
    }

    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) h / w;

        if (sizes == null) return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    private Size getBestPreviewSize(int width, int height, Camera.Parameters parameters) {
        Size result = null;

        for (Size size : parameters.getSupportedPreviewSizes()) {
            if (size.width <= width && size.height <= height) {
                if (result == null) {
                    result = size;
                } else {
                    int resultArea = result.width * result.height;
                    int newArea = size.width * size.height;

                    if (newArea > resultArea) {
                        result = size;
                    }
                }
            }
        }
        
        return (result);
    }

    //
    public void changeTorch(boolean on) {
        try {
            // try to open the camera to turn on the torch
            Camera.Parameters param = mCamera.getParameters();
            if (on) {
                param.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            } else {
                param.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            }
            mCamera.setParameters(param);
            mCamera.startPreview(); // needed for some devices
            Log.v("BSW torch", "Torch " + on);
        } catch (Exception e) {
            // if open camera fails, try to release camera
            Log.w("BSW torch", "Camera is being used trying to turn Torch OFF");
            try {
                mCamera.release();
            } catch (Exception ex) {
                Log.e("BSF torch", "Error releasing camera");
            }
        }
    }

    public void changeCameraId(Camera camera) {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }

        try {
            mCamera = camera;
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
        } catch (Exception e) {e.printStackTrace();}
    }
}


