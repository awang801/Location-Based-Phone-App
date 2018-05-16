package example.team5.samplelocation.testActivities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import example.team5.samplelocation.R;

/**
 * Created by Philip on 10/9/2016.
 * Description: Display the users current longitude and latitude on the screen.
 */

public class DisplayLngLat extends ActionBarActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.greeting);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        final Context con = this;

        // The latitude, longitude, and "display_button" which are all initially INVISIBLE views on the display
        display_location = (Button) findViewById(R.id.display_location);
        store_value_db = (Button) findViewById(R.id.store_current);
        cur_latitude = (TextView) findViewById(R.id.lat_display);
        cur_longitude = (TextView) findViewById(R.id.long_display);

        // Screen contents corresponding to the db values
        db_location = (Button) findViewById(R.id.get_db_location);
        db_latitude = (TextView) findViewById(R.id.db_lat_display);
        db_longitude = (TextView) findViewById(R.id.db_long_display);
        display_db_location = (Button) findViewById(R.id.display_db_location);

        // Button to compare the database and current locations
        comp_location = (Button) findViewById(R.id.comp_location);
        proximity_check = (Button) findViewById(R.id.comp_proximity);

        // Button that bring up all db values and demonstrates map clustering
        button_cluster_example = (Button) findViewById(R.id.map_cluster_example);

        // Add a button listener that will initiate the longitude and latitude find on user's current location
        Button current_location = (Button) findViewById(R.id.current_location);
        current_location.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Find the longitude and latitude in here
                makeLocationRequest();
            }
        });

        // Add a button listener to the "Display Location" button that starts a new map activity with the provided
        display_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create a new intent: This is what you use to start a new activity and pass information between activities
                Intent intent = new Intent(DisplayLngLat.this, MapsActivity.class);
                intent.putExtra("Coordinates", mLatLngLocation);
                startActivity(intent);
            }
        });

        // Add a button listener to the "Store Current Location in DB" button that store the current location in the db
        store_value_db.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveCurrentValues();
            }
        });

        // Button listener to load the last stored location from the db
        db_location.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Get the most recent db location
                loadDBCoordinate();
            }
        });

        // Button listener to display the most recent db location
        display_db_location.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.d("main", "better not be here");

                // Create a new intent: This is what you use to start a new activity and pass information between activities
                Intent intent = new Intent(DisplayLngLat.this, MapsActivity.class);
                intent.putExtra("Coordinates", mDBLatLngLocation);
                startActivity(intent);
            }
        });

        // Button listener for comparison button
        comp_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Create new intent with values for both the current and db location
                Intent intent = new Intent(DisplayLngLat.this, MapsActivity.class);
                intent.putExtra("Coordinates", mLatLngLocation);
                intent.putExtra("DBCoordinates", mDBLatLngLocation);
                startActivity(intent);
            }
        });

        // Button listener for proximity check
        proximity_check.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Location me = new Location("");
                me.setLatitude(mLatLngLocation.latitude);
                me.setLongitude(mLatLngLocation.longitude);

                Location checkin = new Location("");
                checkin.setLatitude(mDBLatLngLocation.latitude);
                checkin.setLongitude(mDBLatLngLocation.longitude);

                float distance = me.distanceTo(checkin);
                if (distance < 500) {
                    Toast.makeText(con, "Checking in!", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(con, "Not in range!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Button listener that brings up the cluster map
        button_cluster_example.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DisplayLngLat.this, ClusterMap.class);
                startActivity(intent);
            }
        });
    }

    private void loadDBCoordinate() {
        DBHelper db = new DBHelper(this);
        SimpleCoordinate simpleCoord = db.getRecentSimpleCoordinate();
        mDBLatLngLocation = new LatLng(simpleCoord.getLatitude(), simpleCoord.getLongitude());

        // Display the database locations and db_display_button
        db_latitude.setText("DB Latitude: " + String.valueOf(simpleCoord.getLatitude()));
        db_longitude.setText("DB Longitude: " + String.valueOf(simpleCoord.getLongitude()));
        db_latitude.setVisibility(View.VISIBLE);
        db_longitude.setVisibility(View.VISIBLE);
        display_db_location.setVisibility(View.VISIBLE);

        // If found the current locationa and db location already then display the comp
        if (mLatLngLocation != null && mDBLatLngLocation != null) {
            comp_location.setVisibility(View.VISIBLE);
            proximity_check.setVisibility(View.VISIBLE);
        }
    }

    private void saveCurrentValues() {
        DBHelper db = new DBHelper(this);
        SimpleCoordinate simpleCoord = new SimpleCoordinate();
        simpleCoord.setLongitude(mLastLocation.getLongitude());
        simpleCoord.setLatitude(mLastLocation.getLatitude());

        db.insertSimpleCoordinate(simpleCoord);

        Toast.makeText(this, "Current location has been store in the db!", Toast.LENGTH_SHORT).show();
    }

    /*@Override
    public boolean onTouchEvent(MotionEvent event) {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        LinearLayout background = (LinearLayout) findViewById(R.id.overall_layout);
        background.requestFocus();
        return true;
    }*/

    // TODO: Change this to execute when called; call this from the onclick
    protected void makeLocationRequest() {
        super.onResume();

        if (mGoogleApiClient == null || !mGoogleApiClient.isConnected()) {

            buildGoogleApiClient();
            mGoogleApiClient.connect();

        } else {
            setUpdateLocation();
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        // Check permission
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Check permission
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSION_REQUEST_ACCESS_FINE_LOCATION);
            return;
        }
        setUpdateLocation();
    }

    // Poll for the location once.  This looks for a single location
    private void setUpdateLocation() {
        // Check permission
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        // Setup location updates to run occasionally
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);

        // We never actually call getUpdates.  We only get the last location
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (mLastLocation != null) {
            mLatLngLocation = new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude());
            // Display the longitude, latitude, and the display button
            cur_latitude.setText("Cur Latitude: " + String.valueOf(mLastLocation.getLatitude()));
            cur_longitude.setText("Cur Longitude: " + String.valueOf(mLastLocation.getLongitude()));
            cur_latitude.setVisibility(View.VISIBLE);
            cur_longitude.setVisibility(View.VISIBLE);
            display_location.setVisibility(View.VISIBLE);
            store_value_db.setVisibility(View.VISIBLE);

            // If found the current locationa and db location already then display the comp
            if (mLatLngLocation != null && mDBLatLngLocation != null) {
                comp_location.setVisibility(View.VISIBLE);
                proximity_check.setVisibility(View.VISIBLE);
            }
        }
        else {
            Toast.makeText(this, "Turn on your location services!", Toast.LENGTH_SHORT).show();
        }
    }

    /* This will be used in the future to poll for the location constantly
    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            mLatLngLocation = new LatLng(location.getLatitude(),location.getLongitude());
            // Display the longitude, latitude, and the display button
            latitude.setText("Latitude: " + String.valueOf(location.getLatitude()));
            longitude.setText("Longitude: " + String.valueOf(location.getLongitude()));
            latitude.setVisibility(View.VISIBLE);
            longitude.setVisibility(View.VISIBLE);
            display_location.setVisibility(View.VISIBLE);
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
        else {
            Toast.makeText(this, "Error finding location!", Toast.LENGTH_SHORT).show();
        }
    }*/

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            // The case is just a number corresponding to the persmission request to the user
            case MY_PERMISSION_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.  We may have multiple since we can ask for multiple permissions at once
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("main", "permission request granted");
                    setUpdateLocation();
                    // permission was granted, yay! Do the
                    // location-related task you need to do.

                } else {
                    Log.d("main", "permission request denied");

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // Unable to connect to Google Play Services...issue!
    }

    @Override
    public void onConnectionSuspended(int i) {

    }
}

