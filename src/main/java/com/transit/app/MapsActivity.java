package com.transit.app;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.transit.app.databinding.ActivityMapsBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,
        OnMapAndViewReadyListener.OnGlobalLayoutAndMapReadyListener{
    private static final int POLYLINE_WIDTH = 10;
    public static final int PATTERN_GAP_LENGTH = 3;

    //Loading the google maps information
    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private URL url;

    //The variables needed to draw the polyline representing the transit path
    public static final List<PatternItem> PATTERN_POLYGON_ALPHA = Arrays.asList(new Dot(), new Gap(PATTERN_GAP_LENGTH));
    static LatLngBounds bounds;
    static LatLng end;
    static String[] travelMode;
    static String[] polyline;

    //Button to the next class
    ImageButton ib1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Manually building the URL required to request transit information
        String transitRequestURL = "https://maps.googleapis.com/maps/api/directions/json?origin=place_id:"
                +MainActivity.parameters[0]+"&destination=place_id:"+MainActivity.parameters[1]+"&"
                +MainActivity.parameters[2]+"_time="+MainActivity.parameters[3]
                +"&transit_routing_preference=fewer_transfers&traffic_model=best_guess&avoid=indoor&mode=transit&key="+getString(R.string.key);

        //Calling the JsonTask to decode the URL results
        try {
            new JsonTask(this).execute(transitRequestURL).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //Sets a listener that reveals all the transit information
        ib1 = findViewById(R.id.Directions);
        ib1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MapsActivity.this, RouteDetails.class);
                startActivity(intent);
            }
        });


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync((OnMapReadyCallback) MapsActivity.this);

        //Colors xP
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(this.getResources().getColor(R.color.dark_pink));

        //Custom Back Button
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        ColorDrawable colorDrawable = new ColorDrawable(getColor(R.color.hot_pink));
        actionBar.setBackgroundDrawable(colorDrawable);
    }

    //Function called whenever the View and Google Maps is ready
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMinZoomPreference(10.0f);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(bounds.getCenter()));
        mMap.addMarker(new MarkerOptions().position(end).title(MainActivity.autoFragment[1]));
        mMap.setLatLngBoundsForCameraTarget(bounds);
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 48));

        //Draws the polyline according to their description
        for(int i=0;i<polyline.length;i++) {
            drawAllPolyLines(polyline[i],travelMode[i]);
        }
    }

    //Depending on the descriptor, a different polyline will be drawn
    private void drawAllPolyLines (String polyline, String travelMode) {
        List<LatLng> coordinates = PolyUtil.decode(polyline);
        if(travelMode.equals("WALKING")) {
            mMap.addPolyline(new PolylineOptions()
                    .color(getResources().getColor(R.color.black)) // Line color
                    .width(POLYLINE_WIDTH) // Line width
                    .clickable(false) // Able to click or not
                    .pattern(PATTERN_POLYGON_ALPHA)
                    .addAll(coordinates) // Polyline coordinates
            );
        }else if(travelMode.equals("TRAIN")){
            mMap.addPolyline(new PolylineOptions()
                    .color(getResources().getColor(R.color.red)) // Line color
                    .width(POLYLINE_WIDTH) // Line width
                    .clickable(false) // Able to click or not
                    .addAll(coordinates) // Polyline coordinates
            );
        }else if(travelMode.equals("BUS")){
            mMap.addPolyline(new PolylineOptions()
                    .color(getResources().getColor(R.color.hot_pink)) // Line color
                    .width(POLYLINE_WIDTH) // Line width
                    .clickable(false) // Able to click or not
                    .addAll(coordinates) // Polyline coordinates
            );
        }
    }
}