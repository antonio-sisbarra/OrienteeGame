package com.sisbarra.orienteegame;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Antonio Sisbarra on 26/06/2017.
 * Classe adapter che costruisce le listview dei percorsi
 */

class PathsAdapter extends ArrayAdapter<Percorso> {

    PathsAdapter(Context context, ArrayList<Percorso> percorsi) {
        super(context, 0, percorsi);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Percorso perc = getItem(position);
        ViewHolder viewHolder; // view lookup cache stored in tag
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            // If there's no view to re-use, inflate a brand new view for row
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.path_item_view,
                    parent, false);
            viewHolder.user_points = (TextView) convertView.findViewById(R.id.user_and_point);
            viewHolder.name_targ = (TextView) convertView.findViewById(R.id.target_name_text);
            // Cache the viewHolder object inside the fresh view
            convertView.setTag(viewHolder);
        }
        else{
            // View is being recycled, retrieve the viewHolder object from tag
            viewHolder = (ViewHolder) convertView.getTag();
        }
        // Populate the data from the data object via the viewHolder object
        // into the template view.
        if (perc != null) {
            viewHolder.user_points.setText(perc.getNamePlayer() + ": " + perc.getPrize() +" punti");
            viewHolder.name_targ.setText(perc.getNameTarget());
        }
        // Return the completed view to render on screen
        return convertView;
    }

    //Classe private per pattern ViewHolder (motivi di prestazioni)
    private class ViewHolder{
        TextView user_points;
        TextView name_targ;
    }

}
