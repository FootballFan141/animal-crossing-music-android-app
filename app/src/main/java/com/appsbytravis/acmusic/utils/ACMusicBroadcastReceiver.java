package com.appsbytravis.acmusic.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.appsbytravis.acmusic.HomeActivity;
import com.appsbytravis.acmusic.R;

import java.io.File;
import java.util.Calendar;

import static com.appsbytravis.acmusic.utils.Constants.CANCEL_DOWNLOAD_REQUESTCODE;
import static com.appsbytravis.acmusic.utils.Constants.CHANGE_MUSIC_REQUESTCODE;
import static com.appsbytravis.acmusic.utils.Constants.FADE_MUSIC_REQUESTCODE;
import static com.appsbytravis.acmusic.utils.Constants.PAUSE_DOWNLOAD_REQUESTCODE;

public class ACMusicBroadcastReceiver extends BroadcastReceiver {


    private PendingIntent pendingIntent;
    private AlarmManager alarmManager;
    private int hour;
    private String ASSETS_PATH;
    private ACMusic music;
    private File file;
    private Intent changeMusicIntent;
    private String path;
    private Calendar calendarFadeMusic;
    private long timeInMillisFadeMusic;
    private Intent fadeMusicIntent;
    private PendingIntent pendingIntentFadeMusic;
    private AlarmManager alarmManagerFadeMusic;

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        Calendar calendar = Calendar.getInstance();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        NotificationCompat.Builder builder;

        boolean rain = prefs.getBoolean("raining", false);
        boolean snow = prefs.getBoolean("snowing", false);
        boolean normal = prefs.getBoolean("normal", false);


        Intent pauseDownloadIntent = new Intent(context, ACMusicBroadcastReceiver.class);
        pauseDownloadIntent.setAction("ACTION_PAUSE");

        Intent cancelDownloadIntent = new Intent(context, ACMusicBroadcastReceiver.class);
        cancelDownloadIntent.setAction("ACTION_CANCEL");

        Intent resumeDownloadIntent = new Intent(context, ACMusicBroadcastReceiver.class);
        resumeDownloadIntent.setAction("ACTION_RESUME");

        PendingIntent cancelDownloadPendingIntent =
                PendingIntent.getBroadcast(context, CANCEL_DOWNLOAD_REQUESTCODE, cancelDownloadIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        PendingIntent pauseDownloadPendingIntent =
                PendingIntent.getBroadcast(context, PAUSE_DOWNLOAD_REQUESTCODE, pauseDownloadIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        PendingIntent resumeDownloadPendingIntent =
                PendingIntent.getBroadcast(context, Constants.RESUME_DOWNLOAD_REQUESTCODE, resumeDownloadIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        switch (intent.getAction()) {
            case "ACTION_START":

                break;
            case "ACTION_CANCEL":
                manager.cancel(R.string.NOTIFICATION_MAIN);
                HomeActivity.firebasetask.cancel();
                break;
            case "ACTION_PAUSE":
                HomeActivity.firebasetask.pause();
                manager.cancel(R.string.NOTIFICATION_MAIN);
                builder = showNotification(context, "Currently paused.")
                        .setSmallIcon(android.R.drawable.ic_media_pause)
                        .addAction(android.R.drawable.stat_sys_download_done, "Resume", resumeDownloadPendingIntent)
                        .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Cancel", cancelDownloadPendingIntent);
                manager.notify(R.string.NOTIFICATION_MAIN, builder.build());
                break;
            case "ACTION_RESUME":
                HomeActivity.firebasetask.resume();
                manager.cancel(R.string.NOTIFICATION_MAIN);
                builder = showNotification(context, "This won't take long. :)")
                        .setSmallIcon(android.R.drawable.stat_sys_download)
                        .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Cancel", cancelDownloadPendingIntent)
                        .addAction(android.R.drawable.ic_media_pause, "Pause", pauseDownloadPendingIntent);
                manager.notify(R.string.NOTIFICATION_MAIN, builder.build());

//                HomeActivity.pauseDownloadBtn.setText(R.string.pause_download);
                break;
            case "ACTION_UPDATE_MUSIC:GC":
                Log.d(HomeActivity.TAG, "ACTION_UPDATE_MUSIC:GC");
                hour = calendar.get(Calendar.HOUR_OF_DAY);
                calendar.set(calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH),
                        calendar.get(Calendar.HOUR_OF_DAY), 0, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                calendar.add(Calendar.HOUR_OF_DAY, 1);
                ASSETS_PATH = "/assets/gamecube/";
                music = new ACMusic(context, ASSETS_PATH);

                //TODO: Update "background image" when music changes based on time of day. Look at com.appsbytravis.acmusic.utils.ACMusic, the bgChange method

                file = null;
                if (normal) {
                    file = music.normal(hour);
                } else if (rain) {
                    file = music.rainingGC();
                } else if (snow) {
                    file = music.snowing(hour);
                }
                path = file.getPath();
                if (ACMusicMediaPlayer.isPlaying()) {
                    ACMusicMediaPlayer.stop();
                }
                ACMusicMediaPlayer.play(context, Uri.parse(path));
                ACMusicMediaPlayer.fadein();
                ACMusicMediaPlayer.start();

                long timeInMillis = calendar.getTimeInMillis();

                changeMusicIntent = new Intent(context, ACMusicBroadcastReceiver.class);
                changeMusicIntent.setAction("ACTION_UPDATE_MUSIC:GC");
                pendingIntent = PendingIntent.getBroadcast(context, CHANGE_MUSIC_REQUESTCODE, changeMusicIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                alarmManager.cancel(pendingIntent);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
                }
                calendarFadeMusic = Calendar.getInstance();
                calendarFadeMusic.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY));
                calendarFadeMusic.add(Calendar.SECOND, -5);

                timeInMillisFadeMusic = calendarFadeMusic.getTimeInMillis();

                fadeMusicIntent = new Intent(context, ACMusicBroadcastReceiver.class);
                fadeMusicIntent.setAction("ACTION_FADEOUT");
                pendingIntentFadeMusic = PendingIntent.getBroadcast(context, FADE_MUSIC_REQUESTCODE, fadeMusicIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                alarmManagerFadeMusic = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                alarmManagerFadeMusic.cancel(pendingIntentFadeMusic);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManagerFadeMusic.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillisFadeMusic, pendingIntentFadeMusic);
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    alarmManagerFadeMusic.setExact(AlarmManager.RTC_WAKEUP, timeInMillisFadeMusic, pendingIntentFadeMusic);
                } else {
                    alarmManagerFadeMusic.set(AlarmManager.RTC_WAKEUP, timeInMillisFadeMusic, pendingIntentFadeMusic);
                }
                break;
            case "ACTION_UPDATE_MUSIC:WWCF":
                Log.d(HomeActivity.TAG, "ACTION_UPDATE_MUSIC:WWCF");
                hour = calendar.get(Calendar.HOUR_OF_DAY);
                calendar.set(calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH),
                        calendar.get(Calendar.HOUR_OF_DAY), 0, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                calendar.add(Calendar.HOUR_OF_DAY, 1);
                ASSETS_PATH = "/assets/wwcf/";
                music = new ACMusic(context, ASSETS_PATH);

                //TODO: Update "background image" when music changes based on time of day. Look at com.appsbytravis.acmusic.utils.ACMusic, the bgChange method

                file = null;
                if (normal) {
                    file = music.normal(hour);
                } else if (rain) {
                    file = music.raining(hour);
                } else if (snow) {
                    file = music.snowing(hour);
                }
                path = file.getPath();
                if (ACMusicMediaPlayer.isPlaying()) {
                    ACMusicMediaPlayer.stop();
                }
                ACMusicMediaPlayer.play(context, Uri.parse(path));
                ACMusicMediaPlayer.fadein();
                ACMusicMediaPlayer.start();

                timeInMillis = calendar.getTimeInMillis();

                changeMusicIntent = new Intent(context, ACMusicBroadcastReceiver.class);
                changeMusicIntent.setAction("ACTION_UPDATE_MUSIC:WWCF");
                pendingIntent = PendingIntent.getBroadcast(context, CHANGE_MUSIC_REQUESTCODE, changeMusicIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                alarmManager.cancel(pendingIntent);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
                }
                calendarFadeMusic = Calendar.getInstance();
                calendarFadeMusic.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY));
                calendarFadeMusic.add(Calendar.SECOND, -5);

                timeInMillisFadeMusic = calendarFadeMusic.getTimeInMillis();

                fadeMusicIntent = new Intent(context, ACMusicBroadcastReceiver.class);
                fadeMusicIntent.setAction("ACTION_FADEOUT");
                pendingIntentFadeMusic = PendingIntent.getBroadcast(context, FADE_MUSIC_REQUESTCODE, fadeMusicIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                alarmManagerFadeMusic = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                alarmManagerFadeMusic.cancel(pendingIntentFadeMusic);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManagerFadeMusic.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillisFadeMusic, pendingIntentFadeMusic);
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    alarmManagerFadeMusic.setExact(AlarmManager.RTC_WAKEUP, timeInMillisFadeMusic, pendingIntentFadeMusic);
                } else {
                    alarmManagerFadeMusic.set(AlarmManager.RTC_WAKEUP, timeInMillisFadeMusic, pendingIntentFadeMusic);
                }
                break;
            case "ACTION_UPDATE_MUSIC:NL":
                Log.d(HomeActivity.TAG, "ACTION_UPDATE_MUSIC:NL");
                hour = calendar.get(Calendar.HOUR_OF_DAY);
                calendar.set(calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH),
                        calendar.get(Calendar.HOUR_OF_DAY), 0, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                calendar.add(Calendar.HOUR_OF_DAY, 1);
                ASSETS_PATH = "/assets/newleaf/";
                music = new ACMusic(context, ASSETS_PATH);

                //TODO: Update "background image" when music changes based on time of day. Look at com.appsbytravis.acmusic.utils.ACMusic, the bgChange method

                file = null;
                if (normal) {
                    file = music.normal(hour);
                } else if (rain) {
                    file = music.raining(hour);
                } else if (snow) {
                    file = music.snowing(hour);
                }
                path = file.getPath();
                if (ACMusicMediaPlayer.isPlaying()) {
                    ACMusicMediaPlayer.stop();
                }
                ACMusicMediaPlayer.play(context, Uri.parse(path));
                ACMusicMediaPlayer.fadein();
                ACMusicMediaPlayer.start();

                timeInMillis = calendar.getTimeInMillis();

                changeMusicIntent = new Intent(context, ACMusicBroadcastReceiver.class);
                changeMusicIntent.setAction("ACTION_UPDATE_MUSIC:NL");
                pendingIntent = PendingIntent.getBroadcast(context, CHANGE_MUSIC_REQUESTCODE, changeMusicIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                alarmManager.cancel(pendingIntent);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
                }
                calendarFadeMusic = Calendar.getInstance();
                calendarFadeMusic.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY));
                calendarFadeMusic.add(Calendar.SECOND, -5);

                timeInMillisFadeMusic = calendarFadeMusic.getTimeInMillis();

                fadeMusicIntent = new Intent(context, ACMusicBroadcastReceiver.class);
                fadeMusicIntent.setAction("ACTION_FADEOUT");
                pendingIntentFadeMusic = PendingIntent.getBroadcast(context, FADE_MUSIC_REQUESTCODE, fadeMusicIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                alarmManagerFadeMusic = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                alarmManagerFadeMusic.cancel(pendingIntentFadeMusic);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManagerFadeMusic.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillisFadeMusic, pendingIntentFadeMusic);
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    alarmManagerFadeMusic.setExact(AlarmManager.RTC_WAKEUP, timeInMillisFadeMusic, pendingIntentFadeMusic);
                } else {
                    alarmManagerFadeMusic.set(AlarmManager.RTC_WAKEUP, timeInMillisFadeMusic, pendingIntentFadeMusic);
                }
                break;
            case "ACTION_UPDATE_MUSIC:NH":
                Log.d(HomeActivity.TAG, "ACTION_UPDATE_MUSIC:NH");
                hour = calendar.get(Calendar.HOUR_OF_DAY);
                calendar.set(calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH),
                        calendar.get(Calendar.HOUR_OF_DAY), 0, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                calendar.add(Calendar.HOUR_OF_DAY, 1);
                ASSETS_PATH = "/assets/newhorizons/";
                music = new ACMusic(context, ASSETS_PATH);

                //TODO: Update "background image" when music changes based on time of day. Look at com.appsbytravis.acmusic.utils.ACMusic, the bgChange method

                file = null;
                if (normal) {
                    file = music.normal(hour);
                } else if (rain) {
                    file = music.raining(hour);
                } else if (snow) {
                    file = music.snowing(hour);
                }
                path = file.getPath();
                if (ACMusicMediaPlayer.isPlaying()) {
                    ACMusicMediaPlayer.stop();
                }
                ACMusicMediaPlayer.play(context, Uri.parse(path));
                ACMusicMediaPlayer.fadein();
                ACMusicMediaPlayer.start();

                timeInMillis = calendar.getTimeInMillis();

                changeMusicIntent = new Intent(context, ACMusicBroadcastReceiver.class);
                changeMusicIntent.setAction("ACTION_UPDATE_MUSIC:NH");
                pendingIntent = PendingIntent.getBroadcast(context, CHANGE_MUSIC_REQUESTCODE, changeMusicIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                alarmManager.cancel(pendingIntent);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
                }
                calendarFadeMusic = Calendar.getInstance();
                calendarFadeMusic.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY));
                calendarFadeMusic.add(Calendar.SECOND, -5);

                timeInMillisFadeMusic = calendarFadeMusic.getTimeInMillis();

                fadeMusicIntent = new Intent(context, ACMusicBroadcastReceiver.class);
                fadeMusicIntent.setAction("ACTION_FADEOUT");
                pendingIntentFadeMusic = PendingIntent.getBroadcast(context, FADE_MUSIC_REQUESTCODE, fadeMusicIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                alarmManagerFadeMusic = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                alarmManagerFadeMusic.cancel(pendingIntentFadeMusic);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManagerFadeMusic.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillisFadeMusic, pendingIntentFadeMusic);
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    alarmManagerFadeMusic.setExact(AlarmManager.RTC_WAKEUP, timeInMillisFadeMusic, pendingIntentFadeMusic);
                } else {
                    alarmManagerFadeMusic.set(AlarmManager.RTC_WAKEUP, timeInMillisFadeMusic, pendingIntentFadeMusic);
                }
                break;
            case "ACTION_UPDATE_MUSIC:PocketCamp":
                Log.d(HomeActivity.TAG, "ACTION_UPDATE_MUSIC:PocketCamp");
                hour = calendar.get(Calendar.HOUR_OF_DAY);
                calendar.set(calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH),
                        calendar.get(Calendar.HOUR_OF_DAY), 0, 0);
                calendar.set(Calendar.MILLISECOND, 0);

                boolean morning = (hour >= 6 && hour < 12);
                boolean day = (hour >= 12 && hour < 16);
                boolean evening = (hour >= 16 && hour < 20);
                boolean night = (hour >= 20 || hour < 6);
                if (morning) {
                    calendar.set(Calendar.HOUR_OF_DAY, 12);
                } else if (day) {
                    calendar.set(Calendar.HOUR_OF_DAY, 16);
                } else if (evening) {
                    calendar.set(Calendar.HOUR_OF_DAY, 20);
                } else if (night) {
                    calendar.add(Calendar.DAY_OF_MONTH, 1);
                    calendar.set(Calendar.HOUR_OF_DAY, 6);
                }

                ASSETS_PATH = "/assets/pocketcamp/";
                music = new ACMusic(context, ASSETS_PATH);

                //TODO: Update "background image" when music changes based on time of day. Look at com.appsbytravis.acmusic.utils.ACMusic, the bgChange method

                file = null;
                file = music.pocketcamp(hour);
                path = file.getPath();
                if (ACMusicMediaPlayer.isPlaying()) {
                    ACMusicMediaPlayer.stop();
                }
                ACMusicMediaPlayer.play(context, Uri.parse(path));
                ACMusicMediaPlayer.fadein();
                ACMusicMediaPlayer.start();

                timeInMillis = calendar.getTimeInMillis();

                changeMusicIntent = new Intent(context, ACMusicBroadcastReceiver.class);
                changeMusicIntent.setAction("ACTION_UPDATE_MUSIC:PocketCamp");
                pendingIntent = PendingIntent.getBroadcast(context, CHANGE_MUSIC_REQUESTCODE, changeMusicIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                alarmManager.cancel(pendingIntent);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
                }

                Calendar calendarFadeMusic = Calendar.getInstance();

                calendarFadeMusic.set(Calendar.MINUTE, 0);
                calendarFadeMusic.set(Calendar.SECOND, 0);
                calendarFadeMusic.set(Calendar.MILLISECOND, 0);

                if (morning) {
                    calendarFadeMusic.set(Calendar.HOUR_OF_DAY, 12);
                } else if (day) {
                    calendarFadeMusic.set(Calendar.HOUR_OF_DAY, 16);
                } else if (evening) {
                    calendarFadeMusic.set(Calendar.HOUR_OF_DAY, 20);
                } else if (night) {
                    if (calendarFadeMusic.get(Calendar.AM_PM) == Calendar.PM) {
                        calendarFadeMusic.add(Calendar.DAY_OF_MONTH, 1);
                    }
                    calendarFadeMusic.set(Calendar.HOUR_OF_DAY, 6);
                }
                calendarFadeMusic.add(Calendar.SECOND, -5);

                timeInMillisFadeMusic = calendarFadeMusic.getTimeInMillis();

                fadeMusicIntent = new Intent(context, ACMusicBroadcastReceiver.class);
                fadeMusicIntent.setAction("ACTION_FADEOUT");
                pendingIntentFadeMusic = PendingIntent.getBroadcast(context, FADE_MUSIC_REQUESTCODE, fadeMusicIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                alarmManagerFadeMusic = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                alarmManagerFadeMusic.cancel(pendingIntentFadeMusic);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManagerFadeMusic.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillisFadeMusic, pendingIntentFadeMusic);
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    alarmManagerFadeMusic.setExact(AlarmManager.RTC_WAKEUP, timeInMillisFadeMusic, pendingIntentFadeMusic);
                } else {
                    alarmManagerFadeMusic.set(AlarmManager.RTC_WAKEUP, timeInMillisFadeMusic, pendingIntentFadeMusic);
                }
                break;
            case "ACTION_FADEOUT":
                ACMusicMediaPlayer.fadeout();
                break;
        }
    }

    public NotificationCompat.Builder showNotification(Context context, String content) {

        return new NotificationCompat.Builder(context, context.getString(R.string.CHANNEL_ID))
                .setContentTitle("Downloading assets")
                .setProgress(100, HomeActivity.progress, false)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_LOW);
    }
}
