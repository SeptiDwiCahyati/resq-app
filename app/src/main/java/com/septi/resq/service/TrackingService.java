package com.septi.resq.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import com.septi.resq.R;
import com.septi.resq.database.TrackingDBHelper;

public class TrackingService extends Service {
    private static final String CHANNEL_ID = "TrackingServiceChannel";
    private static final int NOTIFICATION_ID = 1;
    private TrackingDBHelper dbHelper;

    @Override
    public void onCreate() {
        super.onCreate();
        dbHelper = new TrackingDBHelper(this);
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(NOTIFICATION_ID, createNotification());
        return START_STICKY;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Tracking Service",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    private Notification createNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Ambulance Tracking Active")
                .setContentText("Tracking rescue team location")
                .setSmallIcon(R.drawable.ic_ambulance)
                .build();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}