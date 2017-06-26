package com.sisbarra.orienteegame;


import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.util.ArrayList;

import static android.content.ContentValues.TAG;


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


    public HistoryFragment() {
        // Required empty public constructor
    }


    public static HistoryFragment newInstance() {
        HistoryFragment fragment = new HistoryFragment();
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
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Carico il JSON dei miei percorsi
        new HistoryFragment.LoadingPathTask().execute();
    }

    //Carica dallo storage interno il file Json con i miei percorsi personali
    private void loadData() {
        try {
            InputStream ims = getActivity().openFileInput(getString(R.string.mypaths_filename));

            Gson gson = new Gson();
            Reader reader = new InputStreamReader(ims);

            percorsi = gson.fromJson(reader, new TypeToken<ArrayList<Percorso>>() {
            }.getType());

            ims.close();

        } catch (FileNotFoundException e) {
            //File non trovato lo devo creare
            Gson gson = new Gson();
            percorsi = new ArrayList<Percorso>();
            try {
                OutputStream fos = getActivity().openFileOutput(getString(R.string.mypaths_filename)
                        , Context.MODE_PRIVATE);
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos));
                writer.write(gson.toJson(percorsi));
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

        /**
     * Called when the Fragment is no longer started.  This is generally
     * tied to {Activity#onStop() Activity.onStop} of the containing
     * Activity's lifecycle.
     */
    @Override
    public void onStop() {
        super.onStop();
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
            MyPathsAdapter adapter = new MyPathsAdapter(getContext(), percorsos);
            // Attach the adapter to a ListView
            mPaths = (ListView) getActivity().findViewById(R.id.historyPaths_listview);
            mPaths.setAdapter(adapter);
            getActivity().findViewById(R.id.loadingPathPanel).setVisibility(View.GONE);
        }
    }
}
