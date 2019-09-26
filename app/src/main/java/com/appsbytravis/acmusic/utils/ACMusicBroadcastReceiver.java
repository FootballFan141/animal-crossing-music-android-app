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

import static com.appsbytravis.acmusic.utils.Constants.CANCEL_DOWNLOAD_REQUESTCODE;
import static com.appsbytravis.acmusic.utils.Constants.CHANGE_MUSIC_REQUESTCODE;
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
                ACMusicMediaPlayer.start();

                changeMusicIntent = new Intent(context, ACMusicBroadcastReceiver.class);
                changeMusicIntent.setAction("ACTION_UPDATE_MUSIC:GC");
                pendingIntent = PendingIntent.getBroadcast(context, CHANGE_MUSIC_REQUESTCODE, changeMusicIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                alarmManager.cancel(pendingIntent);
                alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
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
                ACMusicMediaPlayer.start();

                changeMusicIntent = new Intent(context, ACMusicBroadcastReceiver.class);
                changeMusicIntent.setAction("ACTION_UPDATE_MUSIC:WWCF");
                pendingIntent = PendingIntent.getBroadcast(context, CHANGE_MUSIC_REQUESTCODE, changeMusicIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                alarmManager.cancel(pendingIntent);
                alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
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
                ACMusicMediaPlayer.start();

                changeMusicIntent = new Intent(context, ACMusicBroadcastReceiver.class);
                changeMusicIntent.setAction("ACTION_UPDATE_MUSIC:NL");
                pendingIntent = PendingIntent.getBroadcast(context, CHANGE_MUSIC_REQUESTCODE, changeMusicIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                alarmManager.cancel(pendingIntent);
                alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
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
