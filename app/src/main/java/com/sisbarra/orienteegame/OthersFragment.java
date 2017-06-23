package com.sisbarra.orienteegame;


import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.gson.Gson;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link OthersFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class OthersFragment extends Fragment {

    //Riferimenti agli elementi grafici
    private ListView mPaths;

    //Riferimenti a strutture dati
    private ArrayList<Percorso> percorsi;

    //Oggetto GSON per conversioni
    private Gson  mGson = new Gson();


    public OthersFragment() {
        // Required empty public constructor
    }


    public static OthersFragment newInstance(String param1, String param2) {
        OthersFragment fragment = new OthersFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Carico il JSON dei percorsi degli altri
        new LoadingPathTask().execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_others, container, false);
    }

    //Classe privata che mi serve per caricare il JSON dagli asset
    private class LoadingPathTask extends AsyncTask<Void, Void, ArrayList<Percorso>> {

        //Carica in background il JSON dei percorsi degli altri
        @Override
        protected ArrayList<Percorso> doInBackground(Void... params) {
            //TODO: DA IMPLEMENTARE IL CARICAMENTO DEI DATI JSON DAGLI ASSETS
            return null;
        }

        //Crea l'adapter e lo collega alla list dei percorsi
        @Override
        protected void onPostExecute(ArrayList<Percorso> percorsos) {
            super.onPostExecute(percorsos);

            //TODO: DA IMPLEMENTARE LA CREAZIONE DI ADAPTER E COLLEGAMENTO CON LA LISTVIEW
        }
    }

}
