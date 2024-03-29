package example.team5.samplelocation.testActivities;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import example.team5.samplelocation.R;
import example.team5.samplelocation.databaseupdate.dbString;


/**
 * Created by Philip on 12/4/2016.
 * Description: Test activity to retrieve and map all value from the database
 */

public class HttpRequestGetAll extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient mGoogleApiClient;
    private final int MY_PERMISSION_REQUEST_ACCESS_FINE_LOCATION = 0;
    private LatLng mMemberLatLngLocation;       // The member location from the database
    private LatLng mLatLngLocation;       // The current location
    private Location mLastLocation;
    private LocationRequest mLocationRequest;
    private EditText edittext_radius;
    private TextView textview_response;
    private Button button_map;
    private Context contextPass;




    // Store the longitude and latitude values; 0->longitude and 1->latitude
    String accumulateUsers;

    // Store the response in a json array
    JSONArray getAllResponse;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sample_request_get_all);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        contextPass = this;

        // Get the button from the layout
        Button button_http_request = (Button) findViewById(R.id.button_http_request);
        button_map = (Button) findViewById(R.id.button_map);
        textview_response = (TextView) findViewById(R.id.textview_http_request_response);
        edittext_radius = (EditText) findViewById(R.id.edittext_radius);

        // Submit a http request and display the response
        button_http_request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            // Add the location search here
                makeLocationRequest();
            }
        });

        // Display a map with current location and member location onclick
        button_map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HttpRequestGetAll.this, CheckinMap.class);
                intent.putExtra("CheckinLocation", mLatLngLocation);
                intent.putExtra("ValidUsers", getAllResponse.toString());       // Must pass the JSONArray as a string
                intent.putExtra("Radius", Integer.parseInt(edittext_radius.getText().toString().trim()));
                startActivity(intent);
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

            // If found the current locationa and db location already then fire the intent
            if (mLatLngLocation != null) {
                // TODO : CHANGE THE INTENT TO PASS A JSONARRAY
                // TODO : IF YOUR LOCATION IS FOUND THEN FIRE UP THE HTTP REQUEST
                fireGetAll();       // The mLatLngLocation should contain our current location
//                Intent intent = new Intent(HttpRequestGetAll.this, MapsActivity.class);
//                intent.putExtra("Coordinates", mLatLngLocation);
//                intent.putExtra("DBCoordinates", mMemberLatLngLocation);        // THIS IS WHAT WILL NEED TO BE CHANGED
//                intent.putExtra("Radius", Integer.parseInt(edittext_radius.getText().toString().trim()));
//                startActivity(intent);
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

    // This is called once we have our current location
    // Our location is in mLatLngLocation and mLastLocation
    public void fireGetAll() {
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(contextPass);
//                String url ="http://www.google.com";
        String url = dbString.php_root+"getAll";

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @TargetApi(Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onResponse(String response) {
                        // If the request is bad then notify the user
                        if (response.length() == 1) {
                            Toast.makeText(contextPass, "This didn't work!", Toast.LENGTH_SHORT).show();
                        }
                        // The response was successful and is parsable
                        else {
                            // This will be the logic to parse the response into latitude and longitude
                            try {
                                textview_response.setText(response);
                                getAllResponse = new JSONArray(response);

                                accumulateUsers = "";
                                Location tempLocation;
                                double dist;

                                double longVal, latVal;
                                String name;
                                for (int i = 0; i < getAllResponse.length(); i++) {
                                    JSONObject row = getAllResponse.getJSONObject(i);
                                    longVal = row.getDouble("lg");
                                    latVal = row.getDouble("lt");
                                    name = row.getString("n");
                                    tempLocation = new Location("temp");
                                    tempLocation.setLatitude(latVal);
                                    tempLocation.setLongitude(longVal);
                                    dist = mLastLocation.distanceTo(tempLocation);

                                    // Check to see if this user is within the radius and if they are then display their location on the map
                                    if (dist < Integer.parseInt(edittext_radius.getText().toString().trim())) {
                                        accumulateUsers = accumulateUsers + name + "\n";
                                    }
                                    // Otherwise remove them from the JSONArray
                                    else {
                                        // Needs to be changed to accommodate a larger number of android versions
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                            getAllResponse.remove(i);
                                            i--;
                                        }
                                    }
                                }

                                // Change the response to display the string containing all users within range
                                textview_response.setText(accumulateUsers);

                                // Make the rest of pages content visible
                                button_map.setVisibility(View.VISIBLE);
                                edittext_radius.setVisibility(View.GONE);
                                textview_response.setGravity(Gravity.CENTER_HORIZONTAL);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        // Display the response string.
//                                textview_response.setText("Response is: "+ response.toString());
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                textview_response.setText("That didn't work!" + error.getMessage());
            }
        });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }
}
