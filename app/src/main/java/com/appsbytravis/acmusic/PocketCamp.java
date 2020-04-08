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
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.appsbytravis.acmusic.utils.ACMusic;
import com.appsbytravis.acmusic.utils.ACMusicBroadcastReceiver;
import com.appsbytravis.acmusic.utils.ACMusicMediaPlayer;
import com.appsbytravis.acmusic.utils.ACMusicService;
import com.snatik.storage.Storage;

import java.io.File;
import java.util.Calendar;

import static com.appsbytravis.acmusic.utils.Constants.CHANGE_MUSIC_REQUESTCODE;
import static com.appsbytravis.acmusic.utils.Constants.FADE_MUSIC_REQUESTCODE;

public class PocketCamp extends AppCompatActivity {
    private static final String ASSETS_PATH = "/assets/pocketcamp/";
    private PendingIntent pendingIntent;
    private AlarmManager alarmManager;
    private SharedPreferences prefs;
    private AudioManager audioManager;
    private AudioFocusRequest afrBuilder;
    private AudioManager.OnAudioFocusChangeListener focusChangeListener;
    private boolean audioAuthorized = false;
    private Button pauseBtn;
    private boolean isPaused = false;
    private AlarmManager alarmManagerFadeMusic;
    private PendingIntent pendingIntentFadeMusic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pocket_camp);

        audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);

        focusChangeListener = focusChange -> {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN:
                    audioAuthorized = true;
                    if (!isPaused) {
                        ACMusicMediaPlayer.adjustVolume(true);
                        ACMusicMediaPlayer.start();
                    }
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
                pauseBtn.setText(getString(R.string.resume_music));
                ACMusicMediaPlayer.pause();
            } else {
                pauseBtn.setText(getString(R.string.pause_music));
                ACMusicMediaPlayer.start();
            }
        });
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        Calendar calendar = setCalendar();

        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        File file = getMusic(hour);

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

        Storage storage = new Storage(this);
        long timeInMillis = calendar.getTimeInMillis();
        Intent changeMusicIntent = new Intent(this, ACMusicBroadcastReceiver.class);
        changeMusicIntent.setAction("ACTION_UPDATE_MUSIC:PocketCamp");
        changeMusicIntent.putExtra("file", storage.getFile(file.getPath()).toURI());
        pendingIntent = PendingIntent.getBroadcast(this, CHANGE_MUSIC_REQUESTCODE, changeMusicIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
        }

        Calendar calendarFadeMusic = setCalendar();
        int minute = calendarFadeMusic.get(Calendar.MINUTE);
        int seconds = calendarFadeMusic.get(Calendar.SECOND);
        boolean shouldfade = (minute == 59) && (seconds >= 55);

        if (!ACMusicMediaPlayer.isPlaying() && audioAuthorized) {
            ACMusicMediaPlayer.play(this, Uri.parse(file.getPath()));
            if (shouldfade) {
                ACMusicMediaPlayer.fadeout();
            } else {
                ACMusicMediaPlayer.getMediaPlayer().setVolume(1.0f, 1.0f);
            }
            if (isPaused) {
                ACMusicMediaPlayer.pause();
            } else {
                ACMusicMediaPlayer.start();
            }
        } else {
            Toast.makeText(getApplicationContext(), "Another app is possibly playing music.", Toast.LENGTH_SHORT).show();
        }

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

        long timeInMillisFadeMusic = calendarFadeMusic.getTimeInMillis();

        Intent fadeMusicIntent = new Intent(this, ACMusicBroadcastReceiver.class);
        fadeMusicIntent.setAction("ACTION_FADEOUT");
        pendingIntentFadeMusic = PendingIntent.getBroadcast(this, FADE_MUSIC_REQUESTCODE, fadeMusicIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManagerFadeMusic = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmManagerFadeMusic.cancel(pendingIntentFadeMusic);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManagerFadeMusic.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillisFadeMusic, pendingIntentFadeMusic);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManagerFadeMusic.setExact(AlarmManager.RTC_WAKEUP, timeInMillisFadeMusic, pendingIntentFadeMusic);
        } else {
            alarmManagerFadeMusic.set(AlarmManager.RTC_WAKEUP, timeInMillisFadeMusic, pendingIntentFadeMusic);
        }


        Intent intent = new Intent(getApplicationContext(), ACMusicService.class);
        intent.putExtra("pendingIntent", pendingIntent);
        intent.putExtra("pendingIntentFadeMusic", pendingIntentFadeMusic);

        startService(intent);

    }

    private File getMusic(int hour) {

        ACMusic music = new ACMusic(getApplicationContext(), ASSETS_PATH);
        music.setWindow(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            music.bgChange(hour);
        }
        File file;
        file = music.pocketcamp(hour);
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
        stopService(intent);
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
            pendingIntent = null;
        }
        if (pendingIntentFadeMusic != null) {
            alarmManagerFadeMusic.cancel(pendingIntentFadeMusic);
            pendingIntentFadeMusic.cancel();
            pendingIntentFadeMusic = null;
        }
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
            stopService(intent);
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent);
                pendingIntent.cancel();
                pendingIntent = null;
            }
            if (pendingIntentFadeMusic != null) {
                alarmManagerFadeMusic.cancel(pendingIntentFadeMusic);
                pendingIntentFadeMusic.cancel();
                pendingIntentFadeMusic = null;
            }
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
                pauseBtn.setText(getString(R.string.resume_music));
                isPaused = true;
            }
            preparations();
            Intent intent = new Intent(this, ACMusicService.class);
            intent.putExtra("pendingIntent", pendingIntent);
            intent.putExtra("pendingIntent", pendingIntentFadeMusic);
            startService(intent);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!ACMusicMediaPlayer.isPlaying()) {
            Intent intent = new Intent(getBaseContext(), ACMusicService.class);
            intent.putExtra("pendingIntent", pendingIntent);
            stopService(intent);
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent);
                pendingIntent.cancel();
                pendingIntent = null;
            }
            if (pendingIntentFadeMusic != null) {
                alarmManagerFadeMusic.cancel(pendingIntentFadeMusic);
                pendingIntentFadeMusic.cancel();
                pendingIntentFadeMusic = null;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = new Intent(getApplicationContext(), ACMusicService.class);
        intent.putExtra("pendingIntent", pendingIntent);
        intent.putExtra("pendingIntentFadeMusic", pendingIntentFadeMusic);
        startService(intent);
    }
}