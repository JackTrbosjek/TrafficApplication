package diplomski.jakov.trafficapplication;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import javax.inject.Inject;

import diplomski.jakov.trafficapplication.base.Application;
import diplomski.jakov.trafficapplication.models.Enums.FileType;
import diplomski.jakov.trafficapplication.models.Enums.RecordType;
import diplomski.jakov.trafficapplication.models.LocalFile;
import diplomski.jakov.trafficapplication.services.LocalFileService;
import diplomski.jakov.trafficapplication.util.CameraPreview;
import diplomski.jakov.trafficapplication.util.DateFormats;

public class CameraPreviewActivity extends Activity implements CameraPreview.SurfaceCallback {
    Camera mCamera;
    CameraPreview mPreview;
    RecordType recordType;
    FileType fileType;
    @Inject
    LocalFileService localFileService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_preview);
        ((Application) getApplication()).getNetComponent().inject(this);
        recordType = RecordType.detachFrom(getIntent());
        fileType = FileType.detachFrom(getIntent());
        if (recordType != null && fileType !=null) {
            mMediaRecorder = new MediaRecorder();
            mCamera = getCameraInstance();
            mPreview = new CameraPreview(this, mCamera, mMediaRecorder, this);
            FrameLayout cameraPreview = (FrameLayout) findViewById(R.id.camera_preview);
            cameraPreview.addView(mPreview);

            FrameLayout layout = (FrameLayout) findViewById(R.id.layout);
            layout.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view) {
                    finish();
                }
            });
        }else{
            finish();
        }
    }

    private Camera.PictureCallback pictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] bytes, Camera camera) {
            if (bytes != null) {
                LocalFileService.FileModel fileModel= localFileService.createImgeFileFromBytes(recordType,bytes);
                finish();
            }

        }
    };
    MediaRecorder mMediaRecorder;

    private boolean prepareVideoRecorder() {


        // Step 1: Unlock and set camera to MediaRecorder
        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);

        // Step 2: Set sources
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));

        LocalFileService.FileModel fileModel =  localFileService.createVideoFile(recordType);
        // Step 4: Set output file
        mMediaRecorder.setOutputFile(fileModel.file.getAbsolutePath());


        // Step 5: Set the preview output
        mMediaRecorder.setPreviewDisplay(mPreview.getHolder().getSurface());
        mMediaRecorder.setOrientationHint(90);
        // Step 6: Prepare configured MediaRecorder
        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.d("Video preview error", "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            fileModel.localFile.delete();
            return false;
        } catch (IOException e) {
            Log.d("Video preview error", "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            fileModel.localFile.delete();
            return false;
        }
        return true;
    }

    private void releaseMediaRecorder() {
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();   // clear recorder configuration
            mMediaRecorder.release(); // release the recorder object
            mMediaRecorder = null;
            if (mCamera != null) {
                mCamera.lock();// lock camera for later use
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();
        releaseMediaRecorder();
    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }


    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        View view = getWindow().getDecorView();
        WindowManager.LayoutParams lp = (WindowManager.LayoutParams) view.getLayoutParams();
        lp.gravity = Gravity.RIGHT | Gravity.BOTTOM;
        getWindowManager().updateViewLayout(view, lp);
    }

    /**
     * A safe way to get an instance of the Camera object.
     */
    public Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (fileType == FileType.VIDEO) {
            if (prepareVideoRecorder()) {
                mMediaRecorder.start();
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        // stop recording and release camera
                        mMediaRecorder.stop();  // stop the recording
                        releaseMediaRecorder(); // release the MediaRecorder object
                        mCamera.lock();         // take camera access back from MediaRecorder
                        finish();
                    }
                }, 5000);
            }
        }
        if (fileType == FileType.PHOTO) {
            mCamera.takePicture(null, null, pictureCallback);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        releaseCamera();
        releaseMediaRecorder();
    }
}
