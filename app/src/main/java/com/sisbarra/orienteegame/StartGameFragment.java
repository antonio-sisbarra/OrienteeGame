package com.sisbarra.orienteegame;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ListView;

import java.io.IOException;


/**
 * Sottoclasse di Fragment che contiene la parte iniziale di una partita
 * Contiene un listener per la location
 */
public class StartGameFragment extends Fragment implements LoaderCallbacks{

    private final LocationListener mLocationListener = new LocationListener() {
        //TODO: Da implementare
        @Override
        public void onLocationChanged(Location location) {

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };
    private ListView mLstTargets;
    private TargetsListCursorAdapter mAdapter;
    private DataBaseHelper mHelper;

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
        //TODO: ACCESSO AL DB
        /**
        *...

                DataBaseHelper myDbHelper = new DataBaseHelper();
                myDbHelper = new DataBaseHelper(this);

                try {

                    myDbHelper.createDataBase();

            } catch (IOException ioe) {

                throw new Error("Unable to create database");

            }

            try {

                myDbHelper.openDataBase();

            }catch(SQLException sqle){

                throw sqle;

            }

                ...


             // TodoDatabaseHandler is a SQLiteOpenHelper class connecting to SQLite
             TodoDatabaseHandler handler = new TodoDatabaseHandler(this);
             // Get access to the underlying writeable database
             SQLiteDatabase db = handler.getWritableDatabase();
             // Query for items from the database and get a cursor back
             Cursor todoCursor = db.rawQuery("SELECT  * FROM todo_items", null);

             // Find ListView to populate
             ListView lvItems = (ListView) findViewById(R.id.lvItems);
             // Setup cursor adapter using cursor from last step
             TodoCursorAdapter todoAdapter = new TodoCursorAdapter(this, todoCursor);
             // Attach cursor adapter to the ListView
             lvItems.setAdapter(todoAdapter);


             //Listener della posizione
             mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

             mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME,
             LOCATION_REFRESH_DISTANCE, mLocationListener);

             OPPURE

             lm =
             (LocationManager)getSystemService(Context.LOCATION_SERVICE);
             Critera crit = new Criteria();
             crit.setAccuracy(Criteria.ACCURACY_COARSE);
             crit.setCostAllowed(false);
             crit.setPowerRequirement(Criteria.POWER_LOW);
             lm.requestLocationUpdates(prov, 20000, 10, pi);
         */


        //Inizializza il loader -> chiama onCreateLoader
        getActivity().getSupportLoaderManager().initLoader(1, null, this);
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
        return new CursorLoader(getContext(), null, null, null, null, null){
            @Override
            //In Background carico il db dei target e il cursor relativo
            public Cursor loadInBackground()
            {
                mHelper = new DataBaseHelper(getContext());
                try {
                    mHelper.createDataBase();
                } catch (IOException ioe) {
                    throw new Error(getString(R.string.error_create_db));
                }
                try {
                    mHelper.openDataBase();
                }catch(SQLException sqle){
                    throw sqle;
                }

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
     * the {@link CursorAdapter(Context, * Cursor, int)} constructor <em>without</em> passing
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
    //Quando ha finito il caricamento dal db
    public void onLoadFinished(Loader loader, Object data) {
        Cursor cursor = (Cursor) data;
        cursor.moveToFirst();
        mAdapter = new TargetsListCursorAdapter(getContext(), cursor, 0);
        mLstTargets.setAdapter(mAdapter);
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
