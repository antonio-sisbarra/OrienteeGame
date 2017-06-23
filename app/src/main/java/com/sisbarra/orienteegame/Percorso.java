package com.sisbarra.orienteegame;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Created by Antonio Sisbarra on 23/06/2017.
 * Classe che incapsula la lista di
 */

public class Percorso {

    private ArrayList<LatLng> mLatLngs;

    public Percorso(){
        mLatLngs = new ArrayList<LatLng>();
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

}
