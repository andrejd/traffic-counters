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

import java.io.File;
import java.text.SimpleDateFormat;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;

/**
 * Created by Andrej on 26.9.2015.
 */
public class CountersUpdateService extends IntentService {

    public static final String UPDATE_EXTRA = "UPDATE_EXTRA";
    public static final String ONLY_WIFI_EXTRA = "ONLY_WIFI_EXTRA";
    public static final String UPDATE_RETRIES = "com.kvajpoj.UPDATE_RETRIES";
    private Intent mIntent;
    private static int retries = 0;

    

    public CountersUpdateService() {
        super("CounterService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        String currentTIme = sdf.format(new java.util.Date());

        //Log.i("CountersUpdateService", "CountersUpdateService started at " + currentTIme);
        mIntent = intent;


        boolean onlyOnWiFi = false;

        if(mIntent.getBooleanExtra("manual", false) == false) {
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

        if(mIntent.getBooleanExtra("manual", false) == true) {
            manual = true;
        }

        switch (event.getEventType()) {
            case CountersDAO.EVENT_DATA_LOAD:
                Log.i("CountersUpdateService", "Counter data has been loaded at "+ currentTIme);
                retries = 0;
                break;

            case CountersDAO.EVENT_DATA_LOAD_ERROR:
                Log.e("CountersUpdateService", "Counter data loading error: " + event.getInfo() + ", " + currentTIme);
                if(!manual && retries < 2) PreferencesDAO.getInstance(this).retriggerCountersUpdateAlarm(20);
                retries++;
                break;

            case CountersDAO.EVENT_DATA_UP_TO_DATE:
                Log.i("CountersUpdateService", "Counter data is up to date " + currentTIme);
                if(!manual && retries < 2) PreferencesDAO.getInstance(this).retriggerCountersUpdateAlarm(20);
                retries++;
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


