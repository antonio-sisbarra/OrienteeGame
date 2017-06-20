package com.sisbarra.orienteegame;

import android.content.Context;
import android.content.CursorLoader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.AppCompatTextView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ListView;

import static com.sisbarra.orienteegame.MainActivity.PREFERENCE_FILENAME;


/**
 * Sottoclasse di Fragment che contiene la parte iniziale di una partita
 * Contiene un listener per la location
 */
public class StartGameFragment extends Fragment implements LoaderManager.LoaderCallbacks {

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

    //Riferimento al DB
    private DataBaseHelper mHelper;

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
                getActivity().findViewById(R.id.totalpointstext);
        txtView.setText(text);
    }

    //Restituisce la lista dei target alla mainactivity
    public ListView getListTargets(){
        return mLstTargets;
    }

    //Modifica la textview degli obiettivi
    public void setTargetText(String text){
        AppCompatTextView txtView = (AppCompatTextView)
                getActivity().findViewById(R.id.targetTxt);
        txtView.setText(text);
    }

    //Metodo che aggiorna la posizione attuale nel cursoradapter
    public void updatePos(Location location){
        //Verifico se è giaà stato settato un adapter
        if(mLstTargets.getAdapter()==null)
            (getActivity()).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mLstTargets.setAdapter(mAdapter);
                    setTargetText(getString(R.string.header_target_text));
                }
            });

        mAdapter.setCurrentLocation(location);
        (getActivity()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(mAdapter!=null)
                    mAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mHelper = ((MainActivity) getActivity()).getHelper();
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

        //Fa partire il loader dei target
        getActivity().getSupportLoaderManager().initLoader(1, null, this);

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

    }
}
