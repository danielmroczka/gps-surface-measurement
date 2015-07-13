/*
 * Copyright daniel.mroczka@gmail.com. All rights reserved. 
 */
package com.labs.dm.gpssurfacemeasurement;


/**
 * @author daniel
 */
public class Position {

    private double latitude;
    private double longitude;

    /**
     * Creates position object using decimal degrees parameters
     *
     * @param latitude  in decimal degrees
     * @param longitude in decimal degrees
     */
    public Position(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    @Override
    public String toString() {
        return String.format("%.4f,%.4f", getLatitude(), getLongitude());
    }
}
