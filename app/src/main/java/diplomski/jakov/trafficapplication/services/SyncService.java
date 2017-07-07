package diplomski.jakov.trafficapplication.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Stack;

import javax.inject.Inject;

import diplomski.jakov.trafficapplication.MainActivity;
import diplomski.jakov.trafficapplication.R;
import diplomski.jakov.trafficapplication.base.Application;
import diplomski.jakov.trafficapplication.database.LocalFile;
import diplomski.jakov.trafficapplication.database.LocalFileDao;
import diplomski.jakov.trafficapplication.models.Enums.RecordType;
import diplomski.jakov.trafficapplication.util.Connection;

public class SyncService extends Service {
    public static final String ARG_FILE_ID = "SyncService.class.argument_sudden_stopping";
    public static final String STOP_INTENT = SyncService.class.getName() + "STOP_INTENT";

    int mNotificationId;
    NotificationManager mNotificationManager;
    @Inject
    LocalFileDao localFileDao;
    @Inject
    PreferenceService preferenceService;
    @Inject
    FileUploadService fileUploadService;
    Stack<LocalFile> filesForSync;
    int allFiles;
    NotificationCompat.Builder mBuilder;

    @Override
    public void onCreate() {
        super.onCreate();
        ((Application) getApplication()).getNetComponent().inject(this);
        if (!Connection.isInternetConnected(getApplicationContext())) {
            stopSelf();
            return;
        }
        if (preferenceService.getSyncWifiOnly() && !Connection.isWiFiConnection(getApplicationContext())) {
            stopSelf();
            return;
        }
        List<LocalFile> allLocalFiles = localFileDao.getAllUnsyncedLocalFiles();
        if (!preferenceService.getSyncProactive()) {
            removeTypeFromList(allLocalFiles, RecordType.PROACTIVE);
        }
        if (!preferenceService.getSyncReactive()) {
            removeTypeFromList(allLocalFiles, RecordType.REACTIVE);
        }
        if (!preferenceService.getSyncUser()) {
            removeTypeFromList(allLocalFiles, RecordType.USER);
        }
        allFiles = allLocalFiles.size();
        filesForSync = new Stack<>();
        for (LocalFile file : allLocalFiles) {
            filesForSync.push(file);
        }

    }

    private void removeTypeFromList(List<LocalFile> allFiles, RecordType recordType) {
        Iterator<LocalFile> iterator = allFiles.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().recordType == recordType) {
                iterator.remove();
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null || (intent.getAction() != null && intent.getAction().contentEquals(STOP_INTENT))) {
            stopSelf();
            return START_NOT_STICKY;
        }

        long localFileId = intent.getLongExtra(ARG_FILE_ID, -1);
        if (localFileId != -1) {
            LocalFile localFile = localFileDao.getLocalFile(localFileId);
            if (!filesForSync.contains(localFile)) {
                filesForSync.push(localFile);
                allFiles++;
                updateNotification();
            }
        }
        if (filesForSync.size() > 0) {
            if (mNotificationId == 0)
                createNotification();
        } else {
            stopSelf();
            return START_NOT_STICKY;
        }
        startSync();
        return START_STICKY;
    }

    private void startSync() {
        if (!filesForSync.empty()) {
            LocalFile localFile = filesForSync.pop();
            fileUploadService.uploadFile(localFile, new FileUploadService.OnFileUploadListener() {
                @Override
                public void onSuccess() {
                    updateNotification();
                    startSync();
                }

                @Override
                public void unauthorized() {
                    stopSelf();
                }

                @Override
                public void onError() {
                    updateNotification();
                    startSync();
                }
            });
        } else {
            stopSelf();
        }
    }

    private void updateNotification() {
        mBuilder.setProgress(allFiles, allFiles - filesForSync.size(), false);
        Notification notification = mBuilder.build();
        mNotificationManager.notify(mNotificationId, notification);
        startForeground(mNotificationId, notification);
    }

    private void createNotification() {
        mNotificationId = new Random().nextInt();
        mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_notification_icon)
                        .setContentTitle("Traffic Application")
                        .setContentText("Sync Service");

        mBuilder.setProgress(allFiles, 0, false);

        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, MainActivity.class);

        Intent stopIntent = new Intent(this, SyncService.class);
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
        if (mNotificationManager != null && mNotificationId != 0) {
            mNotificationManager.cancel(mNotificationId);
        }
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
