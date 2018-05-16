package example.team5.samplelocation.testActivities;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.util.MutableBoolean;
import android.widget.Toast;

import com.facebook.internal.Mutable;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.SphericalUtil;
import com.google.maps.android.clustering.ClusterManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import android.os.Handler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import example.team5.samplelocation.R;
import example.team5.samplelocation.databaseupdate.dbString;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Philip on 2/19/2017.
 */

public class MapsStaticLite extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLoadedCallback {
    private static boolean firstTime = true;
    private String TAG = "MapsStaticLite";
    ClusterManager<SimpleCoordinate> mClusterManager;
    private GoogleMap mMap;
    private SingleLocation MELBOURNE = new SingleLocation("Melbourne", 100, new LatLng(-37.81319, 144.96298));
    private SingleLocation player1 = new SingleLocation("Player1", new LatLng(-37.81369, 144.96298));
    private SingleLocation player2 = new SingleLocation("Player2", new LatLng(-37.81319, 144.96348));
    private SingleLocation player3 = new SingleLocation("Player3", new LatLng(-37.81319, 144.96248));
    private SingleLocation player4 = new SingleLocation("Player4", new LatLng(-37.81269, 144.96298));
    private SingleLocation LONDON = new SingleLocation("London", new LatLng(51.507351, -0.127758));
    private Marker CheckInLocation;
    private ArrayList<SingleLocation> allLocations = new ArrayList<SingleLocation>();
    private GoogleApiClient client;
    private List<SingleLocation> locationGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_mapfragmentstatic);
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    protected void onResume() {
        super.onResume();

        final OnMapReadyCallback mMapReadyCallback = this;

        // This is the call for testing from the backend at the moment : https://people.eecs.ku.edu/~gmagnuso/getReports.php?group_name=GabeGroup&admin_id=IBzzDdFCpYfbxSmRVitibig3OGA2&size=1&offset=0



        // Right Now I am Just using the url directly
//        RequestBody body = new FormBody.Builder()
//                .add("group_name", "GabeGroup")
//                .add("admin_id", "IBzzDdFCpYfbxSmRVitibig3OGA2")
//                .add("size", "1")
//                .add("offset", "0")
//                .build();

        // Form the HTTP request
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(dbString.php_root+"getReports.php?group_name=GabeGroup&admin_id=IBzzDdFCpYfbxSmRVitibig3OGA2&size=1&offset=0")
//                .post(body)
                .build();

        // This should likely be wrapped in a try block
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // At this point we got a response so we need to check if what we got back was correct then build the map
                final String res = response.body().string();
                Log.d(TAG, "Response From Backend: "+res);

                try {
                    JSONObject jsonResponse = new JSONObject(res);
                    getLocationIntent(jsonResponse.getJSONArray("runs"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }


                // Run on the UI thread.  This is require for interacting with the map fragment
                Handler mainHandler = new Handler(getApplicationContext().getMainLooper());
                mainHandler.post(new Runnable() {

                    @Override
                    public void run() {

                        if (mMap == null) {
                            MapFragment mapFragment = (MapFragment) getFragmentManager()
                                    .findFragmentById(R.id.map_static_lite);

                            mapFragment.getMapAsync(mMapReadyCallback);
                        }
                    }
                });
            }
        });
    }

    // This will grab the list of locations from the intent and assume that checkin is based
    // on the first item with a radius which should just be the first item in general
    // Sets: locationGroup
    private void getLocationIntent(JSONArray currentReport) throws JSONException {
        // Clear What is currectly on the maps
        allLocations.clear();

        //Bundle input_coordinates = getIntent().getExtras();
        //locationGroup = (ArrayLicat<SingleLocation>) input_coordinates.get("Coordinates");
        JSONObject currentReportObject = new JSONObject();
        currentReportObject = (JSONObject) currentReport.get(0);

        // Add the checkin location
        Log.d(TAG, currentReportObject.toString());
//        allLocations.add(new SingleLocation("CheckIn" , currentReportObject.getInt("radius"), new LatLng(currentReportObject.getDouble("latitude"), currentReportObject.getDouble("longitude"))));
        int checkInRadius = currentReportObject.getInt("radius");
        if (checkInRadius == 0) {
            checkInRadius = 50;
        }
        allLocations.add(new SingleLocation("CheckIn" , checkInRadius, new LatLng(currentReportObject.getDouble("latitude"), currentReportObject.getDouble("longitude"))));

        // Get the locations that are checked in and the locations that aren't
        JSONArray currentReportResults = currentReportObject.getJSONArray("results");
        JSONArray checkedInMembers = ((JSONObject) currentReportResults.get(0)).getJSONArray("locations");
        JSONArray notCheckedInMembers = ((JSONObject) currentReportResults.get(0)).getJSONArray("missing");

        // Add all the checked in members to the map
        for (int index = 0; index < checkedInMembers.length(); index++) {
            JSONObject currentMember = (JSONObject) checkedInMembers.get(index);
            allLocations.add(new SingleLocation(currentMember.getString("n"), new LatLng(currentMember.getDouble("lt"), currentMember.getDouble("l"))));
        }

        // Add all the not checked in members to the map
        for (int index = 0; index < notCheckedInMembers.length(); index++) {
            JSONObject currentMember = (JSONObject) notCheckedInMembers.get(index);
            allLocations.add(new SingleLocation(currentMember.getString("n")));
        }

        // These are just example locations; TODO : DELETE THESE LATER
//        allLocations.add(MELBOURNE);
//        allLocations.add(LONDON);
//        allLocations.add(player1);
//        allLocations.add(player2);
//        allLocations.add(player3);
//        allLocations.add(player4);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setAllGesturesEnabled(false);
        mMap.getUiSettings().setMapToolbarEnabled(false);

        // TODO: get the locations from the intent and create a marker for each with the first
        // marker with a radius to be the checkin and the focus of the map on start
        // Should just call get location intent: right now we are using dummy values
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Toast.makeText(getApplicationContext(), "Acknowledge Click!", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                intent.putExtra("Coordinates", allLocations);
                startActivity(intent);
            }
        });
//        GoogleMapOptions options = new GoogleMapOptions();
//        options.mapType(GoogleMap.MAP_TYPE_NORMAL)
//                .compassEnabled(false)
//                .rotateGesturesEnabled(false)
//                .tiltGesturesEnabled(false);

        // This is what I was using originally
//        mMap.setOnMapLoadedCallback(this);
        displayCheckin();
    }

    // Wait till the map is loaded then add and zoom
    @Override
    public void onMapLoaded() {
//        displayCheckin();
    }

    //
    private void displayCheckin() {
        mMap.clear();
        boolean displayedCheckin = false;       // This is just a flag to say whether or not I've displayed a checkin location yet

        // Iterate through the list of all locations and display them on the map
        for (SingleLocation currentLocation : allLocations) {
            // Check If a location was provided
            if (currentLocation.getCheckedIn()) {
                // If this is the first location that has a radius then display the radius and center the map on this location
                if (currentLocation.getRadius() != 0 && !displayedCheckin) {
                    // Get the checking location and draw it on the map
                    Marker newMarker = mMap.addMarker(new MarkerOptions().position(currentLocation.getLocation())
                            .title(currentLocation.getName())
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));

                    // Mark that we've now seen the first value with a raius
                    displayedCheckin = true;

                    // Center the map on this location
//                    newMarker.showInfoWindow();
                    CheckInLocation = newMarker;

                    // Show the circle
                    CircleOptions circleOptions = new CircleOptions()
                            .strokeWidth(4.0f)
                            .strokeColor(Color.BLACK)
                            .fillColor(Color.argb(40, 100, 100, 100))
                            .center(currentLocation.getLocation())
                            .radius(currentLocation.getRadius()); // In meters

                    // Get back the mutable Circle
                    mMap.addCircle(circleOptions);

                    // Position the map.
                    // Great comment on zooming to the correct level: http://stackoverflow.com/questions/14828217/android-map-v2-zoom-to-show-all-the-markers
//                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation.getLocation(), getZoomLevel(currentLocation.getRadius())));
                    int padding = 0; // offset from edges of the map in pixels
                    Log.d(TAG, "long: " + String.valueOf(currentLocation.getLongitude()) + "  lat: " + String.valueOf(currentLocation.getLatitude()) + "  radius: " + String.valueOf(currentLocation.getRadius()));
                    CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(toBounds(currentLocation.getLocation(), currentLocation.getRadius()), padding);
                    mMap.moveCamera(cu);

                    // Temp Fix For the Zoom Level On First Open
                    if (firstTime) {
                        firstTime = false;
                        mMap.moveCamera(CameraUpdateFactory.zoomBy(2));
                    }
                }
                // If the location is a member
                else{
                    // Get the checking location and draw it on the map
                    Marker newMarker = mMap.addMarker(new MarkerOptions().position(currentLocation.getLocation())
                            .title(currentLocation.getName())
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE)));
                }
            }
        }
		
        // Display the information on the CheckInLocation
        CheckInLocation.showInfoWindow();
        CheckInLocation.setZIndex(99999);
    }

    // Get the appropriate zoom based on the radius of the circle
//    public long getZoomLevel(int radius) {
//        double miles = radius * 0.000621371;
//        return Math.round(14-Math.log(miles)/Math.log1p(2));
//    }
//    public int getZoomLevel(int radius) {
//        int zoomLevel = 11;
//        radius = radius + radius / 2;
//        double scale = radius / 500;
//        zoomLevel = (int) (16 - Math.log(scale) / Math.log(2));
//        return zoomLevel;
//    }

    // This will return bounds for the circle
    public LatLngBounds toBounds(LatLng center, double radius) {
        LatLng southwest = SphericalUtil.computeOffset(center, radius * Math.sqrt(2.0), 225);
        LatLng northeast = SphericalUtil.computeOffset(center, radius * Math.sqrt(2.0), 45);
        return new LatLngBounds(southwest, northeast);
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "ClusterMap Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://example.team5.samplelocation/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "ClusterMap Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://example.team5.samplelocation/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }
}
