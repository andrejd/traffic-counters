package com.kvajpoj.service;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.kvajpoj.dao.CountersDAO;
import com.kvajpoj.dao.PreferencesDAO;
import com.kvajpoj.events.BusEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import static java.lang.Math.abs;


/**
 * Created by Andrej on 26.9.2015.
 */

// Counters update service is triggered sending intent to it; intent starts service which does its
// job and terminates itself

public class CountersUpdateService extends IntentService {

    //public static final String UPDATE_EXTRA = "UPDATE_EXTRA";
    public static final String ONLY_WIFI_EXTRA = "ONLY_WIFI_EXTRA";
    //public static final String UPDATE_RETRIES = "com.kvajpoj.UPDATE_RETRIES";
    private Intent mIntent;
    private static int retries = 0;
    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
    

    public CountersUpdateService() {
        super("CounterService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mIntent = intent;
        boolean onlyOnWiFi = false;

        if(!mIntent.getBooleanExtra("manual", false)) {
            onlyOnWiFi = PreferencesDAO.getInstance(this).isRefreshOnlyOnWifi();
        }

        if(EventBus.getDefault().isRegistered(this)) EventBus.getDefault().unregister(this);
        EventBus.getDefault().register(this);
        CountersDAO.getInstance().refreshCounters(this, true, !onlyOnWiFi);
    }

    @Subscribe
    public void onEvent(BusEvent event) {

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        String currentTIme = sdf.format(new java.util.Date());

        boolean manual = false;

        if(mIntent.getBooleanExtra("manual", false)) {
            manual = true;
        }

        switch (event.getEventType()) {
            case CountersDAO.EVENT_DATA_LOAD:
                String passedData = event.getInfo();
                String validDataStr = passedData.split("-")[0];
                String expiresDataStr = passedData.split("-")[1];
                int expiresTime = Integer.parseInt(expiresDataStr);
                int validTime = Integer.parseInt(validDataStr);
                int currentTime = (int) (System.currentTimeMillis() / 1000L);
                Log.i("CountersUpdateService", "Counter data has been loaded at @"
                        + sdf.format(new Date(currentTime*1000L))
                        + ": " + sdf.format(new Date(validTime*1000L))
                        + " - data expires " + sdf.format(new Date(expiresTime*1000L)));
                retries = 0;
                if(!manual) {

                    PreferencesDAO.getInstance(this).cancelCountersUpdateAlarm();
                    PreferencesDAO.getInstance(this).startCountersUpdateAlarm((expiresTime - currentTime) + 120);
                }
                break;

            case CountersDAO.EVENT_DATA_LOAD_ERROR:
                Log.e("CountersUpdateService", "Counter data loading error: " + event.getInfo() + ", " + currentTIme);
                if(!manual && retries < 2) PreferencesDAO.getInstance(this).retriggerCountersUpdateAlarm(20);
                retries++;
                break;

            case CountersDAO.EVENT_DATA_UP_TO_DATE:
                passedData = event.getInfo();
                validDataStr = passedData.split("-")[0];
                expiresDataStr = passedData.split("-")[1];
                expiresTime = Integer.parseInt(expiresDataStr);
                validTime = Integer.parseInt(validDataStr);
                currentTime = (int) (System.currentTimeMillis() / 1000L);
                Log.i("CountersUpdateService", "Counter data is up to date @"
                        + sdf.format(new Date(currentTime*1000L))
                        + ": " + sdf.format(new Date(validTime*1000L))
                        + " - data expires " + sdf.format(new Date(expiresTime*1000L)));
                retries = 0;
                if(!manual) {
                    if(currentTime >= expiresTime) expiresTime = currentTime;
                    PreferencesDAO.getInstance(this).cancelCountersUpdateAlarm();
                    PreferencesDAO.getInstance(this).startCountersUpdateAlarm((expiresTime - currentTime) + 120);
                }
                break;

            case CountersDAO.NO_INTERNET_CONNECTION:
                Log.i("CountersUpdateService", "No internet connection! " + currentTIme);
                break;
            default:

                break;
        }

        EventBus.getDefault().unregister(this);

        if(mIntent.getAction() != Intent.ACTION_SYNC) {
            AlarmReceiver.completeWakefulIntent(mIntent);
        }
    }

    public static class AlarmReceiver extends WakefulBroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Intent sendIntent = new Intent(context, CountersUpdateService.class);
            startWakefulService(context, sendIntent);
        }
    }

    public static class BootReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            PreferencesDAO.getInstance(context);
        }
    }
}


