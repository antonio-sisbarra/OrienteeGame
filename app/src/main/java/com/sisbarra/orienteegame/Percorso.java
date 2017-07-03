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

    public Percorso(String nameTarget, LatLng targ, String player){
        mLatLngs = new ArrayList<LatLng>();
        mNameTarget = nameTarget;
        mTarget = targ;
        mNamePlayer = player;
    }

    //Aggiunge il punto alla lista (se l'ultimo è diverso da quello che voglio aggiungere)
    void addPoint(LatLng p) {
        if (isNew(p))
            mLatLngs.add(p);
    }

    //Metodo private che mi dice se la posizione che si vuole aggiungere è nuova oppure no
    private boolean isNew(LatLng p) {
        if (p == null) return false;
        if (getSize() == 0) return true;
        LatLng lastP = mLatLngs.get(mLatLngs.size() - 1);
        double lastLat = lastP.latitude;
        double lastLong = lastP.longitude;
        return !(p.latitude == lastLat && p.longitude == lastLong);
    }

    int getSize() {
        return mLatLngs.size();
    }

    ArrayList<LatLng> getPointsLists() {
        return new ArrayList<LatLng>(mLatLngs);
    }

    public LatLng getTarget() {
        return mTarget;
    }

    String getNameTarget() {
        return mNameTarget;
    }

    String getNamePlayer() {
        return mNamePlayer;
    }

    int getPrize() {
        return mPrize;
    }

    void setPrize(int prize) {
        mPrize = prize;
    }
}
