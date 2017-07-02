package com.sisbarra.orienteegame;

import android.app.Activity;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.AppCompatTextView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;

import static com.sisbarra.orienteegame.MainActivity.PREFERENCE_FILENAME;
import static com.sisbarra.orienteegame.R.string.percorso_intent_name;


/**
 * Sottoclasse di Fragment che contiene la parte iniziale di una partita
 * Contiene un listener per la location
 */
public class StartGameFragment extends Fragment implements LoaderManager.LoaderCallbacks {

    private ListView mLstTargets;
    //Riferimento al DB
    private DataBaseHelper mHelper;
    //Riferimento all'Activity Main
    private MainActivity mMainActivity;
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
    //CursorAdapter per i target
    private TargetsListCursorAdapter mAdapter;

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
                mMainActivity.findViewById(R.id.totalpointstext);
        txtView.setText(text);
    }

    //Modifica la textview degli obiettivi
    public void setTargetText(String text){
        AppCompatTextView txtView = (AppCompatTextView)
                mMainActivity.findViewById(R.id.targetTxt);
        if(txtView!=null)
            txtView.setText(text);
    }

    //Metodo che aggiorna la posizione attuale nel cursoradapter
    public void updatePos(Location location){
        //Verifico se è già stato settato un adapter
        if(mLstTargets.getAdapter()==null)
            (mMainActivity).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setAdapterAndListener();
                }
            });

        if(mAdapter!=null) {
            mAdapter.setCurrentLocation(location);
            (mMainActivity).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                        mAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    //Metodo che setta adapter e listener per la Listview dei target
    private void setAdapterAndListener(){
        mLstTargets.setAdapter(mAdapter);
        mLstTargets.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startGamingActivity((Cursor) mAdapter.getItem(position));
            }
        });
    }

    //Fa partire la gaming activity estrapolando i dati necessari
    private void startGamingActivity(Cursor c){
        //Eseguo tutto su un thread in background per motivi di efficienza
        final Cursor curs = c;
        new Thread(new Runnable() {
            @Override
            public void run() {
                double lat = curs.getDouble(curs.getColumnIndex(DataBaseHelper.LAT_COLUMN));
                double lng = curs.getDouble(curs.getColumnIndex(DataBaseHelper.LONG_COLUMN));
                double lastlat = mMainActivity.getLastKnownLocation().getLatitude();
                double lastlong = mMainActivity.getLastKnownLocation().getLongitude();
                String titleTarget = curs.getString(curs.getColumnIndex(DataBaseHelper.NAME_COLUMN));
                Intent intent = new Intent(getContext(), GamingActivity.class);
                intent.putExtra(getString(R.string.lat_intent_gaming), lat);
                intent.putExtra(getString(R.string.long_intent_gaming), lng);
                intent.putExtra(getString(R.string.lastLat), lastlat);
                intent.putExtra(getString(R.string.lastLong), lastlong);
                intent.putExtra(getString(R.string.titleTarget_name), titleTarget);
                startActivityForResult(intent, 1);
            }
        }).start();
    }

    //Metodo che chiama la mainactivity per capire se l'adapter è stato creato
    public boolean isThereAdapter(){
        return (mAdapter != null);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mHelper = mMainActivity.getHelper();
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
     * Called when a fragment is first attached to its context.
     * {@link #onCreate(Bundle)} will be called after this.
     *
     * param context
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mMainActivity = (MainActivity) getActivity();
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
        mLstTargets = (ListView) mMainActivity.findViewById(R.id.lstTargets);

        //Fa partire il loader dei target
        mMainActivity.getSupportLoaderManager().initLoader(1, null, this);

        //Registro il listener delle shared pref
        mMainActivity.getSharedPreferences(PREFERENCE_FILENAME,
                Context.MODE_PRIVATE).registerOnSharedPreferenceChangeListener(
                mSharedPreferenceChangeListener);

        //Prende dalle pref user e punti
        SharedPreferences gameSettings = mMainActivity.getSharedPreferences(
                PREFERENCE_FILENAME, Context.MODE_PRIVATE);
        String user = gameSettings.getString(getString(R.string.username_pref), "");
        int points = gameSettings.getInt(getString(R.string.points_pref), 0);

        //Setta la sezione info
        setPointInfoText("Ciao " +user+ ", hai totalizzato fino adesso " +points+ " punti");

        super.onViewCreated(view, savedInstanceState);
    }

    /**
     * Instantiate and return a new Loader for the given ID.
     *
     * @param id   The ID whose loader is to be created.
     * @param args Any arguments supplied by the caller.
     * @return Return a new Loader instance that is ready to start loading.
     */
    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        return new android.support.v4.content.CursorLoader(getContext(), null, null, null,
                null, null) {
            @Override
            //In Background carico il db dei target e il cursor relativo
            public Cursor loadInBackground() {
                return mHelper.getAllTargetCursor();
            }
        };
    }

    /**
     * Called when a previously created loader has finished its load.  Note
     * that normally an application is <em>not</em> allowed to commit fragment
     * transactions while in this call, since it can happen after an
     * activity's state is saved.  See {FragmentManager#beginTransaction()
     * FragmentManager.openTransaction()} for further discussion on this.
     * <p>
     * <p>This function is guaranteed to be called prior to the release of
     * the last data that was supplied for this Loader.  At this point
     * you should remove all use of the old data (since it will be released
     * soon), but should not do your own release of the data since its Loader
     * owns it and will take care of that.  The Loader will take care of
     * management of its data so you don't have to.  In particular:
     * <p>
     * <ul>
     * <li> <p>The Loader will monitor for changes to the data, and report
     * them to you through new calls here.  You should not monitor the
     * data yourself.  For example, if the data is a {@link Cursor}
     * and you place it in a {@link CursorAdapter}, use
     * the { CursorAdapter#CursorAdapter(Context, * Cursor, int)} constructor <em>without</em> passing
     * in either {@link CursorAdapter#FLAG_AUTO_REQUERY}
     * or {@link CursorAdapter#FLAG_REGISTER_CONTENT_OBSERVER}
     * (that is, use 0 for the flags argument).  This prevents the CursorAdapter
     * from doing its own observing of the Cursor, which is not needed since
     * when a change happens you will get a new Cursor throw another call
     * here.
     * <li> The Loader will release the data once it knows the application
     * is no longer using it.  For example, if the data is
     * a {@link Cursor} from a {@link CursorLoader},
     * you should not call close() on it yourself.  If the Cursor is being placed in a
     * {@link CursorAdapter}, you should use the
     * {@link CursorAdapter#swapCursor(Cursor)}
     * method so that the old Cursor is not closed.
     * </ul>
     *
     * @param loader The Loader that has finished.
     * @param data   The data generated by the Loader.
     */
    @Override
    public void onLoadFinished(Loader loader, Object data) {
        //Attacco Adapter a Listview
        Cursor cursor = (Cursor) data;
        cursor.moveToFirst();
        mAdapter = new TargetsListCursorAdapter(getContext(), cursor, 0);
    }

    /**
     * Called when a previously created loader is being reset, and thus
     * making its data unavailable.  The application should at this point
     * remove any references it has to the Loader's data.
     *
     * @param loader The Loader that is being reset.
     */
    @Override
    public void onLoaderReset(Loader loader) {
        mAdapter.swapCursor(null);
    }

    /**
     * Usato per mandare il percorso appena effettuato al terzo fragment
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if(resultCode == Activity.RESULT_OK){
                //Ricavo il percorso effettuato dall'intent
                Gson gson = new Gson();
                String strObj = data.getStringExtra(getString(percorso_intent_name));
                final Percorso perc = gson.fromJson(strObj, Percorso.class);

                //Rimuovo dal DB il target appena raggiunto
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        mHelper.deleteTarget(perc.getNameTarget());
                        final Cursor newCurs = mHelper.getAllTargetCursor();
                        //Notifico il cambiamento dei dati per il cursoradapter
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mAdapter.changeCursor(newCurs);
                                mAdapter.notifyDataSetChanged();
                            }
                        });
                    }
                }).start();

                //Aggiorno la list del terzo fragment
                ((MainActivity) getActivity()).refreshMyPaths(perc);

                //Faccio vedere un toast per notificare l'obiettivo raggiunto
                Toast.makeText(getActivity(), R.string.obiettivo_raggiunto_toast,
                        Toast.LENGTH_LONG).show();
            }
        }
    }
}
