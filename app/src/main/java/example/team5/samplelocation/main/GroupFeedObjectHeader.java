package example.team5.samplelocation.main;

/**
 * Created by dylank1223 on 3/21/17.
 */

public class GroupFeedObjectHeader extends GroupFeedObject {

    // text to be displayed in the header object
    public final String m_text;

    // set the header's text
    public GroupFeedObjectHeader(String title) {
        this.m_text = title;
    }
}
