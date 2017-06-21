package com.sisbarra.orienteegame;

import android.location.Location;

/**
 * Created by Antonio Sisbarra on 21/06/2017.
 * Classe che incapsula tutto lo svolgimento di una partita fino alla sua conclusione.
 */

class Partita {

    private static int MULTIPLICATOR_PRIZE = 5;

    private Location mTarget;
    private Location mActualLocation;
    private int mDistance;
    private int mPrize;

    public Partita(Location target, Location actualLocation) {
        mTarget = new Location(target);
        mActualLocation = new Location(actualLocation);
        mDistance = calculateDistance(actualLocation, target);
        mPrize = mDistance * MULTIPLICATOR_PRIZE;
    }

    //Il metodo finishMatch ritorna il prize
    int finishMatch(){
        return mPrize;
    }

    //Calcola la distanza in m tra due Location
    private int calculateDistance(Location start, Location end){
        double lat_a = start.getLatitude(), lat_b = end.getLatitude();
        double lng_a = start.getLatitude(), lng_b = end.getLatitude();

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
    public Location getTarget() {
        return mTarget;
    }

    public void setTarget(Location target) {
        mTarget = target;
    }

    public Location getActualLocation() {
        return mActualLocation;
    }

    public void setActualLocation(Location actualLocation) {
        mActualLocation = actualLocation;
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
}
