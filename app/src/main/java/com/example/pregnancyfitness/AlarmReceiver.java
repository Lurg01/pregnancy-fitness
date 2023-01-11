package com.example.pregnancyfitness;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.PowerManager;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class AlarmReceiver extends BroadcastReceiver {
    DBHelper DB;
    @Override
    public void onReceive(Context context, Intent intent) {
        DB = new DBHelper(context);

        Intent i = new Intent(context,HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,0, i, PendingIntent.FLAG_IMMUTABLE); // 0 have changed to PendingIntent.FLAG_MUTABLE

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context,"foxandroid")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle("Good Day !")
                .setContentText("Hello there, its Time for your Exercises.")
                .setAutoCancel(true)
                //                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setContentIntent(pendingIntent);


        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(123,builder.build());

        // WE WILL USE VIBRATOR
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(10000);

        MediaPlayer sound = MediaPlayer.create(context, R.raw.sound);
        sound.isPlaying();
        sound.getDuration();
        sound.start();

        PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = pm.isScreenOn();
        Log.e("screen on........", ""+isScreenOn);
        if(!isScreenOn)
        {
            PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK |PowerManager.ACQUIRE_CAUSES_WAKEUP |PowerManager.ON_AFTER_RELEASE,"myApp:MyLock");
            wl.acquire(10000);
            PowerManager.WakeLock wl_cpu = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"myApp:mycpuMyCpuLock");

            wl_cpu.acquire(10000);
        }

        Cursor res = DB.getTime();
        if (res.moveToFirst())
        {
            DB.insertTitle(res.getString(0) + " > Its Time for your Exercises.");
            DB.insertNotification("$");

        }

    }

}
