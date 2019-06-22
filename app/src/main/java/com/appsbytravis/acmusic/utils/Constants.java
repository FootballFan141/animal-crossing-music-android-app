package com.appsbytravis.acmusic.utils;

public interface Constants {
    int RESUME_DOWNLOAD_REQUESTCODE = 1;
    int PAUSE_DOWNLOAD_REQUESTCODE = 2;
    int CANCEL_DOWNLOAD_REQUESTCODE = 3;
    int CHANGE_MUSIC_REQUESTCODE = 4;

    String[] ASSET_FILES = new String[]{
            "gamecube.zip",
            "wwcf.zip",
            "newleaf.zip"
    };

    String[] ASSET_SIZES = new String[]{
            "197.47", // gamecube
            "64.90", // wwcf
            "74.40"  // newleaf

    };

    int GAMECUBE_FILES = 49;
    int WWCF_FILES = 72;
    int NEWLEAF_FILES = 72;

    String LOG_TAG = "ACMUSIC";


}
