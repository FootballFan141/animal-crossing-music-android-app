package com.appsbytravis.acmusic.utils;

import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;

public interface AssetsInterface {

    void downloadAssets(String filename, String destinationPath);

    void pauseDownload();

    void extractAssets(String filename, String size);

    void downloadAlert(String gameid);

    Snackbar createSnackbar(String message, int duration, String gameid);

    NotificationCompat.Builder showNotification(String content);
}
