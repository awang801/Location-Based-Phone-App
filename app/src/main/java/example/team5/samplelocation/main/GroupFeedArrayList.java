package example.team5.samplelocation.main;

import android.content.Context;
import android.database.Observable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.ListIterator;

import example.team5.samplelocation.R;
import example.team5.samplelocation.main.GroupFeedObjectGroup.MemberRank;

/**
 * Created by dylank1223 on 3/21/17.
 */

public class GroupFeedArrayList extends ArrayList<GroupFeedObject> {

    // Helpful Constants
    private static final int NONE = -1;

    // TODO: 3/21/17 fix magic strings
    private static GroupFeedObject INVITES_HEADER = null;
    private static GroupFeedObject ADMIN_HEADER = null;
    private static GroupFeedObject MEMBER_HEADER = null;

    // Member Variables
    private Context m_context;
    private int inviteHdrIndx;
    private int adminHdrIndx;
    private int memberHdrIndx;


    public GroupFeedArrayList(Context context) {
        super();
        this.inviteHdrIndx = NONE;
        this.adminHdrIndx = NONE;
        this.memberHdrIndx = NONE;
        INVITES_HEADER = new GroupFeedObjectHeader(context.getString(R.string.group_row_header_text_invites));
        ADMIN_HEADER =  new GroupFeedObjectHeader(context.getString(R.string.group_row_header_text_admin));
        MEMBER_HEADER =  new GroupFeedObjectHeader(context.getString(R.string.group_row_header_text_member));
    }

    @Override
    public boolean add(GroupFeedObject gfo) {

        boolean result = super.add(gfo);

        // ensure something was actually added so no useless processing and that we aren't adding a header
        if (result && !(gfo instanceof GroupFeedObjectHeader)) {
            removeHeaders();
            Collections.sort(this, customComparator);
            insertHeaders();
        }

        return result;
    }

    @Override
    public GroupFeedObject remove(int index) {

        GroupFeedObject gfo = super.remove(index);

        // searches to see if any Invites are in the list, if not, it removes the Invites Header
        if (objectIs(MemberRank.INVITEE, gfo)) {

            ListIterator<GroupFeedObject> it = this.listIterator();
            boolean containsInvites = false;
            GroupFeedObject gfoTemp;

            while (!containsInvites && it.hasNext()) {

                gfoTemp = it.next();
                if (objectIs(MemberRank.INVITEE, gfoTemp)) {
                    containsInvites = true;
                }
            }

            if (!containsInvites) {
                this.remove(INVITES_HEADER);
                inviteHdrIndx = NONE;
            }
        }
        // searches to see if any Admins are in the list, if not, it removes the Admins Header
        else if (objectIs(MemberRank.ADMIN, gfo)) {

            ListIterator<GroupFeedObject> it = this.listIterator();
            boolean containsAdmins = false;
            GroupFeedObject gfoTemp;

            while (!containsAdmins && it.hasNext()) {

                gfoTemp = it.next();
                if (objectIs(MemberRank.ADMIN, gfoTemp)) {
                    containsAdmins = true;
                }
            }

            if (!containsAdmins) {
                this.remove(ADMIN_HEADER);
                adminHdrIndx = NONE;
            }
        }
        // searches to see if any Members are in the list, if not, it removes the Members Header
        else if (objectIs(MemberRank.MEMBER, gfo)){

            ListIterator<GroupFeedObject> it = this.listIterator();
            boolean containsMembers = false;
            GroupFeedObject gfoTemp;

            while (!containsMembers && it.hasNext()) {

                gfoTemp = it.next();
                if (objectIs(MemberRank.MEMBER, gfoTemp)) {
                    containsMembers = true;
                }
            }

            if (!containsMembers) {
                this.remove(MEMBER_HEADER);
                memberHdrIndx = NONE;
            }
        }

        return gfo;
    }


    @Override
    public boolean remove (Object o) {

        if (o instanceof GroupFeedObjectHeader) {

            GroupFeedObjectHeader gfoHead = (GroupFeedObjectHeader) o;

            /*
            * Determine the type of header label is being removed, update the corresponding index
            * and remove it.
            */
            if (gfoHead.equals(INVITES_HEADER)) {
                inviteHdrIndx = NONE;
                return super.remove(o);
            }
            else if (gfoHead.equals(ADMIN_HEADER)) {
                adminHdrIndx = NONE;
                return super.remove(o);
            }
            else if (gfoHead.equals(MEMBER_HEADER)) {
                memberHdrIndx = NONE;
                return super.remove(o);
            }
            else{
                return false;
            }

        }
        else if (o instanceof GroupFeedObjectGroup) {

            /*
            * Find the index of the object being removed, remove it using the other remove function
            * */
            int index = indexOf(o);

            int oldSize = this.size();

            if (index != NONE) {
                this.remove(index);
            }

            return (oldSize != this.size());
        }
        else {
            return false;
        }
    }


    Comparator<GroupFeedObject> customComparator = new Comparator<GroupFeedObject>() {

        @Override
        public int compare(GroupFeedObject o1, GroupFeedObject o2) {

            GroupFeedObjectGroup t1 = (GroupFeedObjectGroup) o1;
            GroupFeedObjectGroup t2 = (GroupFeedObjectGroup) o2;

            // first try to sort off of rank, then sort alphabetically by name
            if (t1.m_rank != t2.m_rank) {
                return t1.m_rank.compareTo(t2.m_rank);
            }
            else {
                return t1.m_title.compareTo(t2.m_title);
            }
        }
    };

    private void removeHeaders() {

        /*
        * IMPORTANT: the removes MUST occur in this order, otherwise undesired list elements will be removed
        * */

        if (this.memberHdrIndx != NONE) {
            this.remove(memberHdrIndx);
            this.memberHdrIndx = NONE;
        }

        if (this.adminHdrIndx != NONE) {
            this.remove(adminHdrIndx);
            this.adminHdrIndx = NONE;
        }

        if (this.inviteHdrIndx != NONE) {
            this.remove(inviteHdrIndx);
            this.inviteHdrIndx = NONE;
        }
    }

    private void insertHeaders() {

        ListIterator<GroupFeedObject> it = this.listIterator();

        if (it.hasNext()) {

            GroupFeedObject gfo = it.next();

            // block responsible for adding 'Invites' Header
            if (objectIs(MemberRank.INVITEE, gfo)) {

                inviteHdrIndx = it.previousIndex();
                it.previous();
                it.add(INVITES_HEADER);

                // traverse the rest of the Invites
                while (it.hasNext() && objectIs(MemberRank.INVITEE, gfo)) {
                    gfo = it.next();
                }
            }

            // block responsilbe for adding 'Admin' Header
            if (objectIs(MemberRank.ADMIN, gfo)) {

                adminHdrIndx = it.previousIndex();
                it.previous();
                it.add(ADMIN_HEADER);

                //traverse the rest of the Admins
                while (it.hasNext() && objectIs(MemberRank.ADMIN, gfo)) {
                    gfo = it.next();
                }
            }

            // block responsilbe for adding 'Member' Header
            if (objectIs(MemberRank.MEMBER, gfo)) {

                memberHdrIndx = it.previousIndex();
                it.previous();
                it.add(MEMBER_HEADER);
            }
        }
    }


    private boolean objectIs(MemberRank rank, GroupFeedObject gfo) {
        return (gfo instanceof GroupFeedObjectGroup
                && ((GroupFeedObjectGroup) gfo).m_rank == rank);
    }
}