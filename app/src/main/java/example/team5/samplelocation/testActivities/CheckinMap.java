package example.team5.samplelocation.testActivities;

import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import example.team5.samplelocation.R;

/**
 * Created by Philip on 12/5/2016.
 */

public class CheckinMap extends FragmentActivity implements OnMapReadyCallback {

    ClusterManager<SimpleCoordinate> mClusterManager;
    private GoogleMap mMap;
    private int input_radius;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
//        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
//                .findFragmentById(R.id.map);
//        mapFragment.getMapAsync(this);
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mMap == null) {
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);

            mapFragment.getMapAsync(this);
        }
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
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        setUpClusterer();
    }

    //
    private void setUpClusterer() {
        mMap.clear();
        // Declare a variable for the cluster manager.
//        ClusterManager<SimpleCoordinate> mClusterManager;
        // TODO: Make initial map positioning better
        DBHelper db = new DBHelper(this);
        SimpleCoordinate startingLocation = db.getRecentSimpleCoordinate();

        // Position the map.
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(startingLocation.getLatitude(), startingLocation.getLongitude()), 10));

        // Initialize the manager with the context and the map.
        // (Activity extends context, so we can pass 'this' in the constructor.)
        mClusterManager = new ClusterManager<SimpleCoordinate>(this, mMap);

        // Point the map's listeners at the listeners implemented by the cluster
        // manager.
        mMap.setOnCameraIdleListener(mClusterManager);
        mMap.setOnMarkerClickListener(mClusterManager);
        mClusterManager.setRenderer(new OwnRendered(this, mMap, mClusterManager));

        // Get the checking location and draw it on the map
        Bundle input_coordinates = getIntent().getExtras();
        LatLng mCheckinLocation = (LatLng) input_coordinates.get("CheckinLocation");
        mMap.addMarker(new MarkerOptions().position(mCheckinLocation)
                .title("Check-in")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));
        //modmarker.showInfoWindow();

        // Show the circle
        // Instantiates a new CircleOptions object and defines the center and radius
        CircleOptions circleOptions = new CircleOptions()
                .strokeWidth(4.0f)
                .strokeColor(Color.BLACK)
                .fillColor(Color.argb(40, 100,100,100))
                .center(mCheckinLocation)
                .radius((int) input_coordinates.get("Radius")); // In meters

        // Get back the mutable Circle
        mMap.addCircle(circleOptions);

        // Add cluster items (markers) to the cluster manager.
        addItems();
    }

    private void addItems() {
        // TODO: GO THROUGH ALL THE ITEMS THAT WE TAKE IN AND ADD THEM TO THE CLUSTER
        // Get the location from the previous activity and map it
        Bundle input_coordinates = getIntent().getExtras();

        JSONArray validUsers = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            try {
                validUsers = new JSONArray((String) input_coordinates.get("ValidUsers"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        input_radius = (int) input_coordinates.get("Radius");

        double longVal, latVal;
        String name;
        SimpleCoordinate tempCord;

        // Add all the users to the clustermanager
        try {
            for (int i = 0; i < validUsers.length(); i++) {
                JSONObject row = validUsers.getJSONObject(i);
                longVal = row.getDouble("lg");
                latVal = row.getDouble("lt");
                name = row.getString("n");
                Log.d("alert", "Name: " + name + ", Lat: " + String.valueOf(latVal) + ", Long: " + String.valueOf(longVal));
                tempCord = new SimpleCoordinate();
                tempCord.setLongitude(longVal);
                tempCord.setLatitude(latVal);
                tempCord.setName(name);
                mClusterManager.addItem(tempCord);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
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
