package example.team5.samplelocation.testActivities;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Created by Philip on 10/17/2016.
 */
public class SimpleCoordinate implements ClusterItem {
    private int id;
    private double latitude;
    private double longitude;
    private String name;

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public LatLng getPosition() {
        // TODO: Implement this so that the map knows where the marker is
        LatLng retVal = new LatLng(latitude, longitude);
        return retVal;
    }

    public String getTitle() {
        return name;
    }
}
