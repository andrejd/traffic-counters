package com.kvajpoj.dao;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.media.RingtoneManager;
import android.os.AsyncTask;
import android.support.v4.app.NotificationManagerCompat;
//import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.kvajpoj.R;
import com.kvajpoj.activities.CounterDetailActivity;
import com.kvajpoj.activities.MainActivity;
import com.kvajpoj.events.BusEvent;
import com.kvajpoj.models.Counter;
import com.kvajpoj.models.CounterEvent;
import com.kvajpoj.services.TrafficFeed;
import com.kvajpoj.services.TrafficFeedService;
import com.kvajpoj.utils.ConnectivityHelper;
import com.kvajpoj.utils.ServiceGenerator;

import org.greenrobot.eventbus.EventBus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Andrej on 29.8.2015.
 */
public final class CountersDAO {

    public static final String EVENT_DATA_LOAD_ERROR = "DataLoadError";
    public static final String EVENT_DATA_LOAD = "DataLoaded";
    public static final String EVENT_DATA_UP_TO_DATE = "DataUpToDate";
    public static final String NO_INTERNET_CONNECTION = "NoInternetConnection";
    private static CountersDAO instance = null;
    private static Realm realm = null;
    private EventBus bus = EventBus.getDefault();


    private CountersDAO() {

    }

    public static synchronized CountersDAO getInstance() {
        if (instance == null) {
            instance = new CountersDAO();
        }
        return instance;
    }


    /*public void CleanDatabase(Context context) {

        try {
            realm = Realm.getDefaultInstance();
            realm.beginTransaction();

            realm.delete(Counter.class);
            realm.commitTransaction();
            realm.close();
        }
        catch (Exception ex)
        {
            Log.e("Counters", ex.getMessage());
        }
        finally {
            realm.close();
        }

    }*/


    public RealmResults<Counter> findFavorites(Context context) {
        RealmResults<Counter> tmp = null;
        try {
            realm = Realm.getDefaultInstance();
            tmp = realm.where(Counter.class).equalTo("Favorite", true).findAll().sort("Position", Sort.DESCENDING);
            //realm.close();
            return tmp;
        }
        catch (Exception exception)
        {
            Log.e("Counters", exception.getMessage());
        }
        finally {
            //realm.close();
        }

        return tmp;
    }

    public int getDataExpiresTime() {
        Counter tmp = null;
        try {
            Realm realm = Realm.getDefaultInstance();
            tmp = realm.where(Counter.class).findFirst();
        }
        catch (Exception exception) {
            Log.e("Counters", exception.getMessage());
        }
        finally {
            realm.close();
        }

        if(tmp != null) return tmp.getExpires();
        return 0;
    }

    /*public int getUpdateTime() {

        Counter tmp = null;
        try {
            Realm realm = Realm.getDefaultInstance();
            tmp = realm.where(Counter.class).findFirst();
        }
        catch (Exception exception) {
            Log.e("Counters", exception.getMessage());
        }
        finally {
            realm.close();
        }

        if(tmp != null) return tmp.getUpdated();
        return 0;
    }*/

    public RealmResults<Counter> findFavorites(Realm realm)
    {
        RealmResults<Counter> tmp = null;
        try {
            tmp = realm.where(Counter.class).equalTo("Favorite", true).findAll().sort("Position", Sort.DESCENDING);
            return tmp;
        }
        catch (Exception exception) {
            Log.e("Counters", exception.getMessage());
        }

        return tmp;
    }

    public RealmResults<Counter> findAll(Context context, String query) {
        RealmResults<Counter> tmp = null;
        try {
            realm = Realm.getDefaultInstance();
            if (query.matches("")) {
                tmp = realm.where(Counter.class).findAll();
            }
            else {
                tmp = realm.where(Counter.class)
                        .contains("Location", query, Case.INSENSITIVE)
                        .or()
                        .contains("Direction", query, Case.INSENSITIVE)
                        .findAll();
            }
            return tmp.sort(new String[]{"Status", "Location"}, new Sort[]{Sort.DESCENDING, Sort.ASCENDING});
        }
        catch(Exception exception) {
            Log.e("Counters", exception.getMessage());
        }
        return tmp;
    }

    public void refreshCounters(Context context, boolean wifi, boolean mobile) {

        if (wifi && ConnectivityHelper.isConnectedWifi(context)) {
            refreshCounters(context, false);
        }
        else if (mobile && ConnectivityHelper.isConnectedMobile(context)) {
            refreshCounters(context, false);
        }
        else if ((wifi || mobile) && !ConnectivityHelper.isConnected(context)) {
            refreshCounters(context, true);
        }
    }

    public void refreshCounters(final Context context, final Boolean noConnection) {

        // if we have internet connection, we go to server and pull new data...
        if(!noConnection) {
            TrafficFeedService tcs = ServiceGenerator.createService(
                    TrafficFeedService.class,
                    TrafficFeedService.BASE_URL
            );

            tcs.getFeed(new Callback<TrafficFeed>() {
                @Override
                public void success(TrafficFeed trafficFeed, Response response) {
                    new ProcessCounterData(trafficFeed, response, noConnection, context).execute();
                }

                @Override
                public void failure(RetrofitError error) {
                    bus.post(new BusEvent(EVENT_DATA_LOAD_ERROR, error.getMessage()));
                }
            });
        }
        else { // there is no internet connection, so we just clean up
            new ProcessCounterData(null, null, noConnection, context).execute();
        }

    }

    private class ProcessCounterData extends AsyncTask<Void, Void, BusEvent> {

        TrafficFeed trafficFeed;
        Response response;
        boolean noConnection;
        Context mContext;

        public ProcessCounterData(TrafficFeed tf, Response rsp, boolean noConnection, Context context) {
            this.trafficFeed = tf;
            this.response = rsp;
            this.noConnection = noConnection;
            this.mContext = context;
        }

        @Override
        protected BusEvent doInBackground(Void... params) {

            boolean changes = false;
            int updated;
            Realm privateRealm = null;

            if(noConnection) {
                try {
                    Log.i("Counters", "Removing stale object despite lacking internet connection!");
                    removeStaleEvents((int) (new Date().getTime() / 1000));

                }
                catch (Exception ex) {
                    Log.e("Counters", "Exception during data cleaning: " + ex.getMessage());
                }
                finally {
                    return new BusEvent(NO_INTERNET_CONNECTION, "");
                }
            }

            try {
                if (trafficFeed == null) {

                    String msg = "Napaka pri obdelavi prejetih podatkov!";

                    //Try to get response body
                    BufferedReader reader;
                    StringBuilder sb = new StringBuilder();
                    try {
                        reader = new BufferedReader(new InputStreamReader(response.getBody().in()));
                        String line;

                        try {
                            while ((line = reader.readLine()) != null) {
                                sb.append(line);
                            }
                        }
                        catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }

                    if (sb.toString().equals("null")) {
                        msg = "Napaka na strežniku! Poiskusite ponovno!";
                    }
                    return new BusEvent(EVENT_DATA_LOAD_ERROR, msg);
                }//end of traffic feed == null

                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

                GregorianCalendar gc = new GregorianCalendar();
                Log.i("Counters", "Current time " + gc.getTime().toString()
                        + " Timezone offset " + gc.getTimeZone().getRawOffset());

                String updatedTimeStr = trafficFeed.getContents().get(0).getModifiedTime();
                String expiresTimeStr = trafficFeed.getContents().get(0).getExpires();
                //2016-11-19T19:52:04.0511684Z
                DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSS'Z'");
                Date result1 = df1.parse(updatedTimeStr);
                Date dataExpiresTime = df1.parse(expiresTimeStr);

                int expiresTime = (int) (dataExpiresTime.getTime()/1000L);
                expiresTime += (int)(gc.getTimeZone().getRawOffset()/1000L);

                int updatedTime = (int) (result1.getTime()/1000L);
                updatedTime += (int)(gc.getTimeZone().getRawOffset()/1000L);

                updated = updatedTime;

                privateRealm = Realm.getDefaultInstance();
                privateRealm.beginTransaction();

                int notifications           = 0;
                String notificationHeader   = "";
                String notificationText     = "";
                String notifictionIds       = "";

                for (TrafficFeed.TrafficCounter tc : trafficFeed.getCounters()) {

                    Counter c = privateRealm.where(Counter.class).equalTo("Id", tc.getId()).findFirst();
                    if (c == null) {
                        c = new Counter();
                        c.setId(tc.getId());
                    }

                    if (c.getUpdated() != updated) {

                        c.setUpdated(updated);
                        c.setExpires(expiresTime);

                        // update counter data
                        c.setRegion(tc.getStevci_regija());

                        if (tc.getStevci_pasOpis() != null && !tc.getStevci_pasOpis().equals(""))
                            c.setLane(tc.getStevci_pasOpis());

                        c.setLocation(tc.getStevci_lokacijaOpis());
                        c.setRoad(tc.getStevci_cestaOpis());
                        c.setDirection(tc.getStevci_smerOpis());
                        c.setTitle(tc.getTitle());
                        c.setStatus(tc.getStevci_stat());


                        // status 0 means there was no data, so we
                        if (c.getStatus() == 0) c.setStatus(-1);

                        if (c.getStatus() == 6) c.setStatus(0);

                        // create event object
                        CounterEvent e = privateRealm.createObject(CounterEvent.class);

                        e.setUpdated(updated);

                        if(gc.getTimeZone().inDaylightTime(new Date())) {
                            e.setUpdated(e.getUpdated() + 60*60);
                        }

                        e.setTime(sdf.format(new Date(e.getUpdated()*1000L)));
                        e.setBusyIndex(tc.getStevci_occ());
                        e.setAvgGap(tc.getStevci_gap()); // gap between cars, in seconds
                        e.setMaxSpeed(tc.getStevci_vmax()); // max speed of the road
                        e.setNbrOfCars(tc.getStevci_stev()); // number of cars / hour
                        e.setStatus(tc.getStevci_stat());
                        e.setStatusOpis(tc.getStevci_statOpis());

                        // status 0 means there was no data, so we
                        if (e.getStatus() == 0) e.setStatus(-1);
                        if (e.getStatus() == 6) e.setStatus(0);

                        float as;
                        try {
                            as = Float.parseFloat(tc.getStevci_hit().replace(",", "."));
                        }
                        catch (NumberFormatException err) {
                            as = 0;
                        }
                        e.setAvgSpeed((int)as);

                        ////////////////////////////////////////////////////////////////////////////
                        // notifications

                        // TODO: Check if notifications are turned on
                        //CounterEvent prev = c.getEvents().last();
                        CounterEvent prev = null;
                        if(c.getEvents().size() > 0) {
                            prev = c.getEvents().last();
                        }

                        int statusTreshold = 5;

                        // we clear notification time if status is below threshold for two reads
                        if(prev != null && c.getFavorite() && e.getStatus() < statusTreshold && prev.getStatus() < statusTreshold) {
                            c.setNotification(0);
                        }

                        if(prev != null && (e.getUpdated() - prev.getUpdated() < 11*60) &&
                                prev.getStatus() >= statusTreshold && e.getStatus() >= statusTreshold &&
                                c.getFavorite() && c.getNotification() == 0)
                        {
                            notifictionIds += c.getId() + " ";
                            notifications++;
                            notificationHeader += c.getLocation() + ", " + c.getDirection() + " " + c.getLane()  + "; ";
                            notificationText = e.getStatusOpis();
                            c.setNotification((int) (new Date().getTime()/1000L));
                        }

                        // add event to Counter
                        c.getEvents().add(e);

                        while(c.getEvents().size() > 144) { // 6 events/per hour * 24 hours
                            c.getEvents().first().deleteFromRealm();
                        }

                        changes = true;
                        privateRealm.copyToRealmOrUpdate(c);
                    }
                } //end of looping trough objects ...

                ////////////////////////////////////////////////////////////////////////////////////
                // notifications
                if(notifications > 0 && PreferencesDAO.getInstance(mContext).isNotifications()) {
                    NotificationManagerCompat nm = NotificationManagerCompat.from(mContext);

                    // Prepare intent which is triggered if the
                    // notification is selected
                    Intent intent;

                    if(notifications == 1) {
                        intent = new Intent(mContext, CounterDetailActivity.class);
                        intent.putExtra("COUNTER_ID",notifictionIds.trim());
                    }
                    else {
                        intent = new Intent(mContext, MainActivity.class);
                    }

                    PendingIntent pIntent = PendingIntent.getActivity(mContext, (int) System.currentTimeMillis(), intent, 0);

                    // Build notification
                    // Actions are just fake
                    String substring = notificationHeader.substring(0, notificationHeader.length() - 2);
                    Notification noti = new Notification.Builder(mContext)
                            .setLargeIcon(((BitmapDrawable) mContext.getResources().getDrawable(getNotificationIcon())).getBitmap())
                            .setContentTitle(notificationText)
                            .setAutoCancel(true)
                            .setContentText(substring)
                            .setStyle(new Notification.BigTextStyle().bigText(substring))
                            .setSmallIcon(R.drawable.ic_stat_notification)
                            .setContentIntent(pIntent)
                            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                            .setVibrate(new long[]{0, 100, 200, 300})
                            .build();

                    noti.defaults |= Notification.DEFAULT_SOUND;
                    noti.defaults |= Notification.DEFAULT_VIBRATE;
                    noti.defaults |= Notification.DEFAULT_LIGHTS;
                    noti.flags |= Notification.FLAG_AUTO_CANCEL;

                    // calculate id
                    Log.i("Counters", "Showing notification: " + notificationHeader + ": " + notificationText);

                    if(notifications > 1) {
                        nm.notify(200, noti);
                    }
                    else {
                        // get id from
                        int id = Integer.parseInt(notifictionIds.trim().replace("-",""));
                        nm.notify(id, noti);
                    }
                }
                ////////////////////////////////////////////////////////////////////////////////////
                if (changes) {
                    privateRealm.commitTransaction();
                    privateRealm.close();
                    removeStaleEvents(updated);
                    return new BusEvent(EVENT_DATA_LOAD, "" + updated + "-" + expiresTime );
                }
                else {
                    privateRealm.cancelTransaction();
                    privateRealm.close();
                    removeStaleEvents(updated);
                    return new BusEvent(EVENT_DATA_UP_TO_DATE, "" + updated + "-" + expiresTime );
                }
            }
            catch (Exception exception) {

                Log.e("Counters", "Exceprion during data handling: " + exception.getMessage());
                if(privateRealm != null) {
                    privateRealm.cancelTransaction();
                    privateRealm.close();
                }
                return new BusEvent(EVENT_DATA_LOAD_ERROR, exception.getMessage());
            }
        }

        private void removeStaleEvents(int updated)
        {
            Realm myRealm = Realm.getDefaultInstance();
            RealmResults<CounterEvent> q = myRealm.where(CounterEvent.class).findAll();
            Log.i("Counters", "There are total events " + q.size());
        }

        private int getNotificationIcon() {
            boolean whiteIconRequired = (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP);
            return whiteIconRequired ? R.drawable.ic_level_5_black : R.drawable.ic_level5;
        }

        @Override
        protected void onPostExecute(BusEvent event) {
            super.onPostExecute(event);
            bus.post(event);
        }
    }



}


