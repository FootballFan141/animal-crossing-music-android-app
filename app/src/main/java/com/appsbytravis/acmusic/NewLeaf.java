package com.appsbytravis.acmusic;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;

import com.appsbytravis.acmusic.utils.ACMusic;
import com.appsbytravis.acmusic.utils.ACMusicMediaPlayer;

import java.io.File;
import java.util.Calendar;

public class NewLeaf extends AppCompatActivity {
    private static final String ASSETS_PATH = "/assets/newleaf/";
    //    private PendingIntent pendingIntent;
    //    private AlarmManager alarmManager;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_leaf);

        preparations();

        final Button pauseBtn = findViewById(R.id.PauseBtn);
        pauseBtn.setOnClickListener(v -> {
            if (ACMusicMediaPlayer.isPlaying()) {
                pauseBtn.setText("Resume");
                ACMusicMediaPlayer.pause();
            } else {
                pauseBtn.setText("Pause");
                ACMusicMediaPlayer.start();
            }
        });

    }


    private void preparations() {

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        Calendar calendar = setCalendar();
        File file = getMusic(calendar.get(Calendar.HOUR_OF_DAY));

//        TODO: Fix music updating every hour, problem is it random starts even when app is not running.
//        calendar.add(Calendar.HOUR_OF_DAY, 1);
//        Storage storage = new Storage(this);
//        long timeInMillis = calendar.getTimeInMillis();
//        Intent changeMusicIntent = new Intent(this, ACMusicBroadcastReceiver.class);
//        changeMusicIntent.setAction("ACTION_UPDATE_MUSIC:NL");
//        changeMusicIntent.putExtra("file", storage.getFile(file.getPath()).toURI());
//        pendingIntent = PendingIntent.getBroadcast(this, CHANGE_MUSIC_REQUESTCODE, changeMusicIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//        alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
//        alarmManager.set(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);

        if (!ACMusicMediaPlayer.isPlaying()) {
            ACMusicMediaPlayer.play(this, Uri.parse(file.getPath()));
            ACMusicMediaPlayer.start();
        }

//        Intent intent = new Intent(getBaseContext(), ACMusicService.class);
//        intent.putExtra("pendingIntent", pendingIntent);
//        startService(intent);
    }

    private File getMusic(int hour) {

        ACMusic music = new ACMusic(getApplicationContext(), ASSETS_PATH);
        music.setWindow(this);

        boolean rain = prefs.getBoolean("raining", false);
        boolean snow = prefs.getBoolean("snowing", false);
        boolean normal = prefs.getBoolean("normal", false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            music.bgChange(hour);
        }
        File file;
        if (normal) {
            file = music.normal(hour);
        } else if (rain) {
            file = music.raining(hour);
        } else if (snow) {
            file = music.snowing(hour);
        } else {
            prefs.edit().putBoolean("normal", true).apply();
            file = music.normal(hour);
        }
        return file;
    }

    private Calendar setCalendar() {
        Calendar calendar = Calendar.getInstance();

        calendar.set(calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.HOUR_OF_DAY), 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ACMusicMediaPlayer.stop();
//        Intent intent = new Intent(getBaseContext(), ACMusicService.class);
//        intent.putExtra("pendingIntent", pendingIntent);
//        stopService(intent);
//        if (pendingIntent != null) {
//            alarmManager.cancel(pendingIntent);
//            pendingIntent.cancel();
//            pendingIntent = null;
//        }
    }

    @Override
    protected void onStop() {
        super.onStop();
//        if (!ACMusicMediaPlayer.isPlaying()) {
//            Intent intent = new Intent(getBaseContext(), ACMusicService.class);
//            intent.putExtra("pendingIntent", pendingIntent);
//            stopService(intent);
//            if (pendingIntent != null) {
//                alarmManager.cancel(pendingIntent);
//                pendingIntent.cancel();
//                pendingIntent = null;
//            }
//        }
    }

    @Override
    protected void onStart() {
        super.onStart();
//        if (pendingIntent == null) {
//            if (!ACMusicMediaPlayer.isPlaying()) {
//                preparations();
//                ACMusicMediaPlayer.pause();
//            }
//            Intent intent = new Intent(getBaseContext(), ACMusicService.class);
//            intent.putExtra("pendingIntent", pendingIntent);
//            startService(intent);
//        }
    }

    @Override
    protected void onPause() {
        super.onPause();
//        Intent intent = new Intent(getApplicationContext(), ACMusicService.class);
//        intent.putExtra("pendingIntent", pendingIntent);
//        stopService(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
//        Intent intent = new Intent(getApplicationContext(), ACMusicService.class);
//        intent.putExtra("pendingIntent", pendingIntent);
//        startService(intent);
    }
}