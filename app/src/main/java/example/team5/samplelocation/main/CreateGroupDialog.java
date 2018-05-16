package example.team5.samplelocation.main;

import android.app.Activity;
import android.app.Dialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

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
 * Created by johnw on 3/3/2017.
 */

public class CreateGroupDialog extends android.support.v4.app.DialogFragment {

    View fragRoot;
    String TAG = "CreateGroupDialog";
    // flag to be used for avoiding multiple calls to the backend from repeated SAVE button pressesc
    boolean processingBackend = false;
    // Use a static activity rather than getActivity() to safely toast near the time
    // that this fragment is killed
    FragmentActivity activity;

    /** The system calls this to get the DialogFragment's layout, regardless
     of whether it's being displayed as a dialog or an embedded fragment. */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // init activity to safely use toasts later
        activity = getActivity();

        setHasOptionsMenu(true);
        fragRoot = inflater.inflate(R.layout.dialog_create_group, container, false);
        buildToolbar(fragRoot);
        return fragRoot;
    }

    /** The system calls this only when creating the layout in a dialog. */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // The only reason you might override this method when using onCreateView() is
        // to modify any dialog characteristics. For example, the dialog includes a
        // title by default, but your custom layout might not need it. So here you can
        // remove the dialog title, but you must call the superclass to get the Dialog.
        Dialog dialog = super.onCreateDialog(savedInstanceState);
//        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    public void buildToolbar(final View fragRoot) {
        Toolbar toolbar = (Toolbar) fragRoot.findViewById(R.id.toolbar);

        toolbar.inflateMenu(R.menu.menu_create_group_dialog);
        toolbar.setTitle("Create New Group");
        toolbar.setNavigationIcon(R.drawable.ic_clear_white_24dp);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.action_save) {

                    if (!processingBackend) {
                        // Set this flag so that future calls to the backend (e.g. from clicking SAVE
                        // multiple times quickly) are blocked
                        processingBackend = true;

                        // Grab the UI elements with user input
                        EditText nameET = (EditText) fragRoot.findViewById(R.id.etName);
                        EditText descriptionET = (EditText) fragRoot.findViewById(R.id.etDescription);
                        Switch visSwitch = (Switch) fragRoot.findViewById(R.id.switchVis);

                        // Grab the values to be passed to the backend
                        String name = nameET.getText().toString();
                        String description = descriptionET.getText().toString();

                        int vis = 0;
                        if (visSwitch.isChecked()) {
                            vis = 1;
                        }

                        // Max length is enforced in the XML, so only check that there exists values of at least length 1
                        if (name.length() < 1) {
                            Toast.makeText(activity, "You group must have a name", Toast.LENGTH_SHORT).show();
                        } else if (description.length() < 1) {
                            Toast.makeText(activity, "Your group must have a description", Toast.LENGTH_SHORT).show();
                        } else {

                            FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
                            new MyAsyncTask().execute(name, description, Integer.toString(vis));
                        }
                    }
                }
                return true;
            }
        });
    }


    public class MyAsyncTask extends AsyncTask<String, Integer, Double> {

       @Override
        protected Double doInBackground(String... params) {
            // TODO Auto-generated method stub
           //do something
           String name = params[0];
           String desc = params[1];
           String vis = params[2];
           Log.d("AddGroup", name+" "+desc+" "+vis);
           httpConnection(name,desc,vis);
           return null;
        }

        protected void onPostExecute(Double result){
            //UPDATE HERE
            Log.d("AddGroup","post execute");
            getActivity().getSupportFragmentManager().popBackStack();
            getActivity().recreate();
                                }

        private void httpConnection(String name, String desc, String vis)
        {
            BufferedReader reader = null;
            try {
                // create the HttpURLConnection
                FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();

                String urlParameters  = "name="+name+"&desc="+desc+"&id="+mUser.getUid()+"&vis="+vis;
                byte[] postData       = urlParameters.getBytes( "UTF-8" );
                int    postDataLength = postData.length;

                URL url = new URL(dbString.php_root+"addGroup.php");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                // just want to do an HTTP GET here
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                connection.setRequestProperty( "Content-Length", Integer.toString( postDataLength ));
                connection.setDoOutput(true);
                connection.getOutputStream().write(postData);

                // give it 15 seconds to respond
                connection.setReadTimeout(15 * 1000);
                //connection.connect();

                // read the output from the server
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
                StringBuilder stringBuilder = new StringBuilder();

                String line = null;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line + "\n");
                }
                Log.w("AddGroup", "ASync res: "+stringBuilder.toString());
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

    }


    public static void showCreateGroupDialogue(FragmentManager fm) {

        CreateGroupDialog frag = new CreateGroupDialog();

        // Use a transaction to show the fragment fullscreen
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        // To make it fullscreen, use the 'content' root view as the container
        // for the fragment, which is always the root view for the activity
        transaction.add(android.R.id.content, frag).addToBackStack(null).commit();
    }
}
