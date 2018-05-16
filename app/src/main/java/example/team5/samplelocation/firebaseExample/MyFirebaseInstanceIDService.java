package example.team5.samplelocation.firebaseExample;


import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;

/**
 * Created by Philip on 2/1/2017.
 */

public class MyFirebaseInstanceIDService extends FirebaseInstanceIDService {
    private static final String TAG = "MyFirebaseIIDService";

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    // [START refresh_token]
    @Override
    public void onTokenRefresh() {
        // TODO: Place logic here to recognize that a new token is formed
        // Get updated InstanceID token.
        //String refreshedToken = FirebaseInstanceId.getInstance().getToken();
//        Log.d(TAG, "Refreshed token: " + refreshedToken);

        // TODO: Implement this method to send any registration to your app's servers.
        //TODO: we need to make sure the token is refershed!!! we could just sign out
        //sendRegistrationToServer(refreshedToken);
    }
    // [END refresh_token]

    /**
     * Persist token to third-party servers.
     *
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token) {
        // Add custom implementation, as needed.
    }
}
