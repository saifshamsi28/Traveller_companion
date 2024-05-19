package com.saif.traveller;

public class Route {
    private String routeId;
    private String source;
    private String destination;

    public Route(String routeId, String source, String destination) {
        this.routeId = routeId;
        this.source = source;
        this.destination = destination;
    }

    public String getRouteId() {
        return routeId;
    }

    public String getSource() {
        return source;
    }

    public String getDestination() {
        return destination;
    }
}

