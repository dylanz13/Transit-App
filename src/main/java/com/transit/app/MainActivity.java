
package com.transit.app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

//TODO: YO IN CLASS ADD ARRAYS THAT CARRY INTO SECOND CLASS
//TODO: MAYBE FIRST LOOK INTO JSON DESTINATION API
public class MainActivity extends AppCompatActivity {
    boolean doubleBackToExitPressedOnce = false;

    //Location Button Variables
    ImageButton ib1,ib2;
    Button b1;
    int[] images = {R.drawable.marker, R.drawable.grey_marker};
    static int i = 0;

    //GPS Initialization
    GPSTracker gpsTracker;

    //Places Search
    static AutocompleteSupportFragment start;
    AutocompleteSupportFragment end;
    String placeID;

    //Departure/Arrival Radio Buttons
    RadioGroup rg1;
    RadioButton rb1,rb2;

    //Date & Time Picker
    final Calendar myCalendar= Calendar.getInstance();
    EditText et1,et2;

    //Static String Arrays Accessible in any class
    static String[] parameters = new String[4];
    static String[] autoFragment = new String[2];
    static LatLng currentPoint;

    //When the app is launched
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //XML Referencing
        ib1 = findViewById(R.id.Location);
        ib2 = findViewById(R.id.Switch);
        b1 = findViewById(R.id.Plan);
        et1 = findViewById(R.id.Current_Date);
        et2 = findViewById(R.id.Current_Time); //TODO: Arrival Time "NOW" in XML
        rg1 = findViewById(R.id.toggle);
        rb1 = findViewById(R.id.Departure);
        rb2 = findViewById(R.id.Arrival);
        start = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.Start_Address); //Google's AutoFill Place Fragment
        end = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.End_Address);

        //Create a Listener for the Location Button
        ib1.setOnClickListener(v -> {
            ib1.setImageResource(images[i]); //Alternated between two images, 0 is red, 1 is grey
            if (i == 1) {
                i = 0;
            } else if (i == 0) {
                //Checks permissions
                if(ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                    //If granted, update location using GPS
                    updateGPS();
                }else{ //if not granted, request them
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 99);
                }
                i++;
            }
        });

        //Create a listener for the Switch button, basically swaps visiable and non-visible feilds of
        ib2.setOnClickListener(view -> {
            String temp = autoFragment[0];
            start.setText(autoFragment[1]);
            end.setText(temp);
            autoFragment[0] = autoFragment[1];
            autoFragment[1] = temp;
            temp=parameters[0];
            parameters[0] = parameters[1];
            parameters[1] = temp;
        });

        //Google Places Initialization
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getString(R.string.key));
        }

        //Calls the google places api that looks to autofill locations
        searchPlace(start);
        searchPlace(end);

        //Radio Button of Departure/Arrival
        rb1.setChecked(true);
        parameters[2] = "departure";
        rg1.setOnCheckedChangeListener((radioGroup, checkID) -> {
            RadioButton checkedRadioButton = radioGroup.findViewById(checkID);
            if(checkedRadioButton == rb1){
                parameters[2] = "departure";
            }else if(checkedRadioButton == rb2&&!parameters[3].equals("now")){ //Checks to make sure Arrive is valid for the inputted time
                parameters[2] = "arrival";
            }else{
                Toast.makeText(MainActivity.this,"Please choose a valid arrival time",Toast.LENGTH_SHORT).show();
                rb1.setChecked(true);
            }
        });

        //Date UI Listeners
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());
        et1.setText(df.format(c));

        DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, month);
                myCalendar.set(Calendar.DAY_OF_MONTH, day);
                SimpleDateFormat dateFormat=new SimpleDateFormat("dd-MMM-yy", Locale.US);
                et1.setText(dateFormat.format(myCalendar.getTime()));
            }
        };
        et1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(MainActivity.this,date,myCalendar.get(Calendar.YEAR),myCalendar.get(Calendar.MONTH),myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        //Time UI Listeners
        SimpleDateFormat tf = new SimpleDateFormat("HH:mm",Locale.getDefault());
        et2.setText(tf.format(myCalendar.getTime()));
        parameters[3] = "now";

        et2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                int hour = myCalendar.get(Calendar.HOUR_OF_DAY);
                int minute = myCalendar.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker = new TimePickerDialog(MainActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                        myCalendar.set(Calendar.HOUR_OF_DAY,hour);
                        myCalendar.set(Calendar.MINUTE,minute);
                        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm",Locale.US);
                        if(!et2.getText().toString().equals(timeFormat.format(myCalendar.getTime()))) {
                            et2.setText(timeFormat.format(myCalendar.getTime()));
                            parameters[3] = String.valueOf(((Date)myCalendar.getTime()).getTime()/1000);
                            System.out.println(parameters[3]);
                        }
                    }
                }, hour, minute, true);//Yes 24 hour time
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();

            }
        });

        //Button Listener that starts the MapsActivity
        b1.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                //Makes sure the locations are filled out
                if (!parameters[0].equals("") && !parameters[1].equals("")) {
                    RouteDetails.routes.clear();
                    if(RouteDetails.lv!=null) RouteDetails.lv.setAdapter(null);
                    startActivity(intent);
                } else {
                    Toast.makeText(MainActivity.this,"Please fill out all of the fields",Toast.LENGTH_SHORT).show();
                }
            }
        });

        //Colors xP
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(this.getResources().getColor(R.color.dark_pink));

        //Custom Back Button
        ActionBar actionBar = getSupportActionBar();
        ColorDrawable colorDrawable = new ColorDrawable(getColor(R.color.hot_pink));
        actionBar.setBackgroundDrawable(colorDrawable);
    }

    //Makes the user press the back button twice to exit the application
    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Press BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }

    //Google Places Search implementation
    public void searchPlace(AutocompleteSupportFragment autocompleteFragment) {

        autocompleteFragment.setLocationBias(RectangularBounds.newInstance(
                new LatLng(50.849775806533195, -114.220620643057),
                new LatLng(51.18253673231386, -113.88791240983224)));
        autocompleteFragment.setCountries("CA");

        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME));

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                if(autocompleteFragment==start){
                    parameters[0] = place.getId();
                    autoFragment[0] = place.getName();
                }else if(autocompleteFragment == end){
                    parameters[1] = place.getId();
                    autoFragment[1] = place.getName();
                }
            }

            @Override
            public void onError(@NonNull Status status) {
                System.out.println(status);
            }
        });
    }

    //Calls the GPSTracker class in order to get location
    public void updateGPS(){
        gpsTracker = new GPSTracker(MainActivity.this, ib1);

        // Check if GPS enabled
        if (gpsTracker.canGetLocation()) {
            updateUI(gpsTracker.getLocation());
        }
        gpsTracker.stopUsingGPS();
    }

    //Converts location data into a google place search request, decoded using JsonTask
    private void updateUI(Location location) {
        if(location != null) {
            currentPoint = new LatLng(gpsTracker.getLatitude(),gpsTracker.getLongitude());
            String URL ="https://maps.googleapis.com/maps/api/geocode/json?latlng="+gpsTracker.getLatitude()+","+gpsTracker.getLongitude()+"&key="+getString(R.string.key);
            try {
                new JsonTask(MainActivity.this).execute(URL).get();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    //Overrides the default permission request actions
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 99:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    updateGPS();
                } else {
                    Toast.makeText(this, "Your current address will not be autofilled", Toast.LENGTH_SHORT).show();
                    i = 0;
                    ib1.setImageResource(images[1]);
                }
        }
    }


}




//    public static final int DEFAULT_UPDATE_INTERVAL = 30;
//    public static final int FAST_UPDATE_INTERVAL = 5;
//    private static final int PERMISSION_FINE_LOCATION = 99;
//
//    //Google's API for Location Services
//    FusedLocationProviderClient fusedLocationClient;
//    static boolean isGPSEnabled = false,isNetworkEnabled = false;
//    static LocationManager locationManager;
//    LocationRequest locationRequest;
//    Location location; // location
//    private Context mContext;
//    Geocoder geocoder;
//    List<Address> addresses;

// in OnCreate
//Set all properties of LocationRequest
//
//
//    fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

//
//    private void updateGPS() {
//        if (checkPermission()) {
//            //user provided the permission
//            if (isLocationEnabled(MainActivity.this)) {
//
//                locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);
//
//                fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
//                    @Override
//                    public void onSuccess(Location location) {
//
//                        geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
//                        try {
//                            addresses = geocoder.getFromLocation((double) location.getLatitude(), (double) location.getLongitude(), 1);
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                        currentLocation = addresses.get(0).getAddressLine(0);
//                        Toast.makeText(MainActivity.this, currentLocation, Toast.LENGTH_SHORT).show();
//
//                    }
//                });
//            }
////                Toast.makeText(this, "Please turn on location",Toast.LENGTH_SHORT).show(); //TODO:Settings high accuracy GPS
//        } else {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_FINE_LOCATION);
//            }
//        }
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        switch (requestCode) {
//            case PERMISSION_FINE_LOCATION:
//                if (grantResults[0] == PackageManager.PERMISSION_GRANTED
//                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
//                    updateGPS();
//                } else {
//                    Toast.makeText(this, "This app requires permissions to be granted in order to work properly", Toast.LENGTH_SHORT).show();
//                    i = 0;
//                    ib1.setImageResource(images[1]);
//                }
//                break;
//        }
//    }
//
//    public boolean checkPermission() {
//        if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) &&
//                (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
//            return true;
//        } else {
//            return false;
//        }
//    }
//
//    public static boolean isLocationEnabled(Context context) {
//        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
//        boolean gps_enabled = false;
//        boolean network_enabled = false;
//
//        try {
//            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
//        } catch (Exception ex) { }
//
//        try {
//            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
//        } catch (Exception ex) { }
//
//        if (!gps_enabled && !network_enabled) {
//            // notify user
//            new AlertDialog.Builder(context)
//                    .setMessage(R.string.gps_network_not_enabled)
//                    .setPositiveButton(R.string.open_location_settings, new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
//                            context.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
//                        }
//                    })
//                    .setNegativeButton(R.string.Cancel, null)
//                    .show();
//        }
//        return (gps_enabled||network_enabled);
//    }
//}
