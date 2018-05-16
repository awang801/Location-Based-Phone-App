package example.team5.samplelocation.testActivities;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;

/**
 * Created by Philip on 3/6/2017.
 */

// Container for location information; this is used by maps
// A single location may or may not have a radius; use radius when representing a checkin location
public class SingleLocation implements Serializable {
    private String name;
    private int radius;
    private double latitude;
    private double longitude;
    private boolean checkedIn;
    private static int defaultRadius = 0;

    // Used when a member is part of the group but has not provided a location
    // We need to know member for the list in the interactive map
    public SingleLocation(String inputName) {
        this(inputName, null);
    }

    // Used when representing a standard coordinate
    public  SingleLocation(String inputName, LatLng inputLocation) {
        this(inputName, defaultRadius, inputLocation);
    }

    // Used when representing a checkin location
    public SingleLocation(String inputName, int inputRadius, LatLng inputLocation) {
        this.name = inputName;
        this.radius = inputRadius;
        if (inputLocation != null) {
            this.latitude = inputLocation.latitude;
            this.longitude = inputLocation.longitude;
            this.checkedIn = true;
        }
        else {
            this.checkedIn = false;
        }

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getRadius() { return radius; }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public LatLng getLocation() {
        return (new LatLng(this.latitude, this.longitude));
    }

    public boolean getCheckedIn() { return checkedIn; }

    public void setCheckedIn(boolean checkedIn) { this.checkedIn = checkedIn; }
}
