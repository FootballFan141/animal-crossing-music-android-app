package com.appsbytravis.acmusic;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.appsbytravis.acmusic.utils.AssetsInterface;
import com.appsbytravis.acmusic.utils.Constants;
import com.appsbytravis.acmusic.utils.ZipTool;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.snatik.storage.Storage;

import static com.appsbytravis.acmusic.utils.Constants.LOG_TAG;
import static com.appsbytravis.acmusic.utils.Constants.NEWHORIZONS_FILES;
import static com.appsbytravis.acmusic.utils.Constants.POCKET_CAMP_FILES;

public class HomeActivity extends AppCompatActivity implements AssetsInterface {

    private static final String ASSETS_PATH = "assets/";
    private static final int GAMECUBE_FILES = Constants.GAMECUBE_FILES;
    private static final int WWCF_FILES = Constants.WWCF_FILES;
    private static final int NEWLEAF_FILES = Constants.NEWLEAF_FILES;
    private static SharedPreferences prefs;
    private Storage storage;
    private String path;
    private HomeActivity instance;
    private static final String[] ASSET_FILES = Constants.ASSET_FILES;
    private static final String[] ASSET_SIZES = Constants.ASSET_SIZES;
    //    private NotificationManagerCompat manager;
    private AdRequest AdRequest;
    private InterstitialAd mInterstitialAd;


    public static final String TAG = LOG_TAG;
    public Button gamecubeBtn;
    public Button wwcfBtn;
    public Button newleafBtn;
    public Button pocketcampBtn;
    public Button newhorizonsBtn;
    public boolean prepareFinished;
    public MenuItem cancelBtn;
    public static FileDownloadTask firebasetask;
    public static int progress = 0;
    public boolean isPreparing = false;
    public ProgressBar progressBar;
    public Button pauseDownloadBtn;


    //    private NotificationCompat.Action[] actions;

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
        pauseDownloadBtn = findViewById(R.id.pauseDownloadBtn);
        gamecubeBtn = findViewById(R.id.gamecubeBtn);
        wwcfBtn = findViewById(R.id.wwcfBtn);
        newleafBtn = findViewById(R.id.newleafBtn);
        pocketcampBtn = findViewById(R.id.pocketcampBtn);
        newhorizonsBtn = findViewById(R.id.newhorizonsBtn);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
//        manager = NotificationManagerCompat.from(this);
        storage = new Storage(this);
        path = storage.getInternalFilesDirectory() + "/";


        if (isNetworkConnected()) {
            if (bannerAd.getVisibility() == View.GONE) {
                bannerAd.setVisibility(View.VISIBLE);
            }
            bannerAd.setVisibility(View.VISIBLE);
            initializeAds();
            bannerAd.loadAd(AdRequest);
        } else {
            bannerAd.setVisibility(View.GONE);
        }

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            createNotificationChannel();
//        }

        pauseDownloadBtn.setOnClickListener(view -> pauseDownload());

        gamecubeBtn.setOnClickListener(view -> {
            int files = storage.getNestedFiles(path.concat(ASSETS_PATH.concat("gamecube"))).size();
            if (storage.isDirectoryExists(path.concat(ASSETS_PATH).concat("gamecube")) && files == GAMECUBE_FILES) {
                if (isNetworkConnected()) {
                    if (mInterstitialAd != null) {
                        mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                                super.onAdFailedToShowFullScreenContent(adError);
                                Intent i = new Intent(HomeActivity.this, Gamecube.class);
                                startActivity(i);
                            }

                            @Override
                            public void onAdShowedFullScreenContent() {
                                super.onAdShowedFullScreenContent();
                            }

                            @Override
                            public void onAdDismissedFullScreenContent() {
                                super.onAdDismissedFullScreenContent();
                                Intent i = new Intent(HomeActivity.this, Gamecube.class);
                                startActivity(i);
                            }
                        });
                        mInterstitialAd.show(this);
                    } else {
                        Intent i = new Intent(HomeActivity.this, Gamecube.class);
                        startActivity(i);
                    }
                } else {
                    Intent i = new Intent(HomeActivity.this, Gamecube.class);
                    startActivity(i);
                }
            } else {
                if (storage.isFileExist(path.concat(ASSET_FILES[0]))) {
                    view.setEnabled(false);
                    wwcfBtn.setEnabled(false);
                    newleafBtn.setEnabled(false);
                    pocketcampBtn.setEnabled(false);
                    newhorizonsBtn.setEnabled(false);
                    extractAssets(ASSET_FILES[0], ASSET_SIZES[0]);
                } else {
                    if (!isNetworkConnected()) {
                        Toast.makeText(this, getString(R.string.no_internet_msg), Toast.LENGTH_SHORT).show();
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
                if (isNetworkConnected()) {
                    if (mInterstitialAd != null) {
                        mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdFailedToShowFullScreenContent(@NonNull  AdError adError) {
                                super.onAdFailedToShowFullScreenContent(adError);
                                Intent i = new Intent(HomeActivity.this, WildWorldCityFolk.class);
                                startActivity(i);
                            }

                            @Override
                            public void onAdShowedFullScreenContent() {
                                super.onAdShowedFullScreenContent();
                            }

                            @Override
                            public void onAdDismissedFullScreenContent() {
                                super.onAdDismissedFullScreenContent();
                                Intent i = new Intent(HomeActivity.this, WildWorldCityFolk.class);
                                startActivity(i);
                            }
                        });
                        mInterstitialAd.show(this);
                    } else {
                        Intent i = new Intent(HomeActivity.this, WildWorldCityFolk.class);
                        startActivity(i);
                    }
                } else {
                    Intent i = new Intent(HomeActivity.this, WildWorldCityFolk.class);
                    startActivity(i);
                }
            } else {
                if (storage.isFileExist(path.concat(ASSET_FILES[1]))) {
                    view.setEnabled(false);
                    gamecubeBtn.setEnabled(false);
                    newleafBtn.setEnabled(false);
                    pocketcampBtn.setEnabled(false);
                    newhorizonsBtn.setEnabled(false);
                    extractAssets(ASSET_FILES[1], ASSET_SIZES[1]);
                } else {
                    if (!isNetworkConnected()) {
                        Toast.makeText(this, getString(R.string.no_internet_msg), Toast.LENGTH_SHORT).show();
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
                if (isNetworkConnected()) {
                    if (mInterstitialAd != null) {
                        mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                                super.onAdFailedToShowFullScreenContent(adError);
                                Intent i = new Intent(HomeActivity.this, NewLeaf.class);
                                startActivity(i);
                            }

                            @Override
                            public void onAdShowedFullScreenContent() {
                                super.onAdShowedFullScreenContent();
                            }

                            @Override
                            public void onAdDismissedFullScreenContent() {
                                super.onAdDismissedFullScreenContent();
                                Intent i = new Intent(HomeActivity.this, NewLeaf.class);
                                startActivity(i);
                            }
                        });
                        mInterstitialAd.show(this);
                    } else {
                        Intent i = new Intent(HomeActivity.this, NewLeaf.class);
                        startActivity(i);
                    }
                } else {
                    Intent i = new Intent(HomeActivity.this, NewLeaf.class);
                    startActivity(i);
                }
            } else {
                if (storage.isFileExist(path.concat(ASSET_FILES[2]))) {
                    view.setEnabled(false);
                    gamecubeBtn.setEnabled(false);
                    wwcfBtn.setEnabled(false);
                    pocketcampBtn.setEnabled(false);
                    newhorizonsBtn.setEnabled(false);
                    extractAssets(ASSET_FILES[2], ASSET_SIZES[2]);
                } else {
                    if (!isNetworkConnected()) {
                        Toast.makeText(this, getString(R.string.no_internet_msg), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Snackbar snackbar = createSnackbar(getString(R.string.assets_alert_msg), Snackbar.LENGTH_LONG, "newleaf");
                    snackbar.show();
                }
            }
        });
        pocketcampBtn.setOnClickListener(view -> {
            int files = storage.getNestedFiles(path.concat(ASSETS_PATH.concat("pocketcamp"))).size();
            if (storage.isDirectoryExists(path.concat(ASSETS_PATH).concat("pocketcamp")) && files == POCKET_CAMP_FILES) {
                if (isNetworkConnected()) {
                    if (mInterstitialAd != null) {
                        mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                                super.onAdFailedToShowFullScreenContent(adError);
                                Intent i = new Intent(HomeActivity.this, PocketCamp.class);
                                startActivity(i);
                            }

                            @Override
                            public void onAdShowedFullScreenContent() {
                                super.onAdShowedFullScreenContent();
                            }

                            @Override
                            public void onAdDismissedFullScreenContent() {
                                super.onAdDismissedFullScreenContent();
                                Intent i = new Intent(HomeActivity.this, PocketCamp.class);
                                startActivity(i);
                            }
                        });
                        mInterstitialAd.show(this);
                    } else {
                        Intent i = new Intent(HomeActivity.this, PocketCamp.class);
                        startActivity(i);
                    }
                } else {
                    Intent i = new Intent(HomeActivity.this, PocketCamp.class);
                    startActivity(i);
                }
            } else {
                if (storage.isFileExist(path.concat(ASSET_FILES[3]))) {
                    view.setEnabled(false);
                    gamecubeBtn.setEnabled(false);
                    wwcfBtn.setEnabled(false);
                    newleafBtn.setEnabled(false);
                    newhorizonsBtn.setEnabled(false);
                    extractAssets(ASSET_FILES[3], ASSET_SIZES[3]);
                } else {
                    if (!isNetworkConnected()) {
                        Toast.makeText(this, getString(R.string.no_internet_msg), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Snackbar snackbar = createSnackbar(getString(R.string.assets_alert_msg), Snackbar.LENGTH_LONG, "pocketcamp");
                    snackbar.show();
                }
            }
        });
        newhorizonsBtn.setOnClickListener(view -> {
            int files = storage.getNestedFiles(path.concat(ASSETS_PATH.concat("newhorizons"))).size();
            if (storage.isDirectoryExists(path.concat(ASSETS_PATH).concat("newhorizons")) && files == NEWHORIZONS_FILES) {
                if (isNetworkConnected()) {
                    if (mInterstitialAd != null) {
                        mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdFailedToShowFullScreenContent(@NonNull  AdError adError) {
                                super.onAdFailedToShowFullScreenContent(adError);
                                Intent i = new Intent(HomeActivity.this, NewHorizons.class);
                                startActivity(i);
                            }

                            @Override
                            public void onAdShowedFullScreenContent() {
                                super.onAdShowedFullScreenContent();
                            }

                            @Override
                            public void onAdDismissedFullScreenContent() {
                                super.onAdDismissedFullScreenContent();
                                Intent i = new Intent(HomeActivity.this, NewHorizons.class);
                                startActivity(i);
                            }
                        });
                        mInterstitialAd.show(this);
                    } else {
                        Intent i = new Intent(HomeActivity.this, NewHorizons.class);
                        startActivity(i);
                    }
                } else {
                    Intent i = new Intent(HomeActivity.this, NewHorizons.class);
                    startActivity(i);
                }
            } else {
                if (storage.isFileExist(path.concat(ASSET_FILES[4]))) {
                    view.setEnabled(false);
                    gamecubeBtn.setEnabled(false);
                    wwcfBtn.setEnabled(false);
                    newleafBtn.setEnabled(false);
                    pocketcampBtn.setEnabled(false);
                    extractAssets(ASSET_FILES[4], ASSET_SIZES[4]);
                } else {
                    if (!isNetworkConnected()) {
                        Toast.makeText(this, getString(R.string.no_internet_msg), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Snackbar snackbar = createSnackbar(getString(R.string.assets_alert_msg), Snackbar.LENGTH_LONG, "newhorizons");
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
                Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show();
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
            firebasetask = null;
        }
        instance = null;
        AdRequest = null;
        mInterstitialAd = null;
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
        pocketcampBtn.setEnabled(false);
        newhorizonsBtn.setEnabled(false);
        cancelBtn.setVisible(true);
        downloadAssets(ASSET_FILES[0], path.concat(ASSET_FILES[0]));
    }

    private void wwcfAssets() {
        gamecubeBtn.setEnabled(false);
        wwcfBtn.setEnabled(false);
        newleafBtn.setEnabled(false);
        pocketcampBtn.setEnabled(false);
        newhorizonsBtn.setEnabled(false);
        cancelBtn.setVisible(true);
        downloadAssets(ASSET_FILES[1], path.concat(ASSET_FILES[1]));
    }

    private void newLeafAssets() {
        gamecubeBtn.setEnabled(false);
        wwcfBtn.setEnabled(false);
        newleafBtn.setEnabled(false);
        pocketcampBtn.setEnabled(false);
        newhorizonsBtn.setEnabled(false);
        cancelBtn.setVisible(true);
        downloadAssets(ASSET_FILES[2], path.concat(ASSET_FILES[2]));
    }

    private void pocketcampAssets() {
        gamecubeBtn.setEnabled(false);
        wwcfBtn.setEnabled(false);
        newleafBtn.setEnabled(false);
        pocketcampBtn.setEnabled(false);
        newhorizonsBtn.setEnabled(false);
        cancelBtn.setVisible(true);
        downloadAssets(ASSET_FILES[3], path.concat(ASSET_FILES[3]));
    }

    private void newhorizonsAssets() {
        gamecubeBtn.setEnabled(false);
        wwcfBtn.setEnabled(false);
        newleafBtn.setEnabled(false);
        pocketcampBtn.setEnabled(false);
        newhorizonsBtn.setEnabled(false);
        cancelBtn.setVisible(true);
        downloadAssets(ASSET_FILES[4], path.concat(ASSET_FILES[4]));
    }

    @Override
    public void extractAssets(String filename, String size) {
        if (storage.isFileExist(path.concat(filename)) && storage.getReadableSize(storage.getFile(path.concat(filename))).split(" ")[0].equals(size)) {
            Toast.makeText(this, "Please wait while the files are prepared.", Toast.LENGTH_LONG).show();
            isPreparing = true;
            cancelBtn.setVisible(true);
            if (!storage.isDirectoryExists(path.concat(ASSETS_PATH))) {
                storage.createDirectory(path.concat(ASSETS_PATH));
            }

            ZipTool zipTool = new ZipTool(instance);
            ZipTool.ASSET_FILE = "/" + filename;
            zipTool.decompress();

        } else {

            Snackbar snackbar = Snackbar.make(findViewById(R.id.activity_home), "File corrupt or missing", Snackbar.LENGTH_LONG);
            snackbar.setAction(R.string.snackbarAssetsAction2, view -> storage.deleteFile(path.concat(filename)));
            snackbar.show();
            gamecubeBtn.setEnabled(true);
            wwcfBtn.setEnabled(true);
            newleafBtn.setEnabled(true);
            pocketcampBtn.setEnabled(true);
            newhorizonsBtn.setEnabled(true);
        }
    }

    @Override
    public void downloadAssets(String filename, String destinationPath) {

//        Intent pauseDownloadIntent = new Intent(this, ACMusicBroadcastReceiver.class);
//        pauseDownloadIntent.setAction("ACTION_PAUSE");
//        PendingIntent pauseDownloadPendingIntent =
//                PendingIntent.getBroadcast(this, PAUSE_DOWNLOAD_REQUESTCODE, pauseDownloadIntent, FLAG_CANCEL_CURRENT);
//
//        NotificationCompat.Builder downloadNotification = showNotification("This won't take long. :)")
//                .addAction(android.R.drawable.ic_media_pause, "Pause", pauseDownloadPendingIntent);

//        manager.notify(R.string.NOTIFICATION_MAIN, downloadNotification.build());
//        if (mInterstitialAd != null) {
//            mInterstitialAd.show(this);
//        }
        progress = 0;
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
//            Intent resumeDownloadIntent = new Intent(this, ACMusicBroadcastReceiver.class);
//            resumeDownloadIntent.setAction("ACTION_RESUME");
//            PendingIntent resumeDownloadPendingIntent =
//                    PendingIntent.getBroadcast(this, RESUME_DOWNLOAD_REQUESTCODE, resumeDownloadIntent, FLAG_CANCEL_CURRENT);

//            manager.cancel(R.string.NOTIFICATION_MAIN);
//            NotificationCompat.Builder notification;
//            notification = showNotification("Currently paused.")
//                    .setSmallIcon(android.R.drawable.ic_media_pause)
//                    .addAction(android.R.drawable.stat_sys_download_done, "Resume", resumeDownloadPendingIntent)
//                    .setContentIntent(resumeDownloadPendingIntent);
//            manager.notify(R.string.NOTIFICATION_MAIN, notification.build());
            pauseDownloadBtn.setEnabled(true);
            pauseDownloadBtn.setText(R.string.resume_download);
        });
        firebasetask.addOnFailureListener(e -> {
            Log.e(TAG, e.getLocalizedMessage());
//            manager.cancel(R.string.NOTIFICATION_MAIN);
            if (storage.isFileExist(path.concat(filename))) {
                storage.deleteFile(path.concat(filename));
            }
            gamecubeBtn.setEnabled(true);
            wwcfBtn.setEnabled(true);
            newleafBtn.setEnabled(true);
            pocketcampBtn.setEnabled(true);
            newhorizonsBtn.setEnabled(true);
            progressBar.setVisibility(View.INVISIBLE);
            pauseDownloadBtn.setVisibility(View.INVISIBLE);
            cancelBtn.setVisible(false);
        });
        firebasetask.addOnCanceledListener(() -> {
//            manager.cancel(R.string.NOTIFICATION_MAIN);
            if (storage.isFileExist(path.concat(filename))) {
                storage.deleteFile(path.concat(filename));
            }
            gamecubeBtn.setEnabled(true);
            wwcfBtn.setEnabled(true);
            newleafBtn.setEnabled(true);
            pocketcampBtn.setEnabled(true);
            newhorizonsBtn.setEnabled(true);
            progressBar.setVisibility(View.INVISIBLE);
            pauseDownloadBtn.setVisibility(View.INVISIBLE);
            cancelBtn.setVisible(false);
        });
        firebasetask.addOnCompleteListener(task -> {
//            manager.cancel(R.string.NOTIFICATION_MAIN);
            gamecubeBtn.setEnabled(true);
            wwcfBtn.setEnabled(true);
            newleafBtn.setEnabled(true);
            pocketcampBtn.setEnabled(true);
            newhorizonsBtn.setEnabled(true);
            pauseDownloadBtn.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
            cancelBtn.setVisible(false);
        });
        firebasetask.addOnSuccessListener(taskSnapshot -> {
//            manager.cancel(R.string.NOTIFICATION_MAIN);
            gamecubeBtn.setEnabled(true);
            wwcfBtn.setEnabled(true);
            newleafBtn.setEnabled(true);
            pocketcampBtn.setEnabled(true);
            newhorizonsBtn.setEnabled(true);
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
                pauseDownloadBtn.setEnabled(true);
                pauseDownloadBtn.setText(R.string.pause_download);
            } else {
                firebasetask.pause();
                pauseDownloadBtn.setEnabled(false);
                pauseDownloadBtn.setText(R.string.resume_download);
            }
        }
    }

    @Override
    public void downloadAlert(String gameId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
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
                case "pocketcamp":
                    pocketcampAssets();
                    break;
                case "newhorizons":
                    newhorizonsAssets();
                    break;
            }
        });
        builder.create();
        builder.show();
    }

    @Override
    public Snackbar createSnackbar(String message, int duration, String gameid) {

        Snackbar snackbar = Snackbar.make(findViewById(R.id.activity_home), message, duration);
        snackbar.setDuration(BaseTransientBottomBar.LENGTH_LONG);
        snackbar.setAction(R.string.snackbarAssetsAction1, view -> downloadAlert(gameid));

        return snackbar;
    }

//    @Override
//    public NotificationCompat.Builder showNotification(String content) {
//
//        Intent cancelDownloadIntent = new Intent(this, ACMusicBroadcastReceiver.class);
//        cancelDownloadIntent.setAction("ACTION_CANCEL");
//
//        PendingIntent cancelDownloadPendingIntent =
//                PendingIntent.getBroadcast(this, CANCEL_DOWNLOAD_REQUESTCODE, cancelDownloadIntent, FLAG_CANCEL_CURRENT);
//
//        return new NotificationCompat.Builder(this, getString(R.string.CHANNEL_ID))
//                .setSmallIcon(android.R.drawable.stat_sys_download)
//                .setContentTitle("Downloading assets")
//                .setContentText(content)
//                .setPriority(NotificationCompat.PRIORITY_LOW)
//                .setProgress(100, progress, false)
//                .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Cancel", cancelDownloadPendingIntent);
//    }

//    @RequiresApi(api = Build.VERSION_CODES.O)
//    private void createNotificationChannel() {
//        CharSequence name = getString(R.string.channel_name);
//        String description = getString(R.string.channel_description);
//        int importance = NotificationManager.IMPORTANCE_LOW;
//        NotificationChannel channel = new NotificationChannel(getString(R.string.CHANNEL_ID), name, importance);
//        channel.setDescription(description);
//        NotificationManager notificationManager = getSystemService(NotificationManager.class);
//        if (notificationManager != null) {
//            notificationManager.createNotificationChannel(channel);
//        }
//    }

    private void initializeAds() {
        AdRequest = new AdRequest.Builder().build();
        InterstitialAd.load(getApplicationContext(), getString(R.string.ADMOB_INTERSTITIAL), AdRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                super.onAdLoaded(interstitialAd);
                mInterstitialAd = interstitialAd;
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                mInterstitialAd = null;
            }
        });
    }

    private boolean isNetworkConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if (networkInfo != null) {
                return networkInfo.isConnected();
            } else {
                return false;
            }
        } else {
            NetworkInfo wifiInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            NetworkInfo dataInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

            if (wifiInfo != null || dataInfo != null) {

                if (wifiInfo.isConnected()) {
                    return wifiInfo.getState().equals(NetworkInfo.State.CONNECTED);
                } else if (dataInfo != null) {
                    if (dataInfo.isConnected()) {
                        return dataInfo.getState().equals(NetworkInfo.State.CONNECTED);
                    }
                }
            }
        }
        return false;
    }
}