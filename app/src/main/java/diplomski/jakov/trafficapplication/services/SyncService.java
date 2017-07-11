package diplomski.jakov.trafficapplication.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
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
import diplomski.jakov.trafficapplication.util.Util;

public class SyncService extends Service {
    public static final String ARG_FILE_ID = "SyncService.class.argument_sudden_stopping";
    public static final String STOP_INTENT = SyncService.class.getName() + "STOP_INTENT";
    public static final String RECURRING_INTENT = SyncService.class.getName() + "RECURRING_INTENT";

    boolean syncInProgress = false;
    boolean autosync;
    int mNotificationId;
    NotificationManager mNotificationManager;
    Handler handler;
    Runnable handlerRunnable;
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
        if (!connectionAvailable()) {
            stopSelf();
            return;
        }
        refreshFiles();

    }

    private boolean connectionAvailable() {
        if (!Connection.isInternetConnected(getApplicationContext())) {
            return false;
        }
        if (preferenceService.getSyncWifiOnly() && !Connection.isWiFiConnection(getApplicationContext())) {
            return false;
        }
        return true;
    }

    private void refreshFiles() {
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
            }
        }
        checkAutosync();
        if (filesForSync.size() > 0 && !autosync) {
            startSync();
        } else if (filesForSync.size() == 0 && autosync) {
            startAutosync();
        } else if (filesForSync.size() > 0 && autosync) {
            startSync();
            startAutosync();
        } else {
            stopSelf();
            return START_NOT_STICKY;
        }
        return START_STICKY;
    }

    private void startAutosync() {
        handler = new Handler();
        final long finalIntervalInMills = 30 * 1000;
        handlerRunnable = new Runnable() {
            @Override
            public void run() {
                if (!syncInProgress) {
                    refreshFiles();
                    if (filesForSync.size() > 0 && connectionAvailable()) {
                        startSync();
                    }
                }
                checkAutosync();
                if (autosync) {
                    handler.postDelayed(this, finalIntervalInMills);
                }
            }
        };
        handler.postDelayed(handlerRunnable, finalIntervalInMills);
    }

    private boolean checkAutosync() {
        autosync = false;
        if (preferenceService.getSyncProactive() && Util.isMyServiceRunning(ProactiveService.class, this)) {
            autosync = true;
        }
        if (preferenceService.getSyncReactive() && Util.isMyServiceRunning(ReactiveService.class, this)) {
            autosync = true;
        }
        return autosync;
    }

    private void startSync() {
        updateNotification();
        if (!filesForSync.empty()) {
            syncInProgress = true;
            final LocalFile localFile = filesForSync.pop();
            fileUploadService.uploadFile(localFile, new FileUploadService.OnFileUploadListener() {
                @Override
                public void onSuccess() {
                    updateNotification();
                    sendFileSyncedEvent(localFile);
                    syncInProgress = false;
                    startSync();
                }

                @Override
                public void unauthorized() {
                    syncInProgress = false;
                    stopSelf();
                }

                @Override
                public void onError() {
                    updateNotification();
                    sendFileSyncedEvent(localFile);
                    syncInProgress = false;
                    startSync();
                }
            });
        } else if (!autosync) {
            stopSelf();
        }
    }

    private void updateNotification() {
        if (mNotificationId == 0) {
            createNotification();
            return;
        }
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
        if (handler != null && handlerRunnable != null) {
            handler.removeCallbacks(handlerRunnable);
        }
        super.onDestroy();
    }

    private SyncBinder mBinder = new SyncBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private OnFileSyncListener filesSyncListener;

    public class SyncBinder extends Binder {

        public List<LocalFile> getSyncFiles() {
            return filesForSync;
        }

        public void setSyncListener(OnFileSyncListener onFileSyncListener) {
            filesSyncListener = onFileSyncListener;
        }
    }

    public interface OnFileSyncListener {
        void newFileSynced(LocalFile localFile);
    }

    private void sendFileSyncedEvent(LocalFile localFile) {
        if (filesSyncListener != null) {
            filesSyncListener.newFileSynced(localFile);
        }
    }
}
