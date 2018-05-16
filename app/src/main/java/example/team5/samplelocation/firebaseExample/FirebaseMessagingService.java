package example.team5.samplelocation.firebaseExample;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import example.team5.samplelocation.R;
import example.team5.samplelocation.Sync.SyncHelper;
import example.team5.samplelocation.main.MainActivity;

/**
 * Created by filipp on 5/23/2016.
 */
public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService{

    private static final int numRepeats = 3;
    private static final String TAG = "Firebase-Messaging";
    private static final int ID = 556743;
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        if(remoteMessage.getData().get("sync") != null)
        {
            Log.d(TAG,"Caught Sync message");
            FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
            if(mUser == null)
            {
                //TODO: bad bad things may have happened
                Log.w(TAG, "Trying to handle a sync event with no user!");
            }
            SyncHelper syncDB = new SyncHelper(mUser.getUid(),1,this);
            syncDB.syncDB(numRepeats);
        }
        else if(remoteMessage.getData().get("report") != null)
        {
            String groupName = remoteMessage.getData().get("report");
            Log.d(TAG,"Caught Report message for: " + groupName);
            showNotification(groupName);
        }
        else if(remoteMessage.getData().get("out") != null)
        {
            Log.d(TAG, "got an out of range message");
            String groups = "";
            String groupJsonArray = remoteMessage.getData().get("out");
            Log.d(TAG, groupJsonArray);
            try
            {
                JSONArray arr = new JSONArray(groupJsonArray);
                Log.d(TAG,"size: "+arr.length());
                for(int i = 0; i < arr.length(); i++)
                {
                    JSONObject temp_obj = arr.getJSONObject(i);
                    Log.d(TAG,"obj: "+temp_obj.getString("name"));
                    groups = groups + " " + temp_obj.getString("name");
                }
            }
            catch(JSONException e)
            {
                e.printStackTrace();
            }

            if(groups.length() > 0)
            {
                showOutOfRange(groups);
            }

        }
    }

    private void showOutOfRange(String message) {

        Intent i = new Intent(this,MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,i,PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setAutoCancel(true)
                .setContentTitle("YOU ARE OUT OF RANGE!")
                .setContentText("Groups: "+message)
                .setSmallIcon(R.drawable.ic_near_me_white_24dp)
                .setContentIntent(pendingIntent);

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        manager.notify(ID,builder.build());
    }

    private void showNotification(String message) {

        Intent i = new Intent(this,FirebaseMainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,i,PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setAutoCancel(true)
                .setContentTitle("Report Ready!")
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_near_me_white_24dp)
                .setContentIntent(pendingIntent);

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        manager.notify(0,builder.build());
    }


}
