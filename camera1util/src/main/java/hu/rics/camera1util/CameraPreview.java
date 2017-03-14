package hu.rics.camera1util;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import java.util.List;

/**
 * Created by rics on 2017.02.08.
 */
@SuppressWarnings( "deprecation" ) // because of the camera
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback {
    private Context context;
    protected Camera camera;
    private boolean previewIsRunning;

    public CameraPreview(Context context) {
        super(context);
        this.context = context;

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void setCamera(Camera c) {
        camera = c;
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        Log.d(LibraryInfo.TAG,"CameraPreview.surfacecreated");
        try {
            camera.setPreviewDisplay(surfaceHolder);
        } catch (Exception e) {
            Log.e(LibraryInfo.TAG,e.getMessage(),e);
            // Camera is not available (in use or does not exist)
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        Log.d(LibraryInfo.TAG,"CameraPreview.surfacechanged");
        if (previewIsRunning) {
            stopPreview();

        }
        Log.d(LibraryInfo.TAG,"orientation: " + getResources().getConfiguration().orientation);
        setCameraDisplayOrientation(context,MediaRecorderWrapper.CAMERA_ID,camera);
        camera.setPreviewCallback(this);
        startPreview();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        Log.d(LibraryInfo.TAG,"CameraPreview.surfacedestroyed");
        stopPreview();
        camera.setPreviewCallback(null);
        camera.release();
        camera = null;
    }

    // safe call to start the preview
    // if this is called in onResume, the surface might not have been created yet
    // so check that the camera has been set up too.
    public void startPreview() {
        Log.d(LibraryInfo.TAG,"CameraPreview.startPreview");
        if (!previewIsRunning && (camera != null)) {
            int i = 0;
            for( Camera.Size size : camera.getParameters().getSupportedPreviewSizes() ) {
                Log.i(LibraryInfo.TAG,"Supported preview size" + (++i) + ":" + size.width + ":" + size.height);
            }
            camera.startPreview();
            previewIsRunning = true;
        }
    }

    // same for stopping the preview
    public void stopPreview() {
        Log.d(LibraryInfo.TAG,"CameraPreview.stopPreview");
        if (previewIsRunning && (camera != null)) {
            camera.stopPreview();
            previewIsRunning = false;
        }
    }

    public void setPreviewSize(int width, int height) {
        Camera.Parameters parameters = camera.getParameters();
        parameters.setPreviewSize(width,height);
        camera.setParameters(parameters);
    }

    public void startRecording() {
        Log.d(LibraryInfo.TAG,"CameraPreview.startRecording");
    }

    public void stopRecording() {
        Log.d(LibraryInfo.TAG,"CameraPreview.stopRecording");
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        //Log.d(LibraryInfo.TAG,"CameraPreview.onPreviewFrame");
    }

    // correct displayorientation for Ace 3 and SMT800 tab in all four direction
    // taken from here: https://developer.android.com/reference/android/hardware/Camera.html#setDisplayOrientation(int) 
    public static int getCameraDisplayOrientation(Context context,
                                                   int cameraId, android.hardware.Camera camera) {
        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }
        Log.i(LibraryInfo.TAG,"info orientation:" + info.orientation + " rotation:" + degrees + ":");
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        return result;
    }

    public static void setCameraDisplayOrientation(Context context,
                                                   int cameraId, android.hardware.Camera camera) {
        camera.setDisplayOrientation(getCameraDisplayOrientation(context,cameraId,camera));
    }

}
