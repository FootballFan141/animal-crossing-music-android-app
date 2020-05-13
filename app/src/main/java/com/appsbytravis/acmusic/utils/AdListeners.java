package com.appsbytravis.acmusic.utils;

import android.content.Intent;
import android.util.Log;

import com.appsbytravis.acmusic.Gamecube;
import com.appsbytravis.acmusic.HomeActivity;
import com.appsbytravis.acmusic.NewHorizons;
import com.appsbytravis.acmusic.NewLeaf;
import com.appsbytravis.acmusic.PocketCamp;
import com.appsbytravis.acmusic.WildWorldCityFolk;
import com.google.android.gms.ads.AdListener;

public class AdListeners extends AdListener {

    private HomeActivity instance;
    private String game;

    public AdListeners(HomeActivity inst, String game) {
        this.instance = inst;
        this.game = game;
    }

    @Override
    public void onAdLoaded() {
        // Code to be executed when an ad finishes loading.
    }

    @Override
    public void onAdFailedToLoad(int errorCode) {
        Log.d(HomeActivity.TAG, "Admob Error code: " + errorCode);
        Intent i;
        switch (game) {
            case "gamecube":
                i = new Intent(instance, Gamecube.class);
                break;
            case "wwcf":
                i = new Intent(instance, WildWorldCityFolk.class);
                break;
            case "newleaf":
                i = new Intent(instance, NewLeaf.class);
                break;
            case "pocketcamp":
                i = new Intent(instance, PocketCamp.class);
                break;
            case "newhorizons":
                i = new Intent(instance, NewHorizons.class);
                break;
            default:
                return;
        }
        instance.startActivity(i);
    }

    @Override
    public void onAdOpened() {
        // Code to be executed when the ad is displayed.
    }

    @Override
    public void onAdLeftApplication() {
        // Code to be executed when the user has left the app.
    }

    @Override
    public void onAdClosed() {
        // Code to be executed when when the interstitial ad is closed.
        Intent i;
        switch (game) {
            case "gamecube":
                i = new Intent(instance, Gamecube.class);
                break;
            case "wwcf":
                i = new Intent(instance, WildWorldCityFolk.class);
                break;
            case "newleaf":
                i = new Intent(instance, NewLeaf.class);
                break;
            case "pocketcamp":
                i = new Intent(instance, PocketCamp.class);
                break;
            case "newhorizons":
                i = new Intent(instance, NewHorizons.class);
                break;
            default:
                return;
        }
        instance.startActivity(i);
        instance = null;
    }
}
