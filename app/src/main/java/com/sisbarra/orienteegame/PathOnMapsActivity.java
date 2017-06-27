package com.sisbarra.orienteegame;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;

import static com.sisbarra.orienteegame.R.string.percorso_intent_name;

public class PathOnMapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    //Percorso da mostrare a schermo
    private Percorso mPercorso;

    //Ottiene percorso da intent
    private void getPercorso(){
        //Ricavo il percorso effettuato dall'intent
        Gson gson = new Gson();
        String strObj = getIntent().getStringExtra(getString(percorso_intent_name));
        mPercorso = gson.fromJson(strObj, Percorso.class);
    }

    //Disegna il percorso sulla mappa
    private void drawPath(){
        if (mPercorso == null) {
            Log.e("Draw Line", "got null as parameters");
            return;
        }

        Polyline line = mMap.addPolyline(new PolylineOptions().color(Color.RED).
                geodesic(true));
        line.setPoints(mPercorso.getPointsLists());
    }

    //Setta lo stile per la mappa
    private void setStyleMap(){
        MapStyleOptions style = MapStyleOptions.loadRawResourceStyle(
                this, R.raw.style_json);
        mMap.setMapStyle(style);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_path_on_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Ottiene il percorso dall'intent
        getPercorso();
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //Setta lo stile per la mappa
        setStyleMap();

        // Disegna il percorso effettuato
        drawPath();

        // Add a marker in start and in Target and move the camera
        LatLng target = mPercorso.getTarget();
        LatLng start = mPercorso.getPointsLists().get(0);
        mMap.addMarker(new MarkerOptions().position(start).title("Partenza"));
        mMap.addMarker(new MarkerOptions().position(target).title("Obiettivo: "+
                mPercorso.getNameTarget()));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(target, 16));
    }
}
