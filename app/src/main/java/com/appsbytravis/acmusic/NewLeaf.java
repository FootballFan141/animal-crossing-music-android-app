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
import com.snatik.storage.Storage;

import java.io.File;
import java.util.Calendar;

public class NewLeaf extends AppCompatActivity {
    private static final String ASSETS_PATH = "/assets/newleaf/";
    private Storage storage;
    private PendingIntent pendingIntent;
    private AlarmManager alarmManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_leaf);

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
            file = music.raining(hour);
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
        changeMusicIntent.setAction("ACTION_UPDATE_MUSIC:NL");
        changeMusicIntent.putExtra("file", storage.getFile(file.getPath()).toURI());
        pendingIntent = PendingIntent.getBroadcast(this, 0, changeMusicIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC, timeInMillis, timeInMillis, pendingIntent);
        if (!ACMusicMediaPlayer.isPlaying()) {
            ACMusicMediaPlayer.play(this, Uri.parse(file.getPath()));
        }

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        ACMusicMediaPlayer.stop();
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
        }
    }
}