package com.sisbarra.orienteegame;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

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
    private Activity mActivity;
    private Location mLastLoc;
    //I listener
    private LocationListener locationListenerGps = new LocationListener() {
        public void onLocationChanged(Location location) {
            if (!isAcceptable(location))
                return;

            timer1.cancel();
            mLastLoc = location;
            locationResult.gotLocation(location);
        }

        public void onProviderDisabled(String provider) {
            //Faccio vedere un toast per notificare l'obiettivo raggiunto
            Toast.makeText(mActivity, R.string.location_off_gps_toast,
                    Toast.LENGTH_LONG).show();
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };
    private LocationListener locationListenerNetwork = new LocationListener() {
        public void onLocationChanged(Location location) {
            if (!isAcceptable(location))
                return;

            timer1.cancel();
            mLastLoc = location;
            locationResult.gotLocation(location);
        }

        public void onProviderDisabled(String provider) {
            //Faccio vedere un toast per notificare l'obiettivo raggiunto
            Toast.makeText(mActivity, R.string.location_off_net_toast,
                    Toast.LENGTH_LONG).show();
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };

    MyLocation(LocationManager lm, Activity activ) {
        mLocationManager = lm;
        mActivity = activ;
    }

    //Metodo private che descrive le politiche di accettazione di una loc(in base a tempo e accuracy)
    private boolean isAcceptable(Location loc) {
        if (loc == null) return false;
        //CASO NON HO LOC
        if (mLastLoc == null) {
            return loc.getAccuracy() < (3 * GamingActivity.RANGE);
        }

        //CASO TOO OLD
        if (loc.getTime() - mLastLoc.getTime() > 13000 &&
                loc.getAccuracy() < (3 * GamingActivity.RANGE))
            return true;

        //CASO OLD
        if (loc.getTime() - mLastLoc.getTime() > 8000 &&
                loc.getAccuracy() < (2 * GamingActivity.RANGE))
            return true;

        //CASO RECENT
        return loc.getAccuracy() < (GamingActivity.RANGE);

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

        Criteria criteria_gps = new Criteria();
        criteria_gps.setAccuracy(Criteria.ACCURACY_FINE);
        String provider_fine = mLocationManager.getBestProvider(criteria_gps, true);
        if (provider_fine != null)
            mLocationManager.requestLocationUpdates(provider_fine, 0, 0,
                locationListenerGps);

        Criteria criteria_net = new Criteria();
        criteria_net.setAccuracy(Criteria.ACCURACY_COARSE);
        criteria_net.setPowerRequirement(Criteria.POWER_LOW);
        String provider_coarse = mLocationManager.getBestProvider(criteria_net, true);
        if (provider_coarse != null)
            mLocationManager.requestLocationUpdates(provider_coarse, 6000, 0,
                locationListenerNetwork);

        timer1 = new Timer();
        timer1.schedule(new GetLastLocation(), 10000);
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

            Criteria criteria_gps = new Criteria();
            criteria_gps.setAccuracy(Criteria.ACCURACY_FINE);
            String provider_fine = mLocationManager.getBestProvider(criteria_gps, true);
            gps_loc = mLocationManager.getLastKnownLocation(provider_fine);

            Criteria criteria_net = new Criteria();
            criteria_net.setAccuracy(Criteria.ACCURACY_COARSE);
            criteria_net.setPowerRequirement(Criteria.POWER_LOW);
            String provider_coarse = mLocationManager.getBestProvider(criteria_net, true);
            net_loc = mLocationManager.getLastKnownLocation(provider_coarse);

            //Prendo come valore il più accurato
            if (gps_loc != null && net_loc != null) {
                if (net_loc.getAccuracy() >= gps_loc.getAccuracy())
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
