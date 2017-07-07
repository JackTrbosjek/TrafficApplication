package diplomski.jakov.trafficapplication.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;

import javax.inject.Inject;

import diplomski.jakov.trafficapplication.CameraPreviewView;
import diplomski.jakov.trafficapplication.base.Application;
import diplomski.jakov.trafficapplication.database.LocalFileDao;
import diplomski.jakov.trafficapplication.models.Enums.FileType;
import diplomski.jakov.trafficapplication.models.Enums.RecordType;

public class SuddenStoppingDetectionService extends Service implements SensorEventListener {
    private static final double MIN_FORCE = 25;

    private SensorManager senSensorManager;
    private Sensor senAccelerometer;
    private boolean takingPicture = false;
    @Inject
    CameraPreviewView cameraPreviewView;

    @Inject
    LocalFileDao localFileDao;

    @Inject
    LocalFileService localFileService;

    @Override
    public void onCreate() {
        super.onCreate();
        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        ((Application) getApplication()).getNetComponent().inject(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        return START_STICKY;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;

        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];
            double gForce = Math.sqrt(x * x + y * y + z * z);
            if (gForce > MIN_FORCE && !takingPicture) {
                takingPicture = true;
                takePicture();
            }
        }
    }

    private void takePicture() {
        cameraPreviewView.takeRecord(RecordType.REACTIVE, FileType.PHOTO, null, 0);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                takingPicture = false;
            }
        },3000);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        senSensorManager.unregisterListener(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
