package example.team5.samplelocation.testActivities;

import android.content.Context;
import android.content.Intent;
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

import example.team5.samplelocation.R;
import example.team5.samplelocation.databaseupdate.dbString;

/**
 * Created by Philip on 11/14/2016.
 * Description: Test activity for retrieving a single user from the database and displaying their information on screen
 */

public class HttpRequestGet extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private GoogleApiClient mGoogleApiClient;
    private final int MY_PERMISSION_REQUEST_ACCESS_FINE_LOCATION = 0;
    private Button button_http_request;
    private TextView textview_response;
    private LatLng mMemberLatLngLocation;       // The member location from the database
    private LatLng mLatLngLocation;       // The current location
    private Location mLastLocation;
    private LocationRequest mLocationRequest;
    private EditText edittext_radius;
    private EditText edittext_name;

    // Store the longitude and latitude values; 0->longitude and 1->latitude
    String splitResponse[];


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sample_request_get);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        final Context contextPass = this;

        // Get the button from the layout
        Button button_http_request = (Button) findViewById(R.id.button_http_request);
        final Button button_map = (Button) findViewById(R.id.button_map);
        final TextView textview_response = (TextView) findViewById(R.id.textview_http_request_response);
        edittext_name = (EditText) findViewById(R.id.edittext_name);
        edittext_radius = (EditText) findViewById(R.id.edittext_radius);


        // Submit a http request and display the response
        button_http_request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Retrieve the value for the get request
                final String input_name = edittext_name.getText().toString().trim();

                // Instantiate the RequestQueue.
                RequestQueue queue = Volley.newRequestQueue(contextPass);
//                String url ="http://www.google.com";
                String url = dbString.php_root+"get.php?name=" + input_name;

                // Request a string response from the provided URL.
                StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                // If the request is bad then notify the user
                                if (response.length() == 1) {
                                    Toast.makeText(contextPass, "User Not Found!", Toast.LENGTH_SHORT).show();
                                }
                                // The response was successful and is parsable
                                else {
                                    // This will be the logic to parse the response into latitude and longitude
                                    // TODO: IMPLEMENT THIS
                                    splitResponse = response.trim().split(" ");
                                    textview_response.setText("Longitude: " + splitResponse[0] + "\nLatitude: " + splitResponse[1]);
                                    mMemberLatLngLocation = new LatLng(Double.parseDouble(splitResponse[1]), Double.parseDouble(splitResponse[0]));

                                    // Make the rest of pages content visible
                                    textview_response.setVisibility(View.VISIBLE);
                                    edittext_name.setVisibility(View.VISIBLE);
                                    edittext_radius.setVisibility(View.VISIBLE);
                                    button_map.setVisibility(View.VISIBLE);
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
        });

        // Display a map with current location and member location onclick
        button_map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // The user provided a radius
                if (edittext_radius.getText().toString().trim().length() != 0) {
                    makeLocationRequest();
                }
                // Tell the user to provide a radius because they didn't
                else {
                    Toast.makeText(contextPass, "Please Provide a Radius!", Toast.LENGTH_SHORT).show();
                }
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
            if (mLatLngLocation != null && mMemberLatLngLocation != null) {
                Intent intent = new Intent(HttpRequestGet.this, MapsActivity.class);
                intent.putExtra("Coordinates", mLatLngLocation);
                intent.putExtra("DBCoordinates", mMemberLatLngLocation);
                intent.putExtra("Radius", Integer.parseInt(edittext_radius.getText().toString().trim()));
                intent.putExtra("Name", edittext_name.getText().toString().trim());
                startActivity(intent);
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
