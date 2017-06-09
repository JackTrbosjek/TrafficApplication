package diplomski.jakov.trafficapplication;

import android.content.Context;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.WindowManager;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import diplomski.jakov.trafficapplication.database.LocalFileDao;
import diplomski.jakov.trafficapplication.models.Enums.FileType;
import diplomski.jakov.trafficapplication.models.Enums.RecordType;
import diplomski.jakov.trafficapplication.models.Enums.VideoDurationUnits;
import diplomski.jakov.trafficapplication.services.LocalFileService;
import diplomski.jakov.trafficapplication.util.CameraPreview;

public class CameraPreviewView implements CameraPreview.SurfaceCallback {
    private static final int INITIAL_WIDTH = 270;
    private static final int INITIAL_HEIGHT = 200;
    private final Context mContext;
    private final WindowManager mWindowManager;
    private final WindowManager.LayoutParams mWindowParams;
    private Camera mCamera;
    private CameraPreview mPreview;
    private RecordType recordType;
    private FileType fileType;
    LocalFileService localFileService;
    LocalFileDao localFileDao;
    private int videoTimeInMills;

    public CameraPreviewView(Context context, LocalFileDao localFileDao, LocalFileService localFileService, FileType fileType, VideoDurationUnits videoDurationUnits, int forInterval) {
        mContext = context;
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        mWindowParams = createWindowParams(INITIAL_WIDTH, INITIAL_HEIGHT);
        this.localFileService = localFileService;
        this.localFileDao = localFileDao;
        recordType = RecordType.PROACTIVE;
        this.fileType = fileType;

        if (fileType == FileType.VIDEO) {
            videoTimeInMills = forInterval;
            switch (videoDurationUnits) {
                case SEC:
                    videoTimeInMills *= 1000;
                    break;
                case MIN:
                    videoTimeInMills *= 1000 * 60;
                    break;
                case HOUR:
                    videoTimeInMills *= 1000 * 60 * 60;
                    break;
            }
        }
    }

    public void show() {
        if (recordType != null && fileType != null) {
            mMediaRecorder = new MediaRecorder();
            mCamera = getCameraInstance();
            mPreview = new CameraPreview(mContext, mCamera, mMediaRecorder, this);
        }
        mWindowManager.addView(mPreview, mWindowParams);
    }

    public void hide() {
        releaseCamera();
        releaseMediaRecorder();
        mWindowManager.removeView(mPreview);
    }

    private static WindowManager.LayoutParams createWindowParams(int width, int height) {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.width = width;
        params.height = height;
        params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                | WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;
        params.format = PixelFormat.TRANSLUCENT;
        params.gravity = Gravity.START | Gravity.TOP;
        return params;
    }

    private Camera.PictureCallback pictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] bytes, Camera camera) {
            if (bytes != null) {
                LocalFileService.FileModel fileModel = localFileService.createImageFileFromBytes(recordType, bytes);
                hide();
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

        LocalFileService.FileModel fileModel = localFileService.createVideoFile(recordType);
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
            localFileDao.deleteLocalFile(fileModel.localFile);
            return false;
        } catch (IOException e) {
            Log.d("Video preview error", "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            localFileDao.deleteLocalFile(fileModel.localFile);
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

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
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
                        hide();
                    }
                }, videoTimeInMills);
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
