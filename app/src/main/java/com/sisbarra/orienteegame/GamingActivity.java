package com.sisbarra.orienteegame;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;

import static com.sisbarra.orienteegame.R.string.points_pref;
import static com.sisbarra.orienteegame.R.style.AppAlertTheme;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class GamingActivity  extends AppCompatActivity implements SensorEventListener {
    public static final String NA = "N/A";
    public static final String FIXED = "FIXED";
    //Costante per filtro bassa banda
    static final float ALPHA = 0.25f; // if ALPHA = 1 OR 0, no filter applies.
    //Costante per il range per la fine della partita
    static final int RANGE = 7;
    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
        }
    };
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    //Oggetto partita
    private Partita mPartita;
    //LocationResult per il listener della posizione
    private MyLocation.LocationResult mLocationResult;
    //Oggetto MyLocation (astrazione della gestione location)
    private  MyLocation mMyLocation;
    //Riferimento al Location Manager
    private LocationManager mLocationManager;
    //Riferimento al Sensor Manager
    private SensorManager mSensorManager;
    //TextView del layout
    private TextView mTextDistance, mTextLat, mTextLong, mTextPrize;
    //Sensore magnetico e di gravità
    private Sensor mSensorMagnetic, mSensorGravity;
    //Location
    private Location mCurrentLocation;
    //Coordinate del target
    private double mLatTarget;
    private double mLongTarget;
    //Distanza e direzione obiettivo
    private int mDistance;
    // Gravity for accelerometer data
    private float[] gravity = new float[3];
    // magnetic data
    private float[] geomagnetic = new float[3];
    // Rotation data
    private float[] rotation = new float[9];
    // orientation (azimuth, pitch, roll)
    private float[] orientation = new float[3];
    // smoothed values
    private float[] smoothed = new float[3];
    private GeomagneticField geomagneticField;
    private double bearing = 0;
    private CompassView mCompassView;
    //Titolo del Target
    private String mTitleTarget;
    //Nome delle Pref
    private String PREFERENCE_FILENAME;
    //Oggetto percorso, verrà consegnato poi alla mainactivity
    private Percorso mPercorso;

    //Preference di gioco
    private SharedPreferences mGameSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_gaming);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mVisible = true;
        mContentView = findViewById(R.id.content_view);

        // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        //Riferimenti al layout
        getLayoutReferences();
    }

    @Override
    protected void onStart() {
        super.onStart();

        initializeSensors();

        initializeMatch();
    }

    //Inizializza i sensori
    private void initializeSensors(){
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorGravity = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorMagnetic = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        // listen to these sensors
        mSensorManager.registerListener(this, mSensorGravity,
                SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mSensorMagnetic,
                SensorManager.SENSOR_DELAY_FASTEST);
    }

    //Prende i riferimenti al layout
    private void getLayoutReferences(){
        mTextDistance = (TextView) findViewById(R.id.text_distance_gaming);
        mTextPrize = (TextView) findViewById(R.id.text_prize_gaming);
        mTextLat = (TextView) findViewById(R.id.latitude);
        mTextLong = (TextView) findViewById(R.id.longitude);
        mCompassView = (CompassView) findViewById(R.id.compass);
    }

    //Formula di Haversine per la distanza tra due punti
    private int distance (double lat_a, double lng_a, double lat_b, double lng_b ){
        double earthRadius = 3958.75;
        double latDiff = Math.toRadians(lat_b-lat_a);
        double lngDiff = Math.toRadians(lng_b-lng_a);
        double a = Math.sin(latDiff /2) * Math.sin(latDiff /2) +
                Math.cos(Math.toRadians(lat_a)) * Math.cos(Math.toRadians(lat_b)) *
                        Math.sin(lngDiff /2) * Math.sin(lngDiff /2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double distance = earthRadius * c;

        int meterConversion = 1609;

        Double res = distance * meterConversion;
        return res.intValue();
    }

    //Inizializza la partita
    private void initializeMatch(){
        final double latTarget = getIntent().getExtras().getDouble(getString(R.string.lat_intent_gaming),
                0);
        final double lngTarget = getIntent().getExtras().getDouble(getString(R.string.long_intent_gaming),
                0);
        final String titletarget = getIntent().getExtras().getString(getString(R.string.titleTarget_name),
                "");

        //Prendo le coordinate dell'ultima location avuta
        final double lastLat, lastLong;
        if (mCurrentLocation == null) {
            lastLat = getIntent().getExtras().getDouble(getString(R.string.lastLat),
                    0);
            lastLong = getIntent().getExtras().getDouble(getString(R.string.lastLong),
                    0);
        } else {
            lastLat = mCurrentLocation.getLatitude();
            lastLong = mCurrentLocation.getLongitude();
        }

        mLatTarget = latTarget;
        mLongTarget = lngTarget;
        mTitleTarget = titletarget;

        //Calcolo distanza tra due punti
        mDistance = distance(lastLat, lastLong, latTarget, lngTarget);

        //Setto i valori per le textView
        mTextLat.setText(String.format("Lat. %s", lastLat));
        mTextLong.setText(String.format("Long. %s", lastLong));
        mTextDistance.setText("Sei distante "+mDistance+" m dall'obiettivo!");

        //Prendo l'username dalle Pref
        PREFERENCE_FILENAME = getString(R.string.filename_pref);
        mGameSettings = getSharedPreferences(PREFERENCE_FILENAME, Context.MODE_PRIVATE);
        String user = mGameSettings.getString(getString(R.string.username_pref), "");


        //Inizializzo la logica del gioco
        mPartita = new Partita(new LatLng(latTarget, lngTarget),
                new LatLng(lastLat, lastLong), titletarget, mDistance);

        //Inizializzo la text del prize
        mTextPrize.setText("Il tempo scorre, adesso vinceresti " + mPartita.getPrize() + " punti!");

        //Creo il percorso
        mPercorso = new Percorso(mTitleTarget, new LatLng(mLatTarget, mLongTarget), user);

        //AGGIUNGO POSIZIONE ATTUALE ALLA LISTA DEI PUNTI
        mPercorso.addPoint(new LatLng(lastLat, lastLong));

        //VERIFICA DELLA DISTANZA IN RELAZIONE AL RANGE (Situazione vittoria immediata)
        if(mDistance <= RANGE){
            //Setta il punteggio per il percorso
            int points = mPartita.finishMatch();
            mPercorso.setPrize(points);

            //Aggiorno il punteggio nelle preference
            int actualPoints = mGameSettings.getInt(getString(points_pref), 0);
            mGameSettings.edit().putInt(getString(points_pref), points+actualPoints).apply();

            //Torno alla main activity inviando come Intent il percorso svolto
            Intent returnIntent = new Intent();
            returnIntent.putExtra(getString(R.string.percorso_intent_name),
                    (new Gson()).toJson(mPercorso));
            setResult(Activity.RESULT_OK,returnIntent);
            finish();
            return;
        }

        //Prendo riferimento al LocationManager
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //Faccio partire il listener della posizione
        mLocationResult = new MyLocation.LocationResult(){
            @Override
            public void gotLocation(Location location){
                //Got the location!
                if(location!=null) {
                    //Aggiornamento Location
                    mCurrentLocation = location;

                    //AGGIUNGO POSIZIONE ATTUALE ALLA LISTA DEI PUNTI
                    mPercorso.addPoint(new LatLng(mCurrentLocation.getLatitude(),
                            mCurrentLocation.getLongitude()));

                    //Aggiorno le info a schermo
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateLocation(mCurrentLocation);
                        }
                    });
                    geomagneticField = new GeomagneticField(
                            (float) mCurrentLocation.getLatitude(),
                            (float) mCurrentLocation.getLongitude(),
                            (float) mCurrentLocation.getAltitude(),
                            System.currentTimeMillis());

                    //FINE PARTITA SE DIST <= RANGE
                    //VERIFICA DELLA DISTANZA IN RELAZIONE AL RANGE
                    if(mDistance <= RANGE){
                        //Setta il punteggio per il percorso
                        int points = mPartita.finishMatch();
                        mPercorso.setPrize(points);

                        //Aggiorno il punteggio nelle preference
                        int actualPoints = mGameSettings.getInt(getString(points_pref), 0);
                        mGameSettings.edit().putInt(getString(points_pref), points+actualPoints).apply();

                        //Torno alla main activity inviando come Intent il percorso svolto
                        Intent returnIntent = new Intent();
                        returnIntent.putExtra(getString(R.string.percorso_intent_name),
                                (new Gson()).toJson(mPercorso));
                        setResult(Activity.RESULT_OK,returnIntent);
                        finish();
                    }
                }
            }
        };
        mMyLocation = new MyLocation(mLocationManager, this);
        mMyLocation.getLocation(this, mLocationResult);
    }

    private void updateLocation(Location loc){
        if (FIXED.equals(loc.getProvider())) {
            mTextLat.setText(NA);
            mTextLong.setText(NA);
        }

        mTextLat.setText(String.format("Lat. %s", loc.getLatitude()));
        mTextLong.setText(String.format("Long. %s", loc.getLongitude()));
        mDistance = distance(loc.getLatitude(), loc.getLongitude(), mLatTarget, mLongTarget);
        mTextDistance.setText("Sei distante "+mDistance+" m dall'obiettivo!");
        mTextPrize.setText("Il tempo scorre, adesso vinceresti " + mPartita.getPrize() + " punti!");
    }

    //Metodo passa-basso per filtrare i dati dei sensori
    protected float[] lowPass( float[] input, float[] output ) {
        if ( output == null ) return input;
        for ( int i=0; i<input.length; i++ ) {
            output[i] = output[i] + ALPHA * (input[i] - output[i]);
        }
        return output;
    }

    //Crea il dialog builder per l'uscita dal gioco
    private void createExitDialogBuilder(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this, AppAlertTheme);

        // set dialog message
        alertDialogBuilder
                .setMessage(R.string.abbandono_partita)
                .setCancelable(false)
                .setPositiveButton("Sì",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        // if this button is clicked, close
                        // current activity
                        // This ID represents the Home or Up button.

                        //Annullo la partita in corso
                        if(mPartita!=null)
                            mPartita.finishMatch();

                        finish();
                    }
                })
                .setNegativeButton("No",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        // if this button is clicked, just close
                        // the dialog box and do nothing
                        dialog.cancel();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // remove listeners
        mSensorManager.unregisterListener(this, mSensorGravity);
        mSensorManager.unregisterListener(this, mSensorMagnetic);
        if(mMyLocation!=null)
            mMyLocation.removeUpdates();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {

            createExitDialogBuilder();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Take care of popping the fragment back stack or finishing the activity
     * as appropriate.
     */
    @Override
    public void onBackPressed() {
        createExitDialogBuilder();
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    /**
     * Called when there is a new sensor event.  Note that "on changed"
     * is somewhat of a misnomer, as this will also be called if we have a
     * new reading from a sensor with the exact same sensor values (but a
     * newer timestamp).
     * <p>
     * <p>See {@link SensorManager SensorManager}
     * for details on possible sensor types.
     * <p>See also {@link SensorEvent SensorEvent}.
     * <p>
     * <p><b>NOTE:</b> The application doesn't own the
     * {@link SensorEvent event}
     * object passed as a parameter and therefore cannot hold on to it.
     * The object may be part of an internal pool and may be reused by
     * the framework.
     *
     * @param event the {@link SensorEvent SensorEvent}.
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        boolean accelOrMagnetic = false;

        // prende i dati dell'accelerometro
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            // Necessario un filtro passa basso per rendere i dati smoothed
            smoothed = lowPass(event.values.clone(), gravity);
            gravity[0] = smoothed[0];
            gravity[1] = smoothed[1];
            gravity[2] = smoothed[2];
            accelOrMagnetic = true;

        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            smoothed = lowPass(event.values.clone(), geomagnetic);
            geomagnetic[0] = smoothed[0];
            geomagnetic[1] = smoothed[1];
            geomagnetic[2] = smoothed[2];
            accelOrMagnetic = true;

        }

        // prende la matrice di rotazione per prendere i dati dell'acceler. e del magnetic
        SensorManager.getRotationMatrix(rotation, null, gravity, geomagnetic);
        // prende il bearing verso il nord
        SensorManager.getOrientation(rotation, orientation);
        // i gradi est del vero Nord
        bearing = orientation[0];
        // converte da radianti in gradi
        bearing = Math.toDegrees(bearing);

        // fixa la differenza tra nord vero e nord magnetico
        if (geomagneticField != null) {
            bearing += geomagneticField.getDeclination();
        }

        // bearing dovrebbe essere compreso tra 0 e 360
        if (bearing < 0) {
            bearing += 360;
        }

        // aggiorna la compassview
        mCompassView.setBearing((float) bearing);

        if (accelOrMagnetic) {
            mCompassView.postInvalidate();
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        if (sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD
                && accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
            // manage fact that compass data are unreliable ...
            //Toast per avvisare l'utente
            Toast.makeText(this, R.string.accuracy_unreliable, Toast.LENGTH_SHORT).show();
        }
    }
}
