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
    private boolean mFinished = false; //Flag per capire se è finita la partita

    Partita(LatLng target, LatLng actualLocation, String title, int dist) {
        mTarget = target;
        mActualLocation = actualLocation;
        mDistance = dist;
        mPrize = mDistance * MULTIPLICATOR_PRIZE;
        mTargetTitle = title;
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                decreasePrize(10);
            }
        }, SECONDS_FOR_LESSPRIZE * 1000, SECONDS_FOR_LESSPRIZE * 1000);
    }

    //Il metodo finishMatch ritorna il prize
    int finishMatch(){
        mFinished = true;
        mTimer.cancel();
        return mPrize;
    }

    private void decreasePrize(int decrease){
        if (mPrize == 0) {
            mTimer.cancel();
            return;
        }
        mPrize = mPrize - decrease;
        if (mPrize < 0) {
            mPrize = 0;
            mTimer.cancel();
        }
    }

    /** GETTER E SETTER PER I VARI CAMPI **/
    public LatLng getTarget() {
        return mTarget;
    }

    public void setTarget(Location target) {
        mTarget = new LatLng(target.getLatitude(),
                target.getLongitude());
    }

    boolean isFinished(){
        return mFinished;
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

    int getPrize() {
        return mPrize;
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
