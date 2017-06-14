package com.sisbarra.orienteegame;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Created by Antonio Sisbarra on 14/06/2017.
 */

public class Targets {

    ArrayList<LatLng> mTargets;

    public Targets(ArrayList<LatLng> targets) {
        mTargets = targets;
    }

    public LatLng getSimpleTarget(LatLng position){
        /*TODO: Restituisce un LatLng che dista dalla posizione attuale meno di 500 m */
        return position;
    }

    public LatLng getMediumTarget(LatLng position){
        /*TODO: Restituisce un LatLng che dista dalla posizione attuale meno di 1500 m e più di 500 m */
        return position;
    }

    public LatLng getHardTarget(LatLng position){
        /*TODO: Restituisce un LatLng che dista dalla posizione attuale non meno di 1500 m */
        return position;
    }

    public ArrayList<LatLng> getTargets(int nSimple, int nMedium, int nHard){
        /*TODO: Restituisce una lista di target ordinata dal più semplice al più complesso */
        return new ArrayList<LatLng>();
    }

    public void loadTargets(ArrayList<LatLng> targets){
        /*TODO: Carica nuovi target */
    }
}
