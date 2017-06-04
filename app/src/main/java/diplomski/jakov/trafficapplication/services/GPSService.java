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
import android.support.v4.app.ListFragment;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import diplomski.jakov.trafficapplication.models.LocalFile;

public class GPSService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private static final String LOGSERVICE = "Location Service";
    public static final String FILE_ID_ARG = "diplomski.jakov.trafficapplication.fileID.arg";
    private static final int DELAY_IN_MILLISECONDS = 1000 * 20;

    private ArrayList<LocalFile> fileList = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        buildGoogleApiClient();
        Log.e(LOGSERVICE, "onCreate");

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(LOGSERVICE, "onStartCommand");

        Long fileID = intent.getLongExtra(FILE_ID_ARG, -1);//provjeri intent null
        if (fileID != -1) {
            LocalFile localFile = LocalFile.findById(LocalFile.class, fileID);
            if (localFile != null) {
                fileList.add(localFile);
            }
        }
        if (!mGoogleApiClient.isConnected())
            mGoogleApiClient.connect();
        else
            startLocationUpdate();
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
        if(fileList == null || fileList.size() == 0){
            stopLocationUpdate();
        }
        if (location.getAccuracy() < 30) {
            for (int i = 0; i < fileList.size(); i++) {
                saveFile(location, fileList.get(i));
            }
        } else {
            for (int i = 0; i < fileList.size(); i++) {
                LocalFile file = fileList.get(i);
                if (new Date().getTime() - file.dateCreated.getTime() > DELAY_IN_MILLISECONDS) {
                    saveFile(location, file);
                }
            }
        }
    }

    private void saveFile(Location location, LocalFile file) {
        file.latitude = location.getLatitude();
        file.longitude = location.getLongitude();
        file.accuracy = location.getAccuracy();
        file.save();
        fileList.remove(file);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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
        mLocationRequest.setInterval(2000);
        mLocationRequest.setFastestInterval(1000);
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
