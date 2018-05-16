package example.team5.samplelocation.testActivities;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;
import java.util.Map;

import example.team5.samplelocation.R;
import example.team5.samplelocation.databaseupdate.dbString;

/**
 * Created by Philip on 11/14/2016.
 */
public class HttpRequestPost extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private Button button_http_request_post;
    private TextView textview_name;
    private TextView textview_latitude;
    private TextView textview_longitude;
    private TextView textview_response;

    private GoogleApiClient mGoogleApiClient;
    private final int MY_PERMISSION_REQUEST_ACCESS_FINE_LOCATION = 0;
    private LatLng mLatLngLocation;       // The current location
    private Location mLastLocation;
    private LocationRequest mLocationRequest;
    private Context contextPass;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sample_request_post);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        contextPass = this;

        // Get the button from the layout
        Button button_http_request_post = (Button) findViewById(R.id.button_http_request_post);
        textview_name = (TextView) findViewById(R.id.textview_name);
        textview_latitude = (TextView) findViewById(R.id.textview_latitude);
        textview_longitude = (TextView) findViewById(R.id.textview_longitude);
        textview_response = (TextView) findViewById(R.id.textview_response);

        // Submit a http request and display the response
        assert button_http_request_post != null;
        button_http_request_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeLocationRequest();
            }
        });
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

        // Means that we got a current location
        if (mLastLocation != null) {
            mLatLngLocation = new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude());

            // ------------------- If found the current locationa and db location already then change the value in the DB -------------------------------
            if (mLatLngLocation != null) {
                // Get the values that the user inputted
                final String input_name = textview_name.getText().toString().trim();
                final String input_lat = textview_latitude.getText().toString().trim();
                final String input_long = textview_longitude.getText().toString().trim();

                //textview_response.setText("Congrats you clicked the button!");

                // Instantiate the RequestQueue
                RequestQueue queue = Volley.newRequestQueue(contextPass);
//                String url ="http://www.google.com";
                String url = dbString.php_root+"set.php";



                // Request a string response from the provided URL.
                StringRequest stringRequest = new StringRequest( Request.Method.POST, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                // This will be the logic to parse the response into latitude and longitude
                                // TODO: IMPLEMENT THIS
                                textview_response.setText("Success!");
                                textview_response.setVisibility(View.VISIBLE);

                                // Display the first 500 characters of the response string.
//                                textview_response.setText("Response is: "+ response.toString());
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        textview_response.setText("Failure: " + error.getMessage());
                        textview_response.setVisibility(View.VISIBLE);
                    }}) {
                    @Override
                    protected Map<String,String> getParams(){
                        Map<String,String> params = new HashMap<String, String>();
                        params.put("name",input_name);
                        params.put("long", String.valueOf(mLatLngLocation.longitude));
                        params.put("lat", String.valueOf(mLatLngLocation.latitude));
                        return params;
                    }
                };
                // Add the request to the RequestQueue.
                queue.add(stringRequest);
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
