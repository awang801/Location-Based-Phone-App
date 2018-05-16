
package example.team5.samplelocation.databaseupdate;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

import android.support.v4.app.ActivityCompat;
import android.content.pm.PackageManager;
import android.Manifest;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import example.team5.samplelocation.R;
import example.team5.samplelocation.SQLite.Group;
import example.team5.samplelocation.SQLite.GroupDatabaseHandler;
import example.team5.samplelocation.main.MainActivity;
import example.team5.samplelocation.testActivities.MainMenu;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Grishma on 16/5/16.
 */
public class LocationService extends Service implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    protected static final String TAG = "LocationUpdateService";
    private static final int ID = 444;
    private boolean soundNotify ;
    private HashSet<String> nameCheck;
    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 5000;

    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    // Keys for storing activity state in the Bundle.
    protected final static String REQUESTING_LOCATION_UPDATES_KEY = "requesting-location-updates-key";
    protected final static String LOCATION_KEY = "location-key";
    protected final static String LAST_UPDATED_TIME_STRING_KEY = "last-updated-time-string-key";
    /**
     * Tracks the status of the location updates request. Value changes when the user presses the
     * Start Updates and Stop Updates buttons.
     */
    public static Boolean mRequestingLocationUpdates;
    /**
     * Time when the location was updated represented as a String.
     */
    protected String mLastUpdateTime;
    /**
     * Provides the entry point to Google Play services.
     */
    protected GoogleApiClient mGoogleApiClient;

    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    protected LocationRequest mLocationRequest;

    /**
     * Represents a geographical location.
     */
    protected Location mCurrentLocation;

    protected LocalBroadcastManager mBroadcaster;

    static final public String COPA_RESULT = "com.controlj.copame.backend.COPAService.REQUEST_PROCESSED";

    static final public String COPA_MESSAGE = "com.controlj.copame.backend.COPAService.COPA_MSG";

    public void sendResult(String message) {
        Intent intent = new Intent(COPA_RESULT);
        if(message != null)
            intent.putExtra(COPA_MESSAGE, message);
        mBroadcaster.sendBroadcast(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mBroadcaster = LocalBroadcastManager.getInstance(this);

        soundNotify  = true;
        nameCheck = new HashSet<>();

        previousNumGroupsToUpdate = 0;
        previousNumGroups = 0;

        //Refresh even if location services is off because we use this to convey message!
        //In future you could remove this especially if we force location services by signing user out
        //Our if we show a message some other way (there is technically a toast happening currently)
        if(GroupDatabaseHandler.selectGroupsPriority(LocationService.this).size() > 0)
        {
            sendResult(COPA_RESULT);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private boolean isComplete = false;
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Within {@code onPause()}, we pause location updates, but leave the
        // connection to GoogleApiClient intact.  Here, we resume receiving
        // location updates if the user has requested them.
        Log.d(TAG, "Service init...");
        isComplete = false;
        mRequestingLocationUpdates = false;
        mLastUpdateTime = "";

        Notification notification = getNotification("Attendence Activity Check in Progress",true, false);
        startForeground(ID,
                notification);

        buildGoogleApiClient();
        if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
            startLocationUpdates();
        }
        else
        {
            //TODO: I don't think this else matters???
        }
        return START_STICKY;
    }

    private static int counter = 0;
    public Notification getNotification(String message, boolean ongoing, boolean createSound){
        //The intent of the intent is to take you to an activity that allows you
        //to confirm check ins, if there is only one then it would redirect straight there
        PendingIntent contentIntent = PendingIntent.getActivity(this,
                0, new Intent(this, MainActivity.class), 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                            .setContentTitle("Active Groups")
                            .setTicker("Active Groups")
                            .setContentText(message)
                            .setSmallIcon(R.drawable.ic_near_me_white_24dp)
                            .setOngoing(ongoing)
                            .setContentIntent(contentIntent);

        if(createSound)
        {
            soundNotify  = false;
            Uri uri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            builder.setSound(uri);
            builder.setVibrate(new long[] { 250, 666, 250, 666, 250 });
        }

        return builder.build();
    }


    @Override
    public void onConnected(Bundle bundle) {
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.i(TAG, "Connection suspended==");
        mGoogleApiClient.connect();
    }

    private int numGroupsToUpdate;
    private int previousNumGroupsToUpdate;
    private int previousNumGroups;
    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());

        List<Group> groups = GroupDatabaseHandler.selectGroupsPriority(LocationService.this);
        numGroupsToUpdate = 0;

        try {
            JSONArray jsonArr = new JSONArray();
            for (Group group : groups) {

                if(!nameCheck.contains(group.getName()))
                {
                    soundNotify  = true;
                    sendResult(COPA_MESSAGE);
                }
                //NOTE: this check is what group permission hinges on!!!
                if(group.getConf() != 0) {
                    JSONObject tempGroup = new JSONObject();
                    tempGroup.put("name", group.getName());
                    jsonArr.put(tempGroup);
                    numGroupsToUpdate++;
                }
            }

            nameCheck.clear();

            for(Group group : groups)
            {
                nameCheck.add(group.getName());
            }

            FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();

            updateUserLocations(jsonArr.toString(),mUser.getUid(),String.valueOf(location.getLatitude()),String.valueOf(location.getLongitude()));
        }catch(JSONException e)
        {
            //TODO: handle
        }

        if(previousNumGroupsToUpdate != numGroupsToUpdate || previousNumGroups != groups.size())
        {
            String message = "You have " + (groups.size() - numGroupsToUpdate) + " for approval and " + numGroupsToUpdate + " updating";
            previousNumGroups = groups.size();
            previousNumGroupsToUpdate = numGroupsToUpdate;

            Notification notification = getNotification(message, groups.size() > 0,soundNotify);
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(ID, notification);
        }

        if(groups.size() == 0)
        {
            //Stop service without restart
            Log.d(TAG, "No groups require attention, ending...");
            killService();
        }
    }

    private static boolean updateUserLocations(String jsonArray, String uid, String lat, String lng)
    {
        //TODO: Literally all of these parameters
        Log.d(TAG,"Updateing Locations");
        //TODO: the id will need to be the users confirmed id in result of sucessful backend sign up
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .retryOnConnectionFailure(false)
                .build();

        RequestBody body = new FormBody.Builder()
                .add("id", uid)
                .add("groups", jsonArray)
                .add("lat", lat)
                .add("long", lng)
                .build();

        Request request = new Request.Builder()
                .url(dbString.php_root+"userUpdateLocation.php")
                .post(body)
                .build();

        //try {
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String res = response.body().string();
                Log.d(TAG, "Update Res: "+res);
            }
        });
        //TODO: decide if this worked or not!
        return true;
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + connectionResult.getErrorCode());
        killService();
    }

    private void killService()
    {
        Log.d(TAG,"Killing");
        isComplete = true;

        //NOTE: Probably don't need all of these, but hey
        stopLocationUpdates();
        stopForeground(true);
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(ID);
        stopSelf();
    }

    /**
     * Builds a GoogleApiClient. Uses the {@code #addApi} method to request the
     * LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        Log.i(TAG, "Building GoogleApiClient===");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        createLocationRequest();
    }


    /**
     * Sets up the location request. Android has two location request settings:
     * {@code ACCESS_COARSE_LOCATION} and {@code ACCESS_FINE_LOCATION}. These settings control
     * the accuracy of the current location. This sample uses ACCESS_FINE_LOCATION, as defined in
     * the AndroidManifest.xml.
     * <p/>
     * When the ACCESS_FINE_LOCATION setting is specified, combined with a fast update
     * interval (5 seconds), the Fused Location Provider API returns location updates that are
     * accurate to within a few feet.
     * <p/>
     * These settings are appropriate for mapping applications that show real-time location
     * updates.
     */
    protected void createLocationRequest() {
        mGoogleApiClient.connect();
        mLocationRequest = new LocationRequest();

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * Requests location updates from the FusedLocationApi.
     */
    protected void startLocationUpdates() {
        if (!mRequestingLocationUpdates) {
            mRequestingLocationUpdates = true;

            // The final argument to {@code requestLocationUpdates()} is a LocationListener
            // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                Log.d(TAG,"Permissions Failed, deal with it");
                Log.e(TAG, "ToastTest");

                Handler handler = new Handler(Looper.getMainLooper());

                handler.post(new Runnable() {

                    @Override
                    public void run() {
                        if(getApplicationContext() != null) {
                            Toast.makeText(getApplicationContext(), "Provide Location Services Permission! (Logout/In)", Toast.LENGTH_LONG).show();
                        }
                    }
                });
                killService();
                return;
            }

            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
            Log.i(TAG, " startLocationUpdates===");
        }
    }

    /**
     * Removes location updates from the FusedLocationApi.
     */
    private void stopLocationUpdates() {
        if (mRequestingLocationUpdates) {
            mRequestingLocationUpdates = false;
            // It is a good practice to remove location requests when the activity is in a paused or
            // stopped state. Doing so helps battery performance and is especially
            // recommended in applications that request frequent location updates.

            Log.d(TAG, "stopLocationUpdates();==");
            // The final argument to {@code requestLocationUpdates()} is a LocationListener
            // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);

            Log.d(TAG,"Location Services Stopped Exitting...");
        }
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "ondestroy!");
        if(!isComplete)
        {
            Intent broadcastIntent = new Intent("example.team5.samplelocation.RestartSensor");
            sendBroadcast(broadcastIntent);
        }
        super.onDestroy();
    }


}