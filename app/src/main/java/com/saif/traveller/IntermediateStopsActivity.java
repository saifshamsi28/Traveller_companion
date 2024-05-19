package com.saif.traveller;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class IntermediateStopsActivity extends AppCompatActivity implements IntermediateStationAdapter.OnItemClickListener{
    private static final int REQUEST_LOCATION_CODE = 1;
    TextView routeId,source,destination;
    RecyclerView recyclerView;
    String busRouteId;
     static List<String> stationNames=new ArrayList<>();
    List<LatLng> stationLocations=new ArrayList<>();
    TextView intermediateStation;
    private FusedLocationProviderClient locationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_intermediate_stops);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        locationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        //to set the colour of status bar(where time,wifi,battery level,volte LTE  is showing) same as toolbar
        Window window = getWindow();
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.Teal));

        routeId=findViewById(R.id.routeIdTextView);
        source=findViewById(R.id.sourceTextView);
        destination=findViewById(R.id.destinationTextView);
        intermediateStation=findViewById(R.id.station_name);

        Intent intent=getIntent();
        String bus=intent.getStringExtra("ROUTE_ID");
        busRouteId=bus;
        List<String> routeIds = intent.getStringArrayListExtra("routeIds");
        String sourceName=intent.getStringExtra("SOURCE");
        String destName=intent.getStringExtra("DESTINATION");
            routeId.setText(bus);
                // to split the number from routeId....like 19 from 19CK,217 from 217K,1 from 1AD
                StringBuilder filename = new StringBuilder();
                for (char c : busRouteId.toCharArray()) {
                    if (Character.isDigit(c)) {
                        filename.append(c);
                    }
                    else {
                        break;
                    }
                }
                String csvFileName = filename + ".csv";
                // Read the CSV file and extract station names
                readStationNamesFromCSV(csvFileName);
        source.setText(sourceName);
        destination.setText(destName);

        // Display station names in ListView
         recyclerView = findViewById(R.id.station_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        IntermediateStationAdapter intermediateStationAdapter=new IntermediateStationAdapter(stationNames,stationLocations,this);
        recyclerView.setAdapter(intermediateStationAdapter);
        Drawable dividerDrawable = ContextCompat.getDrawable(this, R.drawable.custom_divider);
// Set the divider with the custom drawable
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(dividerDrawable);
        recyclerView.addItemDecoration(dividerItemDecoration);

    }
    void readStationNamesFromCSV(String fileName) {
        try {
            InputStream inputStream = getAssets().open("route_stops_order/" + fileName);
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            CSVReader csvReader = new CSVReader(inputStreamReader);
            String[] nextRecord;
            while ((nextRecord = csvReader.readNext()) != null) {
                if (nextRecord.length >= 5) {
                    LatLng location=new LatLng( Double.valueOf(nextRecord[1]),Double.valueOf(nextRecord[2]));
                    String stationName = nextRecord[3]; // Index 3 for the station name
                    stationNames.add(stationName);
                    stationLocations.add(location);
                }
            }
            csvReader.close();
        } catch (IOException | CsvException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onItemClick(String stationName, LatLng latLng) {
        // Perform your desired action here when an item is clicked
        // For example, display a toast with the clicked station name
//        Toast.makeText(this, "You just Clicked the station: " + stationName+" location : "+latLng, Toast.LENGTH_SHORT).show();
//        LiveGPSFragment liveGPSFragment=new LiveGPSFragment();
//        liveGPSFragment.getLocationByLatLng(stationName,latLng);
//        Intent intent = new Intent(this, NearByPlacesActivity.class);
//        intent.putExtra("STATION_NAME", stationName);
//        intent.putExtra("LATITUDE", latLng.latitude);
//        intent.putExtra("LONGITUDE", latLng.longitude);
//        startActivity(intent);
            // Retrieve user location before starting the NearbyPlacesActivity
            getLastLocation(stationName, latLng);
    }
    private void getLastLocation(String stationName, LatLng latLng) {
        // Check if location permission is granted
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Location permission not granted, request it
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_CODE);
            return;
        }

        // Location permission granted, get the last known location
        Task<Location> task = locationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    // User location retrieved successfully, start NearbyPlacesActivity
                    Intent intent = new Intent(IntermediateStopsActivity.this, NearByPlacesActivity.class);
                    intent.putExtra("STATION_NAME", stationName);
                    intent.putExtra("LATITUDE", latLng.latitude);
                    intent.putExtra("LONGITUDE", latLng.longitude);
                    intent.putExtra("USER_LATITUDE", location.getLatitude());
                    intent.putExtra("USER_LONGITUDE", location.getLongitude());
                    startActivity(intent);
                } else {
                    // Unable to get user location, show a toast message
                    Toast.makeText(IntermediateStopsActivity.this, "Unable to get user location", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}