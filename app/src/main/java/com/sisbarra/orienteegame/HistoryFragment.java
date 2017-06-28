package com.sisbarra.orienteegame;


import android.content.Context;
import android.content.Intent;
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

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;
import static com.sisbarra.orienteegame.R.string.mypaths_filename;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HistoryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HistoryFragment extends Fragment {
    //Riferimenti agli elementi grafici
    private ListView mPaths;

    //Riferimenti a strutture dati
    private ArrayList<Percorso> percorsi;

    //Arraryadapter per i dati
    private MyPathsAdapter mAdapter;


    public HistoryFragment() {
        // Required empty public constructor
    }


    public static HistoryFragment newInstance() {
        return new HistoryFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    /**
     * Called when the Fragment is visible to the user.  This is generally
     * tied to {Activity.onStart} of the containing
     * Activity's lifecycle.
     */
    @Override
    public void onStart() {
        super.onStart();

        //Carico il JSON dei miei percorsi (se non ci sono percorsi)
        if(percorsi == null)
            new HistoryFragment.LoadingPathTask().execute();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    //Carica dallo storage interno il file Json con i miei percorsi personali
    private void loadData() {
        try {
            InputStream ims = getActivity().openFileInput(getString(mypaths_filename));

            Gson gson = new Gson();
            Reader reader = new InputStreamReader(ims);

            percorsi = gson.fromJson(reader, new TypeToken<List<Percorso>>() {
            }.getType());

            ims.close();

        } catch (FileNotFoundException e) {
            //File non trovato lo devo creare
            Gson gson = new Gson();
            percorsi = new ArrayList<Percorso>();
            try {
                FileOutputStream fos = getActivity().openFileOutput(getString(mypaths_filename)
                        , Context.MODE_PRIVATE);
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos));
                Type listOfPercorsi = new TypeToken<List<Percorso>>(){}.getType();
                writer.write(gson.toJson(percorsi, listOfPercorsi));
                writer.flush();
                fos.close();
            } catch (Exception e1) {
                Log.e(TAG, e1.toString());
                getActivity().finish();
            }
        } catch (IOException e) {
            Log.e(TAG, e.toString());
            getActivity().finish();
        }
    }

    //Salva nello storage interno il JSON dei percorsi
    private void storePaths(){
        Type listOfPercorsi = new TypeToken<List<Percorso>>(){}.getType();
        Gson gson = new Gson();
        try {
            OutputStream fos = getActivity().openFileOutput(getString(mypaths_filename)
                    , Context.MODE_PRIVATE);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos));
            writer.write(gson.toJson(percorsi, listOfPercorsi));
            writer.flush();
            fos.close();
        } catch (Exception e1) {
            Log.e(TAG, e1.toString());
            getActivity().finish();
        }
    }

    //Dato un percorso aggiorna la lista dei MyPaths
    void refreshMyPaths(Percorso p){
        //Aggiungo il percorso alla lista dei percorsi e aggiorno l'adapter
        mAdapter.add(p);
        mAdapter.notifyDataSetChanged();
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

        //Devo salvare nel file JSON i percorsi miei effettuati fino adesso
        storePaths();
    }

    //Classe privata che mi serve per caricare il JSON dai file dell'app
    private class LoadingPathTask extends AsyncTask<Void, Void, ArrayList<Percorso>> {

        //Carica in background il JSON dei miei percorsi
        @Override
        protected ArrayList<Percorso> doInBackground(Void... params) {
            //CARICAMENTO DEI DATI JSON
            loadData();
            return percorsi;
        }

        //Crea l'adapter e lo collega alla list dei percorsi
        @Override
        protected void onPostExecute(ArrayList<Percorso> percorsos) {
            super.onPostExecute(percorsos);

            //CREAZIONE DI ADAPTER E COLLEGAMENTO CON LA LISTVIEW
            mAdapter = new MyPathsAdapter(getContext(), percorsos);
            // Attach the adapter to a ListView
            mPaths = (ListView) getActivity().findViewById(R.id.historyPaths_listview);
            mPaths.setAdapter(mAdapter);
            mPaths.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    startPathMapActivity(mAdapter.getItem(position));
                }
            });
            getActivity().findViewById(R.id.loadingMyPathPanel).setVisibility(View.GONE);
        }
    }
}
