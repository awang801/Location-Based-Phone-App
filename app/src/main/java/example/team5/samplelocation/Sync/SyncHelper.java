package example.team5.samplelocation.Sync;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.IOException;
import java.util.List;

import example.team5.samplelocation.SQLite.Group;
import example.team5.samplelocation.SQLite.GroupDatabaseHandler;
import example.team5.samplelocation.databaseupdate.LocationService;
import example.team5.samplelocation.databaseupdate.dbString;
import example.team5.samplelocation.login.LoginActivity;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by magnu_000 on 2/20/2017.
 */

public class SyncHelper {

    private static final String TAG = "SyncHelper";
    private String userId;
    private int t;
    private int r;

    private String timeStamp;
    private int r2;

    private Context ctx;

    public SyncHelper(String uid, int type, Context ctx)
    {
        userId = uid;
        t = type;
        this.ctx = ctx;
    }

    public void syncDB(int repeats)
    {
        r = repeats-1;
        r2 = repeats;

        //TODO: the id will need to be the users confirmed id in result of sucessful backend sign up
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(dbString.php_root+"sync.php?id=" + userId + "&syncAll="+t)
                .get()
                .build();

        //try {
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if(r > 0)
                {
                    syncDB(r);
                }
                else
                {
                    e.printStackTrace();
                }

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //TODO: If the response says they can't sign in then we need to sign them out right away
                //This could happen in the event they are currently "signed in" on another device
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }
                else {
                    //Take result of sync script
                    String res = response.body().string();
                    Log.d(TAG, "Response: " + res);

                    //Extract Group objects/Time from result
                    List<Group> groups = GroupDatabaseHandler.getGroups(res);
                    timeStamp = GroupDatabaseHandler.getTime(res);
                    Log.d(TAG, "Groups: " + groups.size() + " Time: " + timeStamp);

                    //Add Groups to database
                    int numNewOrCurrentEvents = GroupDatabaseHandler.addGroups(groups, ctx);

                    LocationService mMyService = new LocationService();
                    Intent mServiceIntent = new Intent(ctx, mMyService.getClass());
                    if(!isMyServiceRunning(mMyService.getClass(), ctx)
                            && numNewOrCurrentEvents > 0){
                        ctx.startService(mServiceIntent);
                    }

                    //Return a successful result
                    syncComplete(r2);
                }
            }
        });
    }

    private void syncComplete(int repeats)
    {
        r2 = repeats - 1;

        OkHttpClient client = new OkHttpClient();

        RequestBody body = new FormBody.Builder()
                .add("id", userId)
                .add("datetime", timeStamp)
                .build();

        Request request = new Request.Builder()
                .url(dbString.php_root+"syncComplete.php")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if(r2 > 0)
                {
                    syncComplete(r2);
                }
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }
                else {
                    //Take result of sync script
                    String res = response.body().string();
                    Log.d(TAG, "SyncComplete Returned: "+res);
                }
            }
        });
    }

    public static boolean isMyServiceRunning(Class<?> serviceClass, Context ctx) {
        ActivityManager manager = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i ("isMyServiceRunning?", true+"");
                return true;
            }
        }
        Log.i ("isMyServiceRunning?", false+"");
        return false;
    }

}
