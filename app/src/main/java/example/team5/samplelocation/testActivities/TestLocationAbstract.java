package example.team5.samplelocation.testActivities;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.model.LatLng;

import example.team5.samplelocation.R;

/**
 * Created by Philip on 2/18/2017.
 */

public class TestLocationAbstract extends Activity{
    final String TAG = "TestLocationAbstract";

    private GoogleApiClient mGoogleApiClient;

    // TODO: Come up with a naming convention that isn't garbage
    private Location mLastLocation;
    protected LatLng mLatLngLocation;
    private LatLng mDBLatLngLocation;
    private final int MY_PERMISSION_REQUEST_ACCESS_FINE_LOCATION = 0;
    private Button display_location;
    private Button store_value_db;
    private TextView cur_latitude;
    private TextView cur_longitude;
    private Button db_location;
    private TextView db_latitude;
    private TextView db_longitude;
    private Button display_db_location;
    private Button comp_location;
    private Button proximity_check;
    private Button button_cluster_example;
    private LocationRequest mLocationRequest;
    private LocationAbstract testLS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_locationabstract);
        final Context con = this;

        // Instance of LocationAbstract given current app context
        testLS = new LocationAbstract(getApplicationContext());

        // Add a button listener that will initiate the longitude and latitude find on user's current location
        Button current_location = (Button) findViewById(R.id.current_location);
        current_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Ran when a new location is received; place the appropriate update call to the backend within run
                LocationCallbackInterface callback = new LocationCallbackInterface() {
                    @Override
                    public void run(Location location) {
                        String latitude = String.valueOf(location.getLatitude());
                        String longitude = String.valueOf(location.getLongitude());
                        String latlngDisplay = "Longitude: " + longitude + ", Latitude: " + latitude;
                        Log.d(TAG, latlngDisplay);
                    }
                };

                // This is if you just want to get the last location back.  It will be of type Location.  Not as accurate!
                Location currentLocation = testLS.singleLocation();
                if (currentLocation == null) {
                    Toast.makeText(getApplicationContext(), "The location returned was null", Toast.LENGTH_LONG).show();
                } else {
                    String latitude = String.valueOf(currentLocation.getLatitude());
                    String longitude = String.valueOf(currentLocation.getLongitude());
                    String latlngDisplay = "Longitude: " + longitude + ", Latitude: " + latitude;
                    Log.d(TAG, latlngDisplay);
                    Toast.makeText(getApplicationContext(), latlngDisplay, Toast.LENGTH_LONG).show();
                }

                // This will obtain a new location each time, define your functionality in the run method of LocationCallbackInterface and pass it as a callback function when we receive a new location
                testLS.singleNewLocation(callback);

                // Example of continuous checkin
                // poll every 5 seconds, poll for a total of one minute, each time we receive a location execute the code in the run method of callback
                testLS.continuousLocations(5 * 1000, 60 * 1000, callback);
            }
        });
    }
}


