package com.mantraideas.audiotour;

/**
 * Created by rowsun on 11/23/16.
 */

public class Landmarks {
    String title;
    public Double geo_lat, geo_lng;

    public Landmarks(){

    }

    public Landmarks(String title, Double geo_lat, Double geo_lng) {
        this.title = title;
        this.geo_lat = geo_lat;
        this.geo_lng = geo_lng;
    }
}
