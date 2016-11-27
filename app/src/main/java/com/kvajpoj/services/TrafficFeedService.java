package com.kvajpoj.services;

import retrofit.Callback;
import retrofit.http.GET;

/**
 * Created by Andrej on 26.8.2015.
 */
public interface TrafficFeedService {

    String BASE_URL = "http://opendata.si";

    @GET("/promet/counters/")
    void getFeed(Callback<TrafficFeed> callback);
}
