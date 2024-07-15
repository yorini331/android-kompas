package com.example.compas;

public class CompassData {
    private float azimuth;
    private double longitude;
    private double latitude;
    private String timestamp;

    public CompassData(float azimuth, double longitude, double latitude, String timestamp) {
        this.azimuth = azimuth;
        this.longitude = longitude;
        this.latitude = latitude;
        this.timestamp = timestamp;
    }

    public float getAzimuth() {
        return azimuth;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
