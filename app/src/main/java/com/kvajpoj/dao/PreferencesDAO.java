package com.kvajpoj.dao;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.kvajpoj.service.CountersForegroundService;
import com.kvajpoj.service.CountersUpdateService;
import com.kvajpoj.service.ServiceConstants;

import java.util.Calendar;

/**
 * Created by Andrej on 27.9.2015.
 */
public final class PreferencesDAO implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static PreferencesDAO instance = null;
    private boolean mRefreshManual;
    private boolean mRefreshBackground;
    private boolean mRefreshOnlyOnWifi;
    private boolean mNotifications;
    private Context mContext;


    private PreferencesDAO(Context ctx) {
        mContext = ctx;
        setupUpdate(ctx);
    }

    public static synchronized PreferencesDAO getInstance(Context ctx) {
        if (instance == null) {
            instance = new PreferencesDAO(ctx);
        }
        return instance;
    }

    public boolean isRefreshOnlyOnWifi() {
        return mRefreshOnlyOnWifi;
    }

    public void setRefreshOnlyOnWifi(boolean refreshOnlyOnWifi) {
        mRefreshOnlyOnWifi = refreshOnlyOnWifi;
    }

    public boolean isNotifications() {
        return mNotifications;
    }

    public void setNotifications(boolean notifications) {
        mNotifications = notifications;
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("prefNotifications", notifications);
        editor.commit();
    }

    public boolean isRefreshManual() {
        return mRefreshManual;
    }

    public boolean isRefreshBackground() {
        return mRefreshBackground;
    }


    public void reloadValues(Context ctx)
    {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        sharedPrefs.registerOnSharedPreferenceChangeListener(this);

        mRefreshBackground = sharedPrefs.getBoolean("prefRefreshOnBackground", false);
        mRefreshOnlyOnWifi = sharedPrefs.getBoolean("prefRefreshWifiOnly", false);

        mNotifications = sharedPrefs.getBoolean("prefNotifications", false);
    }



    public void setupUpdate(Context ctx) {

        reloadValues(ctx);

        if (mRefreshBackground == true) {

            mRefreshManual = false;
            updateCounters();

        } else {

            mRefreshManual = true;
            mRefreshOnlyOnWifi = false;
            mNotifications = false;
            cancelUpdateCounters();

        }
    }

    public void updateCounters() {

        // calculate when to update counter
        Calendar c = Calendar.getInstance();
        int minutes = c.get(Calendar.MINUTE);
        int secs = c.get(Calendar.SECOND);
        int seconds = minutes * 60;
        int nextTriggerSeconds = 360 - secs - (seconds % (5 * 60));
        Log.i("CounterService", "Setting next trigger in " + nextTriggerSeconds);

        cancelCountersUpdateAlarm();
        //cancelAlarmClockAlarm();

        startCountersForegroundService(nextTriggerSeconds);
        startCountersUpdateAlarm(nextTriggerSeconds);

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setAlarmClockAlarm(System.currentTimeMillis() + (nextTriggerSeconds - 5) * 1000);
        }*/


    }


    public void cancelUpdateCounters() {
        stopCountersForegroundService();
        cancelCountersUpdateAlarm();
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            cancelAlarmClockAlarm();
        }*/
    }

    public void startCountersUpdateAlarm(int nextTriggerSeconds) {

        Intent alarmIntent = new Intent(mContext, CountersUpdateService.AlarmReceiver.class);
        alarmIntent.putExtra(CountersUpdateService.ONLY_WIFI_EXTRA, mRefreshOnlyOnWifi);
        //Wrap in a pending intent which only fires once.
        PendingIntent pi = PendingIntent.getBroadcast(mContext, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);//getBroadcast(context, 0, i, 0);
        AlarmManager am = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);

        am.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + (nextTriggerSeconds * 1000), 1000 * 60 * 5, pi); //5 minutes
    }

    public void retriggerCountersUpdateAlarm(int nextTriggerSeconds) {

        Intent alarmIntent = new Intent(mContext, CountersUpdateService.AlarmReceiver.class);
        alarmIntent.putExtra(CountersUpdateService.ONLY_WIFI_EXTRA, mRefreshOnlyOnWifi);
        alarmIntent.setAction("" + Math.random());

        //Wrap in a pending intent which only fires once.
        PendingIntent pi = PendingIntent.getBroadcast(mContext, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);//getBroadcast(context, 0, i, 0);

        AlarmManager am = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + (nextTriggerSeconds * 1000), pi); //5 minutes
    }


    public void cancelCountersUpdateAlarm() {

        Intent alarmIntent = new Intent(mContext, CountersUpdateService.AlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(mContext, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);//getBroadcast(context, 0, i, 0);
        AlarmManager am = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pi);
    }


    private void startCountersForegroundService(int nextTriggerSeconds) {

        Intent service = new Intent(mContext, CountersForegroundService.class);

        if (!CountersForegroundService.IS_SERVICE_RUNNING) {

            Log.i("CountersMService", "Starting service ...");
            service.setAction(ServiceConstants.ACTION.START_FOREGROUND_ACTION);
            service.putExtra("Delay", nextTriggerSeconds);
            CountersForegroundService.IS_SERVICE_RUNNING = true;
            mContext.startService(service);
        } else {
            Log.i("CountersMService", "Service is already running ...");
        }
    }


    private void stopCountersForegroundService() {

        if (CountersForegroundService.IS_SERVICE_RUNNING) {

            Log.i("CountersMService", "Stopping service ...");
            Intent service = new Intent(mContext, CountersForegroundService.class);
            service.setAction(ServiceConstants.ACTION.STOP_FOREGROUND_ACTION);
            CountersForegroundService.IS_SERVICE_RUNNING = false;
            mContext.startService(service);
        } else {
            Log.i("CountersMService", "Service is already stopped!");
        }
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if (key.equals("prefRefreshOnBackground")) {

            setupUpdate(mContext);

        }
        else {

            reloadValues(mContext);
        }
    }


}

