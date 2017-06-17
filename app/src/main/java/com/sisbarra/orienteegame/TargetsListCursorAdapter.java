package com.sisbarra.orienteegame;

import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Antonio Sisbarra on 17/06/2017.
 * Adapter che prende i target dal db e ne spara 3 sulla listview
 */

public class TargetsListCursorAdapter extends CursorAdapter {

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
        return mInflater.inflate(R.layout.challenge_view, parent, false);
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
        //TODO: Calcola distanza dall'obiettivo in m
        int distance = calculateDistance(cursor);

        //TODO: Calcola difficoltà
        //TODO: Setta la view

        /**
        if(cursor.getPosition()%2==1) {
            view.setBackgroundColor(context.getResources().getColor(R.color.background_odd));
        }
        else {
            view.setBackgroundColor(context.getResources().getColor(R.color.background_even));
        }

        TextView content = (TextView) view.findViewById(R.id.row_content);
        content.setText(cursor.getString(cursor.getColumnIndex(Table.CONTENT)));

         // Find fields to populate in inflated template
         TextView tvBody = (TextView) view.findViewById(R.id.tvBody);
         TextView tvPriority = (TextView) view.findViewById(R.id.tvPriority);
         // Extract properties from cursor
         String body = cursor.getString(cursor.getColumnIndexOrThrow("body"));
         int priority = cursor.getInt(cursor.getColumnIndexOrThrow("priority"));
         // Populate fields with extracted properties
         tvBody.setText(body);
         tvPriority.setText(String.valueOf(priority));

         */

    }

    //Calcola la distanza in linea d'aria dal target
    private int calculateDistance(Cursor cursor){
        int dist = 0;
        LatLng target = new LatLng(cursor.getDouble(cursor.getColumnIndex(DataBaseHelper.LAT_COLUMN)),
                cursor.getDouble(cursor.getColumnIndex(DataBaseHelper.LONG_COLUMN)));
        //TODO: Bisogna aspettare che la Location venga settata prima di calcolare la distanza?

        return dist;
    }

    /**
     *
     * Called when the ContentObserver on the cursor receives a change notification.
     * The default implementation provides the auto-requery logic, but may be overridden by
     * sub classes.
     *
     * @see (android.database.ContentObserver) onChange(boolean)
     */
    @Override
    protected void onContentChanged() {
        //TODO: COSA SUCCEDE QUI?
        super.onContentChanged();
    }

    public void setCurrentLocation(Location loc){
        mCurrentPos = loc;
    }

}
