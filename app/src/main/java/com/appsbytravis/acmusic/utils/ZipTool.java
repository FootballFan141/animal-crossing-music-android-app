package com.appsbytravis.acmusic.utils;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import com.appsbytravis.acmusic.HomeActivity;
import com.snatik.storage.Storage;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.channels.FileChannel;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipTool {
    private static final int BUFFER_SIZE = 1024;
    private final HomeActivity instance;
    private Storage storage;
    private static String path;

    public String ASSET_FILE = "";
    private static final String ASSETS_PATH = "/assets/";

    public ZipTool(HomeActivity instance) {
        this.instance = instance;
        storage = new Storage(instance);
        path = storage.getInternalFilesDirectory() + "/";

    }

    public void decompress() {
        storage = new Storage(instance);
        instance.progressBar.setVisibility(View.VISIBLE);
        File file = storage.getFile(path.concat(ASSET_FILE));
        AssetsAsyncTask task = new AssetsAsyncTask(instance);
        task.setStorage(storage);
        task.execute(file);
    }

    static class AssetsAsyncTask extends AsyncTask<File, String, Boolean> {


        private WeakReference<HomeActivity> instance;
        private Storage storage;

        AssetsAsyncTask(HomeActivity instance) {
            this.instance = new WeakReference<>(instance);
        }

        void setStorage(Storage storage) {
            this.storage = storage;
        }

        @Override
        protected Boolean doInBackground(File... params) {
            try {
                FileInputStream fis = new FileInputStream(params[0].getPath());
                FileChannel channel = fis.getChannel();
                ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
                ZipEntry entry;
                byte[] bytesIn = new byte[BUFFER_SIZE];
                while ((entry = zis.getNextEntry()) != null) {
                    if (instance.get().isPreparing) {
                        String filePath = path.concat(ASSETS_PATH).concat(entry.getName());
                        if (!entry.isDirectory()) {
                            FileOutputStream fos = new FileOutputStream(filePath);
                            BufferedOutputStream bos = new BufferedOutputStream(fos);
                            int read;
                            while ((read = zis.read(bytesIn)) != -1) {
                                bos.write(bytesIn, 0, read);
                                publishProgress(String.valueOf(channel.position()), String.valueOf(params[0].length()));
                            }
                            fos.close();
                        } else {
                            storage.createDirectory(filePath);
                        }

                        zis.closeEntry();
                    } else {
                        return false;
                    }
                }
                zis.close();
            } catch (IOException e) {
                Log.d(HomeActivity.TAG, e.getLocalizedMessage());
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            instance.get().prepareFinished = aBoolean;
            instance.get().isPreparing = false;
            instance.get().progressBar.setVisibility(View.INVISIBLE);
            instance.get().cancelBtn.setVisible(false);
            instance.get().gamecubeBtn.setEnabled(true);
            instance.get().wwcfBtn.setEnabled(true);
            instance.get().newleafBtn.setEnabled(true);
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            instance.get().progressBar.setProgress(((int) Long.parseLong(values[0])));
            instance.get().progressBar.setMax((int) Long.parseLong(values[1]));
        }
    }
}
