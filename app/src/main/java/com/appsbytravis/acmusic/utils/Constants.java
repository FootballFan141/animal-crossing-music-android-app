package com.appsbytravis.acmusic.utils;

public interface Constants {
    int RESUME_DOWNLOAD_REQUESTCODE = 1;
    int PAUSE_DOWNLOAD_REQUESTCODE = 2;
    int CANCEL_DOWNLOAD_REQUESTCODE = 3;
    int CHANGE_MUSIC_REQUESTCODE = 4;
    int FADE_MUSIC_REQUESTCODE = 5;

    String[] ASSET_FILES = new String[]{
            "gamecube.zip",
            "wwcf.zip",
            "newleaf.zip",
            "pocketcamp.zip"
    };

    String[] ASSET_SIZES = new String[]{
            "197.47", // gamecube
            "64.90", // wwcf
            "74.40",  // newleaf
            "33.57"  // pocketcamp

    };

    int GAMECUBE_FILES = 49;
    int WWCF_FILES = 72;
    int NEWLEAF_FILES = 72;
    int POCKET_CAMP_FILES = 4;

    String LOG_TAG = "ACMUSIC";


}
