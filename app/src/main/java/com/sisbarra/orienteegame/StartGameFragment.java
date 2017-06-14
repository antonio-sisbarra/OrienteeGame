package com.sisbarra.orienteegame;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * Sottoclasse di Fragment che contiene la parte iniziale di una partita
 */
public class StartGameFragment extends Fragment {

    public StartGameFragment() {
        // Required empty public constructor
    }

    /**
     * Metodo Factory per creare il fragment (senza parametri per ora)
     */
    public static StartGameFragment newInstance() {
        return new StartGameFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        /** TODO: Bisogna creare un asynctask per prendere i dati degli obiettivi
         **/

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_start_game, container, false);
    }
}
