package com.kvajpoj.models;

import io.realm.RealmObject;

/**
 * Created by Andrej on 2.9.2015.
 */
public class CounterEvent extends RealmObject {

    private String Id;
    private int Updated;
    private int NbrOfCars;
    private int Status;
    private int AvgSpeed;
    private String AvgGap = "";
    private int BusyIndex;
    private int MaxSpeed;
    private String Time = "";
    private String StatusOpis = "";

    public String getStatusOpis() {
        return StatusOpis;
    }

    public void setStatusOpis(String statusOpis) {
        StatusOpis = statusOpis;
    }

    public String getTime() { return Time; }

    public void setTime(String time) { Time = time; }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public int getUpdated() {
        return Updated;
    }

    public void setUpdated(int updated) {
        Updated = updated;
    }

    public int getNbrOfCars() {
        return NbrOfCars;
    }

    public void setNbrOfCars(int nbrOfCars) {
        NbrOfCars = nbrOfCars;
    }

    public int getStatus() {
        return Status;
    }

    public void setStatus(int status) {
        Status = status;
    }

    public int getAvgSpeed() {
        return AvgSpeed;
    }

    public void setAvgSpeed(int avgSpeed) {
        AvgSpeed = avgSpeed;
    }

    public String getAvgGap() {
        return AvgGap;
    }

    public void setAvgGap(String avgGap) {
        AvgGap = avgGap;
    }

    public int getBusyIndex() {
        return BusyIndex;
    }

    public void setBusyIndex(int busyIndex) {
        BusyIndex = busyIndex;
    }

    public int getMaxSpeed() {
        return MaxSpeed;
    }

    public void setMaxSpeed(int maxSpeed) {
        MaxSpeed = maxSpeed;
    }
}
