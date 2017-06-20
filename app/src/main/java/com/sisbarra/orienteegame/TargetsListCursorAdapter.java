package com.sisbarra.orienteegame;

import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by Antonio Sisbarra on 17/06/2017.
 * Adapter che prende i target dal db e li spara sulla listview
 */

class TargetsListCursorAdapter extends CursorAdapter {

    private static int NVIEWTYPES = 3;
    private LayoutInflater mInflater;
    private Location mCurrentPos;

    //Costruttore
    public TargetsListCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    /**
     * Makes a new view to hold the data pointed to by cursor.
     *
     * @param context Interface to application's global information
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        switch (getItemViewType(cursor.getPosition())){
            case 0: return LayoutInflater.from(context).inflate(R.layout.challenge_view_easy,
                    parent, false);
            case 1: return LayoutInflater.from(context).inflate(R.layout.challenge_view_medium,
                    parent, false);
            case 2: return LayoutInflater.from(context).inflate(R.layout.challenge_view_hard,
                    parent, false);
            default: return null;
        }
    }

    /**
     * Bind an existing view to the data pointed to by cursor
     *
     * @param view    Existing view, returned earlier by newView
     * @param context Interface to application's global information
     * @param cursor  The cursor from which to get the data. The cursor is already
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        //Controllo se esiste una posizione attuale e prevedo situazione di view null (filtro)
        if(mCurrentPos==null)
            return;

        //Calcola distanza dall'obiettivo in m
        int distance = calculateDistance(cursor);

        //Setta la view
        setTheView(view, context, distance);

    }

    //Setta la vista in base alla distanza
    private void setTheView(View view, Context context, int dist){
        TextView distxt = (TextView) view.findViewById(R.id.distancetext);

        distxt.setText(new StringBuilder().append("Sei distante ").append(dist).append(" m dall'obiettivo").toString());
    }

    //Calcola difficoltà in base alla distanza (500m 0, 1500m 1, else 2)
    private int calculateDiff(int dist){
        if (dist < 500) return 0;
        else if(dist < 1500) return 1;
             else return 2;
    }

    //Calcola la distanza in linea d'aria dal target (Senza usare API di Google)
    private int calculateDistance(Cursor cursor){
        int dist;
        double latTarg = cursor.getDouble(cursor.getColumnIndex(DataBaseHelper.LAT_COLUMN));
        double longTarg = cursor.getDouble(cursor.getColumnIndex(DataBaseHelper.LONG_COLUMN));

        dist = distance(latTarg, longTarg, mCurrentPos.getLatitude(), mCurrentPos.getLongitude());

        return dist;
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

    public void setCurrentLocation(Location loc){
        //Verifico se ho già una location, se sì uso set e non new
        if(mCurrentPos == null)
            mCurrentPos = new Location(loc);
        else
            mCurrentPos.set(loc);
    }

    @Override
    public int getItemViewType(int position) {
        if (mCurrentPos==null) return 0;
        else return calculateDiff(calculateDistance((Cursor) getItem(position)));
    }

    @Override
    public int getViewTypeCount() {
        return NVIEWTYPES;
    }
}
