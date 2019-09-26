package com.appsbytravis.acmusic;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.Toast;

import com.appsbytravis.acmusic.utils.ACMusic;
import com.appsbytravis.acmusic.utils.ACMusicBroadcastReceiver;
import com.appsbytravis.acmusic.utils.ACMusicMediaPlayer;
import com.appsbytravis.acmusic.utils.ACMusicService;
import com.snatik.storage.Storage;

import java.io.File;
import java.util.Calendar;

import static com.appsbytravis.acmusic.utils.Constants.CHANGE_MUSIC_REQUESTCODE;

public class NewLeaf extends AppCompatActivity {
    private static final String ASSETS_PATH = "/assets/newleaf/";
    private PendingIntent pendingIntent;
    private AlarmManager alarmManager;
    private SharedPreferences prefs;
    private AudioManager audioManager;
    private AudioFocusRequest afrBuilder;
    private AudioManager.OnAudioFocusChangeListener focusChangeListener;
    private boolean audioAuthorized = false;
    private Button pauseBtn;
    private boolean isPaused = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_leaf);

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        focusChangeListener = focusChange -> {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN:
                    audioAuthorized = true;
                    ACMusicMediaPlayer.adjustVolume(true);
                    ACMusicMediaPlayer.start();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS:
                    audioAuthorized = false;
                    if (ACMusicMediaPlayer.isPlaying()) {
                        ACMusicMediaPlayer.stop();
                    }
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    audioAuthorized = false;
                    if (ACMusicMediaPlayer.isPlaying()) {
                        ACMusicMediaPlayer.pause();
                    }
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    audioAuthorized = false;
                    if (ACMusicMediaPlayer.isPlaying()) {
                        ACMusicMediaPlayer.adjustVolume(false);
                    }
                    break;
            }
        };

        audioFocus();
        preparations();
    }

    private void audioFocus() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build();

            afrBuilder = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setFocusGain(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(audioAttributes)
                    .setWillPauseWhenDucked(true)
                    .setAcceptsDelayedFocusGain(true)
                    .setOnAudioFocusChangeListener(focusChangeListener).build();
            int result = audioManager.requestAudioFocus(afrBuilder);
            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                audioAuthorized = true;
            } else if (result == AudioManager.AUDIOFOCUS_REQUEST_FAILED) {
                audioAuthorized = false;
            }
        } else {
            int result = audioManager.requestAudioFocus(focusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                audioAuthorized = true;
            } else if (result == AudioManager.AUDIOFOCUS_REQUEST_FAILED) {
                audioAuthorized = false;
            }
        }

    }

    private void preparations() {

        pauseBtn = findViewById(R.id.PauseBtn);
        pauseBtn.setOnClickListener(v -> {
            if (ACMusicMediaPlayer.isPlaying()) {
                pauseBtn.setText("Resume");
                isPaused = true;
                ACMusicMediaPlayer.pause();
            } else {
                pauseBtn.setText("Pause");
                isPaused = false;
                ACMusicMediaPlayer.start();
            }
        });

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Calendar calendar = setCalendar();
        File file = getMusic(calendar.get(Calendar.HOUR_OF_DAY));

//        TODO: Fix music updating every hour, problem is it random starts even when app is not running.
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        Storage storage = new Storage(this);
        long timeInMillis = calendar.getTimeInMillis();
        Intent changeMusicIntent = new Intent(this, ACMusicBroadcastReceiver.class);
        changeMusicIntent.setAction("ACTION_UPDATE_MUSIC:NL");
        changeMusicIntent.putExtra("file", storage.getFile(file.getPath()).toURI());
        pendingIntent = PendingIntent.getBroadcast(this, CHANGE_MUSIC_REQUESTCODE, changeMusicIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);


        if (!ACMusicMediaPlayer.isPlaying() && audioAuthorized) {
            ACMusicMediaPlayer.play(this, Uri.parse(file.getPath()));
            if (isPaused) {
                ACMusicMediaPlayer.pause();
            } else {
                ACMusicMediaPlayer.start();
            }
        } else {
            Toast.makeText(getApplicationContext(), "Another app is possibly playing music.", Toast.LENGTH_SHORT).show();
        }

        Intent intent = new Intent(getBaseContext(), ACMusicService.class);
        intent.putExtra("pendingIntent", pendingIntent);
        startService(intent);
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
        Intent intent = new Intent(getBaseContext(), ACMusicService.class);
        intent.putExtra("pendingIntent", pendingIntent);
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
            pendingIntent = null;
        }
        stopService(intent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioManager.abandonAudioFocusRequest(afrBuilder);
        } else {
            audioManager.abandonAudioFocus(focusChangeListener);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!ACMusicMediaPlayer.isPlaying()) {
            Intent intent = new Intent(getBaseContext(), ACMusicService.class);
            intent.putExtra("pendingIntent", pendingIntent);
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent);
                pendingIntent.cancel();
                pendingIntent = null;
            }
            stopService(intent);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!audioAuthorized) {
            audioFocus();
            preparations();
            if (ACMusicMediaPlayer.isPlaying()) {
                isPaused = false;
            }
        }
        if (pendingIntent == null) {
            if (!ACMusicMediaPlayer.isPlaying()) {
                ACMusicMediaPlayer.pause();
                pauseBtn.setText("Resume");
                isPaused = true;
            }
            preparations();
            Intent intent = new Intent(getBaseContext(), ACMusicService.class);
            intent.putExtra("pendingIntent", pendingIntent);
            startService(intent);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = new Intent(getApplicationContext(), ACMusicService.class);
        intent.putExtra("pendingIntent", pendingIntent);
        startService(intent);
    }
}