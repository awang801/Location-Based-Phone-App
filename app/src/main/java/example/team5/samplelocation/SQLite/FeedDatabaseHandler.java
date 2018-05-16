package example.team5.samplelocation.SQLite;

import android.app.Activity;
import android.content.Context;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.format.DateUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import example.team5.samplelocation.R;
import example.team5.samplelocation.databaseupdate.dbString;
import example.team5.samplelocation.main.FeedItemAdapter;
import example.team5.samplelocation.main.FeedObject;
import example.team5.samplelocation.main.FeedObjectEvent;
import example.team5.samplelocation.main.FeedObjectMessage;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static example.team5.samplelocation.SQLite.GroupDatabaseHandler.selectGroupsPriority;

/**
 * Created by Joe on 3/12/2017.
 */

public class FeedDatabaseHandler {
    private static final String TAG = "FeedDatabaseHandler";
    private static Context ctx;

    private static String limit = "5";

    public static void addFeed(final ArrayList<FeedObject> feedObjArr, final Context context, final FeedItemAdapter adapter, final SwipeRefreshLayout swipeRefresh) {
        ctx = context;
        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
        // This checks to make sure that there is a corresponding firebase user; If no one is signed in then we shouldn't add anything to the feed
        if (mUser == null)
            return;
        String uid = mUser.getUid();

        feedObjArr.clear();

        OkHttpClient client = new OkHttpClient.Builder()
                              .connectTimeout(30, TimeUnit.SECONDS)
                              .writeTimeout(30, TimeUnit.SECONDS)
                              .readTimeout(30, TimeUnit.SECONDS)
                              .retryOnConnectionFailure(false)
                              .build();

        Request request = new Request.Builder()
                .url(dbString.php_root+"getFeedUser.php?user_id=" + uid + "&size=" + limit)
                .get()
                .build();

        try
        {
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                    if(context != null)
                    {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {

                            @Override
                            public void run() {
                                Toast.makeText(context, "Feed Timeout",Toast.LENGTH_LONG).show();
                            }
                        });
                    }

                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if(!response.isSuccessful()) {
                        Log.d(TAG,"FAILURE");
                        throw new IOException("encountered error: " + response);
                    }
                    else {
                        //grab result of script
                        final String res = response.body().string();
                        Log.d(TAG, "Response: " + res);

                        if(ctx != null && adapter != null)
                        {
                            ((Activity)ctx).runOnUiThread(new Runnable() {
                                public void run() {
                                    //decode JSON
                                    int expectedFeedLength = decodeFeedJSON(res, feedObjArr);
                                    //add events
                                    expectedFeedLength += addEventsToFeed(feedObjArr, ctx, adapter);

                                    if(feedObjArr.size() > expectedFeedLength)
                                    {
                                        //swipeRefresh could be null here, passing just in case it
                                        //failed on a refresh which I presume is likely
                                        addFeed(feedObjArr, context, adapter, swipeRefresh);
                                    }
                                    adapter.notifyDataSetChanged();

                                    if(swipeRefresh != null)
                                    {
                                        swipeRefresh.setRefreshing(false);
                                    }
                                }
                            });
                        }
                    }
                }
            });
        }
        catch(Exception e)
        {
            Log.e(TAG,"Caught a timeout maybe");
            e.printStackTrace();
        }
    }

    private static int decodeFeedJSON(String response, ArrayList<FeedObject> feedObjArr) {
        //decode the JSON response and populate the Array List with the data
        int jsonLength = 0;
        try {
            JSONObject obj = new JSONObject(response);
            JSONArray feedArr = obj.getJSONArray("feed");
            int imgId = R.drawable.anon_user_48dp; //TODO: fix this to use actual avatar
            String username, title, message;
            Date timeReceived;
            if(feedArr.length() != 0) {
                jsonLength = feedArr.length();
                for (int i = 0; i < feedArr.length(); i++) {
                    JSONObject tempObj = (JSONObject) feedArr.get(i);
                    username = tempObj.getString("n"); //probably good to have when the above avatar fix is worked on
                    message = tempObj.getString("r");
                    timeReceived = new Date(Long.valueOf(tempObj.getString("t"))*1000);
                    title = tempObj.getString("gn");

                    FeedObject tempFeedObj = new FeedObjectMessage(imgId, title, timeReceived, message, true);
                    feedObjArr.add(tempFeedObj);
                }
            }
            Log.d(TAG, "feedObjectArray size: " + feedObjArr.size());
        }
        catch (JSONException e) {
            //should never happen RIGHT????
        }
        return jsonLength;
    }

    private static int addEventsToFeed(final ArrayList<FeedObject> feedObjArr, final Context context, final FeedItemAdapter adapter) {
        int numEvents = 0;

        List<Group> tempGroupList = selectGroupsPriority(context);
        int imgId = R.drawable.anon_user_48dp; //TODO: fix this to use actual avatar
        String title, subtitle;
        Date timeReceived;
        subtitle = "Event";
        long timeReceived_INTEGER;
        if(tempGroupList.size() != 0) {
            numEvents = tempGroupList.size();
            for(int i = 0; i < tempGroupList.size(); i++) {
                title = tempGroupList.get(i).getName();

                timeReceived_INTEGER = tempGroupList.get(i).getStop();
                final long timeToStop = ((long)tempGroupList.get(i).getStop())*1000 - System.currentTimeMillis();

                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {

                    @Override
                    public void run() {
                        new CountDownTimer(timeToStop, timeToStop) {

                            public void onTick(long millisUntilFinished) {
                            }

                            public void onFinish() {
                                //try and refresh the page again...
                                addFeed(feedObjArr, context, adapter,null);
                            }
                        }.start();
                    }
                });


                //convert from int to a date time...
                timeReceived = new Date(timeReceived_INTEGER*1000);

                boolean confStatus = GroupDatabaseHandler.getConfStatus(title,context);
                FeedObject tempFeedObj = new FeedObjectEvent(imgId, title, subtitle, timeReceived, confStatus);
                feedObjArr.add(tempFeedObj);
            }
        }
        return numEvents;
    }
}
