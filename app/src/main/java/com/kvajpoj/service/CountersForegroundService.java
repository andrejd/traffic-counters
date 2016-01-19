package com.kvajpoj.service;

/**
 * Created by Andrej on 29.10.2015.
 */

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.kvajpoj.R;
import com.kvajpoj.activities.MainActivity;
import com.kvajpoj.dao.PreferencesDAO;

public class CountersForegroundService extends Service {

    private static final String LOG_TAG = "CountersMService";
    private static final int period = 1000 * 60 * 5; // repeat check every 5 min.
    public static boolean IS_SERVICE_RUNNING = false;
    private IntentMessageReceiver mIntentMessageReceiver;


    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        try {

            if(intent == null) {

            } else if (intent.getAction().equals(ServiceConstants.ACTION.START_FOREGROUND_ACTION)) {

                Log.i(LOG_TAG, "Received Start Foreground Intent with delay " + intent.getIntExtra("Delay", 0));

                if (mIntentMessageReceiver != null) {
                    unregisterReceiver(mIntentMessageReceiver);
                }

                mIntentMessageReceiver = new IntentMessageReceiver();

                IntentFilter screenStateFilter = new IntentFilter();
                screenStateFilter.addAction(Intent.ACTION_SCREEN_ON);
                screenStateFilter.addAction(Intent.ACTION_SCREEN_OFF);
                screenStateFilter.addAction(ServiceConstants.ACTION.HIDE_ACTION);
                screenStateFilter.addAction(ServiceConstants.ACTION.SHOW_ACTION);

                registerReceiver(mIntentMessageReceiver, screenStateFilter);

                showNotification(PreferencesDAO.getInstance(this).isNotifications(), false);

                Log.i(LOG_TAG, "Service has started!");

            } else if (intent.getAction().equals(ServiceConstants.ACTION.SILENT_ACTION)) {

                PreferencesDAO.getInstance(this).setNotifications(false);
                showNotification(PreferencesDAO.getInstance(this).isNotifications(), false);

            } else if (intent.getAction().equals(ServiceConstants.ACTION.LOUD_ACTION)) {

                PreferencesDAO.getInstance(this).setNotifications(true);
                showNotification(PreferencesDAO.getInstance(this).isNotifications(), false);

            } else if (intent.getAction().equals(ServiceConstants.ACTION.STOP_FOREGROUND_ACTION)) {

                Log.i(LOG_TAG, "Received Stop Foreground Intent");
                stopForeground(true);
                stopSelf();

            }

        } catch (Exception ex) {
            Log.e(LOG_TAG, ex.toString());

        } finally {
            return START_STICKY;

            //return START_NOT_STICKY;

        }


    }

    private int getIntentIcon(boolean loud) {
        if (loud) {
            return R.drawable.ic_notifications_off_white_18dp;
        } else {
            return R.drawable.ic_notifications_white_18dp;
        }
    }

    private String getIntentText(boolean loud) {
        if (loud) {
            return "IZKLOPI OBVESTILA";
        } else {
            return "VKLOPI OBVESTILA";
        }
    }

    private PendingIntent getIntent(boolean loud) {
        if (loud) {
            Intent silentIntent = new Intent(this, CountersForegroundService.class);
            silentIntent.setAction(ServiceConstants.ACTION.SILENT_ACTION);
            PendingIntent pSilentIntent = PendingIntent.getService(this, 0, silentIntent, 0);
            return pSilentIntent;
        } else {
            Intent loudIntent = new Intent(this, CountersForegroundService.class);
            loudIntent.setAction(ServiceConstants.ACTION.LOUD_ACTION);
            PendingIntent pLoudIntent = PendingIntent.getService(this, 0, loudIntent, 0);
            return pLoudIntent;
        }
    }

    private void showNotification(boolean loud, boolean noAction) {

        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);

        notificationBuilder.setContentTitle("Števci prometa")
                .setTicker("Aplikacija se izvaja v ozadju")
                .setContentText("Aplikacija se izvaja v ozadju")
                .setSmallIcon(R.drawable.ic_notification_run)
                .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .addAction(getIntentIcon(loud), getIntentText(loud), getIntent(loud));

        if (noAction) {
            notificationBuilder.mActions.clear();
        }

        Notification noti = notificationBuilder.build();

        startForeground(ServiceConstants.NOTIFICATION_ID.FOREGROUND_SERVICE, noti);

    }

    @Override
    public void onDestroy() {
        unregisterReceiver(mIntentMessageReceiver);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Used only in case if services are bound (Bound Services).
        return null;
    }

    public void setAlarmClockAlarm(long nextTriggerSeconds) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            AlarmManager am = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);

            Intent intent = new Intent(this, CountersForegroundService.AlarmClockReceiver.class);
            intent.setAction("com.kvajpoj.counters.ALARM_ALERT_ACTION");

            PendingIntent sender = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            am.setAlarmClock(new AlarmManager.AlarmClockInfo(nextTriggerSeconds, sender), sender);
        }
    }


    public void cancelAlarmClockAlarm() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            Log.i("CountersMService", "Canceling alarm clock ...");
            AlarmManager am = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(this, CountersForegroundService.AlarmClockReceiver.class);
            intent.setAction("com.kvajpoj.counters.ALARM_ALERT_ACTION");
            am.cancel(PendingIntent.getBroadcast(this, 0, intent, 0));
        }
    }


    public static class AlarmClockReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intnt) {

            Log.i("CountersMService", "Received " + intnt.getAction());

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                    Log.i("CountersMService", "Setting alarm clock ...");
                    AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                    Intent intent = new Intent(context, CountersForegroundService.AlarmClockReceiver.class);
                    intent.setAction("com.kvajpoj.counters.ALARM_ALERT_ACTION");
                    PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    am.setAlarmClock(new AlarmManager.AlarmClockInfo(System.currentTimeMillis() + (10 * 60) * 1000, sender), sender);
                }

                //setAlarmClockAlarm(System.currentTimeMillis() + (10 * 60) * 1000); //10 min
            }

        }


    }

    public class IntentMessageReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {


            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                Log.i("CountersMService", Intent.ACTION_SCREEN_OFF);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    setAlarmClockAlarm(System.currentTimeMillis() + (10 * 60) * 1000); //10 min
                }
            } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                Log.i(LOG_TAG, Intent.ACTION_SCREEN_ON);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    cancelAlarmClockAlarm();
                }
            } else if (intent.getAction().equals(ServiceConstants.ACTION.HIDE_ACTION)) {

                Log.i(LOG_TAG, "Received intent " + intent.getAction());
                showNotification(PreferencesDAO.getInstance(CountersForegroundService.this).isNotifications(), true);

            } else if (intent.getAction().equals(ServiceConstants.ACTION.SHOW_ACTION)) {

                Log.i(LOG_TAG, "Received intent " + intent.getAction());
                showNotification(PreferencesDAO.getInstance(CountersForegroundService.this).isNotifications(), false);

            }

        }
    }

}