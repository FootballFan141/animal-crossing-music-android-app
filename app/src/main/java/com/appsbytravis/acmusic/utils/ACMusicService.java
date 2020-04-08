package com.appsbytravis.acmusic.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

public class ACMusicService extends Service {

    private PendingIntent pendingIntent;
    private AlarmManager alarmManager;
    private PendingIntent pendingIntentFadeMusic;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        pendingIntent = intent.getParcelableExtra("pendingIntent");
        pendingIntentFadeMusic = intent.getParcelableExtra("pendingIntentFadeMusic");
        alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);

        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
        if (pendingIntentFadeMusic != null) {
            alarmManager.cancel(pendingIntentFadeMusic);
            pendingIntentFadeMusic.cancel();
        }
        alarmManager = null;
        pendingIntent = null;
    }

    @Override
    public boolean stopService(Intent name) {
        super.stopService(name);
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
        if (pendingIntentFadeMusic != null) {
            alarmManager.cancel(pendingIntentFadeMusic);
            pendingIntentFadeMusic.cancel();
        }
        alarmManager = null;
        pendingIntent = null;
        return true;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
        if (pendingIntentFadeMusic != null) {
            alarmManager.cancel(pendingIntentFadeMusic);
            pendingIntentFadeMusic.cancel();
        }
        alarmManager = null;
        pendingIntent = null;
        stopSelf();
    }
}
