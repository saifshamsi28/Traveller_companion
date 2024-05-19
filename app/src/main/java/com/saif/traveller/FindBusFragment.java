package com.saif.traveller;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class FindBusFragment extends Fragment{

    private RecyclerView busRecyclerView;
    static List<Route> busDetailsList;
    BusInfoAdapter adapter;
    SearchView searchView;

    public FindBusFragment(List<Route> busDetailsList) {
        this.busDetailsList = busDetailsList;
    }
    public FindBusFragment() {
        //default constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_find_bus, container, false);

        busRecyclerView = view.findViewById(R.id.busRecyclerView);

        busRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        //to async load map in background to avoid lagging app
        LiveGPSFragment liveGPSFragment=new LiveGPSFragment();
        liveGPSFragment.onCreateView(getLayoutInflater(), (ViewGroup) getView(),getArguments());

        adapter = new BusInfoAdapter(busDetailsList, new BusInfoAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Route route) {
                Intent intent=new Intent(getContext(),IntermediateStopsActivity.class);
                intent.putExtra("SOURCE",route.getSource());
                intent.putExtra("DESTINATION",route.getDestination());
                intent.putExtra("ROUTE_ID",route.getRouteId());
                startActivity(intent);
            }
        });

        busRecyclerView.setAdapter(adapter);
        // Create a custom divider drawable
        Drawable dividerDrawable = ContextCompat.getDrawable(getContext(), R.drawable.custom_divider);
// Set the divider with the custom drawable
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(busRecyclerView.getContext(), DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(dividerDrawable);

// Add the decoration to the RecyclerView
        busRecyclerView.addItemDecoration(dividerItemDecoration);

        searchView=view.findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filteredList(newText);
                return true;
            }
        });

        return view;
    }

    private void filteredList(String newText) {
        List<Route> busNumber=new ArrayList<>();
        for(Route busId:busDetailsList) {
            if (busId.getRouteId().toLowerCase().contains(newText.toLowerCase())) {
                busNumber.add(busId);
            }
        }
        adapter.filteredList(busNumber);
    }

}
