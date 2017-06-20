package diplomski.jakov.trafficapplication.services;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;

import javax.inject.Inject;

import diplomski.jakov.trafficapplication.CameraPreviewView;
import diplomski.jakov.trafficapplication.base.Application;
import diplomski.jakov.trafficapplication.database.LocalFile;
import diplomski.jakov.trafficapplication.database.LocalFileDao;
import diplomski.jakov.trafficapplication.models.Enums.FileType;
import diplomski.jakov.trafficapplication.models.Enums.RecordType;

public class TrafficJamGPSService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {
    public static final String TRAFFIC_JAM_DURATION_ARG = TrafficJamGPSService.class.getName() + "ARG_TRAFFIC_JAM";
    private static final String LOGSERVICE = TrafficJamGPSService.class.getName();
    private static final int INTERVAL = 15 * 1000;
    private static final int FASTEST_INTERVAL = 10 * 1000;
    private static final long STARING_DELAY = 40 * 1000;

    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private LinkedList<Location> listOfLocations;
    private long trafficJamDuration;
    private long startingTime;
    CameraPreviewView cameraPreviewView;

    @Inject
    LocalFileDao localFileDao;

    @Inject
    LocalFileService localFileService;


    @Override
    public void onCreate() {
        super.onCreate();
        buildGoogleApiClient();
        Log.e(LOGSERVICE, "onCreate");
        listOfLocations = new LinkedList<>();
        ((Application) getApplication()).getNetComponent().inject(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(LOGSERVICE, "onStartCommand");
        if (intent != null) {
            trafficJamDuration = intent.getLongExtra(TRAFFIC_JAM_DURATION_ARG, 60 * 1000);
            startingTime = new Date().getTime() + STARING_DELAY;
            if (!mGoogleApiClient.isConnected())
                mGoogleApiClient.connect();
            else
                startLocationUpdate();
        }
        cameraPreviewView = new CameraPreviewView(getApplicationContext(), localFileDao, localFileService, RecordType.REACTIVE, FileType.PHOTO, null, 0);
        return START_STICKY;
    }


    @Override
    public void onConnected(Bundle bundle) {
        Log.e(LOGSERVICE, "onConnected" + bundle);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location l = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (l != null) {
            Log.e(LOGSERVICE, "lat " + l.getLatitude());
            Log.e(LOGSERVICE, "lng " + l.getLongitude());

        }
        startLocationUpdate();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e(LOGSERVICE, "onConnectionSuspended " + i);

    }

    @Override
    public void onLocationChanged(Location location) {
        Log.e(LOGSERVICE, "lat " + location.getLatitude());
        Log.e(LOGSERVICE, "lng " + location.getLongitude());
        //stop updates if no files
        listOfLocations.add(location);

        checkTrafficJam();
    }

    private void checkTrafficJam() {
        long now = new Date().getTime();
        float approxSpeed = 0;
        Iterator<Location> iterator = listOfLocations.iterator();
        while (iterator.hasNext()) {
            Location loc = iterator.next();
            if (now - loc.getTime() > trafficJamDuration) {
                iterator.remove();
                continue;
            }
            approxSpeed+=loc.getSpeed();
        }
        approxSpeed/= (float)listOfLocations.size();
        if(approxSpeed < 5.556 && startingTime - now < 0){
            reportTrafficJam();
        }
    }

    private void reportTrafficJam() {
        cameraPreviewView.show();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        stopLocationUpdate();
        Log.e(LOGSERVICE, "onDestroy");

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(LOGSERVICE, "onConnectionFailed ");

    }

    private void initLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    }

    private void startLocationUpdate() {
        initLocationRequest();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    private void stopLocationUpdate() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);

    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addOnConnectionFailedListener(this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();
    }

}
