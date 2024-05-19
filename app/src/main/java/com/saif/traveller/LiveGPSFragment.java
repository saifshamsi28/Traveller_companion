package com.saif.traveller;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

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

import java.util.ArrayList;

public class LiveGPSFragment extends Fragment  implements RouteListener {

    private FusedLocationProviderClient fusedLocationProviderClient;
    private GoogleMap googleMap;
    private SupportMapFragment supportMapFragment;
    private static final int REQUEST_LOCATION_CODE = 1;
    private LatLng userLocation,destinationLocation;
    ArrayList<Polyline> polylines = null;
    private ProgressDialog progressDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_live_g_p_s, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //requireActivity to get your actual context
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity());
         progressDialog=new ProgressDialog(getContext());

        supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_container);
        if (supportMapFragment != null) {
            supportMapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(@NonNull GoogleMap googleMap) {
                    LiveGPSFragment.this.googleMap = googleMap;
                    LatLng deptOfCSIT = new LatLng(17.423743936845078, 78.36472634886442);
                    LiveGPSFragment.this.googleMap.addMarker(new MarkerOptions().position(deptOfCSIT).title("Dept of CS & IT"));
                    LiveGPSFragment.this.googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(deptOfCSIT, 18f));

                    googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                        @Override
                        public void onMapClick(@NonNull LatLng latLng) {
                            googleMap.clear();
                            destinationLocation=latLng;
                            progressDialog.setMessage("Route is generating, please wait");
                            progressDialog.show();
                            googleMap.addMarker(new MarkerOptions().position(latLng));
                            getRoutePoints(userLocation,destinationLocation);
                        }
                    });
                }
            });
        } else {
            Toast.makeText(requireContext(), "Map fragment not found", Toast.LENGTH_SHORT).show();
        }

        // Set onClickListener for GPS Button
        view.findViewById(R.id.button_GPS).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getUserCurrentLocation();
            }
        });
    }

    private void getUserCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getLastLocation();
        } else {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION_CODE);
        }
    }

    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(requireActivity(), new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null && googleMap != null) {
                            LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            userLocation=currentLocation;
                            googleMap.addMarker(new MarkerOptions().position(currentLocation).title("You are here"));
                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 18f));
                        } else {
                            Toast.makeText(requireContext(), "Location not available", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void getRoutePoints(LatLng start, LatLng end) {
        if (start == null ) {
            if( end == null) {
                Toast.makeText(getContext(), "Unable to user and destination location", Toast.LENGTH_LONG).show();
                Log.e("route", " latlngs are null");
            }else {
                Toast.makeText(getContext(), "Unable to get user location", Toast.LENGTH_SHORT).show();
            }
        } else {
            RouteDrawing routeDrawing = new RouteDrawing.Builder()
                    .context(getContext())
                    .travelMode(AbstractRouting.TravelMode.DRIVING)
                    .withListener(this).alternativeRoutes(true)
                    .waypoints(start, end)
                    .build();
            routeDrawing.execute();
//            Toast.makeText(getContext(), "route is drawing", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRouteFailure(ErrorHandling errorHandling) {
        Log.e("route generation ---", "onRouteFailure: "+errorHandling );
        Toast.makeText(getContext(), "Route generating failed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRouteStart() {
        getLastLocation();
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
                polylineOptions.color(Color.BLACK);
                polylineOptions.width(12);
                polylineOptions.color(R.color.GreenishBlue);
                polylineOptions.addAll(routeInfoModelArrayList.get(routeIndexing).getPoints());
                polylineOptions.startCap(new RoundCap());
                polylineOptions.endCap(new RoundCap());
                Polyline polyline = googleMap.addPolyline(polylineOptions);
                polylines.add(polyline);
                progressDialog.dismiss();
            }
        }

    }

    @Override
    public void onRouteCancelled() {
        Toast.makeText(getContext(), "Route loading cancelled", Toast.LENGTH_SHORT).show();
    }
}
