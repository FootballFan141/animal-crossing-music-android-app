package com.appsbytravis.acmusic.utils;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.view.Window;

import com.appsbytravis.acmusic.R;
import com.snatik.storage.Storage;

import java.io.File;

public class ACMusic {
    private String ASSETS_PATH;
    private String path;
    private Storage storage;
    private Context context;
    private Window window;

    public ACMusic(Context context, String ASSETS_PATH) {
        this.context = context;
        this.ASSETS_PATH = ASSETS_PATH;

        this.storage = new Storage(context);
        this.path = storage.getInternalFilesDirectory();
    }

    public File normal(int hour) {
        File file = null;
        switch (hour) {
            case 0:
                file = storage.getFile(path.concat(ASSETS_PATH).concat("am/").concat("normal/").concat("12am.mp3"));
                break;
            case 1:
                file = storage.getFile(path.concat(ASSETS_PATH).concat("am/").concat("normal/").concat("1am.mp3"));
                break;
            case 2:
                file = storage.getFile(path.concat(ASSETS_PATH).concat("am/").concat("normal/").concat("2am.mp3"));
                break;
            case 3:
                file = storage.getFile(path.concat(ASSETS_PATH).concat("am/").concat("normal/").concat("3am.mp3"));
                break;
            case 4:
                file = storage.getFile(path.concat(ASSETS_PATH).concat("am/").concat("normal/").concat("4am.mp3"));
                break;
            case 5:
                file = storage.getFile(path.concat(ASSETS_PATH).concat("am/").concat("normal/").concat("5am.mp3"));
                break;
            case 6:
                file = storage.getFile(path.concat(ASSETS_PATH).concat("am/").concat("normal/").concat("6am.mp3"));
                break;
            case 7:
                file = storage.getFile(path.concat(ASSETS_PATH).concat("am/").concat("normal/").concat("7am.mp3"));
                break;
            case 8:
                file = storage.getFile(path.concat(ASSETS_PATH).concat("am/").concat("normal/").concat("8am.mp3"));
                break;
            case 9:
                file = storage.getFile(path.concat(ASSETS_PATH).concat("am/").concat("normal/").concat("9am.mp3"));
                break;
            case 10:
                file = storage.getFile(path.concat(ASSETS_PATH).concat("am/").concat("normal/").concat("10am.mp3"));
                break;
            case 11:
                file = storage.getFile(path.concat(ASSETS_PATH).concat("am/").concat("normal/").concat("11am.mp3"));
                break;
            case 12:
                file = storage.getFile(path.concat(ASSETS_PATH).concat("pm/").concat("normal/").concat("12pm.mp3"));
                break;
            case 13:
                file = storage.getFile(path.concat(ASSETS_PATH).concat("pm/").concat("normal/").concat("1pm.mp3"));
                break;
            case 14:
                file = storage.getFile(path.concat(ASSETS_PATH).concat("pm/").concat("normal/").concat("2pm.mp3"));
                break;
            case 15:
                file = storage.getFile(path.concat(ASSETS_PATH).concat("pm/").concat("normal/").concat("3pm.mp3"));
                break;
            case 16:
                file = storage.getFile(path.concat(ASSETS_PATH).concat("pm/").concat("normal/").concat("4pm.mp3"));
                break;
            case 17:
                file = storage.getFile(path.concat(ASSETS_PATH).concat("pm/").concat("normal/").concat("5pm.mp3"));
                break;
            case 18:
                file = storage.getFile(path.concat(ASSETS_PATH).concat("pm/").concat("normal/").concat("6pm.mp3"));
                break;
            case 19:
                file = storage.getFile(path.concat(ASSETS_PATH).concat("pm/").concat("normal/").concat("7pm.mp3"));
                break;
            case 20:
                file = storage.getFile(path.concat(ASSETS_PATH).concat("pm/").concat("normal/").concat("8pm.mp3"));
                break;
            case 21:
                file = storage.getFile(path.concat(ASSETS_PATH).concat("pm/").concat("normal/").concat("9pm.mp3"));
                break;
            case 22:
                file = storage.getFile(path.concat(ASSETS_PATH).concat("pm/").concat("normal/").concat("10pm.mp3"));
                break;
            case 23:
                file = storage.getFile(path.concat(ASSETS_PATH).concat("pm/").concat("normal/").concat("11pm.mp3"));
                break;
        }
        return file;
    }

    public File rainingGC() {
        return storage.getFile(path.concat(ASSETS_PATH).concat("other/").concat("raining.mp3"));
    }

    public File raining(int hour) {
        File file = null;
        switch (hour) {
            case 0:
                file = storage.getFile(path.concat(ASSETS_PATH).concat("am/").concat("rain/").concat("12am.mp3"));
                break;
            case 1:
                file = storage.getFile(path.concat(ASSETS_PATH).concat("am/").concat("rain/").concat("1am.mp3"));
                break;
            case 2:
                file = storage.getFile(path.concat(ASSETS_PATH).concat("am/").concat("rain/").concat("2am.mp3"));
                break;
            case 3:
                file = storage.getFile(path.concat(ASSETS_PATH).concat("am/").concat("rain/").concat("3am.mp3"));
                break;
            case 4:
                file = storage.getFile(path.concat(ASSETS_PATH).concat("am/").concat("rain/").concat("4am.mp3"));
                break;
            case 5:
                file = storage.getFile(path.concat(ASSETS_PATH).concat("am/").concat("rain/").concat("5am.mp3"));
                break;
            case 6:
                file = storage.getFile(path.concat(ASSETS_PATH).concat("am/").concat("rain/").concat("6am.mp3"));
                break;
            case 7:
                file = storage.getFile(path.concat(ASSETS_PATH).concat("am/").concat("rain/").concat("7am.mp3"));
                break;
            case 8:
                file = storage.getFile(path.concat(ASSETS_PATH).concat("am/").concat("rain/").concat("8am.mp3"));
                break;
            case 9:
                file = storage.getFile(path.concat(ASSETS_PATH).concat("am/").concat("rain/").concat("9am.mp3"));
                break;
            case 10:
                file = storage.getFile(path.concat(ASSETS_PATH).concat("am/").concat("rain/").concat("10am.mp3"));
                break;
            case 11:
                file = storage.getFile(path.concat(ASSETS_PATH).concat("am/").concat("rain/").concat("11am.mp3"));
                break;
            case 12:
                file = storage.getFile(path.concat(ASSETS_PATH).concat("pm/").concat("rain/").concat("12pm.mp3"));
                break;
            case 13:
                file = storage.getFile(path.concat(ASSETS_PATH).concat("pm/").concat("rain/").concat("1pm.mp3"));
                break;
            case 14:
                file = storage.getFile(path.concat(ASSETS_PATH).concat("pm/").concat("rain/").concat("2pm.mp3"));
                break;
            case 15:
                file = storage.getFile(path.concat(ASSETS_PATH).concat("pm/").concat("rain/").concat("3pm.mp3"));
                break;
            case 16:
                file = storage.getFile(path.concat(ASSETS_PATH).concat("pm/").concat("rain/").concat("4pm.mp3"));
                break;
            case 17:
                file = storage.getFile(path.concat(ASSETS_PATH).concat("pm/").concat("rain/").concat("5pm.mp3"));
                break;
            case 18:
                file = storage.getFile(path.concat(ASSETS_PATH).concat("pm/").concat("rain/").concat("6pm.mp3"));
                break;
            case 19:
                file = storage.getFile(path.concat(ASSETS_PATH).concat("pm/").concat("rain/").concat("7pm.mp3"));
                break;
            case 20:
                file = storage.getFile(path.concat(ASSETS_PATH).concat("pm/").concat("rain/").concat("8pm.mp3"));
                break;
            case 21:
                file = storage.getFile(path.concat(ASSETS_PATH).concat("pm/").concat("rain/").concat("9pm.mp3"));
                break;
            case 22:
                file = storage.getFile(path.concat(ASSETS_PATH).concat("pm/").concat("rain/").concat("10pm.mp3"));
                break;
            case 23:
                file = storage.getFile(path.concat(ASSETS_PATH).concat("pm/").concat("rain/").concat("11pm.mp3"));
                break;
        }
        return file;
    }

    public File snowing(int hour) {
        File file = null;

        switch (hour) {
            case 0:
                file = storage.getFile(path.concat(ASSETS_PATH).concat("am/").concat("snow/").concat("12am.mp3"));
                break;
            case 1:
                file = storage.getFile(path.concat(ASSETS_PATH).concat("am/").concat("snow/").concat("1am.mp3"));
                break;
            case 2:
                file = storage.getFile(path.concat(ASSETS_PATH).concat("am/").concat("snow/").concat("2am.mp3"));
                break;
            case 3:
                file = storage.getFile(path.concat(ASSETS_PATH).concat("am/").concat("snow/").concat("3am.mp3"));
                break;
            case 4:
                file = storage.getFile(path.concat(ASSETS_PATH).concat("am/").concat("snow/").concat("4am.mp3"));
                break;
            case 5:
                file = storage.getFile(path.concat(ASSETS_PATH).concat("am/").concat("snow/").concat("5am.mp3"));
                break;
            case 6:
                file = storage.getFile(path.concat(ASSETS_PATH).concat("am/").concat("snow/").concat("6am.mp3"));
                break;
            case 7:
                file = storage.getFile(path.concat(ASSETS_PATH).concat("am/").concat("snow/").concat("7am.mp3"));
                break;
            case 8:
                file = storage.getFile(path.concat(ASSETS_PATH).concat("am/").concat("snow/").concat("8am.mp3"));
                break;
            case 9:
                file = storage.getFile(path.concat(ASSETS_PATH).concat("am/").concat("snow/").concat("9am.mp3"));
                break;
            case 10:
                file = storage.getFile(path.concat(ASSETS_PATH).concat("am/").concat("snow/").concat("10am.mp3"));
                break;
            case 11:
                file = storage.getFile(path.concat(ASSETS_PATH).concat("am/").concat("snow/").concat("11am.mp3"));
                break;
            case 12:
                file = storage.getFile(path.concat(ASSETS_PATH).concat("pm/").concat("snow/").concat("12pm.mp3"));
                break;
            case 13:
                file = storage.getFile(path.concat(ASSETS_PATH).concat("pm/").concat("snow/").concat("1pm.mp3"));
                break;
            case 14:
                file = storage.getFile(path.concat(ASSETS_PATH).concat("pm/").concat("snow/").concat("2pm.mp3"));
                break;
            case 15:
                file = storage.getFile(path.concat(ASSETS_PATH).concat("pm/").concat("snow/").concat("3pm.mp3"));
                break;
            case 16:
                file = storage.getFile(path.concat(ASSETS_PATH).concat("pm/").concat("snow/").concat("4pm.mp3"));
                break;
            case 17:
                file = storage.getFile(path.concat(ASSETS_PATH).concat("pm/").concat("snow/").concat("5pm.mp3"));
                break;
            case 18:
                file = storage.getFile(path.concat(ASSETS_PATH).concat("pm/").concat("snow/").concat("6pm.mp3"));
                break;
            case 19:
                file = storage.getFile(path.concat(ASSETS_PATH).concat("pm/").concat("snow/").concat("7pm.mp3"));
                break;
            case 20:
                file = storage.getFile(path.concat(ASSETS_PATH).concat("pm/").concat("snow/").concat("8pm.mp3"));
                break;
            case 21:
                file = storage.getFile(path.concat(ASSETS_PATH).concat("pm/").concat("snow/").concat("9pm.mp3"));
                break;
            case 22:
                file = storage.getFile(path.concat(ASSETS_PATH).concat("pm/").concat("snow/").concat("10pm.mp3"));
                break;
            case 23:
                file = storage.getFile(path.concat(ASSETS_PATH).concat("pm/").concat("snow/").concat("11pm.mp3"));
                break;
        }
        return file;
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    public void bgChange(int hour) {
        if (hour > 0 && hour < 4) {
            window.setBackgroundDrawable(context.getDrawable(R.drawable.app_bg6));
        } else if (hour == 4) {
            window.setBackgroundDrawable(context.getDrawable(R.drawable.app_bg5));
        } else if (hour == 5) {
            window.setBackgroundDrawable(context.getDrawable(R.drawable.app_bg4));
        } else if (hour == 6) {
            window.setBackgroundDrawable(context.getDrawable(R.drawable.app_bg3));
        } else if (hour == 7) {
            window.setBackgroundDrawable(context.getDrawable(R.drawable.app_bg2));
        } else if (hour > 7 && hour < 18) {
            window.setBackgroundDrawable(context.getDrawable(R.drawable.app_bg1));
        } else if (hour == 18) {
            window.setBackgroundDrawable(context.getDrawable(R.drawable.app_bg2));
        } else if (hour == 19) {
            window.setBackgroundDrawable(context.getDrawable(R.drawable.app_bg3));
        } else if (hour == 20) {
            window.setBackgroundDrawable(context.getDrawable(R.drawable.app_bg4));
        } else if (hour == 21) {
            window.setBackgroundDrawable(context.getDrawable(R.drawable.app_bg5));
        } else if (hour == 22 || hour == 23 || hour == 0) {
            window.setBackgroundDrawable(context.getDrawable(R.drawable.app_bg6));
        }
    }

    public void setWindow(Context context) {
        window = ((Activity) context).getWindow();
    }
}
