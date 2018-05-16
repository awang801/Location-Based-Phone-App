package example.team5.samplelocation.login;

//TODO: pass db connection around instead of opening them everywhere!!!

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.iid.FirebaseInstanceId;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import example.team5.samplelocation.R;
import example.team5.samplelocation.Sync.SyncHelper;
import example.team5.samplelocation.databaseupdate.LocationService;
import example.team5.samplelocation.databaseupdate.dbString;
import example.team5.samplelocation.main.MainActivity;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {


    final String TAG = "LoginActivity";

    Intent mServiceIntent;
    private LocationService mMyService;
    private Context ctx;
    public Context getCtx(){
        return ctx;
    }

    // The client will be initialized in onCreate(), connected in onStart(), and disconnected in onStop()
    private GoogleApiClient mGoogleApiClient;
    private static final int RC_SIGN_IN = 100;
    private static final String GOOGLE_TOS_URL = "https://www.google.com/policies/terms/";
    private static final int numRepeat = 3;
    private static final int PERMISSION_REQUEST = 99;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ctx = this;
        setContentView(R.layout.activity_login);

//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        toolbar.setTitle("Check-in App Login");
//        setSupportActionBar(toolbar);

        // Set up GoogleApiClient to prep for logout
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getResources().getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder( this )
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        RelativeLayout layout_locationPermissions = (RelativeLayout) findViewById(R.id.layout_locationPermissions);
        layout_locationPermissions.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                triggerLocationPermissionsRequest();
                updateLocationPermissionsLayout();
            }
        });


        Button button_signIn = (Button) findViewById(R.id.button_signIn);
        // Send the user to the login page
        button_signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (locationPermissionsGranted()) {
                    List<AuthUI.IdpConfig> selectedProviders = new ArrayList<>();
                    List<String> permissionsList = new ArrayList<>();
                    selectedProviders.add(
                            new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER)
                                    .setPermissions(permissionsList)
                                    .build());
                    startActivityForResult(
                            AuthUI.getInstance().createSignInIntentBuilder()
                                    .setTheme(R.style.AppTheme)
                                    .setLogo(R.drawable.anon_user_48dp)
                                    .setProviders(selectedProviders)
                                    .setTosUrl(GOOGLE_TOS_URL)
                                    .setIsSmartLockEnabled(false)
                                    .build(),
                            RC_SIGN_IN);
                } else {
                    DialogFragment dialog = new QuickDialog();
                    dialog.show(getSupportFragmentManager(), "QuickDialog");
                }
            }
        });
    }

    public static class QuickDialog extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            // Build the dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Wait up!\n\nNot surprisingly, Locale requires location permissions to get just about anything done.\n\nTo turn on location permissions for Locale, try hitting the banner at the top of this page or go to:\n   Device Settings ->\n   Applications ->\n   Locale -> \n   Permissions\nand turn on location permissions.")
                    .setTitle("Location Services")
                    .setNeutralButton("Got it", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User selected neutral button
                        }
                    });

            // Create and return the dialog
            return builder.create();
        }
    }

    private void updateLocationPermissionsLayout() {
        if( locationPermissionsGranted() ) {

            ImageView iv = (ImageView) findViewById(R.id.iv_locationPermissions);
            TextView tv = (TextView) findViewById(R.id.tv_locationPermissions);

            // Update the layouts image
            iv.setImageResource(R.drawable.ic_check_white_24dp);
            // Update the layouts text
            tv.setText("Locale has been granted location permissions");

            // Update the button on the bottom of the screen to have white text
            Button bt = (Button) findViewById(R.id.button_signIn);
            bt.setTextColor(Color.WHITE);
        }
    }

    private boolean locationPermissionsGranted() {
        int permCheckFine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int permCheckCourse = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);

        return( permCheckFine == PackageManager.PERMISSION_GRANTED && permCheckCourse == PackageManager.PERMISSION_GRANTED );
    }

    private void triggerLocationPermissionsRequest() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSION_REQUEST);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if( mGoogleApiClient != null ) {
            mGoogleApiClient.connect();
        } else {
            Log.d(TAG, "Google API client object null in onStart");
        }
        triggerLocationPermissionsRequest();
        updateLocationPermissionsLayout();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if( mGoogleApiClient != null ) {
            mGoogleApiClient.connect();
        } else {
            Log.d(TAG, "Google API client object null in onStop");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST: {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG,"Permissions Accepted");
                    updateLocationPermissionsLayout();
                } else {
                    //NOTE: if you grant access to both, then coarse will show up as not granted!!!!
                    Log.d(TAG,"Permissions Rejected");
                }
                return;
            }
        }
    }

    @Override
    public void onBackPressed() {
        // Do nothing so that we cannot enter MainActivity by spamming back presses
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "Activity came back!");
        if (requestCode == RC_SIGN_IN) {

            if (resultCode == RESULT_OK) {
                //Http Request to Send the UserId to the backend
                FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
                mUser.getToken(true)
                        .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                            public void onComplete(@NonNull Task<GetTokenResult> task) {
                                if (task.isSuccessful()) {
                                    // Store the instance id for use in the okhttp request later
                                    String instanceID = FirebaseInstanceId.getInstance().getToken();
                                    String idToken = task.getResult().getToken();

                                    Log.d(TAG, "UserToken: " + idToken);
                                    Log.d(TAG, "InstanceID: " + instanceID);
                                    // Send token to your backend via HTTPS
                                    OkHttpClient client = new OkHttpClient();
                                    RequestBody body = new FormBody.Builder()
                                            .add("acc_id", idToken)
                                            .add("instance", instanceID)
                                            .build();

                                    Request request = new Request.Builder()
                                            .url(dbString.php_root+"addUser.php")
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
                                            if (!response.isSuccessful()) {
                                                throw new IOException("Unexpected code " + response);
                                            }
                                            else {
                                                String res = response.body().string();

                                                try
                                                {
                                                    int errorRes = Integer.parseInt(res);
                                                    if(errorRes == 1)
                                                    {
                                                        Log.d(TAG, res+" They are currently signed in on this device!");

                                                        new Handler(Looper.getMainLooper()).post(new Runnable() {

                                                            @Override
                                                            public void run() {
                                                                Toast.makeText(getApplicationContext(), "You are already signed in on this device!", Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                                        finish();
                                                    }
                                                    else
                                                    {
                                                        Log.d(TAG,"AddUser.php failure with code: "+res);

                                                        handleError();

                                                        startActivity(getIntent());
                                                    }
                                                }
                                                catch(NumberFormatException e)
                                                {
                                                }

                                                //This logic only works if failures above kill this activity
                                                FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
                                                if(mUser == null)
                                                {
                                                    handleError();
                                                    throw new IOException("Failed to retrieve User");

                                                }

                                                String uid = mUser.getUid();

                                                SyncHelper syncDB = new SyncHelper(uid, 1, ctx);
                                                syncDB.syncDB(numRepeat);
                                            }
                                        }
                                    });
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
                                    // ...
                                } else {

                                    // The source of this else is unknown and only spoken of in legends
                                    // You are not expected to understand this
                                    handleError();
                                }
                            }
                        });

                // Reload the MainActivity
                Intent intent = new Intent();
                intent.putExtra("response", "All Good");
                setResult(RESULT_OK, intent);
                // This was here already
                finish();
            }
        }
    }

    // Called when a user was unable to sign into the backend
    private void handleError() {
        Log.d("DB", "Dropped table!");
        ctx.deleteDatabase("groups.db");

        // Ensure that the user is signed out
        AccountUtils.signOutFirebaseAndBackend( mGoogleApiClient );

        new Handler(Looper.getMainLooper()).post(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), "A sign in error has occurred, please sign in again.", Toast.LENGTH_SHORT).show();
            }
        });
        
        // End activity and ask the user to sign in again
        finish();
    }
}
