package com.kvajpoj.events;

/**
 * Created by Andrej on 30.8.2015.
 */
public class BusEvent {

    private String eventType = "";
    private String mInfo;
    private Object mSender;
    private int mValue;

    public BusEvent(String type, String info) {
        this.eventType = type;
        mInfo = info;
    }

    public BusEvent(String type) {
        this.eventType = type;
    }

    public BusEvent(String type, int value) {
        mValue = value;
        this.eventType = type;
    }

    public BusEvent(Object sender, String type, String info) {
        this.eventType = type;
        mInfo = info;
        mSender = sender;
    }

    public Object getSender() {
        return mSender;
    }

    public void setSender(Object sender) {
        mSender = sender;
    }

    public int getValue() {
        return mValue;
    }

    public void setValue(int value) {
        mValue = value;
    }

    public String getInfo() {
        return mInfo;
    }

    public void setInfo(String info) {
        mInfo = info;
    }

    // interface
    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }


}
