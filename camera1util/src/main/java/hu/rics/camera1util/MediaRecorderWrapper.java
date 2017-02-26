package hu.rics.camera1util;

import android.app.Activity;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.util.Log;
import android.widget.FrameLayout;

import java.io.IOException;

import static android.R.attr.id;
import static android.graphics.ImageFormat.NV21;

/**
 * Created by rics on 2017.02.08.
 */
@SuppressWarnings( "deprecation" ) // because of the camera
public class MediaRecorderWrapper {

    Activity activity;
    MediaRecorder mediaRecorder;
    Camera camera;
    CameraPreview cameraPreview;
    boolean isRecording;
    static final int CAMERA_ID = 0;

    public MediaRecorderWrapper(Activity activity, int viewId, CameraPreview cameraPreview) {
        this.cameraPreview = cameraPreview;
        init(activity,viewId);
    }

    public MediaRecorderWrapper(Activity activity, int viewId) {
        cameraPreview = new CameraPreview(activity);
        init(activity,viewId);
    }

    private void init(Activity activity, int viewId) {
        this.activity = activity;
        FrameLayout preview = (FrameLayout) activity.findViewById(viewId);
        preview.addView(cameraPreview);
    }

    public void startPreview() {
        camera=Camera.open(CAMERA_ID); // attempt to get a Camera instance
        cameraPreview.setCamera(camera);
        cameraPreview.startPreview();
    }

    public void stopPreview() {
        cameraPreview.stopPreview();
        releaseMediaRecorder();       // if you are using MediaRecorder, release it first
    }

    public void startRecording(String outputfileName) {
        if (prepareMediaRecorder(outputfileName)) {
            mediaRecorder.start();
            cameraPreview.startRecording();

            isRecording = true;
        } else {
            cameraPreview.stopRecording();
            releaseMediaRecorder();
            isRecording = false;
        }
    }

    public void stopRecording() {
        // stop recording and release camera
        cameraPreview.stopRecording();
        mediaRecorder.stop();  // stop the recording
        releaseMediaRecorder(); // release the MediaRecorder object
        camera.lock();         // take camera access back from MediaRecorder
        isRecording = false;
    }

    private boolean prepareMediaRecorder(String outputfileName) {

        mediaRecorder = new MediaRecorder();

        Log.d(LibraryInfo.TAG, "prep1");
        CamcorderProfile profile;
        // ugly profile selection:
        // - SM-T800 has no 480P
        // - Ace3 has 720P but it hangs
        if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_480P) ) {
            profile = CamcorderProfile.get(CamcorderProfile.QUALITY_480P);
        } else if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_HIGH)) {
            profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
        } else if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_LOW)) {
            profile = CamcorderProfile.get(CamcorderProfile.QUALITY_LOW);
        } else {
            Log.d(LibraryInfo.TAG,"Cannot get camcorderprofile");
            return false;
        }
        Log.d(LibraryInfo.TAG,"prep: profile width: " + profile.videoFrameWidth + " height:" + profile.videoFrameHeight);
        //http://stackoverflow.com/a/16543157/21047
        Camera.Parameters parameters = camera.getParameters();
        Log.d(LibraryInfo.TAG,"prep3");
        parameters.setPreviewSize(profile.videoFrameWidth,profile.videoFrameHeight);
        Log.d(LibraryInfo.TAG,"prep4");
        parameters.setPreviewFormat(ImageFormat.NV21); // YUV12 boots Ace3! RGB565 hangs on SM-T800 keeping default format
        Log.d(LibraryInfo.TAG,"prep5");
        camera.setParameters(parameters);
        Log.d(LibraryInfo.TAG,"prep6");

        camera.unlock();
        Log.d(LibraryInfo.TAG,"prep7");
        mediaRecorder.setCamera(camera);

        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mediaRecorder.setOrientationHint(CameraPreview.getCameraDisplayOrientation(activity,CAMERA_ID,camera));
        mediaRecorder.setProfile(profile);
        mediaRecorder.setOutputFile(outputfileName);

        mediaRecorder.setPreviewDisplay(cameraPreview.getHolder().getSurface());

        try {
            mediaRecorder.prepare();
        } catch (IllegalStateException | IOException e) {
            Log.e(LibraryInfo.TAG, "Exception preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }
        Log.d(LibraryInfo.TAG, "prepareMediaRecorder ready++++++!!!!!!!!!!!");
        return true;
    }

    private void releaseMediaRecorder() {
        if (mediaRecorder != null) {
            mediaRecorder.reset();   // clear recorder configuration
            mediaRecorder.release(); // release the recorder object
            mediaRecorder = null;
        }
    }

    public boolean isRecording() {
        return isRecording;
    }
}
