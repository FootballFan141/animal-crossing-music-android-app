package com.appsbytravis.acmusic.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import java.util.Calendar;

import static android.app.PendingIntent.getBroadcast;
import static com.appsbytravis.acmusic.utils.Constants.CHANGE_MUSIC_REQUESTCODE;
import static com.appsbytravis.acmusic.utils.Constants.FADE_MUSIC_REQUESTCODE;

public class ACMusicService extends Service {

    private Intent changeMusicIntent;
    private Intent fadeMusicIntent;
    private AlarmManager alarmManager;
    private String assetsPath;
    private PendingIntent changeMusicPendingIntent;
    private PendingIntent fadeMusicPendingIntent;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        changeMusicIntent = intent.getParcelableExtra("changeMusicIntent");
        fadeMusicIntent = intent.getParcelableExtra("fadeMusicIntent");
        changeMusicPendingIntent = intent.getParcelableExtra("changeMusicPendingIntent");
        fadeMusicPendingIntent = intent.getParcelableExtra("fadeMusicPendingIntent");
        assetsPath = intent.getStringExtra("assetsPath");
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        changeMusicAlarm();
        fadeMusicAlarm();
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (changeMusicPendingIntent != null) {
            alarmManager.cancel(changeMusicPendingIntent);
            changeMusicPendingIntent.cancel();
        }
        if (fadeMusicPendingIntent != null) {
            alarmManager.cancel(fadeMusicPendingIntent);
            fadeMusicPendingIntent.cancel();
        }
        alarmManager = null;
        changeMusicPendingIntent = null;
        fadeMusicPendingIntent = null;
    }

    @Override
    public boolean stopService(Intent name) {
        super.stopService(name);
        if (changeMusicPendingIntent != null) {
            alarmManager.cancel(changeMusicPendingIntent);
            changeMusicPendingIntent.cancel();
        }
        if (fadeMusicPendingIntent != null) {
            alarmManager.cancel(fadeMusicPendingIntent);
            fadeMusicPendingIntent.cancel();
        }
        alarmManager = null;
        changeMusicPendingIntent = null;
        fadeMusicPendingIntent = null;
        return true;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        if (changeMusicPendingIntent != null) {
            alarmManager.cancel(changeMusicPendingIntent);
            changeMusicPendingIntent.cancel();
        }
        if (fadeMusicPendingIntent != null) {
            alarmManager.cancel(fadeMusicPendingIntent);
            fadeMusicPendingIntent.cancel();
        }
        alarmManager = null;
        changeMusicPendingIntent = null;
        fadeMusicPendingIntent = null;
        stopSelf();
    }

    private void changeMusicAlarm() {
        Calendar calendar = setCalendar();
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        long timeInMillis = calendar.getTimeInMillis();
        PendingIntent changeMusicIntent = getBroadcast(getApplicationContext(), CHANGE_MUSIC_REQUESTCODE, this.changeMusicIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, changeMusicIntent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, changeMusicIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, timeInMillis, changeMusicIntent);
        }
    }

    private void fadeMusicAlarm() {

        Calendar calendarFadeMusic = getCalendar();
        calendarFadeMusic.set(Calendar.HOUR_OF_DAY, calendarFadeMusic.get(Calendar.HOUR_OF_DAY));
        calendarFadeMusic.set(Calendar.MINUTE, 59);
        calendarFadeMusic.set(Calendar.SECOND, 55);
        calendarFadeMusic.set(Calendar.MILLISECOND, 0);
        long timeInMillisFadeMusic = calendarFadeMusic.getTimeInMillis();

        PendingIntent fadeMusicIntent = getBroadcast(getApplicationContext(), FADE_MUSIC_REQUESTCODE, this.fadeMusicIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManagerFadeMusic = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManagerFadeMusic.cancel(fadeMusicIntent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManagerFadeMusic.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillisFadeMusic, fadeMusicIntent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManagerFadeMusic.setExact(AlarmManager.RTC_WAKEUP, timeInMillisFadeMusic, fadeMusicIntent);
        } else {
            alarmManagerFadeMusic.set(AlarmManager.RTC_WAKEUP, timeInMillisFadeMusic, fadeMusicIntent);
        }
    }

    private Calendar setCalendar() {
        Calendar calendar = getCalendar();

        calendar.set(calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.HOUR_OF_DAY), 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }

    private Calendar getCalendar() {
        return Calendar.getInstance();
    }

}
