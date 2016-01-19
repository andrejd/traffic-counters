package com.kvajpoj.models;

import io.realm.RealmObject;

/**
 * Created by Andrej on 9.9.2015.
 */
public class Settings extends RealmObject {

    public static final int STATE_FAVORITES = 0;
    public static final int STATE_LIST = 1;
    public static final int STATE_MAP = 2;

    private int state = STATE_FAVORITES;


    public Settings() {

    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }
}
