package example.team5.samplelocation.testActivities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
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
import android.support.v7.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import example.team5.samplelocation.R;

public class MapsActivity extends AppCompatActivity implements  OnMapReadyCallback {

    private String TAG = "MapsActivity";

    private Marker marker;
    private Marker modmarker;
    private GoogleMap mMap;
    private LatLng mLatLngLocation;
    private LatLng mDBLatLngLocation;
    private int input_radius;     // This is in meters
    private String input_name;
    private ArrayList<SingleLocation> allLocations = new ArrayList<SingleLocation>();
    private HashMap<String, SingleLocation> allLocationsHash = new HashMap<String, SingleLocation>();
    private HashMap<String, Marker> allMarkers = new HashMap<String, Marker>();
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    private CustomAdapter mAdapter;

    private SingleLocation checkInLocation;
    private ArrayList<String> checkInLocation_Names = new ArrayList<String>();
    private ArrayList<String> membersCheckedIn_Names = new ArrayList<String>();
    private ArrayList<String> membersNotCheckedIn_Names = new ArrayList<String>();


    // Seperate checkin from members, then divide members into those who are checked in and those who are not
    // Gets called when we initially parse the intent extras
    public void seperatePatamters() {
        // Just go through and identify the checkin location
        for (SingleLocation singleLocation: allLocations) {
            // This is the checkin location
            if (singleLocation.getRadius() != 0) {
                checkInLocation_Names.add(singleLocation.getName());
                checkInLocation = singleLocation;
            }
        }

        // Now go through and determine if each member has provided a location within the checkin radius or not
        for (SingleLocation singleLocation : allLocations) {
            // Make sure that we aren't looking at the checkin location
            if (singleLocation != checkInLocation) {
                // The user has not yet provided a location
                if (!singleLocation.getCheckedIn()) {
                    membersNotCheckedIn_Names.add(singleLocation.getName());
                }
                else {
                    Location checkInLocationLoc = new Location("Checkin");
                    checkInLocationLoc.setLatitude(checkInLocation.getLatitude());
                    checkInLocationLoc.setLongitude(checkInLocation.getLongitude());

                    Location currentComparison = new Location("Current");
                    currentComparison.setLatitude(singleLocation.getLatitude());
                    currentComparison.setLongitude(singleLocation.getLongitude());

                    float distance = checkInLocationLoc.distanceTo(currentComparison);

                    // The user has provided a location which is in the checkin radius
                    if (distance < checkInLocation.getRadius()) {
                        membersCheckedIn_Names.add(singleLocation.getName());
                    }
                    // The user is not in the checkin radius at the moment
                    else {
                        membersNotCheckedIn_Names.add(singleLocation.getName());
                    }
                }
            }
        }
    }

    // Listener for our member drawer
    public class MemberDrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    /** Recognize what item has been selected */
    // We will know when we originally create each single location whether the member is checked in or not; so just check if their checked in before moving the camera
    private void selectItem(int position) {
        // Make sure that the user pressed a member and not just the title
        if (mAdapter.getItemViewType(position) == 0) {
            mDrawerLayout.closeDrawer(mDrawerList);
            SingleLocation selectedLocation = allLocationsHash.get(mAdapter.getItem(position));
            if (selectedLocation.getCheckedIn()) {
                Marker currentLocation = allMarkers.get(mAdapter.getItem(position));
                currentLocation.showInfoWindow();
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation.getPosition(), 19), 1200, null);
            }
            else {
                Toast.makeText(getApplicationContext(), "Member Has Not Provided Location", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // This will grab the list of locations from the intent and assume that checkin is based
    // on the first item with a radius which should just be the first item in general
    // Sets: locationGroup
    private void getLocationIntent() {
        Bundle input_coordinates = getIntent().getExtras();
        allLocations = (ArrayList<SingleLocation>) input_coordinates.get("Coordinates");
        for (SingleLocation singleLocation: allLocations) {
            allLocationsHash.put(singleLocation.getName(), singleLocation);
        }
        seperatePatamters();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        Toolbar mapsToolbar = (Toolbar) findViewById(R.id.maps_toolbar);
        mapsToolbar.setTitle("Interactive Map");
        setSupportActionBar(mapsToolbar);

        getLocationIntent();


        ArrayList<String> membersList = new ArrayList<String>(Arrays.asList("Player1", "Player2"));

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.right_drawer);
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.string.drawer_open,         /* "open drawer" description */
                R.string.drawer_close         /* "close drawer" description */
                ) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
//                Toast.makeText(getApplicationContext(), "Drawer Closed", Toast.LENGTH_SHORT).show();
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
//                Toast.makeText(getApplicationContext(), "Drawer Closed", Toast.LENGTH_SHORT).show();
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.addDrawerListener(mDrawerToggle);

        // TODO: Remove this, after ensuring that the new adapter works
        // Set the adapter for checkinlocation
//        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
//                R.layout.member_list_item, checkInLocation_Names));
//        // TODO: Set the list's click listener
//        mDrawerList.setOnItemClickListener(new MemberDrawerItemClickListener());

        // Now use the custom adapter to segment the list
        mAdapter = new CustomAdapter(getApplicationContext());
        // Add the checkin locations
        mAdapter.addSectionHeaderItem("CheckIn Location");
        for (String singleCheckIn : checkInLocation_Names) {
            mAdapter.addItem(singleCheckIn);
        }
        // Add the checked in members
        mAdapter.addSectionHeaderItem("CheckedIn Members");
        for (String singleMember : membersCheckedIn_Names) {
            mAdapter.addItem(singleMember);
        }
        // Add members that aren't checked in
        mAdapter.addSectionHeaderItem("Not CheckedIn Members");
        for (String singleMember : membersNotCheckedIn_Names) {
            mAdapter.addItem(singleMember);
        }

        // Apply our new adapter and our onClickListener
        mDrawerList.setAdapter(mAdapter);
        mDrawerList.setOnItemClickListener(new MemberDrawerItemClickListener());
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_members:
                // Toggle the drawer and zoom to the correct value
                if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
                    mDrawerLayout.closeDrawer(mDrawerList);
                }
                else {
                    mDrawerLayout.openDrawer(mDrawerList);
                }
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mMap == null) {
            MapFragment mapFragment = (MapFragment) getFragmentManager()
                    .findFragmentById(R.id.map);

            mapFragment.getMapAsync(this);
        }
    }

    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
//        GoogleMapOptions options = new GoogleMapOptions();
//        options.mapType(GoogleMap.MAP_TYPE_NORMAL)
//                .compassEnabled(false)
//                .rotateGesturesEnabled(false)
//                .tiltGesturesEnabled(false);
        displayCheckin();
    }

    //
    private void displayCheckin() {
        mMap.clear();
        boolean displayedCheckin = false;       // This is just a flag to say whether or not I've displayed a checkin location yet

        // TODO: get the locations from the intent and create a marker for each with the first
        // marker with a radius to be the checkin and the focus of the map on start
        // Should just call get location intent: right now we are using dummy values
//        getLocationIntent();

        // Iterate through the list of all locations and display them on the map
        for (SingleLocation currentLocation : allLocations) {
            // Check if a location was provided
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
                    newMarker.showInfoWindow();

                    // Add the new marker to the list of markers
                    allMarkers.put(currentLocation.getName(), newMarker);

                    // Show the circle
                    CircleOptions circleOptions = new CircleOptions()
                            .strokeWidth(4.0f)
                            .strokeColor(Color.BLACK)
                            .fillColor(Color.argb(40, 100,100,100))
                            .center(currentLocation.getLocation())
                            .radius(currentLocation.getRadius()); // In meters

                    // Get back the mutable Circle
                    mMap.addCircle(circleOptions);

                    // Position the map.
                    // Great comment on zooming to the correct level: http://stackoverflow.com/questions/14828217/android-map-v2-zoom-to-show-all-the-markers
//                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation.getLocation(), getZoomLevel(currentLocation.getRadius())));
                    int padding = 0; // offset from edges of the map in pixels
                    CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(toBounds(currentLocation.getLocation(), currentLocation.getRadius()), padding);
                    mMap.moveCamera(cu);
                }
                // If the location is a member
                else{
                    // Get the checking location and draw it on the map
                    Marker newMarker = mMap.addMarker(new MarkerOptions().position(currentLocation.getLocation())
                            .title(currentLocation.getName())
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE)));

                    // Add the new marker to the list of markers
                    allMarkers.put(currentLocation.getName(), newMarker);
                }

            }
        }
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
//    /**
//     * Manipulates the map once available.
//     * This callback is triggered when the map is ready to be used.
//     * This is where we can add markers or lines, add listeners or move the camera. In this case,
//     * we just add a marker near Sydney, Australia.
//     * If Google Play services is not installed on the device, the user will be prompted to install
//     * it inside the SupportMapFragment. This method will only be triggered once the user has
//     * installed Google Play services and returned to the app.
//     */
//    @Override
//    public void onMapReady(GoogleMap googleMap) {
//        mMap = googleMap;
//        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
//
//        // Remove previous current location Marker
//        if (marker != null) {
//            marker.remove();
//        }
//
//        // Remove mod marker if there is already one
//        if (modmarker != null) {
//            modmarker.remove();
//        }
//
//        // Get the location from the previous activity and map it
//        Bundle input_coordinates = getIntent().getExtras();
//        mLatLngLocation = (LatLng) input_coordinates.get("Coordinates");
//        mDBLatLngLocation = (LatLng) input_coordinates.get("DBCoordinates");
//
//        marker = mMap.addMarker(new MarkerOptions().position(mLatLngLocation)
//                .title("Check-in")
//                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
//        marker.showInfoWindow();
//
//        if (mDBLatLngLocation != null) {
//            input_radius = (int) input_coordinates.get("Radius");
//            input_name = (String) input_coordinates.get("Name");
//
//            modmarker = mMap.addMarker(new MarkerOptions().position(mDBLatLngLocation)
//                    .title(input_name)
//                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
//            //modmarker.showInfoWindow();
//
//            // Show the circle
//            // Instantiates a new CircleOptions object and defines the center and radius
//            CircleOptions circleOptions = new CircleOptions()
//                    .strokeWidth(4.0f)
//                    .strokeColor(Color.BLACK)
//                    .fillColor(Color.argb(40, 100,100,100))
//                    .center(mLatLngLocation)
//                    .radius(input_radius); // In meters
//
//            // Get back the mutable Circle
//            Circle circle = mMap.addCircle(circleOptions);
//            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mLatLngLocation, 18));
//        }
//        else {
//            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mLatLngLocation, 18));
//        }
//    }
}
