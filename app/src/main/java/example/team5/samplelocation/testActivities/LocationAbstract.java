package example.team5.samplelocation.testActivities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import static example.team5.samplelocation.R.id.display_location;

/**
 * Created by Philip on 2/18/2017.
 */

public class LocationAbstract implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    final String TAG = "LocationAbstract";

    private GoogleApiClient mGoogleApiClient;
    private final int MY_PERMISSION_REQUEST_ACCESS_FINE_LOCATION = 0;
    private LocationRequest mLocationRequest;
    private Context context;

    public LocationAbstract(Context context) {
        this.context = context;
        checkGoogleServices();
    }

    // Make a single location request and receive back a Location object of the last known location
    public Location singleLocation() {
        if (checkGoogleServices() && checkLocationPermissions()) {
            Location currentLocation = requestSingleLocation();
            return currentLocation;
        }
        return null;
    }

    // Make a single request for a new location; executes the run method of the callback provided
    public void singleNewLocation(LocationCallbackInterface callback) {
        if (checkGoogleServices() && checkLocationPermissions()) {
            setUpdateLocationSingle(callback);
        }
    }

    // How frequently and how long do we want locations; executes the run method of the callback provided
    public void continuousLocations(long interval, long duration, LocationCallbackInterface callback) {
        if (checkGoogleServices() && checkLocationPermissions()) {
            setUpdateLocation(interval, duration, callback);
        }
    }

    /* ------- The following are helper functions for the above ------- */

    // Get last known location
    private Location requestSingleLocation() {
        // Because Android Studio is a fukking dumbass and can't recognize that I already made the permissions checks...kill me
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {}

        Location currentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (currentLocation != null) {
            Log.d(TAG, "Cur Latitude: " + String.valueOf(currentLocation.getLatitude()));
            Log.d(TAG, "Cur Longitude: " + String.valueOf(currentLocation.getLongitude()));

            return currentLocation;
        } else {
            Log.d(TAG, "Turn on your location services!");
        }
        return null;
    }

    // Request a singular new location
    private void setUpdateLocationSingle(final LocationCallbackInterface callback) {
        // Setup location updates to run occasionally
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);

        // Because Android Studio is a fukking dumbass and can't recognize that I already made the permissions checks...kill me
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {}
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                callback.run(location);
                LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            }
        });
    }

    // Request constantly updating new locations
    private void setUpdateLocation(long interval, long duration, final LocationCallbackInterface callback) {
        // Setup location updates to run occasionally
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(interval);
        mLocationRequest.setFastestInterval(interval/2);
        mLocationRequest.setExpirationDuration(duration);

        // Because Android Studio is a fukking dumbass and can't recognize that I already made the permissions checks...kill me
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {}
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                callback.run(location);
            }
        });
    }

    // Return true if connected to play services and false if not
    private boolean checkGoogleServices() {
        if (mGoogleApiClient == null || !mGoogleApiClient.isConnected()) {
            buildGoogleApiClient();
            mGoogleApiClient.connect();
            Log.w(TAG, "Attempt connection to Google Play Services!");
        } else {
            Log.d(TAG, "Confirmed Google Location Services Active!");
            return true;
        }

        // Tried the max number of times but couldn't connect
        return false;
    }

    private boolean checkLocationPermissions() {
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Cannot make the permission request from a non-activity class
            Log.w(TAG, "Permissions rejected by the user. Location requests cannot be made.  Please request the location permission from the user.");
            return false;
        } else {
            Log.d(TAG, "Confirmed Permissions Given!");
            return true;
        }
    }

    private synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        // Successfull connection to google play services
        Log.d(TAG, "Connection to Google Play Services Success!");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // Unable to connect to Google Play Services...issue!
        Log.d(TAG, "Connection to Google Play Services FAILED onConnectionFailed!");
    }

    @Override
    public void onConnectionSuspended(int i) {

    }
}

