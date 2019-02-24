package com.appsbytravis.acmusic;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;

import com.appsbytravis.acmusic.utils.ACMusic;
import com.appsbytravis.acmusic.utils.ACMusicBroadcastReceiver;
import com.appsbytravis.acmusic.utils.ACMusicMediaPlayer;
import com.appsbytravis.acmusic.utils.ACMusicService;
import com.snatik.storage.Storage;

import java.io.File;
import java.util.Calendar;

public class Gamecube extends AppCompatActivity {
    private static final String ASSETS_PATH = "/assets/gamecube/";
    private Storage storage;
    private PendingIntent pendingIntent;
    private AlarmManager alarmManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gamecube);
        storage = new Storage(this);
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

        ACMusic music = new ACMusic(getApplicationContext(), ASSETS_PATH);
        music.setWindow(this);
        Calendar calendar = Calendar.getInstance();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        boolean rain = prefs.getBoolean("raining", false);
        boolean snow = prefs.getBoolean("snowing", false);
        boolean normal = prefs.getBoolean("normal", false);

        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            music.bgChange(hour);
        }
        File file;
        if (normal) {
            file = music.normal(hour);
        } else if (rain) {
            file = music.rainingGC();
        } else if (snow) {
            file = music.snowing(hour);
        } else {
            prefs.edit().putBoolean("normal", true).apply();
            file = music.normal(hour);
        }

        calendar.set(calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.HOUR_OF_DAY), 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        long timeInMillis = calendar.getTimeInMillis();


        Intent changeMusicIntent = new Intent(this, ACMusicBroadcastReceiver.class);
        changeMusicIntent.setAction("ACTION_UPDATE_MUSIC:GC");
        changeMusicIntent.putExtra("file", storage.getFile(file.getPath()).toURI());
        pendingIntent = PendingIntent.getBroadcast(this, 0, changeMusicIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
        if (!ACMusicMediaPlayer.isPlaying()) {
            ACMusicMediaPlayer.play(this, Uri.parse(file.getPath()));
            ACMusicMediaPlayer.start();
        }

        Intent intent = new Intent(getBaseContext(), ACMusicService.class);
        intent.putExtra("pendingIntent", pendingIntent);
        startService(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ACMusicMediaPlayer.stop();
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
        Intent intent = new Intent(getBaseContext(), ACMusicService.class);
        intent.putExtra("pendingIntent", pendingIntent);
        stopService(intent);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!ACMusicMediaPlayer.isPlaying()) {
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent);
                pendingIntent.cancel();
                pendingIntent = null;
            }
            Intent intent = new Intent(getBaseContext(), ACMusicService.class);
            intent.putExtra("pendingIntent", pendingIntent);
            stopService(intent);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (pendingIntent == null) {
            if (!ACMusicMediaPlayer.isPlaying()) {
                preparations();
                ACMusicMediaPlayer.pause();
            }
            Intent intent = new Intent(getBaseContext(), ACMusicService.class);
            intent.putExtra("pendingIntent", pendingIntent);
            startService(intent);
        }
    }
}