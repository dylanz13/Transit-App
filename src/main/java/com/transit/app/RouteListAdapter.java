package com.transit.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Calendar;

public class RouteListAdapter extends ArrayAdapter<Values> {

    final Calendar myCalendar= Calendar.getInstance();
    private Context mContext;
    private int mResource;

    //Function acting as a pointer to the original values in RouteDetails
    public RouteListAdapter (Context context, int resource, ArrayList<Values> objects){
        super(context,resource,objects);
        mContext = context;
        mResource = resource;
    }
    //Where the pointer to Route Details and the XML Layout adapter_view_layout is called
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        //If the transit mode is walking, these will be implemented
        String distance = getItem(position).getDistance();
        String duration = getItem(position).getDuration();
        String html_instructions = getItem(position).getHtml_instructions();
        String vehicle = getItem(position).getVehicle();
        int image = getItem(position).getImageResource();

        //What is going to be returned after everything is set
        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(mResource, parent, false);

        TextView tvDistance = (TextView) convertView.findViewById(R.id.distance);
        TextView tvDuration = (TextView) convertView.findViewById(R.id.duration);
        TextView tvInstructions = (TextView) convertView.findViewById(R.id.instructions);
        TextView tvVehicle = (TextView) convertView.findViewById(R.id.vehicle);
        ImageView ivType = (ImageView) convertView.findViewById(R.id.image_line);

        tvDistance.setText(distance);
        tvDuration.setText(duration);
        tvInstructions.setText(html_instructions);
        tvVehicle.setText(vehicle);
        ivType.setImageResource(image);

        //If the transit mode isn't walking, these parameters will be added
        try{
            String arrival_time = getItem(position).getArrival_time();
            String departure_time = getItem(position).getDeparture_time();
            String name = getItem(position).getName();

            TextView tvArrive = (TextView) convertView.findViewById(R.id.arriveTime);
            TextView tvDepart = (TextView) convertView.findViewById(R.id.departTime);
            TextView tvName = (TextView) convertView.findViewById(R.id.name);

            tvArrive.setText(arrival_time);
            tvDepart.setText(departure_time);
            tvName.setText(name);
        }catch(Exception e){}
        return convertView;
    }
}
