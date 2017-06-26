package com.sisbarra.orienteegame;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by Antonio Sisbarra on 23/06/2017.
 * Classe che incapsula la lista di Punti del cammino verso il target e altre info.
 */

public class Percorso {

    @SerializedName("points")
    private ArrayList<LatLng> mLatLngs;

    @SerializedName("target")
    private LatLng mTarget;

    @SerializedName("nameTarget")
    private String mNameTarget;

    @SerializedName("namePlayer")
    private String mNamePlayer;

    @SerializedName("prize")
    private int mPrize;

    public Percorso(String nameTarget, LatLng targ, String player, int prize){
        mLatLngs = new ArrayList<LatLng>();
        mNameTarget = nameTarget;
        mTarget = targ;
        mNamePlayer = player;
        mPrize = prize;
    }

    //Aggiunge il punto alla lista
    public void addPoint(LatLng p){
        mLatLngs.add(p);
    }

    public int getSize(){
        return mLatLngs.size();
    }

    public ArrayList<LatLng> getPointsLists(){
        return new ArrayList<LatLng>(mLatLngs);
    }

    public LatLng getTarget() {
        return mTarget;
    }

    public String getNameTarget() {
        return mNameTarget;
    }

    String getNamePlayer() {
        return mNamePlayer;
    }

    public int getPrize() {
        return mPrize;
    }
}
