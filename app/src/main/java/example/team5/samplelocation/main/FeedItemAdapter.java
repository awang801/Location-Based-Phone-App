package example.team5.samplelocation.main;

import example.team5.samplelocation.R;
import example.team5.samplelocation.SQLite.GroupDatabaseHandler;
import example.team5.samplelocation.Sync.SyncHelper;
import example.team5.samplelocation.databaseupdate.LocationService;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by dylanklohr on 3/2/17.
 */

public class FeedItemAdapter extends ArrayAdapter<FeedObject> {

    // Row Types
    private static final int TYPE_EVENT = 0;
    private static final int TYPE_MESSAGE = 1;
    private static final int NUM_TYPES = 2;

    // Holders for the View-Holder Java Pattern
    private static class EventHolder {
        ImageView groupAvatar;
        TextView titleRef;
        TextView subtitleRef;
        TextView timeRecRef;
        Switch responseRef;
    }

    private static class MessageHolder {
        ImageView groupAvatar;
        TextView titleRef;
        TextView subtitleRef;
        TextView timeRecRef;
        ImageView deleteIcon;
    }

    // Member Variables
    private ArrayList<FeedObject> data = null;
    private Activity m_activity;
    private LayoutInflater m_inflater;
    private EventHolder eHolder = null;
    private MessageHolder mHolder = null;
    private boolean switchOverrideFlag = false;
    private Context con = null;


    // Public Constructor
    public FeedItemAdapter(Context context, Activity activity, ArrayList<FeedObject> data) {
        super(context, 0, data); // TODO: 3/3/17 determine why 0 works here instead of layoutResourceId
        this.m_activity = activity;
        this.data = data;
        this.m_inflater = LayoutInflater.from(context);
        con = context;
    }

    @Override
    public int getItemViewType(int position) {

        if (data.get(position) instanceof FeedObjectEvent) {
            return TYPE_EVENT;
        }
        else {
            return TYPE_MESSAGE;
        }
    }

    @Override
    public int getViewTypeCount() {
        return NUM_TYPES;
    }

    @Override
    public FeedObject getItem(int position) {
        return data.get(position);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        int objectType = getItemViewType(position);

        if (convertView == null) {

            if (objectType == TYPE_EVENT) {
                convertView = m_inflater.inflate(R.layout.feed_list_row_event, null);
                eHolder = new EventHolder();
                eHolder.groupAvatar = (ImageView) convertView.findViewById(R.id.feedRow_event_list_image);
                eHolder.titleRef = (TextView) convertView.findViewById(R.id.feedRow_event_title);
                eHolder.subtitleRef = (TextView) convertView.findViewById(R.id.feedRow_event_subtitle);
                eHolder.timeRecRef = (TextView) convertView.findViewById(R.id.feedRow_event_time_received);
                eHolder.responseRef = (Switch) convertView.findViewById(R.id.feedRow_event_response);
                convertView.setTag(eHolder);
            }
            else if (objectType == TYPE_MESSAGE) {
                convertView = m_inflater.inflate(R.layout.feed_list_row_message, null);
                mHolder = new MessageHolder();
                mHolder.groupAvatar = (ImageView) convertView.findViewById(R.id.feedRow_message_list_image);
                mHolder.titleRef = (TextView) convertView.findViewById(R.id.feedRow_message_title);
                mHolder.subtitleRef = (TextView) convertView.findViewById(R.id.feedRow_message_subtitle);
                mHolder.timeRecRef = (TextView) convertView.findViewById(R.id.feedRow_message_time_received);
                mHolder.deleteIcon = (ImageView) convertView.findViewById(R.id.feedRow_message_delete_icon);
                convertView.setTag(mHolder);
            }
        }
        else {
            if (objectType == TYPE_EVENT) {
                eHolder = (EventHolder) convertView.getTag();
            }
            else if (objectType == TYPE_MESSAGE) {
                mHolder = (MessageHolder) convertView.getTag();
            }
        }

        // get the object passed on the position of the row
        final FeedObject row = data.get(position);

        // event listener injections
        if (objectType == TYPE_EVENT) {

            /*
            * FeedObjectEvent switch listener
            *
            * The following event-listener is triggered when a user changes the state of a switch,
            * found on an FeedObjectEvent instance within the Feed tab. The user is prompted to
            * confirm an event response via a modal confirmation dialog if they toggle the
            * aforementioned switch from OFF –> ON, indicating they are trying to RESPOND to an
            * event. A similar confirmation is presented to the user in the event that they attempt
            * to UNRESPOND to an event.
            *
            * */

            eHolder.responseRef.setChecked(((FeedObjectEvent)row).m_switchStatus);
            eHolder.responseRef.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(final CompoundButton buttonView, boolean isChecked) {

                    // user attempted to RESPOND to an event
                    if (!switchOverrideFlag && isChecked) {
                        LocationService mMyService = new LocationService();
                        int message = SyncHelper.isMyServiceRunning(mMyService.getClass(), getContext()) ? R.string.feed_row_event_confirmation_message : R.string.feed_row_event_LocFailed_message;
                        // create the AlertDialog with a "Builder" java pattern
                        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(m_activity);
                        alertBuilder.setMessage(message)
                                .setCancelable(false)
                                // triggered if the user CONFIRMS their RESPONSE to an event
                                .setPositiveButton(R.string.alert_dialog_checkin_confirm_RESPOND, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        // update the java object representing the state of the row
                                        ((FeedObjectEvent) row).m_switchStatus = true;

                                        /*
                                        * TODO: Connect to backend
                                        *
                                        * At this point, the user has indicated and confirmed that
                                        * they desire to respond to an event represented by the
                                        * 'row' object. This needs to be reflected on the backend.
                                        *
                                        * */
                                        GroupDatabaseHandler.setConfirm(((FeedObjectEvent) row).m_title, true, con);


                                    }
                                })
                                .setNegativeButton(R.string.alert_dialog_checkin_confirm_CANCEL, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // reanimate the switch to false if the checkin isn't confirmed
                                        switchOverrideFlag = true;
                                        buttonView.toggle();
                                        switchOverrideFlag = false;
                                    }
                                });

                        AlertDialog confirmDialog = alertBuilder.create();
                        //Note: in case of location service not working we could always set toggle to off
                        //and disable the positive button here??
                        confirmDialog.show();


                    }
                    // user attempted to UNRESPOND to an event
                    else if (!switchOverrideFlag && !isChecked) {
                        LocationService mMyService = new LocationService();
                        int message = SyncHelper.isMyServiceRunning(mMyService.getClass(), getContext()) ? R.string.feed_row_event_confirmation_message : R.string.feed_row_event_LocFailed_message;
                        // create the AlertDialog with a "Builder" java pattern
                        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(m_activity);
                        alertBuilder.setMessage(message)
                            .setCancelable(false)
                            // triggered if the user UNCONFIRMS their RESPONSE to an event
                            .setPositiveButton(R.string.alert_dialog_checkin_confirm_UNRESPOND, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    // update the java object representing the state of the row
                                    ((FeedObjectEvent) row).m_switchStatus = false;

                                    /*
                                    * TODO: Connect to backend
                                    *
                                    * At this point, the user has indicated and confirmed that
                                    * they desire to unrespond to an event represented by the
                                    * 'row' object. This needs to be reflected on the backend.
                                    *
                                    * */
                                    GroupDatabaseHandler.setConfirm(((FeedObjectEvent) row).m_title, false, con);

                                }
                            })
                            .setNegativeButton(R.string.alert_dialog_checkin_confirm_CANCEL, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // reanimate the switch to true if the checkin is unconfirmed
                                    switchOverrideFlag = true;
                                    buttonView.toggle();
                                    switchOverrideFlag = false;
                                }
                            });

                        AlertDialog confirmDialog = alertBuilder.create();
                        confirmDialog.show();
                    }

                }
            });
        }
        else {
            mHolder.deleteIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                // create the AlertDialog with a "Builder" java pattern
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(m_activity);
                alertBuilder.setMessage(R.string.feed_row_message_delete_confirmation)
                    .setCancelable(false)

                    // triggered if the user CONFIRMS their DELETE to an event
                    .setPositiveButton(R.string.alert_dialog_checkin_confirm_DELETE, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            data.remove(row);
                            notifyDataSetChanged();

                        }
                    })
                    .setNegativeButton(R.string.alert_dialog_checkin_confirm_CANCEL, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            /*
                            * Nothing needs to be done here, this simply needs to be
                            * implemented so we have a "CANCEL" button.
                            * */
                        }
                    });

                AlertDialog confirmDialog = alertBuilder.create();
                confirmDialog.show();
                }
            });
        }

        // set all fields in each respective layout appropriately
        if (row != null) {
            if (objectType == TYPE_EVENT) {
                eHolder.titleRef.setText(row.m_title);
                eHolder.subtitleRef.setText(row.m_subtitle);
                eHolder.timeRecRef.setText(fixTimestampC(row.m_timeReceived));
            }
            else if (objectType == TYPE_MESSAGE) {
                mHolder.titleRef.setText(row.m_title);
                mHolder.subtitleRef.setText(row.m_subtitle);
                mHolder.timeRecRef.setText(fixTimestampC(row.m_timeReceived));
            }
        }

        return convertView;
    }
    //Date version
    public static String fixTimestamp(Date timeStamp) {
        String returnString;// = timeStamp.toString();
        SimpleDateFormat m_format;
        Date currDate = new Date(System.currentTimeMillis());
        if(timeStamp.getYear() == currDate.getYear()) { //check for same year
            int currDayOfMonth = currDate.getDate();
            int feedDayOfMonth = timeStamp.getDate();
            if(timeStamp.getMonth() == currDate.getMonth() && feedDayOfMonth == currDayOfMonth) { //check if its from today
                m_format = new SimpleDateFormat("hh:mm a");
            }
            else {
                if((currDayOfMonth - feedDayOfMonth) < 7) { //check if its within a week
                    m_format = new SimpleDateFormat("EEE hh:mm a");
                }
                else {
                    m_format = new SimpleDateFormat("EEE MMM dd  hh:mm a");
                }
            }
        }
        else {
            m_format = new SimpleDateFormat("EEE MMM dd, yyyy  hh:mm a");
        }
        returnString = m_format.format(timeStamp);
        return returnString;
    }
    //Calender version
    //Since these seem to get cut off on a real phone I may need to change this to switch between the time and the date
    // (i.e. Wed Mar 22  5:34 PM  would change to 03/22/17) probably would display the full time on the pop-up card
    //TODO update this so that the current Calendar object is passed in so its not needlessly created over and over again.
    public static String fixTimestampC(Date timeStamp) {
        String returnString;
        SimpleDateFormat m_format;
        Calendar currDate = Calendar.getInstance();
        Calendar timeStampC = Calendar.getInstance();//.setTime(timeStamp);
        timeStampC.setTime(timeStamp);
        //run your if checks here:
        if(currDate.get(Calendar.YEAR) == timeStampC.get(Calendar.YEAR)) {
            if(currDate.get(Calendar.MONTH) == timeStampC.get(Calendar.MONTH)) {
                if(currDate.get(Calendar.DATE) == timeStampC.get(Calendar.DATE)) {
                    m_format = new SimpleDateFormat("hh:mm a");
                }
                else {
                    if((currDate.get(Calendar.DATE) - timeStampC.get(Calendar.DATE)) < 7) {
                        m_format = new SimpleDateFormat("EEE hh:mm a");
                    }
                    else {
//                        m_format = new SimpleDateFormat("EEE MMM dd  hh:mm a");
                        m_format = new SimpleDateFormat("MM/dd/yy");
                    }
                }
            }
            else {
                if ((currDate.get(Calendar.MONTH) - timeStampC.get(Calendar.MONTH)) == 1) {
                    //check if within a week...
                    if (currDate.get(Calendar.DAY_OF_YEAR) - timeStampC.get(Calendar.DAY_OF_YEAR) < 7) {
                        m_format = new SimpleDateFormat("EEE hh:mm a");
                    } else {
//                        m_format = new SimpleDateFormat("EEE MMM dd  hh:mm a");
                        m_format = new SimpleDateFormat("MM/dd/yy");
                    }

                } else {
//                    m_format = new SimpleDateFormat("EEE MMM dd  hh:mm a");
                    m_format = new SimpleDateFormat("MM/dd/yy");
                }
            }
        }
        else { //following if covers the edge case of currDate = January 2 and timeStampC = December 29
            if((currDate.get(Calendar.YEAR) - timeStampC.get(Calendar.YEAR) == 1) && (currDate.get(Calendar.DAY_OF_YEAR) - timeStampC.get(Calendar.DAY_OF_YEAR) > -358)) {
                m_format = new SimpleDateFormat("EEE hh:mm a");
            }
            else
            {
//                m_format = new SimpleDateFormat("EEE MMM dd, yyyy  hh:mm a");
                m_format = new SimpleDateFormat("MM/dd/yy");
            }
        }
        returnString = m_format.format(timeStamp);
        return returnString;
    }
}
