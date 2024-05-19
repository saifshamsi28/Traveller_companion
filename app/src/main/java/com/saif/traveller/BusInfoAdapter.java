package com.saif.traveller;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class BusInfoAdapter extends RecyclerView.Adapter<BusInfoAdapter.RouteViewHolder> {

    private List<Route> routeList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Route route);
    }

    public BusInfoAdapter(List<Route> routeList, OnItemClickListener listener) {
        this.routeList = routeList;
        this.listener = listener;
    }


    @NonNull
    @Override
    public RouteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.route_item, parent, false);
        return new RouteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RouteViewHolder holder, int position) {
        if (routeList != null && position < routeList.size()) {
            Route route = routeList.get(position);
            holder.bind(route, listener);
        }
    }

    @Override
    public int getItemCount() {
        return routeList != null ? routeList.size() : 0;
    }
    public void filteredList(List<Route> busNumber){
        routeList=busNumber;
        notifyDataSetChanged();
    }

    static class RouteViewHolder extends RecyclerView.ViewHolder {
        private TextView routeIdTextView;
        private TextView sourceTextView;
        private TextView destinationTextView;

        public RouteViewHolder(@NonNull View itemView) {
            super(itemView);
            routeIdTextView = itemView.findViewById(R.id.routeIdTextView);
            sourceTextView = itemView.findViewById(R.id.sourceTextView);
            destinationTextView = itemView.findViewById(R.id.destinationTextView);
        }

        public void bind(final Route route, final OnItemClickListener listener) {
            routeIdTextView.setText(route.getRouteId());
            sourceTextView.setText("Source: " + route.getSource());
            destinationTextView.setText("Destination: " + route.getDestination());

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(route);
                }
            });
        }
    }

}
