package com.sisbarra.orienteegame;


import android.content.Intent;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;

import static android.content.ContentValues.TAG;


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


    public OthersFragment() {
        // Required empty public constructor
    }


    public static OthersFragment newInstance() {
        OthersFragment fragment = new OthersFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_others, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Carico il JSON dei percorsi degli altri
        new LoadingPathTask().execute();
    }

    //Carica dagli assets il file Json
    private void loadData(){
        try {
            AssetManager assetManager = getActivity().getAssets();
            InputStream ims = assetManager.open(getString(R.string.otherpaths_filename));

            Gson gson = new Gson();
            Reader reader = new InputStreamReader(ims);

            percorsi = gson.fromJson(reader, new TypeToken<ArrayList<Percorso>>(){}.getType());

        }catch(IOException e) {
            Log.e(TAG, e.toString());
            getActivity().finish();
        }
    }

    //Dato un percorso fa partire la Path on map Activity con quel percorso come intent
    void startPathMapActivity(Percorso p){
        Intent intent = new Intent(getContext(), PathOnMapsActivity.class);
        intent.putExtra(getString(R.string.percorso_intent_name),
                (new Gson()).toJson(p));
        startActivity(intent);
    }

    /**
     * Called when the Fragment is no longer started.  This is generally
     * tied to {Activity#onStop() Activity.onStop} of the containing
     * Activity's lifecycle.
     */
    @Override
    public void onStop() {
        super.onStop();
    }

    //Classe privata che mi serve per caricare il JSON dagli asset
    private class LoadingPathTask extends AsyncTask<Void, Void, ArrayList<Percorso>> {

        //Carica in background il JSON dei percorsi degli altri
        @Override
        protected ArrayList<Percorso> doInBackground(Void... params) {
            //CARICAMENTO DEI DATI JSON DAGLI ASSETS
            loadData();
            return percorsi;
        }

        //Crea l'adapter e lo collega alla list dei percorsi
        @Override
        protected void onPostExecute(ArrayList<Percorso> percorsos) {
            super.onPostExecute(percorsos);

            //CREAZIONE DI ADAPTER E COLLEGAMENTO CON LA LISTVIEW
            final PathsAdapter adapter = new PathsAdapter(getContext(), percorsos);
            // Attach the adapter to a ListView
            mPaths = (ListView) getActivity().findViewById(R.id.otherPaths_listview);
            mPaths.setAdapter(adapter);
            mPaths.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    startPathMapActivity(adapter.getItem(position));
                }
            });
            getActivity().findViewById(R.id.loadingPathPanel).setVisibility(View.GONE);
        }
    }
}
