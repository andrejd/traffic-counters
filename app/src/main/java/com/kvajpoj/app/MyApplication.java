package com.kvajpoj.app;

import android.app.Application;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by Andrej on 29.8.2015.
 */
public class MyApplication extends Application {
    @Override
    public void onCreate() {

        Realm.init(this);
        RealmConfiguration config = new RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .name("counters.realm")
                .build();

        Realm.setDefaultConfiguration(config);
        //Realm.deleteRealm(config);

        super.onCreate();
    }
}