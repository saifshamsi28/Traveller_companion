package com.saif.traveller;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FindRouteFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FindRouteFragment extends Fragment implements BusInfoAdapter.OnItemClickListener{
    SearchView sourceSearchView, destSearchView;
    BusInfoAdapter adapter;
    private List<Route> busDetails;
    RecyclerView recyclerView,intermediaterecyclerview;
    IntermediateStationAdapter intermediateStationAdapter;
    IntermediateStopsActivity intermediateStopsActivity;
    Button findButton;
    TextView sourceTextview,destinationTextview,seperator;
    GridLayout gridLayout;
    FloatingActionButton backButton;


    public FindRouteFragment() {
        // Required empty public constructor
    }
    public FindRouteFragment(List<Route> routeList) {
        busDetails = routeList;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_find_route, container, false);
        sourceSearchView = view.findViewById(R.id.source_searchview);
        destSearchView = view.findViewById(R.id.destination_searchview);
        recyclerView = view.findViewById(R.id.filtered_list_recyclerview);
        sourceTextview=view.findViewById(R.id.sourceTextView);
        destinationTextview=view.findViewById(R.id.destinationTextView);
        gridLayout=view.findViewById(R.id.grid_layout);
        seperator=view.findViewById(R.id.seperator);
        intermediaterecyclerview=view.findViewById(R.id.mid_stops_recyclerview);
        findButton=view.findViewById(R.id.find_button);
        backButton=view.findViewById(R.id.back_button);
        findButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.find_button)));
        backButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.find_button)));

        // Initialize the intermediateStationAdapter with an empty list
        intermediateStationAdapter = new IntermediateStationAdapter(new ArrayList<>(),
                Collections.singletonList(new LatLng(17.423743936845078, 78.36472634886442)),
                new IntermediateStationAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String stationName, LatLng latLng) {
                if(sourceSearchView.hasFocus())
                    sourceSearchView.setQuery(stationName, false);
                else
                    destSearchView.setQuery(stationName, false);

                recyclerView.setVisibility(View.GONE);
                findButton.setVisibility(View.VISIBLE);

                //to dhow the find button only after entering source and destination
//                if(!sourceSearchView.getQuery().toString().trim().isEmpty() && !destSearchView.getQuery().toString().trim().isEmpty())
//                    findButton.setVisibility(View.VISIBLE);
            }

        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(intermediateStationAdapter);
        recyclerView.setVisibility(View.GONE);

        sourceSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                findButton.setVisibility(View.VISIBLE);
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                if (!newText.isEmpty()) {
                    findButton.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    filteredSourceList(newText);
                } else {
                    recyclerView.setVisibility(View.GONE);
                    findButton.setVisibility(View.VISIBLE);
                }
                return true;
            }
        });

        destSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                findButton.setVisibility(View.VISIBLE);
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                if (!newText.isEmpty()) {
                    findButton.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    filteredDestinationList(newText);
                } else {
                    recyclerView.setVisibility(View.GONE);
                    findButton.setVisibility(View.VISIBLE);
                }
                return true;
            }
        });

        findButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sourceSearchView.getQuery().toString().trim().isEmpty() &&
                        destSearchView.getQuery().toString().trim().isEmpty()) {
                    Toast.makeText(getContext(), "enter the source and destination first", Toast.LENGTH_LONG).show();
                }else if(sourceSearchView.getQuery().toString().trim().isEmpty())
                {
                    Toast.makeText(getContext(), "Enter the source first", Toast.LENGTH_SHORT).show();
                }else if(destSearchView.getQuery().toString().trim().isEmpty()){
                    Toast.makeText(getContext(), "enter the destination first", Toast.LENGTH_SHORT).show();
                }else{
                    String source=sourceSearchView.getQuery().toString().trim().toUpperCase();
                    String destination=destSearchView.getQuery().toString().trim().toUpperCase();
                    findRoute(source,destination);
                    findButton.setVisibility(View.GONE);
                    sourceSearchView.setVisibility(View.GONE);
                    destSearchView.setVisibility(View.GONE);
                    sourceTextview.setText(source);
                    destinationTextview.setText(destination);
                    gridLayout.setVisibility(View.VISIBLE);
                    seperator.setVisibility(View.VISIBLE);
                    intermediaterecyclerview.setVisibility(View.VISIBLE);
                    backButton.setVisibility(View.VISIBLE);
                    backButton.setTranslationY(-160);
                }
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findButton.setVisibility(View.VISIBLE);
                sourceSearchView.setVisibility(View.VISIBLE);
                destSearchView.setVisibility(View.VISIBLE);
                gridLayout.setVisibility(View.GONE);
                seperator.setVisibility(View.GONE);
                backButton.setVisibility(View.GONE);
                intermediaterecyclerview.setVisibility(View.GONE);

            }
        });

        return view;
    }

    private void findRoute(String source, String destination) {
        List<Route>routeIds=new ArrayList<>();
        int count=0;// for showing only 2 set of intermediate station
        for(Route route:busDetails){
            count++;
            if(route.getSource().toUpperCase().equals(source) || route.getSource().toUpperCase().equals(destination) ){
                routeIds.add(route);
            }
            else {
                if(route.getDestination().toUpperCase().equals(source) || route.getDestination().toUpperCase().equals(destination) ){
                    routeIds.add(route);
                }
            }
        }

        intermediaterecyclerview.setLayoutManager(new LinearLayoutManager(getContext()));
        BusInfoAdapter busInfoAdapter=new BusInfoAdapter(routeIds,  this);
        intermediaterecyclerview.setAdapter(busInfoAdapter);
        Drawable dividerDrawable = ContextCompat.getDrawable(getContext(), R.drawable.custom_divider);
// Set the divider with the custom drawable
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(intermediaterecyclerview.getContext(), DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(dividerDrawable);

// Add the decoration to the RecyclerView
        intermediaterecyclerview.addItemDecoration(dividerItemDecoration);
    }

    private void filteredDestinationList(String newText) {
        Set<String> filteredSet = new HashSet<>();
        if (busDetails != null) {
            for (Route route : busDetails) {
                if (route.getDestination().toLowerCase().contains(newText.toLowerCase())) {
                    filteredSet.add(route.getDestination());
                }
            }
        }
        List<String> filteredList = new ArrayList<>(filteredSet); // Convert Set to List
        intermediateStationAdapter.filteredList(filteredList);
    }

    private void filteredSourceList(String newText) {
        Set<String> filteredSet = new HashSet<>(); // Using a Set to avoid  duplicate entry
        if (busDetails != null){
            for (Route route : busDetails) {
                if (route.getSource().toLowerCase().contains(newText.toLowerCase())) {
                    filteredSet.add(route.getSource());
                }
            }
        }
        List<String> filteredList = new ArrayList<>(filteredSet); // Convert Set to List
        intermediateStationAdapter.filteredList(filteredList);
    }

    @Override
    public void onItemClick(Route route) {
        Intent intent=new Intent(getContext(),IntermediateStopsActivity.class);
        intent.putExtra("SOURCE",route.getSource());
        intent.putExtra("DESTINATION",route.getDestination());
        intent.putExtra("ROUTE_ID",route.getRouteId());
        startActivity(intent);
    }
}