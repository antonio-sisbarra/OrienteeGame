package com.sisbarra.orienteegame;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Antonio Sisbarra on 21/06/2017.
 * Classe che incapsula tutto lo svolgimento di una partita fino alla sua conclusione.
 */

class Partita {

    private static int MULTIPLICATOR_PRIZE = 5;
    private static int SECONDS_FOR_LESSPRIZE = 10;

    private LatLng mTarget;
    private LatLng mActualLocation;
    private int mDistance;
    private int mPrize;
    private String mTargetTitle;
    private Timer mTimer; //Timer che diminuisce il punteggio

    public Partita(LatLng target, LatLng actualLocation, String title) {
        mTarget = target;
        mActualLocation = actualLocation;
        mDistance = calculateDistance(mActualLocation, mTarget);
        mPrize = mDistance * MULTIPLICATOR_PRIZE;
        mTargetTitle = title;
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                decreasePrize(10);
            }
        }, SECONDS_FOR_LESSPRIZE * 1000);
    }

    //Il metodo finishMatch ritorna il prize
    int finishMatch(){
        return mPrize;
    }

    private void decreasePrize(int decrease){
        mPrize = mPrize - decrease;
    }

    //Calcola la distanza in m tra due Location
    private int calculateDistance(LatLng start, LatLng end){
        double lat_a = start.latitude, lat_b = end.latitude;
        double lng_a = start.longitude, lng_b = end.longitude;

        double earthRadius = 3958.75;
        double latDiff = Math.toRadians(lat_b-lat_a);
        double lngDiff = Math.toRadians(lng_b-lng_a);
        double a = Math.sin(latDiff /2) * Math.sin(latDiff /2) +
                Math.cos(Math.toRadians(lat_a)) * Math.cos(Math.toRadians(lat_b)) *
                        Math.sin(lngDiff /2) * Math.sin(lngDiff /2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double distance = earthRadius * c;

        int meterConversion = 1609;

        Double res = distance * meterConversion;
        return res.intValue();
    }

    /** GETTER E SETTER PER I VARI CAMPI **/
    public LatLng getTarget() {
        return mTarget;
    }

    public void setTarget(Location target) {
        mTarget = new LatLng(target.getLatitude(),
                target.getLongitude());
    }

    public LatLng getActualLocation() {
        return mActualLocation;
    }

    public void setActualLocation(Location actualLocation) {
        mActualLocation = new LatLng(
                actualLocation.getLatitude(), actualLocation.getLongitude());
    }

    public int getDistance() {
        return mDistance;
    }

    public void setDistance(int distance) {
        mDistance = distance;
    }

    public void setPrize(int prize) {
        mPrize = prize;
    }

    public String getTargetTitle() {
        return mTargetTitle;
    }

    public void setTargetTitle(String targetTitle) {
        mTargetTitle = targetTitle;
    }
}
