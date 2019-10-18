package com.example.chisu.myapplication.ar.model;

import android.location.Location;

/**
 * Created by ntdat on 1/16/17.
 */

//카메라에 나타나는 Arpoint를 정의하는 클래스.
public class ARPoint {
    Location location;
    String name;

    //생성자. ARpoint는 이름과 위치를 가진다.
    public ARPoint(String name, double lat, double lon, double altitude) {
        this.name = name;
        location = new Location("ARPoint");
        location.setLatitude(lat);
        location.setLongitude(lon);
        //고도
        location.setAltitude(altitude);
    }

    public Location getLocation() {
        return location;
    }

    public String getName() {
        return name;
    }
}
