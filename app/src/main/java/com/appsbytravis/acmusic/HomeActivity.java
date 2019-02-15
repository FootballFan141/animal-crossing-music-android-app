package com.appsbytravis.acmusic;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.appsbytravis.acmusic.utils.ACMusicBroadcastReceiver;
import com.appsbytravis.acmusic.utils.AdListeners;
import com.appsbytravis.acmusic.utils.AssetsInterface;
import com.appsbytravis.acmusic.utils.ZipTool;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.snatik.storage.Storage;

public class HomeActivity extends AppCompatActivity implements AssetsInterface {

    public static final String TAG = "ACMUSIC";

    private static final String ASSETS_PATH = "assets/";
    private static final int GAMECUBE_FILES = 49;
    private static final int WWCF_FILES = 72;
    private static final int NEWLEAF_FILES = 72;

    private static SharedPreferences prefs = null;
    private Storage storage;
    private String path;
    private HomeActivity instance;


    private static final String[] ASSET_FILES = new String[]{
            "gamecube.zip",
            "wwcf.zip",
            "newleaf.zip"
    };

    private static final String[] ASSET_SIZES = new String[]{
//            "486.61", // gamecube
//            "225.59", // wwcf
//            "245.79"  // newleaf¨
            "197.47", // gamecube
            "64.90", // wwcf
            "74.40"  // newleaf

    };

    private NotificationManagerCompat manager;
    private AdRequest.Builder AdRequestBuilder;
    private InterstitialAd mInterstitialAd;

    public Button gamecubeBtn;
    public Button wwcfBtn;
    public Button newleafBtn;
    public boolean prepareFinished;
    public MenuItem cancelBtn;
    public static FileDownloadTask firebasetask;
    public static int progress = 0;
    public boolean isPreparing = false;
    public ProgressBar progressBar;
    public Button pauseDownloadBtn;

    public HomeActivity getInstance() {
        if (instance == null) {
            instance = this;
        }
        return instance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        instance = getInstance();


        AdView bannerAd = findViewById(R.id.adView);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);
        pauseDownloadBtn = findViewById(R.id.pauseDownloadBtn);
        gamecubeBtn = findViewById(R.id.gamecubeBtn);
        wwcfBtn = findViewById(R.id.wwcfBtn);
        newleafBtn = findViewById(R.id.newleafBtn);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        manager = NotificationManagerCompat.from(this);
        storage = new Storage(this);
        path = storage.getInternalFilesDirectory() + "/";


        if (isNetworkConnected()) {
            if (bannerAd.getVisibility() == View.GONE) {
                bannerAd.setVisibility(View.VISIBLE);
            }
            bannerAd.setVisibility(View.VISIBLE);
            initializeAds();
            prepareInterstitialAd();
            bannerAd.loadAd(AdRequestBuilder.build());
        } else {
            bannerAd.setVisibility(View.GONE);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel();
        }

        pauseDownloadBtn.setOnClickListener(view -> pauseDownload());

        gamecubeBtn.setOnClickListener(view -> {
            int files = storage.getNestedFiles(path.concat(ASSETS_PATH.concat("gamecube"))).size();
            if (storage.isDirectoryExists(path.concat(ASSETS_PATH).concat("gamecube")) && files == GAMECUBE_FILES) {
                if (storage.isFileExist(path.concat(ASSET_FILES[0]))) {
                    storage.deleteFile(path.concat(ASSET_FILES[0]));
                }
                if (isNetworkConnected()) {
                    if (mInterstitialAd.isLoaded()) {
                        mInterstitialAd.setAdListener(new AdListeners(this, "gamecube"));
                        mInterstitialAd.show();
                    } else {
                        Intent i = new Intent(HomeActivity.this, Gamecube.class);
                        startActivity(i);
                    }
                } else {
                    Intent i = new Intent(HomeActivity.this, Gamecube.class);
                    startActivity(i);
                }
            } else {
                storage.deleteDirectory(path.concat(ASSETS_PATH).concat("gamecube"));
                if (storage.isFileExist(path.concat(ASSET_FILES[0]))) {
                    view.setEnabled(false);
                    wwcfBtn.setEnabled(false);
                    newleafBtn.setEnabled(false);
                    extractAssets(ASSET_FILES[0], ASSET_SIZES[0]);
                } else {
                    if (!isNetworkConnected()) {
                        Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Snackbar snackbar = createSnackbar(getString(R.string.assets_alert_msg), Snackbar.LENGTH_LONG, "gamecube");
                    snackbar.show();
                }
            }
        });
        wwcfBtn.setOnClickListener(view -> {
            int files = storage.getNestedFiles(path.concat(ASSETS_PATH.concat("wwcf"))).size();
            if (storage.isDirectoryExists(path.concat(ASSETS_PATH).concat("wwcf")) && files == WWCF_FILES) {
                if (storage.isFileExist(path.concat(ASSET_FILES[1]))) {
                    storage.deleteFile(path.concat(ASSET_FILES[1]));
                }
                if (isNetworkConnected()) {
                    if (mInterstitialAd.isLoaded()) {
                        mInterstitialAd.setAdListener(new AdListeners(this, "wwcf"));
                        mInterstitialAd.show();
                    } else {
                        Intent i = new Intent(HomeActivity.this, WildWorldCityFolk.class);
                        startActivity(i);
                    }
                } else {
                    Intent i = new Intent(HomeActivity.this, WildWorldCityFolk.class);
                    startActivity(i);
                }
            } else {
                storage.deleteDirectory(path.concat(ASSETS_PATH).concat("wwcf"));
                if (storage.isFileExist(path.concat(ASSET_FILES[1]))) {
                    view.setEnabled(false);
                    gamecubeBtn.setEnabled(false);
                    newleafBtn.setEnabled(false);
                    extractAssets(ASSET_FILES[1], ASSET_SIZES[1]);
                } else {
                    if (!isNetworkConnected()) {
                        Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Snackbar snackbar = createSnackbar(getString(R.string.assets_alert_msg), Snackbar.LENGTH_LONG, "wwcf");
                    snackbar.show();
                }
            }
        });
        newleafBtn.setOnClickListener(view -> {
            int files = storage.getNestedFiles(path.concat(ASSETS_PATH.concat("newleaf"))).size();
            if (storage.isDirectoryExists(path.concat(ASSETS_PATH).concat("newleaf")) && files == NEWLEAF_FILES) {
                if (storage.isFileExist(path.concat(ASSET_FILES[2]))) {
                    storage.deleteFile(path.concat(ASSET_FILES[2]));
                }
                if (isNetworkConnected()) {
                    if (mInterstitialAd.isLoaded()) {
                        mInterstitialAd.setAdListener(new AdListeners(this, "newleaf"));
                        mInterstitialAd.show();
                    } else {
                        Intent i = new Intent(HomeActivity.this, NewLeaf.class);
                        startActivity(i);
                    }
                } else {
                    Intent i = new Intent(HomeActivity.this, NewLeaf.class);
                    startActivity(i);
                }
            } else {
                storage.deleteDirectory(path.concat(ASSETS_PATH).concat("newleaf"));
                if (storage.isFileExist(path.concat(ASSET_FILES[2]))) {
                    view.setEnabled(false);
                    gamecubeBtn.setEnabled(false);
                    wwcfBtn.setEnabled(false);
                    extractAssets(ASSET_FILES[2], ASSET_SIZES[2]);
                } else {
                    if (!isNetworkConnected()) {
                        Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Snackbar snackbar = createSnackbar(getString(R.string.assets_alert_msg), Snackbar.LENGTH_LONG, "newleaf");
                    snackbar.show();
                }
            }
        });
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        boolean raining = prefs.getBoolean("raining", false);
        boolean snowing = prefs.getBoolean("snowing", false);
        boolean normal = prefs.getBoolean("normal", false);

        MenuItem normalWeather = menu.findItem(R.id.normal);
        MenuItem rainWeather = menu.findItem(R.id.raining);
        MenuItem snowWeather = menu.findItem(R.id.snowing);
        cancelBtn = menu.findItem(R.id.menuCancel);

        if (normal) {
            normalWeather.setChecked(true);
            rainWeather.setChecked(false);
            snowWeather.setChecked(false);
        } else if (raining) {
            rainWeather.setChecked(true);
            normalWeather.setChecked(false);
            snowWeather.setChecked(false);
        } else if (snowing) {
            snowWeather.setChecked(true);
            normalWeather.setChecked(false);
            rainWeather.setChecked(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        SharedPreferences.Editor editor = prefs.edit();

        switch (item.getItemId()) {
            case R.id.licenses:
                startActivity(new Intent(this, OssLicensesMenuActivity.class));
                return true;
            case R.id.raining:
                editor.putBoolean("normal", false);
                editor.putBoolean("raining", true);
                editor.putBoolean("snowing", false);
                editor.apply();
                invalidateOptionsMenu();

                return true;

            case R.id.snowing:
                editor.putBoolean("normal", false);
                editor.putBoolean("raining", false);
                editor.putBoolean("snowing", true);
                editor.apply();
                invalidateOptionsMenu();

                return true;

            case R.id.normal:
                editor.putBoolean("normal", true);
                editor.putBoolean("raining", false);
                editor.putBoolean("snowing", false);
                editor.apply();
                invalidateOptionsMenu();

                return true;
            case R.id.menuCancel:
                isPreparing = false;
                if (firebasetask != null) {
                    firebasetask.cancel();
                }
                Toast.makeText(this, "Canceling...", Toast.LENGTH_SHORT).show();
                item.setVisible(false);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isPreparing = false;
        if (firebasetask != null) {
            firebasetask.cancel();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        isPreparing = false;
    }

    private void gamecubeAssets() {
        gamecubeBtn.setEnabled(false);
        wwcfBtn.setEnabled(false);
        newleafBtn.setEnabled(false);
        cancelBtn.setVisible(true);
        downloadAssets(ASSET_FILES[0], path.concat(ASSET_FILES[0]));
    }

    private void wwcfAssets() {
        gamecubeBtn.setEnabled(false);
        wwcfBtn.setEnabled(false);
        newleafBtn.setEnabled(false);
        cancelBtn.setVisible(true);
        downloadAssets(ASSET_FILES[1], path.concat(ASSET_FILES[1]));
    }

    private void newLeafAssets() {
        gamecubeBtn.setEnabled(false);
        wwcfBtn.setEnabled(false);
        newleafBtn.setEnabled(false);
        cancelBtn.setVisible(true);
        downloadAssets(ASSET_FILES[2], path.concat(ASSET_FILES[2]));
    }

    @Override
    public void extractAssets(String filename, String size) {
        if (storage.isFileExist(path.concat(filename)) && storage.getReadableSize(storage.getFile(path.concat(filename))).split(" ")[0].equals(size)) {
            Toast.makeText(this, "Please wait while the files are prepared. This will only happen once.", Toast.LENGTH_LONG).show();
            isPreparing = true;
            cancelBtn.setVisible(true);
            if (!storage.isDirectoryExists(path.concat(ASSETS_PATH))) {
                storage.createDirectory(path.concat(ASSETS_PATH));
            }

            ZipTool zipTool = new ZipTool(instance);
            zipTool.ASSET_FILE = filename;
            zipTool.decompress();
        } else {

            Snackbar snackbar = Snackbar.make(findViewById(R.id.activity_home), "File corrupt or missing", Snackbar.LENGTH_LONG);
            snackbar.setAction(R.string.snackbarAssetsAction2, view -> storage.deleteFile(path.concat(filename)));
            snackbar.show();
            gamecubeBtn.setEnabled(true);
            wwcfBtn.setEnabled(true);
            newleafBtn.setEnabled(true);
        }
    }

    @Override
    public void downloadAssets(String filename, String destinationPath) {

        NotificationCompat.Builder downloadNotification = showNotification("This won't take long. :)");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            manager.notify(R.string.NOTIFICATION_MAIN, downloadNotification.build());
        } else {
            manager.notify(R.string.NOTIFICATION_MAIN, downloadNotification.getNotification());
        }
        if (mInterstitialAd != null) {
            if (mInterstitialAd.isLoaded()) {
                mInterstitialAd.show();
            }
        }

        progressBar.setVisibility(View.VISIBLE);
        pauseDownloadBtn.setVisibility(View.VISIBLE);
        pauseDownloadBtn.setText(R.string.pause_download);

        FirebaseStorage firebasestorage = FirebaseStorage.getInstance();
        StorageReference reference = firebasestorage.getReference();
        StorageReference remoteFile = reference.child(filename);
        firebasetask = remoteFile.getFile(Uri.parse(destinationPath));
        firebasetask.addOnProgressListener(taskSnapshot -> {
            progress = (int) ((100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount());
            progressBar.setProgress(progress);
        });
        firebasetask.addOnPausedListener(taskSnapshot -> {
            progressBar.setProgress(progress);
            Intent resumeDownloadIntent = new Intent(this, ACMusicBroadcastReceiver.class);
            resumeDownloadIntent.setAction("ACTION_RESUME");
            PendingIntent resumeDownloadPendingIntent =
                    PendingIntent.getBroadcast(this, 0, resumeDownloadIntent, 0);
            NotificationCompat.Builder notification = showNotification("Currently paused.");
            notification.setSmallIcon(android.R.drawable.stat_sys_download_done);
            notification.setProgress(100, progress, false);
            notification.mActions.get(1).title = "Resume";
            notification.mActions.get(1).actionIntent = resumeDownloadPendingIntent;
            notification.setContentIntent(resumeDownloadPendingIntent);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                manager.notify(R.string.NOTIFICATION_MAIN, notification.build());
            } else {
                manager.notify(R.string.NOTIFICATION_MAIN, notification.getNotification());
            }
        });
        firebasetask.addOnFailureListener(e -> {
            Log.e(TAG, e.getLocalizedMessage());
            manager.cancel(R.string.NOTIFICATION_MAIN);
            if (storage.isFileExist(path.concat(filename))) {
                storage.deleteFile(path.concat(filename));
            }
            gamecubeBtn.setEnabled(true);
            wwcfBtn.setEnabled(true);
            newleafBtn.setEnabled(true);
            progressBar.setVisibility(View.INVISIBLE);
            pauseDownloadBtn.setVisibility(View.INVISIBLE);
            cancelBtn.setVisible(false);
        });
        firebasetask.addOnCanceledListener(() -> {
            manager.cancel(R.string.NOTIFICATION_MAIN);
            if (storage.isFileExist(path.concat(filename))) {
                storage.deleteFile(path.concat(filename));
            }
            gamecubeBtn.setEnabled(true);
            wwcfBtn.setEnabled(true);
            newleafBtn.setEnabled(true);
            progressBar.setVisibility(View.INVISIBLE);
            pauseDownloadBtn.setVisibility(View.INVISIBLE);
            cancelBtn.setVisible(false);
        });
        firebasetask.addOnCompleteListener(task -> {
            manager.cancel(R.string.NOTIFICATION_MAIN);
            gamecubeBtn.setEnabled(true);
            wwcfBtn.setEnabled(true);
            newleafBtn.setEnabled(true);
            pauseDownloadBtn.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
            cancelBtn.setVisible(false);
        });
        firebasetask.addOnSuccessListener(taskSnapshot -> {
            manager.cancel(R.string.NOTIFICATION_MAIN);
            gamecubeBtn.setEnabled(true);
            wwcfBtn.setEnabled(true);
            newleafBtn.setEnabled(true);
            pauseDownloadBtn.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
            cancelBtn.setVisible(false);
        });
    }

    @Override
    public void pauseDownload() {
        if (firebasetask != null) {
            if (firebasetask.isPaused()) {
                firebasetask.resume();
                return;
            }
            firebasetask.pause();
            pauseDownloadBtn.setText(R.string.resume_download);
        }
    }

    @Override
    public void downloadAlert(String gameId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Ready to download?");
        builder.setMessage("Please make sure you are connected to Wi-Fi.");
        builder.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss());
        builder.setPositiveButton("Start", (dialogInterface, i) -> {
            switch (gameId) {
                case "gamecube":
                    gamecubeAssets();
                    break;
                case "wwcf":
                    wwcfAssets();
                    break;
                case "newleaf":
                    newLeafAssets();
                    break;
            }
        });
        builder.create();
        builder.show();
    }

    @Override
    public Snackbar createSnackbar(String message, int duration, String gameid) {

        Snackbar snackbar = Snackbar.make(findViewById(R.id.activity_home), message, duration);
        snackbar.setAction(R.string.snackbarAssetsAction, view -> downloadAlert(gameid));

        return snackbar;
    }

    private NotificationCompat.Builder showNotification(String content) {
        Intent cancelDownloadIntent = new Intent(this, ACMusicBroadcastReceiver.class);
        cancelDownloadIntent.setAction("ACTION_CANCEL");
        Intent pauseDownloadIntent = new Intent(this, ACMusicBroadcastReceiver.class);
        pauseDownloadIntent.setAction("ACTION_PAUSE");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            cancelDownloadIntent.putExtra(Notification.EXTRA_NOTIFICATION_ID, 0);
        }
        PendingIntent cancelDownloadPendingIntent =
                PendingIntent.getBroadcast(this, 0, cancelDownloadIntent, 0);
        PendingIntent pauseDownloadPendingIntent =
                PendingIntent.getBroadcast(this, 0, pauseDownloadIntent, 0);

        return new NotificationCompat.Builder(this, getString(R.string.CHANNEL_ID))
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setContentTitle("Downloading assets")
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setProgress(100, progress, false)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Cancel", cancelDownloadPendingIntent)
                .addAction(android.R.drawable.ic_media_pause, "Pause", pauseDownloadPendingIntent)
                .setContentIntent(cancelDownloadPendingIntent)
                .setContentIntent(pauseDownloadPendingIntent);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotificationChannel() {
        CharSequence name = getString(R.string.channel_name);
        String description = getString(R.string.channel_description);
        int importance = NotificationManager.IMPORTANCE_LOW;
        NotificationChannel channel = new NotificationChannel(getString(R.string.CHANNEL_ID), name, importance);
        channel.setDescription(description);
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void prepareInterstitialAd() {
        Bundle extras = new Bundle();
        mInterstitialAd.loadAd(AdRequestBuilder.addNetworkExtrasBundle(AdMobAdapter.class, extras).build());
    }

    private void initializeAds() {
        MobileAds.initialize(this, getString(R.string.ADMOB_ID));
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.ADMOB_INTERSTITIAL));
        mInterstitialAd.setAdListener(new AdListeners(this, ""));
        AdRequestBuilder = new AdRequest.Builder();
    }

    private boolean isNetworkConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo wifiInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo dataInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if (wifiInfo != null) {

            if (wifiInfo.isConnected()) {
                return wifiInfo.getState().equals(NetworkInfo.State.CONNECTED);
            }
        } else if (dataInfo != null) {

            if (dataInfo.isConnected()) {
                return dataInfo.getState().equals(NetworkInfo.State.CONNECTED);
            }

        }
        return false;
    }
}
