package com.megacoreapps.sun2meg.securedcamerashot;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaRecorder;
import android.media.ToneGenerator;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.io.File;

public class LockService extends Service {
    boolean isRunning = false;
//    private MainActivity mainActivity;

    NotificationManager mNotifyManager;
    NotificationCompat.Builder mBuilder;
    NotificationChannel notificationChannel;
    String NOTIFICATION_CHANNEL_ID = "1";
    PendingIntent pendingIntent;
    public static final String ACTION_STOP_RECORDING = "com.android.sun2meg.voltrig.ACTION_STOP_RECORDING";
    public static final String ACTION_START_RECORDING = "com.android.sun2meg.voltrig.ACTION_START_RECORDING";


    private MediaRecorder mRecorder;
    private String mFileName;
    private boolean isRecording = false;
    private Vibrator v;
    private ToneGenerator tone,tone2;
    private File storageDir;


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();


        Log.d("RUNNER : ", "OnCreate... \n");

        Bitmap IconLg = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher_foreground);

        mNotifyManager = (NotificationManager) getApplicationContext().getSystemService(NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(this, "null");
//        mBuilder = new NotificationCompat.Builder(LockService,null);
        mBuilder.setContentTitle("SecureCameraShots")
                .setContentText("SecureCameraShots running...")
                .setTicker("SecureCameraShots running...")
                .setSmallIcon(R.drawable.cam)
                .setLargeIcon(IconLg)
                .setPriority(Notification.PRIORITY_HIGH)
                .setVibrate(new long[] {1000})
//                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setOngoing(true)
                .setAutoCancel(false);

        Intent notificationIntent = new Intent(this, MainActivity.class);
//        Intent notificationIntent = new Intent(this, Records.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
        } else {
            pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "My Notifications", NotificationManager.IMPORTANCE_HIGH);

            // Configure the notification channel.
            notificationChannel.setDescription("Channel description");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{1000});
            notificationChannel.enableVibration(true);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            mNotifyManager.createNotificationChannel(notificationChannel);

            mBuilder.setChannelId(NOTIFICATION_CHANNEL_ID);
            startForeground(1, mBuilder.build());
        }
        else
        {
            mBuilder.setChannelId(NOTIFICATION_CHANNEL_ID);
            mNotifyManager.notify(1, mBuilder.build());
            startForeground(1, mBuilder.build());
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {


        Intent notificationIntent = new Intent(this, MainActivity.class);
//        Intent notificationIntent = new Intent(this, Records.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
           pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
        } else {
            pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        }
//        Intent notificationIntent = new Intent(this, MainActivity.class);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("MYID", "CHANNELFOREGROUND", NotificationManager.IMPORTANCE_DEFAULT);

            NotificationManager m = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            m.createNotificationChannel(channel);

            Notification notification = new Notification.Builder(this, "MYID")
                    .setContentTitle("Secure Camera Shots")
                    .setContentText("Capture and Upload")
                    .setSmallIcon(R.drawable.cam)
                    .setContentIntent(pendingIntent)
                    .build();
            this.startForeground(115, notification);
        } else {
            Notification notification1 = new NotificationCompat.Builder(getApplicationContext(), "CHANNEL_1_ID")
                    .setSmallIcon(R.drawable.cam)
                    .setContentTitle("Secure Camera Shots")
                    .setContentText("Capture and Upload")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                    .build();
            this.startForeground(115, notification1);
            isRunning = true;
        }
        return START_STICKY;
    }

    private void restartService() {
        Intent restartIntent = new Intent(this, LockService.class);
        // Indicate that the service is restarting due to a crash
        restartIntent.setAction("CrashRestart");
        startService(restartIntent);
    }


    @Override
    public void onDestroy()
    {
        Log.d("RUNNER : ", "\nDestroyed....");
        Log.d("RUNNER : ", "\nWill be created again automaticcaly....");

        super.onDestroy();
    }


    public class LocalBinder extends Binder {
        LockService getService() {
            return LockService.this;
        }
    }
}