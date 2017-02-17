package hu.rics.camera1util;

import android.app.Activity;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.util.Log;
import android.widget.FrameLayout;

import java.io.IOException;

/**
 * Created by rics on 2017.02.08.
 */
@SuppressWarnings( "deprecation" ) // because of the camera
public class MediaRecorderWrapper {

    MediaRecorder mediaRecorder;
    Camera camera;
    CameraPreview cameraPreview;
    boolean isRecording;
    static final int CAMERA_ID = 0;

    public MediaRecorderWrapper(Activity activity, int id) {
        cameraPreview = new CameraPreview(activity);
        FrameLayout preview = (FrameLayout) activity.findViewById(id);
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

            isRecording = true;
        } else {
            releaseMediaRecorder();
            isRecording = false;
        }
    }

    public void stopRecording() {
        // stop recording and release camera
        mediaRecorder.stop();  // stop the recording
        releaseMediaRecorder(); // release the MediaRecorder object
        camera.lock();         // take camera access back from MediaRecorder
        isRecording = false;
    }

    private boolean prepareMediaRecorder(String outputfileName) {

        mediaRecorder = new MediaRecorder();

        Log.d(LibraryInfo.TAG, "prep1");
        CamcorderProfile profile;
        if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_480P)) {
            profile = CamcorderProfile.get(CamcorderProfile.QUALITY_480P);
        } else if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_720P)) {
            profile = CamcorderProfile.get(CamcorderProfile.QUALITY_720P);
        } else if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_LOW)) {
            profile = CamcorderProfile.get(CamcorderProfile.QUALITY_LOW);
        } else {
            Log.d(LibraryInfo.TAG,"Cannot get camcorderprofile");
            return false;
        }
        Log.d(LibraryInfo.TAG,"prep2");
        //http://stackoverflow.com/a/16543157/21047
        Camera.Parameters parameters = camera.getParameters();
        Log.d(LibraryInfo.TAG,"prep3");
        parameters.setPreviewSize(profile.videoFrameWidth,profile.videoFrameHeight);
        Log.d(LibraryInfo.TAG,"prep4");
        parameters.setPreviewFormat(ImageFormat.NV21);
        Log.d(LibraryInfo.TAG,"prep5");
        camera.setParameters(parameters);
        Log.d(LibraryInfo.TAG,"prep6");

        camera.unlock();
        Log.d(LibraryInfo.TAG,"prep7");
        mediaRecorder.setCamera(camera);

        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
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
