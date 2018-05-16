package example.team5.samplelocation.main;


import example.team5.samplelocation.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import example.team5.samplelocation.databaseupdate.dbString;
import example.team5.samplelocation.main.GroupFeedObjectGroup.MemberRank;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import org.w3c.dom.Text;

import java.io.IOException;
import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Created by dylank1223 on 3/21/17.
 */

public class GroupFeedAdapter extends ArrayAdapter<GroupFeedObject> {

    // Row Types
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_GROUP = 1;
    private static final int NUM_TYPES = 2;

    // Holders for the View-Holder Java Pattern
    private static class HeaderHolder {
        TextView headerRef;
    }

    private static class RowHolder {
        ImageView groupAvatar;
        TextView titleRef;
        TextView subtitleRef;
        LinearLayout inviteOptions;
        ImageView inviteAccept;
        ImageView inviteDecline;
    }

    // Member Variables
    private ArrayList<GroupFeedObject> data = null;
    private Activity m_activity;
    private LayoutInflater m_inflater;
    private HeaderHolder hHolder = null;
    private RowHolder rHolder = null;
    private GroupFeedAdapter m_adapter = null;

    // Public Constructor
    public GroupFeedAdapter(Context context, Activity activity, ArrayList<GroupFeedObject> data) {
        super(context, 0, data); // TODO: 3/21/17 remove magic number
        this.m_activity = activity;
        this.data = data;
        this.m_inflater = LayoutInflater.from(context);
        this.m_adapter = this;
    }

    @Override
    public int getItemViewType(int position) {

        if (data.get(position) instanceof GroupFeedObjectHeader) {
            return TYPE_HEADER;
        }
        else {
            return TYPE_GROUP;
        }
    }

    @Override
    public int getViewTypeCount() {
        return NUM_TYPES;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        int objectType = getItemViewType(position);

        if (convertView == null) {

            if (objectType == TYPE_HEADER) {
                convertView = m_inflater.inflate(R.layout.group_list_header, null);
                hHolder = new HeaderHolder();
                hHolder.headerRef = (TextView) convertView.findViewById(R.id.groupRow_header_text);
                convertView.setTag(hHolder);
            }
            else if (objectType == TYPE_GROUP) {
                convertView = m_inflater.inflate(R.layout.group_list_row, null);
                rHolder = new RowHolder();
                rHolder.groupAvatar = (ImageView) convertView.findViewById(R.id.groupRow_list_image);
                rHolder.titleRef = (TextView) convertView.findViewById(R.id.groupRow_title);
                rHolder.subtitleRef = (TextView) convertView.findViewById(R.id.groupRow_subtitle);
                rHolder.inviteOptions = (LinearLayout) convertView.findViewById(R.id.groupRow_invite_options);
                rHolder.inviteAccept = (ImageView) convertView.findViewById(R.id.groupRow_invite_accept);
                rHolder.inviteDecline = (ImageView) convertView.findViewById(R.id.groupRow_invite_decline);
                convertView.setTag(rHolder);
            }
        }
        else {
            if (objectType == TYPE_HEADER) {
                hHolder = (HeaderHolder) convertView.getTag();
            }
            else if (objectType == TYPE_GROUP) {
                rHolder = (RowHolder) convertView.getTag();
            }
        }

        final GroupFeedObject row = data.get(position);

        // event listener injections
        if (objectType == TYPE_GROUP) {

            // event listener for clicking the 'ACCEPT' on an invite group
            rHolder.inviteAccept.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    // create the AlertDialog with a "Builder" java pattern
                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(m_activity);
                    alertBuilder.setMessage(R.string.group_row_invite_confirmation_accept_message)
                            .setCancelable(false)
                            // Triggered if the user CONFIRMS their JOIN to a group invite
                            .setPositiveButton(R.string.alert_dialog_invite_confirm_JOIN, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    /*
                                    * TODO: Connect to backend
                                    *
                                    * At this point, the user has indicated and confirmed that they
                                    * desire to JOIN a group that they were invited to represented
                                    * by the 'row' object. This needs to be represented on the
                                    * backend.
                                    *
                                    * */
                                    FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
                                    String groupName = ((GroupFeedObjectGroup) row).m_title;
                                    Log.d("GroupFeedAdapter", "Name: "+groupName);

                                    OkHttpClient client = new OkHttpClient.Builder()
                                            .connectTimeout(10, TimeUnit.SECONDS)
                                            .writeTimeout(10, TimeUnit.SECONDS)
                                            .readTimeout(10, TimeUnit.SECONDS)
                                            .retryOnConnectionFailure(false)
                                            .build();

                                    RequestBody body = new FormBody.Builder()
                                            .add("id", mUser.getUid())
                                            .add("group_name", groupName)
                                            .build();

                                    Request request = new Request.Builder()
                                            .url(dbString.php_root+"groupRequest.php")
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
                                            Log.d("GroupFeedAdapter", "Update Res: "+res);
                                            if (res.equals("Invited: invitation accepted"))
                                            {
                                                //Success
                                                //TODO: Dylan is there a call here that can be made
                                                //To refresh the page in some way?
                                                if(data != null)
                                                {
                                                    data.remove(row);
                                                }

                                                if(m_activity != null)
                                                {
                                                    m_activity.runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            m_activity.recreate();
                                                        }
                                                    });
                                                }
                                            }
                                            else
                                            {
                                                //Failure
                                            }
                                        }
                                    });
                                }
                            })
                            // triggered if the user CANCELS their JOIN to a group invite
                            .setNegativeButton(R.string.alert_dialog_invite_confirm_CANCEL, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    /*
                                    * NOTE: nothing needs to be done here, the user is simply
                                    *       CANCELING their response to JOIN a group.
                                    * */
                                }
                            });
                    AlertDialog joinDialog = alertBuilder.create();
                    joinDialog.show();
                }
            });

            // event listener for clicking 'DECLINE' on an invite group
            rHolder.inviteDecline.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // create the AlertDialog with a "Builder" java pattern
                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(m_activity);
                    alertBuilder.setMessage(R.string.group_row_invite_confirmation_decline_message)
                            .setCancelable(false)
                            // Triggered if the user CONFIRMS their JOIN to a group invite
                            .setPositiveButton(R.string.alert_dialog_invite_confirm_DECLINE, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    /*
                                    * TODO: Connect to backend
                                    *
                                    * At this point, the user has indicated and confirmed that they
                                    * desire to DECLINE a group that they were invited to,
                                    * represented by the 'row' object. This needs to be represented
                                    * on the backend.
                                    *
                                    * */
                                    FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
                                    String rowID = ((GroupFeedObjectGroup) row).m_id;

                                    Log.d("GroupFeedAdapter", "id: "+rowID);

                                    OkHttpClient client = new OkHttpClient.Builder()
                                            .connectTimeout(10, TimeUnit.SECONDS)
                                            .writeTimeout(10, TimeUnit.SECONDS)
                                            .readTimeout(10, TimeUnit.SECONDS)
                                            .retryOnConnectionFailure(false)
                                            .build();

                                    RequestBody body = new FormBody.Builder()
                                            .add("id", mUser.getUid())
                                            .add("row", rowID)
                                            .add("state", "1")
                                            .build();

                                    Request request = new Request.Builder()
                                            .url(dbString.php_root+"removeGroupUsers.php")
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
                                            if(Integer.parseInt(res) > 0)
                                            {
                                                Log.d("GroupFeedAdapter", "Delete Success");

                                                if(data != null)
                                                {
                                                    data.remove(row);
                                                }

                                                if(m_activity != null)
                                                {
                                                    m_activity.runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            m_adapter.notifyDataSetChanged();
                                                        }
                                                    });
                                                }
                                            }
                                            else
                                            {
                                                Log.d("GroupFeedAdapter", "Delete Fail");
                                            }
                                        }
                                    });
                                }
                            })
                            // triggered if the user CANCELS their JOIN to a group invite
                            .setNegativeButton(R.string.alert_dialog_invite_confirm_CANCEL, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    /*
                                    * NOTE: nothing needs to be done here, the user is simply
                                    *       CANCELING their response to DECLINE a group.
                                    * */
                                }
                            });
                    AlertDialog declineDialog = alertBuilder.create();
                    declineDialog.show();
                }
            });
        }

        // set all fields in each respective layout appropriately
        if (row != null) {
            if (objectType == TYPE_HEADER) {
                GroupFeedObjectHeader row_header = (GroupFeedObjectHeader) row;
                hHolder.headerRef.setText(row_header.m_text);
            }
            else if (objectType == TYPE_GROUP) {
                GroupFeedObjectGroup row_group = (GroupFeedObjectGroup) row;
                rHolder.titleRef.setText(row_group.m_title);
                rHolder.subtitleRef.setText(row_group.m_subtitle);

                if (row_group.m_rank == MemberRank.INVITEE) {
                    rHolder.inviteOptions.setVisibility(View.VISIBLE);
                }
                else if ((row_group.m_rank == MemberRank.ADMIN) || (row_group.m_rank == MemberRank.MEMBER)) {
                    rHolder.inviteOptions.setVisibility(View.GONE);
                }
            }
        }

        return convertView;
    }
}