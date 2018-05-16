package example.team5.samplelocation.main;

import java.util.Date;
import java.sql.Time;

import example.team5.samplelocation.R;

/**
 * Created by dylank1223 on 3/4/17.
 */

public class FeedObjectMessage extends FeedObject {

    private static final String SUBTITLE_DEFAULT = "Click to view";
    private static final int allowedLength = 18;


    // elements specific only to Message type FeedObject instances
    public String m_message;
    public boolean m_display;


    /**
     *
     * @param imgResId - image to be displayed as the event's group avatar
     * @param title - title of message to be displayed
     * @param timeReceived - time the notification was received
     * @param message - message to be displayed when clicked
     */
    public FeedObjectMessage(int imgResId, String title, Date timeReceived, String message, boolean display) {
        super(imgResId, message.length() > allowedLength ? message.substring(0,allowedLength-3)+"..." : message, title, timeReceived);

        this.m_message = message;
        this.m_display = display;
    }

    /**
     *
     * @param imgResId - image to be displayed as the event's group avatar
     * @param title - title of message to be displayed
     * @param subtitle - subtitle of message to be displayed
     * @param timeReceived - time the noticifaction was received
     * @param message - message to be displayed when clicked
     */
    @Deprecated // not actually deprecated, but use is strongly discouraged
    public FeedObjectMessage(int imgResId, String title, String subtitle, Date timeReceived, String message) {

        super(imgResId, title, subtitle, timeReceived);

        this.m_message = message;
    }
}
