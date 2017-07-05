package diplomski.jakov.trafficapplication.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import java.util.Random;

import javax.inject.Inject;

import diplomski.jakov.trafficapplication.CameraPreviewView;
import diplomski.jakov.trafficapplication.MainActivity;
import diplomski.jakov.trafficapplication.R;
import diplomski.jakov.trafficapplication.base.Application;
import diplomski.jakov.trafficapplication.database.LocalFileDao;
import diplomski.jakov.trafficapplication.models.Enums.FileType;
import diplomski.jakov.trafficapplication.models.Enums.RecordType;

public class ReactiveService extends Service {
    public static final String ARG_SUDDEN_STOPPING = "ReactiveService.class.argument_sudden_stopping";
    public static final String ARG_TRAFFIC_JAM = "ReactiveService.class.argument_traffic_jam";
    public static final String STOP_INTENT = ReactiveService.class.getName() + "STOP_INTENT";

    boolean recordSuddenStopping;
    Intent suddenStoppingIntent;
    boolean recordTrafficJam;
    Intent trafficJamIntent;
    int mNotificationId;
    NotificationManager mNotificationManager;


    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null || (intent.getAction() != null && intent.getAction().contentEquals(STOP_INTENT))) {
            stopSelf();
            return START_NOT_STICKY;
        }
        recordSuddenStopping = intent.getBooleanExtra(ARG_SUDDEN_STOPPING, false);
        recordTrafficJam = intent.getBooleanExtra(ARG_TRAFFIC_JAM, false);

        if (recordTrafficJam) {
            trafficJamIntent = new Intent(this, TrafficJamGPSService.class);
            long jamDuration = intent.getLongExtra(TrafficJamGPSService.TRAFFIC_JAM_DURATION_ARG, 0);
            trafficJamIntent.putExtra(TrafficJamGPSService.TRAFFIC_JAM_DURATION_ARG, jamDuration);
            startService(trafficJamIntent);
        }
        if (recordSuddenStopping) {
            suddenStoppingIntent = new Intent(this, SuddenStoppingDetectionService.class);
            startService(suddenStoppingIntent);
        }
        createNotification();

        return START_STICKY;
    }

    private void createNotification() {
        mNotificationId = new Random().nextInt();
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_notification_icon)
                        .setContentTitle("Traffic Application")
                        .setContentText("Reactive Service");

        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, MainActivity.class);

        Intent stopIntent = new Intent(this, ReactiveService.class);
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
        if (recordTrafficJam && trafficJamIntent != null) {
            stopService(trafficJamIntent);
        }
        if (recordSuddenStopping && suddenStoppingIntent != null) {
            stopService(suddenStoppingIntent);
        }
        mNotificationManager.cancel(mNotificationId);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
