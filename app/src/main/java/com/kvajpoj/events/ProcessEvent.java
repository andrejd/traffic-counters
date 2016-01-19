package com.kvajpoj.events;

/**
 * Created by Andrej on 9.10.2015.
 */
public class ProcessEvent {

    public static final int HOUR24 = 687;
    public static final int HOUR08 = 203;
    public static final int HOUR01 = 205;


    private int mMode;

    public ProcessEvent(int mode) {
        mMode = mode;


    }

    public int getMode() {
        return mMode;
    }

    public void setMode(int mode) {
        mMode = mode;
    }
}
