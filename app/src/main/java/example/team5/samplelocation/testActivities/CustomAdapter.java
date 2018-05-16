package example.team5.samplelocation.testActivities;

/**
 * Created by Philip on 3/27/2017.
 * Taken From : http://stacktips.com/tutorials/android/listview-with-section-header-in-android
 */
import example.team5.samplelocation.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

class CustomAdapter extends BaseAdapter {

    private static final int TYPE_ITEM = 0;
    private static final int TYPE_SEPARATOR = 1;
    private int count = 0;          // Keep track of the current number of values that we have in our list

    private HashMap<Integer, String> mData = new HashMap<Integer, String>();
    private HashMap<Integer, String> sectionHeader = new HashMap<Integer, String>();

    private LayoutInflater mInflater;

    public CustomAdapter(Context context) {
        mInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void addItem(final String item) {
        mData.put(count , item);
        notifyDataSetChanged();
        count++;
    }

    public void addSectionHeaderItem(final String item) {
        sectionHeader.put(count , item);
        notifyDataSetChanged();
        count++;
    }

    @Override
    public int getItemViewType(int position) {
        return sectionHeader.containsKey(position) ? TYPE_SEPARATOR : TYPE_ITEM;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getCount() {
        return mData.size()+ sectionHeader.size();
    }

    @Override
    public String getItem(int position) { return sectionHeader.containsKey(position) ? sectionHeader.get(position) : mData.get(position); }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        int rowType = getItemViewType(position);

        if (convertView == null) {
            holder = new ViewHolder();
            switch (rowType) {
                case TYPE_ITEM:
                    convertView = mInflater.inflate(R.layout.member_list_item, null);
                    holder.textView = (TextView) convertView.findViewById(R.id.text);
                    break;
                case TYPE_SEPARATOR:
                    convertView = mInflater.inflate(R.layout.member_list_header, null);
                    holder.textView = (TextView) convertView.findViewById(R.id.text);
                    break;
            }
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.textView.setText(getItem(position));

        return convertView;
    }

    public static class ViewHolder {
        public TextView textView;
    }

}
