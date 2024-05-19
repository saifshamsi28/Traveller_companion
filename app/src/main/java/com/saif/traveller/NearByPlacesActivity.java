package com.saif.traveller;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.codebyashish.googledirectionapi.AbstractRouting;
import com.codebyashish.googledirectionapi.ErrorHandling;
import com.codebyashish.googledirectionapi.RouteDrawing;
import com.codebyashish.googledirectionapi.RouteInfoModel;
import com.codebyashish.googledirectionapi.RouteListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class NearByPlacesActivity extends AppCompatActivity implements OnMapReadyCallback,RouteListener {
    GoogleMap gmap;
    SupportMapFragment supportMapFragment;
    FloatingActionButton currentLocationButton;
    FusedLocationProviderClient locationProviderClient;
    public int REQUEST_LOCATION_CODE = 1;
    private LatLng userLocation, destinationLocation;
    ArrayList<Polyline> polylines = null;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_near_by_places);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //to set the colour of status bar(where time,wifi,battery level,volte LTE  is showing) same as toolbar
        Window window = getWindow();
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.Teal));

        progressDialog=new ProgressDialog(this);
        currentLocationButton = findViewById(R.id.button_GPS);
        supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_container);
        if (supportMapFragment != null) {
            supportMapFragment.getMapAsync(this);
        } else {
            Toast.makeText(this, "Map fragment not found", Toast.LENGTH_SHORT).show();
        }
        locationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        currentLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLastLocation();
            }
        });

    }

    public void onMapReady(@NonNull GoogleMap googleMap) {
        gmap = googleMap;
        LatLng deptOfCSIT = new LatLng(17.423743936845078, 78.36472634886442);
        gmap.addMarker(new MarkerOptions().position(deptOfCSIT).title("Dept of CS & IT"));
        gmap.animateCamera(CameraUpdateFactory.newLatLngZoom(deptOfCSIT, 16f));

        LatLng MANUU = new LatLng(17.425610289763586, 78.36353651495902);
        gmap.addMarker(new MarkerOptions().position(MANUU).title("Maulana Azad National Urdu University"));
        gmap.animateCamera(CameraUpdateFactory.newLatLngZoom(MANUU, 16f));
//        userLocation=MANUU;
//        getLastLocation();

        // to get location when clicked to intermediate station location icon
        Intent intent = getIntent();
        String stationName = intent.getStringExtra("STATION_NAME");
        double destlatitude = intent.getDoubleExtra("LATITUDE", 0);
        double destlongitude = intent.getDoubleExtra("LONGITUDE", 0);
        double userLatitude = intent.getDoubleExtra("USER_LATITUDE", 0);
        double userLongitude = intent.getDoubleExtra("USER_LONGITUDE", 0);
        userLocation=new LatLng(userLatitude,userLongitude);
        gmap.addMarker(new MarkerOptions().position(userLocation).title("You are here"));



        LatLng stationLocation = new LatLng(destlatitude, destlongitude);
        if (intent != null)
            getLocationByLatLng(stationName, stationLocation);

        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng latLng) {
                googleMap.clear();
                destinationLocation = latLng;
                googleMap.addMarker(new MarkerOptions().position(latLng));
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 20f));
                getRoutePoints(userLocation, destinationLocation);
            }
        });

    }

    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Location permission not granted, request it

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION_CODE);
            }
            return;
        }

        Task<Location> task = locationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    userLocation = currentLocation;
//                    Toast.makeText(NearByPlacesActivity.this, "user location set to current location", Toast.LENGTH_LONG).show();
                    gmap.addMarker(new MarkerOptions().position(currentLocation).title("You are here"));
                    gmap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 18f));
                } else {
                    Toast.makeText(NearByPlacesActivity.this, "Location not available", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void getLocationByLatLng(String stationName, LatLng location) {
        if (stationName != null) {
            if (location != null) {
                if (gmap != null) {
                    gmap.addMarker(new MarkerOptions().position(location).title(stationName));
                    gmap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 16));
                    progressDialog.setMessage("Route is generating , please wait");
                    progressDialog.show();
                    getRoutePoints(userLocation, location);
                }
            } else {
                Toast.makeText(this, "Station location is null", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Station name is null", Toast.LENGTH_SHORT).show();
        }

    }

    public void getRoutePoints(LatLng start, LatLng end) {
        if (start == null && end == null) {
                Toast.makeText(this, "Unable to get user and destination location", Toast.LENGTH_LONG).show();
                Log.e("route", " latlngs are null");
            } else if(start==null) {
                Toast.makeText(this, "Unable to get user location", Toast.LENGTH_SHORT).show();
            } else if (end==null) {
            Toast.makeText(this, "Unable to get destination location", Toast.LENGTH_SHORT).show();
            } else{
                RouteDrawing routeDrawing = new RouteDrawing.Builder()
                    .context(this)
                    .travelMode(AbstractRouting.TravelMode.DRIVING)
                    .withListener(this).alternativeRoutes(true)
                    .waypoints(start, end)
                    .build();
            routeDrawing.execute();
//            Toast.makeText(this, "route is drawing", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRouteFailure(ErrorHandling errorHandling) {
        Log.e("TAG", "onRouteFailure: " + errorHandling);
        Toast.makeText(this, "Route generation failed ", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRouteStart() {
//        getLastLocation();
        Toast.makeText(this, "Route generation started ", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRouteSuccess(ArrayList<RouteInfoModel> routeInfoModelArrayList, int routeIndexing) {
        if (polylines != null) {
            polylines.clear();
        }
        PolylineOptions polylineOptions = new PolylineOptions();
        polylines = new ArrayList<>();
        for (int i = 0; i < routeInfoModelArrayList.size(); i++) {
            if (i == routeIndexing) {
                Log.e("TAG", "onRoutingSuccess: routeIndexing" + routeIndexing);
                polylineOptions.color(ContextCompat.getColor(this, R.color.GreenishBlue));
                polylineOptions.width(12);
                polylineOptions.addAll(routeInfoModelArrayList.get(routeIndexing).getPoints());
                polylineOptions.startCap(new RoundCap());
                polylineOptions.endCap(new RoundCap());
                Polyline polyline = gmap.addPolyline(polylineOptions);
                polylines.add(polyline);
                progressDialog.dismiss();
            }
        }
    }

        @Override
        public void onRouteCancelled () {
            Toast.makeText(this, "Route generation cancelled ", Toast.LENGTH_SHORT).show();
        }
}