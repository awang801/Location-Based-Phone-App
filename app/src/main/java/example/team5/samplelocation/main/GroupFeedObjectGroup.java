package example.team5.samplelocation.main;

/**
 * Created by dylank1223 on 3/21/17.
 */

public class GroupFeedObjectGroup extends GroupFeedObject {

    // enum representing all possible ranks a user can have in a group
    public static enum MemberRank {
        /*
        * IMPORTANT: these MUST stay in this order, changing the order changes the way
        *               in which we sort by group type
        * */

        INVITEE, ADMIN, MEMBER
    }


    // member variables for the GroupFeedObjectRow class
    public MemberRank m_rank;
    public int m_groupAvatarResId;
    public String m_title;
    public String m_subtitle;
    public String m_id;

    // Constructor – sets the fields unique to a group row
    public GroupFeedObjectGroup(MemberRank rank, int imgResId, String title, String subtitle) {
        this.m_rank = rank;
        this.m_groupAvatarResId = imgResId;
        this.m_title = title;
        this.m_subtitle = subtitle;
        this.m_id = "";
    }

    public void setID(String id)
    {
        m_id = id;
    }
}
