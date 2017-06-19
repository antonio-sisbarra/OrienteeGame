package com.sisbarra.orienteegame;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatTextView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import static com.sisbarra.orienteegame.MainActivity.PREFERENCE_FILENAME;


/**
 * Sottoclasse di Fragment che contiene la parte iniziale di una partita
 * Contiene un listener per la location
 */
public class StartGameFragment extends Fragment {

    //Definisco il listener sulle shared preferences
    private final SharedPreferences.OnSharedPreferenceChangeListener mSharedPreferenceChangeListener
            = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            //Aggiorna la textview con user o punti nuovi
            if (key.contentEquals(getString(R.string.username_pref))
                || key.contentEquals(getString(R.string.points_pref))){
                String user = sharedPreferences.getString(getString(R.string.username_pref), "");
                int points = sharedPreferences.getInt(getString(R.string.points_pref), 0);
                setPointInfoText("Ciao " +user+ ", hai totalizzato fino adesso " +points+ " punti");
            }
        }
    };
    private LocationManager mLocationManager;
    private ListView mLstTargets;

    public StartGameFragment() {
        // Required empty public constructor
    }

    /**
     * Metodo Factory per creare il fragment (senza parametri per ora)
     */
    public static StartGameFragment newInstance() {
        return new StartGameFragment();
    }

    //Metodo che dato del testo modifica la sezione info punti
    public void setPointInfoText(String text){
        AppCompatTextView txtView = (AppCompatTextView)
                getActivity().findViewById(R.id.totalpointstext);
        txtView.setText(text);
    }

    //Restituisce la lista dei target alla mainactivity
    public ListView getListTargets(){
        return mLstTargets;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * Called when the fragment is visible to the user and actively running.
     * This is generally
     * tied to {Activity#onResume() Activity.onResume} of the containing
     * Activity's lifecycle.
     */
    @Override
    public void onResume() {
        super.onResume();
    }

    /**
     * Called when the Fragment is no longer resumed.  This is generally
     * tied to { Activity#onPause() Activity.onPause} of the containing
     * Activity's lifecycle.
     */
    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_start_game, container, false);
    }

    /**
     * Called immediately after {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}
     * has returned, but before any saved state has been restored in to the view.
     * This gives subclasses a chance to initialize themselves once
     * they know their view hierarchy has been completely created.  The fragment's
     * view hierarchy is not however attached to its parent at this point.
     *
     * @param view               The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     */
    @Override
    //Faccio override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mLstTargets = (ListView) getActivity().findViewById(R.id.lstTargets);

        //Registro il listener delle shared pref
        getActivity().getSharedPreferences(PREFERENCE_FILENAME,
                Context.MODE_PRIVATE).registerOnSharedPreferenceChangeListener(
                mSharedPreferenceChangeListener);

        //Prende dalle pref user e punti
        SharedPreferences gameSettings = getActivity().getSharedPreferences(
                PREFERENCE_FILENAME, Context.MODE_PRIVATE);
        String user = gameSettings.getString(getString(R.string.username_pref), "");
        int points = gameSettings.getInt(getString(R.string.points_pref), 0);

        //Setta la sezione info
        setPointInfoText("Ciao " +user+ ", hai totalizzato fino adesso " +points+ " punti");

        super.onViewCreated(view, savedInstanceState);
    }
}
