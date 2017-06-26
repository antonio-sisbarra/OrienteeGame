package com.sisbarra.orienteegame;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Antonio Sisbarra on 19/06/2017.
 * Classe che incapsula il lavoro di procacciamento della posizione attuale
 */

class MyLocation {

    private LocationResult locationResult;
    private Timer timer1;
    private LocationManager mLocationManager;

    //I listener
    private LocationListener locationListenerGps = new LocationListener() {
        public void onLocationChanged(Location location) {
            timer1.cancel();
            locationResult.gotLocation(location);
        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };
    private LocationListener locationListenerNetwork = new LocationListener() {
        public void onLocationChanged(Location location) {
            timer1.cancel();
            locationResult.gotLocation(location);
        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };
    private Activity mActivity;

    MyLocation(LocationManager lm, Activity activ) {
        mLocationManager = lm;
        mActivity = activ;
    }

    boolean getLocation(Context context, LocationResult result) {
        //Uso la callback di location result per passare la posizione attuale all'utente
        locationResult = result;

        //Controllo esplicitamente i permessi
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(mActivity,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    0);

        }


        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 0,
                locationListenerGps);

        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 8000, 0,
                locationListenerNetwork);

        timer1 = new Timer();
        timer1.schedule(new GetLastLocation(), 7000);
        return true;
    }

    //Con questo metodo setto anche un fattore di cambio posizione nel location updates
    boolean setDistanceForUpdates(Context context, LocationResult result, int minDistance){
        //Uso la callback di location result per passare la posizione attuale all'utente
        locationResult = result;

        //Rimuovo i precedenti updates
        mLocationManager.removeUpdates(locationListenerGps);
        mLocationManager.removeUpdates(locationListenerNetwork);

        //Controllo esplicitamente i permessi
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(mActivity,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    0);

        }

        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, minDistance,
                    locationListenerGps);

        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, minDistance,
                    locationListenerNetwork);

        timer1 = new Timer();
        timer1.schedule(new GetLastLocation(), 20000);
        return true;
    }

    //Rimuove i listener dall'ascolto degli aggiornamenti
    void removeUpdates(){
        if(mLocationManager!=null) {
            mLocationManager.removeUpdates(locationListenerGps);
            mLocationManager.removeUpdates(locationListenerNetwork);
        }
    }

    static abstract class LocationResult {
        public abstract void gotLocation(Location location);
    }

    private class GetLastLocation extends TimerTask {
        @Override
        public void run() {

            Location net_loc, gps_loc;

            //Controllo esplicitamente i permessi
            if (ActivityCompat.checkSelfPermission(mActivity.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(mActivity.getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(mActivity,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        0);

            }

            gps_loc = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            net_loc = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            //if there are both values use the latest one
            if (gps_loc != null && net_loc != null) {
                if (gps_loc.getTime() > net_loc.getTime())
                    locationResult.gotLocation(gps_loc);
                else
                    locationResult.gotLocation(net_loc);
                return;
            }

            if (gps_loc != null) {
                locationResult.gotLocation(gps_loc);
                return;
            }
            if (net_loc != null) {
                locationResult.gotLocation(net_loc);
                return;
            }
            locationResult.gotLocation(null);
        }

    }

}
