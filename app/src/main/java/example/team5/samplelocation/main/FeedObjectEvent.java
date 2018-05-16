package example.team5.samplelocation.main;

import java.util.Date;

/**
 * Created by dylank1223 on 3/4/17.
 */

public class FeedObjectEvent extends FeedObject {

    // elements specific only to Event type FeedObject instances
    public boolean m_switchStatus;

    public FeedObjectEvent(int imgResId, String title, String subtitle, Date timeReceived, boolean switchStatus) {

        super(imgResId, title, subtitle, timeReceived);

        this.m_switchStatus = switchStatus;
    }
}
