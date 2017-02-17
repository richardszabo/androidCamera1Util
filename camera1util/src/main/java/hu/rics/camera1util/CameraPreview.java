package hu.rics.camera1util;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

/**
 * Created by rics on 2017.02.08.
 */
@SuppressWarnings( "deprecation" ) // because of the camera
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private Context context;
    private Camera camera;
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
        try {
            camera.setPreviewDisplay(surfaceHolder);
            Log.d(LibraryInfo.TAG,"surfacecreated");
        } catch (Exception e) {
            Log.e(LibraryInfo.TAG,e.getMessage(),e);
            // Camera is not available (in use or does not exist)
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        if (previewIsRunning) {
            stopPreview();
        }
        setCameraDisplayOrientation(context,MediaRecorderWrapper.CAMERA_ID,camera);

        startPreview();
        Log.d(LibraryInfo.TAG,"surfacechanged---------------------");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        stopPreview();
        camera.release();
        camera = null;
        Log.d(LibraryInfo.TAG,"surfacedestroyed");
    }

    // safe call to start the preview
    // if this is called in onResume, the surface might not have been created yet
    // so check that the camera has been set up too.
    public void startPreview() {
        if (!previewIsRunning && (camera != null)) {
            camera.startPreview();
            previewIsRunning = true;
        }
    }

    // same for stopping the preview
    public void stopPreview() {
        if (previewIsRunning && (camera != null)) {
            camera.stopPreview();
            previewIsRunning = false;
        }
    }

    // correct displayorientation for Ace 3 and SMT800 tab in all four direction
    // taken from here: http://stackoverflow.com/a/10218309/21047
    public static void setCameraDisplayOrientation(Context context,
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

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

}
