package example.team5.samplelocation.main;


import java.util.Date;

/**
 * Created by dylanklohr on 3/2/17.
 *
 * Class for holding each Feed Row Item to be fed to Adapter for display
 */
public abstract class FeedObject {

    // elements common to both row types
    public int m_groupAvatarResId;
    public String m_title;
    public String m_subtitle;
    public Date m_timeReceived;


    // Constructor - set the fields common to all FeedObject instances
    public FeedObject(int imgResId, String title, String subtitle, Date timeReceived) {
        this.m_groupAvatarResId = imgResId;
        this.m_title = title;
        this.m_subtitle = subtitle;
        this.m_timeReceived = timeReceived;
    }
}
