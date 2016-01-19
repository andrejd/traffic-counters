package com.kvajpoj.models;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
//import io.realm.annotations.Ignore;

/**
 * Created by Andrej on 27.8.2015.
 */
public class Counter extends RealmObject {

    @PrimaryKey
    private String Id;
    private String Region = "";
    private String Location = "";
    private String Road = "";
    private String Direction = "";
    private String Lane = "";
    private int Updated = 0;
    private String Title = "";
    private int Status;
    private boolean Favorite = false;
    private int Position = 0;
    private int Notification = 0;
    private RealmList<CounterEvent> Events = new RealmList<>();

    public int getNotification() {
        return Notification;
    }

    public void setNotification(int notification) {
        Notification = notification;
    }

    public boolean getFavorite() {
        return Favorite;
    }

    public void setFavorite(boolean favorite) {
        Favorite = favorite;
    }

    public int getPosition() {
        return Position;
    }

    public void setPosition(int position) {
        Position = position;
    }

    public int getStatus() {
        return Status;
    }

    public void setStatus(int status) {
        Status = status;
    }

    public RealmList<CounterEvent> getEvents() {
        return Events;
    }

    public void setEvents(RealmList<CounterEvent> events) {
        Events = events;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public int getUpdated() {
        return Updated;
    }

    public void setUpdated(int updated) {
        Updated = updated;
    }

    public String getLane() {
        return Lane;
    }

    public void setLane(String lane) {
        Lane = lane;
    }

    public String getDirection() {
        return Direction;
    }

    public void setDirection(String direction) {
        Direction = direction;
    }

    public String getRoad() {
        return Road;
    }

    public void setRoad(String road) {
        Road = road;
    }

    public String getLocation() {
        return Location;
    }

    public void setLocation(String location) {
        Location = location;
    }

    public String getRegion() {
        return Region;
    }

    public void setRegion(String region) {
        Region = region;
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }


}
