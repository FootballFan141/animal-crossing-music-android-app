//package com.appsbytravis.acmusic.utils;
//
//import android.app.AlarmManager;
//import android.app.PendingIntent;
//import android.app.Service;
//import android.content.Context;
//import android.content.Intent;
//import android.os.IBinder;
//
//import com.appsbytravis.acmusic.HomeActivity;
//
//public class ACMusicService extends Service {
//
//    private PendingIntent pendingIntent;
//
//    @Override
//    public IBinder onBind(Intent intent) {
//        return null;
//    }
//
//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        pendingIntent = intent.getParcelableExtra("pendingIntent");
//
//        return START_NOT_STICKY;
//    }
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//    }
//
//    @Override
//    public void onTaskRemoved(Intent rootIntent) {
//        super.onTaskRemoved(rootIntent);
//        AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
//        if (pendingIntent != null) {
//            alarmManager.cancel(pendingIntent);
//            pendingIntent.cancel();
//        }
//        stopSelf();
//    }
//}
