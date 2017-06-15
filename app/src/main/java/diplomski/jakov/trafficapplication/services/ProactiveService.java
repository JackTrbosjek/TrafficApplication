package diplomski.jakov.trafficapplication.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import java.util.Objects;
import java.util.Random;

import javax.inject.Inject;

import diplomski.jakov.trafficapplication.CameraPreviewView;
import diplomski.jakov.trafficapplication.MainActivity;
import diplomski.jakov.trafficapplication.R;
import diplomski.jakov.trafficapplication.base.Application;
import diplomski.jakov.trafficapplication.database.LocalFileDao;
import diplomski.jakov.trafficapplication.models.Enums.FileType;
import diplomski.jakov.trafficapplication.models.Enums.TimeUnits;
import diplomski.jakov.trafficapplication.models.Enums.VideoDurationUnits;

public class ProactiveService extends Service {
    public static final String ARG_INTERVAL = "ProactiveService.class.argument_interval";
    public static final String ARG_FOR_INTERVAL = "ProactiveService.class.argument_for_interval";
    public static final String STOP_INTENT = ProactiveService.class.getName() + "STOP_INTENT";

    int interval;
    FileType fileType;
    TimeUnits timeUnits;
    int forInterval;
    VideoDurationUnits videoDurationUnits;
    CameraPreviewView cameraPreviewView;
    Handler handler;
    Runnable handlerRunnable;
    int mNotificationId;
    NotificationManager mNotificationManager;

    @Inject
    LocalFileDao localFileDao;

    @Inject
    LocalFileService localFileService;

    @Override
    public void onCreate() {
        super.onCreate();
        ((Application) getApplication()).getNetComponent().inject(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null || (intent.getAction() != null && intent.getAction().contentEquals(STOP_INTENT))) {
            stopSelf();
            return START_NOT_STICKY;
        }
        fileType = FileType.detachFrom(intent);
        timeUnits = TimeUnits.detachFrom(intent);
        videoDurationUnits = VideoDurationUnits.detachFrom(intent);
        interval = intent.getIntExtra(ARG_INTERVAL, -1);
        forInterval = intent.getIntExtra(ARG_FOR_INTERVAL, -1);

        cameraPreviewView = new CameraPreviewView(getApplicationContext(), localFileDao, localFileService, fileType, videoDurationUnits, forInterval);

        createNotification();

        createHandler();

        return START_STICKY;
    }

    private void createHandler() {
        long intervalInMills = interval;
        switch (timeUnits) {
            case SEC:
                intervalInMills *= 1000;
                break;
            case MIN:
                intervalInMills *= 1000 * 60;
                break;
            case HOUR:
                intervalInMills *= 1000 * 60 * 60;
                break;
        }
        handler = new Handler();
        final long finalIntervalInMills = intervalInMills;
        handlerRunnable = new Runnable() {
            @Override
            public void run() {
                cameraPreviewView.show();
                handler.postDelayed(this, finalIntervalInMills);
            }
        };
        handler.post(handlerRunnable);
    }

    private void createNotification() {
        mNotificationId = new Random().nextInt();
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_notification_icon)
                        .setContentTitle("Traffic Application")
                        .setContentText("Proactive Service");

        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, MainActivity.class);

        Intent stopIntent = new Intent(this, ProactiveService.class);
        stopIntent.setAction(STOP_INTENT);
        PendingIntent stopPeIntent = PendingIntent.getService(this, 2, stopIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        mBuilder.addAction(R.drawable.ic_notification_stop, "Stop Service", stopPeIntent);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        Notification notification = mBuilder.build();
        mNotificationManager.notify(mNotificationId, notification);
        startForeground(mNotificationId, notification);
    }


    @Override
    public void onDestroy() {
        handler.removeCallbacks(handlerRunnable);
        mNotificationManager.cancel(mNotificationId);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
