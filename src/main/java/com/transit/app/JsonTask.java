package com.transit.app;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class JsonTask extends AsyncTask<String, String, String> {
    Context mContext;
    String formattedAddress;
    ProgressDialog pd;

    public JsonTask(Context context) {
        mContext = context;
    }
        protected void onPreExecute() {
            super.onPreExecute();

            pd = new ProgressDialog(mContext);
            pd.setMessage("Please wait");
            pd.setCancelable(false);
            pd.show();
        }

    protected String doInBackground(String... params) {

        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try {
            URL url = new URL(params[0]);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            System.out.println(params[0]);


            InputStream stream = connection.getInputStream();

            reader = new BufferedReader(new InputStreamReader(stream));

            StringBuilder buffer = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                buffer.append(line).append("\n");
                Log.d("Response: ", "> " + line);   //Logs the whole response
            }

            return buffer.toString();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        if (pd.isShowing()){
            pd.dismiss();
        }
        //Place Listener Results
        try {
            JSONObject obj = new JSONObject(result);
            JSONArray results = obj.getJSONArray("results");
            formattedAddress = results.getJSONObject(0).getString("formatted_address");
            MainActivity.parameters[0] = results.getJSONObject(0).getString("place_id");
            MainActivity.autoFragment[0] = formattedAddress;
            MainActivity.start.setText(formattedAddress);
        } catch (JSONException e) {
        }
        //Google Transit request Results, with multiple branches referencing specific values
        //This calls multiple static values from MapsActivity that is vital to the descriptions and polylines of the map
        try {
            JSONObject obj = new JSONObject(result);
            String stat = obj.getString("status");
            if(stat.equals("OK")){
                JSONObject results = obj.getJSONArray("routes").getJSONObject(0);
                LatLng northEast = new LatLng(results.getJSONObject("bounds").getJSONObject("northeast").getDouble("lat"),
                        results.getJSONObject("bounds").getJSONObject("northeast").getDouble("lng"));
                LatLng southWest = new LatLng(results.getJSONObject("bounds").getJSONObject("southwest").getDouble("lat"),
                        results.getJSONObject("bounds").getJSONObject("southwest").getDouble("lng"));
                System.out.println(northEast+": "+southWest);
                MapsActivity.bounds = new LatLngBounds(southWest,northEast);
                MapsActivity.end = new LatLng(results.getJSONArray("legs").getJSONObject(0).getJSONObject("end_location").getDouble("lat"),results.getJSONArray("legs").getJSONObject(0).getJSONObject("end_location").getDouble("lng"));
                JSONArray steps = results.getJSONArray("legs").getJSONObject(0).getJSONArray("steps");
                String[] travel_modes = new String[steps.length()];
                String[] stepPolylines = new String[steps.length()];
                for(int i=0 ;i<steps.length();i++){
                    String condition = steps.getJSONObject(i).getString("travel_mode");
                    if(condition.equals("WALKING")) {
                        travel_modes[i] = condition;

                        RouteDetails.routes.add(new Values(steps.getJSONObject(i).getJSONObject("distance").getString("text"),
                                steps.getJSONObject(i).getJSONObject("duration").getString("text"),
                                steps.getJSONObject(i).getString("html_instructions"),
                                "Walking",
                                R.drawable.vertical_dots_line)); //TODO: WALKING START AND END TIMES

                    }else if(condition.equals("TRANSIT")){
                        RouteDetails.start_Time = results.getJSONArray("legs").getJSONObject(0).getJSONObject("departure_time").getString("text");
                        RouteDetails.end_Time = results.getJSONArray("legs").getJSONObject(0).getJSONObject("arrival_time").getString("text");
                        String isTrain = steps.getJSONObject(i).getJSONObject("transit_details").getJSONObject("line").getJSONObject("vehicle").getString("type");
                        JSONObject details = steps.getJSONObject(i).getJSONObject("transit_details");
                        if(isTrain.equals("TRAM")){
                            travel_modes[i] = "TRAIN";

                            RouteDetails.routes.add(new Values(steps.getJSONObject(i).getJSONObject("distance").getString("text"),
                                    steps.getJSONObject(i).getJSONObject("duration").getString("text"),
                                    steps.getJSONObject(i).getString("html_instructions"),
                                    "Train",
                                    R.drawable.red_line,
                                    details.getJSONObject("arrival_time").getString("text"),
                                    details.getJSONObject("departure_time").getString("text"),
                                    details.getJSONObject("line").getString("short_name")+
                                            " "+details.getJSONObject("line").getString("name")));
                        }else{
                            travel_modes[i] = "BUS";

                            RouteDetails.routes.add(new Values(steps.getJSONObject(i).getJSONObject("distance").getString("text"),
                                    steps.getJSONObject(i).getJSONObject("duration").getString("text"),
                                    steps.getJSONObject(i).getString("html_instructions"),
                                    "Bus",
                                    R.drawable.pink_line,
                                    details.getJSONObject("arrival_time").getString("text"),
                                    details.getJSONObject("departure_time").getString("text"),
                                    details.getJSONObject("line").getString("short_name")+
                                            " "+details.getJSONObject("line").getString("name")));
                        }
                    }
                    stepPolylines[i] = steps.getJSONObject(i).getJSONObject("polyline").getString("points");
                }
                MapsActivity.travelMode = travel_modes;
                MapsActivity.polyline = stepPolylines;
            }else{
                LatLng northEast = new LatLng(51.18465142831979, -113.90178089957037);
                LatLng southWest = new LatLng(50.84685029141629, -114.22725085987447);
                MapsActivity.bounds = new LatLngBounds(southWest,northEast);
                if(MainActivity.currentPoint!=null){
                    MapsActivity.end = MainActivity.currentPoint;
                }else{
                    MapsActivity.end = MapsActivity.bounds.getCenter();
                }
                String[] travel_modes = new String[1];
                String[] stepPolylines = new String[1];
                travel_modes[0] = "WALKING";
                stepPolylines[0] = String.valueOf(new LatLng(50.98645585656319, -114.08449487967361));
                MapsActivity.travelMode = travel_modes;
                MapsActivity.polyline=stepPolylines;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
