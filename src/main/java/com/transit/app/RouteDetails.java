package com.transit.app;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class RouteDetails extends AppCompatActivity {
    static ArrayList<Values> routes = new ArrayList<>();
    static String start_Time;
    static String end_Time;
    static ListView lv;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route);

        //XML References
        lv = findViewById(R.id.list);
        TextView et1 = findViewById(R.id.time_start);
        TextView et2 = findViewById(R.id.time_end);
        ImageButton backMap = findViewById(R.id.ToMap);

        et1.setText(start_Time);
        et2.setText(end_Time);

        //Calls on a custom list adapter in order to implement adapter_view_layout.XML
        RouteListAdapter adapter = new RouteListAdapter(this,R.layout.adapter_view_layout,routes);
        lv.setAdapter(adapter);

        //closes the activity to get back to the map
        backMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //Colors xP
    Window window = this.getWindow();
    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
    window.setStatusBarColor(this.getResources().getColor(R.color.hot_pink));

        //Custom Back Button
        ActionBar actionBar = getSupportActionBar();
        ColorDrawable colorDrawable = new ColorDrawable(getColor(R.color.hot_pink));
        actionBar.setBackgroundDrawable(colorDrawable);
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setHomeButtonEnabled(false);
    }
}
