package com.appsbytravis.acmusic.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.IOException;

public class ACMusicMediaPlayer {

    private static MediaPlayer player;

    public static MediaPlayer getMediaPlayer() {
        return player;
    }

    public static void play(Context context, Uri file) {
        if (player == null) {
            player = MediaPlayer.create(context, file);

            if (player == null) {

                player = new MediaPlayer();
                try {
                    FileInputStream inputStream = new FileInputStream(file.getPath());
                    player.setDataSource(inputStream.getFD());
                    inputStream.close();
                    player.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(context, "Problem with music...", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        }
        player.setLooping(true);
    }

    public static void stop() {
        if (player != null) {
            player.stop();
            player.reset();
            player.release();
            player = null;
        }
    }

    public static void start() {
        if (player != null) player.start();
    }

    public static void pause() {
        if (player != null) player.pause();

    }

    public static boolean isPlaying() {
        return player != null && player.isPlaying();
    }

    @TargetApi(Build.VERSION_CODES.O)
    private static void fade() {
        //TODO; Create fade effect at the end of current audio
    }

    public static void adjustVolume(boolean focused) {
        if (player != null) {
            if (!focused) {
                player.setVolume(0.2f, 0.2f);
            } else {
                player.setVolume(1.0f, 1.0f);
            }
        }
    }
}
