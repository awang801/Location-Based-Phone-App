package example.team5.samplelocation.login;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInApi;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.iid.FirebaseInstanceId;

import java.io.IOException;

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
 * Created by johnw on 3/9/2017.
 */

public class AccountUtils {


    // Signs the user out of Firebase AND out of the backend AND out of the GoogleSignInApi
    // NOTE: This requires setting up a GoogleApiClientObject and passing it in here
    // The object should be initialized in onCreate(), connected in onStart(), and disconnected in onStop().
    // See MainActivity.java for an example of how to prep a GoogleApiClient object for this signout method.
    public static void signOutFirebaseAndBackend(GoogleApiClient mGoogleApiClient) {
        final String TAG = "AccountUtils";

        // Remove the device from the backend
        String instanceID = FirebaseInstanceId.getInstance().getToken();
        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();

        if( mUser != null && instanceID != null ) {
            String uid = mUser.getUid();
            removeDevice(uid, instanceID);
        } else {
            Log.d(TAG, "Sign out called on null user (user is not logged into Firebase)");
        }

        // Sign the device out of Firebase
        FirebaseAuth.getInstance().signOut();

        // Delete current GCM token to kill the session (eliminates edge cases with reinstalls)

        if( FirebaseInstanceId.getInstance().getToken() != null ) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String scope = "GCM";
                        FirebaseInstanceId.getInstance().deleteToken(FirebaseInstanceId.getInstance().getToken(), scope);
                        Log.d(TAG, "Token deleted");
                    } catch (IOException e) {
                        Log.d(TAG, "Deleting tokens for ID failed");
                        e.printStackTrace();
                    }
                }
            }).start();
        } else {
            Log.d(TAG, "Token was null and no attempt to delete it was made");
        }

        // Google sign out
        if( mGoogleApiClient != null && mGoogleApiClient.isConnected() ) {
            // This is required to prompt users for a gmail account each Google login,
            // otherwise the GoogleSignInApi will cache the login and when login with
            // Google is selected it will always use the cached Google account.
            Auth.GoogleSignInApi.signOut(mGoogleApiClient);
        } else {
            // The Google API client is not connected, for now just do nothing
            Log.d(TAG, "Signout was given a null GoogleApiClient object or one that was not connected.");
        }

    }

    // Private method that signs out of the backend
    // Returns false on failure
    private static void removeDevice(String uid, String instance) {
        final String TAG = "AccountUtils";
        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add("user_id", uid)
                .add("instance", instance)
                .build();

        Request request = new Request.Builder()
                .url(dbString.php_root+"removeDevice.php")
                .post(body)
                .build();

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

                    if(!res.equals("Success"))
                    {
                        Log.e(TAG, "Device removal request failed.");
                    }
                }
            }
        });
    }
}
