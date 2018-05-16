package example.team5.samplelocation.main;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;

import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import example.team5.samplelocation.R;
import example.team5.samplelocation.Sync.SyncHelper;
import example.team5.samplelocation.SQLite.FeedDatabaseHandler;
import example.team5.samplelocation.databaseupdate.dbString;
import example.team5.samplelocation.login.AccountUtils;
import example.team5.samplelocation.login.LoginActivity;
import example.team5.samplelocation.main.GroupFeedObjectGroup.MemberRank;
import example.team5.samplelocation.testActivities.MainMenu;


public class MainActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    // The client will be initialized in onCreate(), connected in onStart(), and disconnected in onStop()
    private GoogleApiClient mGoogleApiClient;

    private static String GroupJSONString;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    final String TAG = "MainActivity";
    FirebaseAuth.AuthStateListener mAuthListener;
    SharedPreferences prefs;
    boolean loginPagePushed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "ONCREATE CALLED");
        super.onCreate(savedInstanceState);

        final Activity mainAct = this;
        loginPagePushed = false;

        // Check if this is the first time run of the app and push to login page if it is
        prefs = this.getPreferences( MODE_PRIVATE );
        // If the app has run before, searching for IsFirstRun will return false.
        // Otherwise it will return true by default if it previously didn't exist.
        // Therefore if it is true then we know the app has never run before
        if(prefs.getBoolean("IsFirstRun", true)) {
            Log.d(TAG, "APP IS RUNNING FOR THE FIRST TIME");
            if( FirebaseAuth.getInstance().getCurrentUser() != null ) {
                // THIS STATE SHOULD NOT HAPPEN
                // Log the user out and send them to the login page
                Log.d(TAG, "DUMB APP REINSTALL CASE HAS OCCURRED");
                getApplicationContext().deleteDatabase("groups.db");
                AccountUtils.signOutFirebaseAndBackend(mGoogleApiClient);
                pushLoginActivity();
            }
            // Store the keyvalue as false to avoid triggering this block on subsequent runs
            prefs.edit().putBoolean("IsFirstRun", false).apply();
        } else {
            Log.d(TAG, "APP HAS RUN BEFORE");
        }

        // Immediately check if a user is signed in and send them to login if necessary to keep
        // fragments that depend on valid users from starting their processes
        if( FirebaseAuth.getInstance().getCurrentUser() == null ) {
            pushLoginActivity();
        }

        //If for some reason we lost location permissions let's shove them back to the login to sort themselves out!
        if( ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED )
        {
            //Log.d("DB", "Dropped table!");
            //this.deleteDatabase("groups.db");

            // Ensure that the user is signed out
            //AccountUtils.signOutFirebaseAndBackend( mGoogleApiClient );

            //pushLoginActivity();
        }

        // Use an AuthStateListener to ensure that if a user is logged out
        // when this activity is created that they are kicked to the sign in page
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());

                    //We should start up the locationservice just in case!
                    SyncHelper syncDB = new SyncHelper(user.getUid(),1,getApplicationContext());
                    syncDB.syncDB(3);
                } else {
                    // User is not signed in, kick to sign in page
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    pushLoginActivity();
                }
            }
        };

        // Set up GoogleApiClient to prep for logout
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getResources().getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder( this )
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        mGoogleApiClient.connect();
        Log.d(TAG, "Google API Client connected.");

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        // TODO remove magic strings below
        toolbar.setTitle("Check-in App");
        setSupportActionBar(toolbar);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
    }

    private void pushLoginActivity() {
        if( !loginPagePushed ) {
            loginPagePushed = true;
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivityForResult(intent, 1);
        } else {
            Log.d(TAG, "login page pushed more than once");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if(mAuthListener != null) {
            FirebaseAuth.getInstance().addAuthStateListener(mAuthListener);
        }
        if( mGoogleApiClient != null ) {
            mGoogleApiClient.connect();
        } else {
            Log.d(TAG, "Google API client object null in onStart");
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            FirebaseAuth.getInstance().removeAuthStateListener(mAuthListener);
        }
        if( mGoogleApiClient != null ) {
            mGoogleApiClient.connect();
        } else {
            Log.d(TAG, "Google API client object null in onStop");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        final MenuItem menuItem = menu.findItem( R.id.action_search);
        final SearchView sv = (SearchView) menuItem.getActionView();
        if( sv != null ) {
            sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    if (!sv.isIconified()) {
                        sv.setIconified(true);
                    }
                    new startSearch().execute(query);
                    menuItem.collapseActionView();
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    return false;
                }
            });
            sv.setQueryHint("Search for a Group");
        }
        return true;
    }

    public class startSearch extends AsyncTask<String, Integer, Double> {

        String searchKey;
        String json;
        @Override
        protected Double doInBackground(String... params) {
            // TODO Auto-generated method stub
            searchKey = params[0];
            json = "{\"groups\":[]}";
            getSearchResults(searchKey);
            return null;
        }

        protected void onPostExecute(Double result){
            //UPDATE HERE
            Log.d("MainActivity","search res: "+json);
        }

        private void getSearchResults(String key)
        {
            Log.d("MainTest", "Begin getSearchResults");
            BufferedReader reader = null;
            try {
                // create the HttpURLConnection
                URL url = new URL(dbString.php_root+"groupSearch.php?search=" + key + "&limit=10");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                // just want to do an HTTP GET here
                connection.setRequestMethod("GET");

                // give it 60 seconds to respond
                connection.setConnectTimeout(10*1000);
                connection.setReadTimeout(10*1000);

                connection.connect();

                // read the output from the server
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();

                String line = null;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line + "\n");
                }

                Log.w("MainTest", "Done with search request: "+stringBuilder.toString());
                json = stringBuilder.toString();
            }
            catch (java.net.SocketTimeoutException e)
            {
                Log.e("MainTest","Timeout Search");
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                // close the reader; this can throw an exception too, so
                // wrap it in another try/catch block.
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                }
            }
        }

    }



    public void notifySearchJSONArrived(String JSON)
    {
        //here we can do a lot of things with the JSON
        //assuming the variables that we use to populate the search will be here
        //we can manipulate them and then let the search view we are done
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if( id == R.id.action_settings ) {

            return true;
        } else if( id == R.id.action_logout ) {
            // Log the device out of the backend and out of Firebase
            Log.d("DB", "Dropped table!");
            this.deleteDatabase("groups.db");
            AccountUtils.signOutFirebaseAndBackend( mGoogleApiClient );

            return true;
        } else if( id == R.id.action_testPage ) {
            Intent intent = new Intent(getApplicationContext(), MainMenu.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A fragment containing the feed tab
     */
    public static class FeedFragment extends Fragment {

        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        private static Context m_context = null;

        // actual list item of rows
        private static ListView lv_feedItems = null;

        // SwipeRefreshLayout wrapping the list of group items
        private static SwipeRefreshLayout swipeContainer;

        // adapter to render rows
        private static FeedItemAdapter adapter;

        // data source
        private ArrayList<FeedObject> feedObjects = new ArrayList<FeedObject>() {
            public boolean add(FeedObject fo) {

                boolean returnVal;

                if (fo instanceof FeedObjectMessage) {
                    if (((FeedObjectMessage) fo).m_display) {
                        returnVal = super.add(fo);
                    }
                    else {
                        return false;
                    }
                }
                else {
                    returnVal = super.add(fo);
                }

                Collections.sort(feedObjects, new Comparator<FeedObject>() {

                    private static final int O1_FIRST = -1;
                    private static final int O2_FIRST = 1;

                    @Override
                    public int compare(FeedObject o1, FeedObject o2) {
                        if (isMessage(o1) && isEvent(o2)) {
                           return O1_FIRST;
                        }
                        else if (isEvent(o1) && isMessage(o2)) {
                            return O2_FIRST;
                        }
                        else {
                            return o2.m_timeReceived.compareTo(o1.m_timeReceived);
                        }
                    }

                    private boolean isEvent(FeedObject feedObject) {
                        return (feedObject instanceof FeedObjectEvent);
                    }

                    private boolean isMessage(FeedObject feedObject) {
                        return (feedObject instanceof FeedObjectMessage);
                    }
                });

                return returnVal;
            }
        };


        public FeedFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static FeedFragment newInstance(int sectionNumber, Context context) {
            FeedFragment fragment = new FeedFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        static final public String COPA_RESULT = "com.controlj.copame.backend.COPAService.REQUEST_PROCESSED";

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main_feed, container, false);

            swipeContainer = (SwipeRefreshLayout) rootView.findViewById(R.id.feedTab_swipeRefreshLayout);
            swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

                @Override
                public void onRefresh() {
                   /*
                   * TODO: Connect to Backend
                   *
                   *
                   * NOTE: the 'swipeContainer.setRefreshing(false)' call should STILL be made to
                   *       after content has been loaded to ensure the refresh animation continues
                   *       to function as normal.
                   *
                   * */

                   //Note: Yes this is trol
                   //The sync will kick off the location service which will kick off refresh if new event is found
                   //While the add feed is also refreshing feed. That is the one that handles the swipe obvi
                   FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
                   if(mUser != null) {
                       SyncHelper syncDB = new SyncHelper(mUser.getUid(), 1, getContext());
                       syncDB.syncDB(3);
                   }
                   FeedDatabaseHandler.addFeed(feedObjects, getContext(), adapter, swipeContainer);
                }
            });

            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mYourBroadcastReceiver,
                    new IntentFilter(COPA_RESULT));

            Date tempDate = new Date(117, 2, 1, 0,0,0);
            Log.d("Main Activity", "feedObjects size: " + feedObjects.size());

            // create an adapter to convert the array to views
            adapter = new FeedItemAdapter(getContext(), getActivity(), feedObjects);
            FeedDatabaseHandler.addFeed(feedObjects, getContext(), adapter, null);

            // inject the lv_feedItems ID defined in fragment_main_feed.xml into Java
            lv_feedItems = (ListView) rootView.findViewById(R.id.lv_feedItems);

            lv_feedItems.setAdapter(adapter);
            return rootView;
        }

        @Override
        public void onStart() {
            super.onStart();

            // listener responsible for responding to clicking an item in the feed tab
            lv_feedItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    // get a copy of the object that was clicked
                    FeedObject fo = feedObjects.get(position);

                    if (fo instanceof FeedObjectEvent) {

                        /*
                        *  TODO: respond to a user clicking on an Event in the Feed Tab
                        *
                        *  A Detailed Event View needs to be rendering displaying a more detailed
                        *  view of the user-desired event.
                        * */

                    }
                    else {

                        /*
                        *  TODO: respond to a user clicking on a Message in the Feed Tab
                        *
                        *  A Detailed Message View needs to be rendering displaying a more detailed
                        *  view of the user-desired event.
                        * */

                    }
                }
            });
        }

        @Override
        public void onDestroyView()
        {
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mYourBroadcastReceiver);
            super.onDestroyView();
        }

        private final BroadcastReceiver mYourBroadcastReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                Log.d("FeedDatabaseHandler", "Inside Frag Receiver");
                FeedDatabaseHandler.addFeed(feedObjects, getContext(), adapter, null);
            }
        };
    }

    /**
     * A fragment containing the groups tab
     */
    public static class GroupsFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        // actual list item of rows
        private static ListView lv_groupItems = null;

        // SwipeRefreshLayout wrapping the list of group items
        private static SwipeRefreshLayout swipeContainer;

        // adapter to render rows
        private static GroupFeedAdapter adapter;

        // ArrayList extension (see below) responsible for adding appropriate headers
        private static ArrayList<GroupFeedObject> feedObjects;
        private static ArrayList<GroupFeedObject> tempFeedObjects;

        public GroupsFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static GroupsFragment newInstance(int sectionNumber, Context context) {
            GroupsFragment fragment = new GroupsFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main_groups, container, false);
            feedObjects = new GroupFeedArrayList(getContext());
            tempFeedObjects = new GroupFeedArrayList(getContext());

            FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    CreateGroupDialog.showCreateGroupDialogue( getActivity().getSupportFragmentManager());
                }
            });

            swipeContainer = (SwipeRefreshLayout) rootView.findViewById(R.id.groupsTab_swipeRefreshLayout);
            swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    new updateFeed().execute("1");
                }
            });


            /*
             * TODO: dynamically populate the groups tab with groups the user is a member of
             * IMPORTANT: the GroupFeedArrayList class is an extension of the ArrayList class,
             * see commit 'c85295008c3736de4bd60bde0fa144c4ecb971a0' in Dylan's branch for detailed
             * instructions on list alteration.
             *
             * */

            Log.d("AddGroup","onCreateView");

            new updateFeed().execute("0");

            adapter = new GroupFeedAdapter(getContext(), getActivity(), feedObjects);

            lv_groupItems = (ListView) rootView.findViewById(R.id.lv_groupItems);

            lv_groupItems.setAdapter(adapter);
            return rootView;
        }

        @Override
        public void onStart() {
            super.onStart();

            // listener responsible for responding to clicking an item in the feed tab
            lv_groupItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    // get a copy of the object that was clicked
                    GroupFeedObject gfo = feedObjects.get(position);

                    // we only want to respond if the user selects a group, not a group header
                    if (gfo instanceof GroupFeedObjectGroup) {

                        GroupFeedObjectGroup gfog = (GroupFeedObjectGroup) gfo;

                        // Create a new instance of our detailed group view
                        BottomSheetDialogFragment bottomSheetDialogFragment = new GroupBottomSheetDialogFragment();

                        // Set args to pass to to the detailed view
                        Bundle args = new Bundle();
                        args.putString("name", gfog.m_title);
                        args.putString("description", gfog.m_subtitle);
                        args.putInt("avatarID", gfog.m_groupAvatarResId);
                        if (gfog.m_rank == MemberRank.INVITEE) {
                            args.putInt("rank", 0);
                        }
                        else if (gfog.m_rank == MemberRank.ADMIN) {
                            args.putInt("rank", 1);
                        }
                        else if (gfog.m_rank == MemberRank.MEMBER) {
                            args.putInt("rank", 2);
                        }

                        // Show the detailed group view
                        bottomSheetDialogFragment.setArguments( args );
                        bottomSheetDialogFragment.show(getActivity().getSupportFragmentManager(), "Bottom Sheet Dialog Fragment");
                    }

                    adapter.notifyDataSetChanged();
                }
            });
        }

        public void updateFeedObjects() {
            Log.d("MainTest", "Begin updateFeedObjects");
            BufferedReader reader = null;
            try {
                // create the HttpURLConnection
                FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
                URL url = new URL(dbString.php_root+"getGroups.php?id=" + mUser.getUid());
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                // just want to do an HTTP GET here
                connection.setRequestMethod("GET");

                // give it 60 seconds to respond
                connection.setConnectTimeout(15*1000);
                connection.setReadTimeout(15 * 1000);
                try {
                    connection.connect();
                }
                catch (Exception e)
                {
                    Log.e("MainTest","Timeout Group");

                    if(getContext() != null)
                    {
                        Handler handler = new Handler(Looper.getMainLooper());

                        handler.post(new Runnable() {

                            @Override
                            public void run() {
                                Toast.makeText(getContext(), "Group Timeout", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }

                // read the output from the server
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();

                String line = null;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line + "\n");
                }

                Log.w("MainTest", stringBuilder.toString());
                if(getContext() != null)
                {
                    buildFeedObjects(stringBuilder.toString());
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                // close the reader; this can throw an exception too, so
                // wrap it in another try/catch block.
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                }
            }
        }

        public void buildFeedObjects(String jsonString)
        {

            Log.d("MainTest", jsonString);

            tempFeedObjects = new GroupFeedArrayList(getContext());

            Log.d("MainTest", "tempFeedObjects size: "+tempFeedObjects.size());

            if(jsonString != null)
            {
                if(jsonString.length() == 0 || jsonString.charAt(0) != '{')
                {
                    Log.d("MainTest", "Error Result From getGroups: "+jsonString);
                    return;
                }
                try
                {
                    Log.d("MainTest", "try");
                    JSONObject obj = new JSONObject(jsonString);
                    JSONArray members = obj.getJSONArray("member");
                    Log.d("MainTest", "mem length: "+members.length());
                    for (int i = 0; i < members.length(); i++) {
                        JSONObject tempObj = (JSONObject) members.get(i);
                        String name = (tempObj.getString("n"));
                        String desc = (tempObj.getString("d"));
                        int image = (tempObj.getInt("img"));
                        tempFeedObjects.add(new GroupFeedObjectGroup(MemberRank.MEMBER, R.drawable.anon_user_48dp, name, desc));
                    }

                    JSONArray owned = obj.getJSONArray("owner");
                    Log.d("MainTest", "own length: "+owned.length());
                    for (int i = 0; i < owned.length(); i++) {
                        JSONObject tempObj = (JSONObject) owned.get(i);
                        String name = (tempObj.getString("n"));
                        String desc = (tempObj.getString("d"));
                        int image = (tempObj.getInt("img"));
                        tempFeedObjects.add(new GroupFeedObjectGroup(MemberRank.ADMIN, R.drawable.anon_user_48dp, name, desc));
                    }

                    JSONArray invites = obj.getJSONArray("ri");
                    Log.d("MainTest", "in length: "+invites.length());
                    for (int i = 0; i < invites.length(); i++) {
                        JSONObject tempObj = (JSONObject) invites.get(i);
                        String name = (tempObj.getString("n"));
                        String desc = (tempObj.getString("d"));
                        String id = (tempObj.getString("id"));
                        int image = (tempObj.getInt("img"));
                        GroupFeedObjectGroup invite = new GroupFeedObjectGroup(MemberRank.INVITEE, R.drawable.anon_user_48dp, name, desc);
                        invite.setID(id);
                        tempFeedObjects.add(invite);
                    }
                }
                catch(Exception e)
                {
                    Log.d("MainTest","Issue with adding");
                    Log.w("MainTest","Error: "+e.getMessage());
                    e.printStackTrace();
                }
            }
        }

        public class updateFeed extends AsyncTask<String, Integer, Double> {

            boolean stopRefresh = false;
            @Override
            protected Double doInBackground(String... params) {
                stopRefresh = Integer.parseInt(params[0]) == 1;
                updateFeedObjects();
                return null;
            }

            protected void onPostExecute(Double result){
                 //UPDATE HERE
                Log.d("MainTest","notified data set has changed");

                if(getContext() != null)
                {
                    feedObjects = tempFeedObjects;
                    adapter = new GroupFeedAdapter(getContext(), getActivity(), feedObjects);
                    lv_groupItems.setAdapter(adapter);

                    //Note: This shouldn't be an issue if context is shot right??
                    if(stopRefresh)
                    {
                        swipeContainer.setRefreshing(false);
                    }
                }
            }

        }
    }

    // Handle the result from the login activity
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "Hit OnActivityResult");
        // Collect data from the intent and use it
        if( data != null ) {
            Bundle extras = data.getExtras();
            if (extras != null) {
                if (extras.containsKey("response")) {
                    this.recreate();
                }
            }
        }
    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            // TODO remove magic numbers & strings below
            if(position == 0) {
                return FeedFragment.newInstance(position + 1, getApplicationContext());
            } else {
                return GroupsFragment.newInstance(position + 1, getApplicationContext());
            }
        }

        @Override
        public int getCount() {
            // TODO remove magic numbers & strings below
            // Show 2 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            // TODO remove magic numbers & strings below
            switch (position) {
                case 0:
                    return "FEED";
                case 1:
                    return "GROUPS";
            }
            return null;
        }
    }
}
