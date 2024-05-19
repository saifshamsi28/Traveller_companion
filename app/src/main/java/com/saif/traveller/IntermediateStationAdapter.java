package com.saif.traveller;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public class IntermediateStationAdapter extends RecyclerView.Adapter <IntermediateStationAdapter.StationViewHolder>{
    private List<String> stationName;
    private OnItemClickListener listener;
    static List<LatLng> stationLocations;
    int positionOfStation=0;

    public interface OnItemClickListener {
        void onItemClick(String stationName, LatLng latLng);
    }

    public IntermediateStationAdapter(List<String> stationName, List<LatLng> stationLocations, OnItemClickListener listener) {
        this.stationName = stationName;
        this.stationLocations = stationLocations; // Store stationLocations
        this.listener = listener;
    }

    @NonNull
    @Override
    public StationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.intermediate_stations,parent,false);
        return new StationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IntermediateStationAdapter.StationViewHolder holder, int position) {
        String station = stationName.get(position);
        positionOfStation=position;
        holder.station_Name.setText(station);
        holder.station_Name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null && position < stationLocations.size()) {
                    listener.onItemClick(station, stationLocations.get(position)); // Pass LatLng along with station name
                }
            }
        });
        holder.location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null && position < stationLocations.size()) {
                    listener.onItemClick(station, stationLocations.get(position)); // Pass LatLng along with station name
                }
            }
        });
    }

    public void filteredList(List<String> busNumber){
        stationName=busNumber;
        notifyDataSetChanged();
    }
    @Override
    public int getItemCount() {
        return stationName.size();
    }
    static class StationViewHolder extends RecyclerView.ViewHolder {
        TextView station_Name;
        ImageView location;

        public StationViewHolder(@NonNull View itemView) {
            super(itemView);
            station_Name=itemView.findViewById(R.id.station_name);
            location=itemView.findViewById(R.id.location_button);
        }
        public void bind(final String station, final OnItemClickListener listener) {
            station_Name.setText(station);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(station, stationLocations.get(getAdapterPosition()));
                }
            });
        }
    }

}
