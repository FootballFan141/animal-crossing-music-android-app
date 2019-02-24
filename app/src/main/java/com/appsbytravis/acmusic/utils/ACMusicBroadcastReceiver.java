package com.appsbytravis.acmusic.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.appsbytravis.acmusic.HomeActivity;
import com.appsbytravis.acmusic.R;

import java.io.File;
import java.util.Calendar;

public class ACMusicBroadcastReceiver extends BroadcastReceiver {


    private PendingIntent pendingIntent;
    private AlarmManager alarmManager;
    private int hour;
    private String ASSETS_PATH;
    private ACMusic music;
    private File file;
    private Intent changeMusicIntent;
    private String path;

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        Calendar calendar = Calendar.getInstance();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Intent serviceIntent;

        boolean rain = prefs.getBoolean("raining", false);
        boolean snow = prefs.getBoolean("snowing", false);
        boolean normal = prefs.getBoolean("normal", false);


        switch (intent.getAction()) {
            case "ACTION_START":

                break;
            case "ACTION_CANCEL":
                manager.cancel(R.string.NOTIFICATION_MAIN);
                HomeActivity.firebasetask.cancel();
                break;
            case "ACTION_PAUSE":
                HomeActivity.firebasetask.pause();
                break;
            case "ACTION_RESUME":
                HomeActivity.firebasetask.resume();

                Intent pauseDownloadIntent = new Intent(context, ACMusicBroadcastReceiver.class);
                pauseDownloadIntent.setAction("ACTION_PAUSE");
                Intent cancelDownloadIntent = new Intent(context, ACMusicBroadcastReceiver.class);
                cancelDownloadIntent.setAction("ACTION_CANCEL");
                PendingIntent cancelDownloadPendingIntent =
                        PendingIntent.getBroadcast(context, 0, cancelDownloadIntent, 0);
                PendingIntent pauseDownloadPendingIntent =
                        PendingIntent.getBroadcast(context, 0, pauseDownloadIntent, 0);
                NotificationCompat.Builder builder = new NotificationCompat.Builder(context, context.getString(R.string.CHANNEL_ID));
                builder.setContentTitle("Downloading assets");
                builder.setProgress(100, HomeActivity.progress, false);
                builder.setContentText("This won't take long. :)");
                builder.setSmallIcon(android.R.drawable.stat_sys_download);
                builder.addAction(android.R.drawable.ic_menu_close_clear_cancel, "Cancel", cancelDownloadPendingIntent);
                builder.setContentIntent(cancelDownloadPendingIntent);
                builder.addAction(android.R.drawable.ic_media_pause, "Pause", pauseDownloadPendingIntent);
                builder.setContentIntent(pauseDownloadPendingIntent);
                manager.notify(R.string.NOTIFICATION_MAIN, builder.build());
                break;
            case "ACTION_UPDATE_MUSIC:GC":
                hour = calendar.get(Calendar.HOUR_OF_DAY);
                calendar.set(calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH),
                        calendar.get(Calendar.HOUR_OF_DAY), 0, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                calendar.add(Calendar.HOUR_OF_DAY, 1);
                Log.d(HomeActivity.TAG, "ACTION_UPDATE_MUSIC:GC");
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
                ACMusicMediaPlayer.start();

                changeMusicIntent = new Intent(context, ACMusicBroadcastReceiver.class);
                changeMusicIntent.setAction("ACTION_UPDATE_MUSIC:GC");
                pendingIntent = PendingIntent.getBroadcast(context, 0, changeMusicIntent, PendingIntent.FLAG_CANCEL_CURRENT);
                alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                alarmManager.cancel(pendingIntent);
                alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);

                serviceIntent = new Intent(context, ACMusicService.class);
                intent.putExtra("pendingIntent", pendingIntent);
                context.startService(serviceIntent);
                break;
            case "ACTION_UPDATE_MUSIC:WWCF":
                hour = calendar.get(Calendar.HOUR_OF_DAY);
                calendar.set(calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH),
                        calendar.get(Calendar.HOUR_OF_DAY), 0, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                calendar.add(Calendar.HOUR_OF_DAY, 1);
                Log.d(HomeActivity.TAG, "ACTION_UPDATE_MUSIC:WWCF");
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
                ACMusicMediaPlayer.start();

                changeMusicIntent = new Intent(context, ACMusicBroadcastReceiver.class);
                changeMusicIntent.setAction("ACTION_UPDATE_MUSIC:WWCF");
                pendingIntent = PendingIntent.getBroadcast(context, 0, changeMusicIntent, PendingIntent.FLAG_CANCEL_CURRENT);
                alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                alarmManager.cancel(pendingIntent);
                alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);

                serviceIntent = new Intent(context, ACMusicService.class);
                intent.putExtra("pendingIntent", pendingIntent);
                context.startService(serviceIntent);
                break;
            case "ACTION_UPDATE_MUSIC:NL":
                hour = calendar.get(Calendar.HOUR_OF_DAY);
                calendar.set(calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH),
                        calendar.get(Calendar.HOUR_OF_DAY), 0, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                calendar.add(Calendar.HOUR_OF_DAY, 1);
                Log.d(HomeActivity.TAG, "ACTION_UPDATE_MUSIC:NL");
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
                ACMusicMediaPlayer.start();

                changeMusicIntent = new Intent(context, ACMusicBroadcastReceiver.class);
                changeMusicIntent.setAction("ACTION_UPDATE_MUSIC:NL");
                pendingIntent = PendingIntent.getBroadcast(context, 0, changeMusicIntent, PendingIntent.FLAG_CANCEL_CURRENT);
                alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                alarmManager.cancel(pendingIntent);
                alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);

                serviceIntent = new Intent(context, ACMusicService.class);
                intent.putExtra("pendingIntent", pendingIntent);
                context.startService(serviceIntent);
                break;
        }
    }
}
