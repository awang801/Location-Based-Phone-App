package example.team5.samplelocation.SQLite;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;
import android.database.Cursor;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by magnu_000 on 2/11/2017.
 */

public class GroupDatabaseHandler {

       private static final String TAG = "GroupDatabaseHandler";

        // Database fields
        private SQLiteDatabase database;
        private GroupSQLiteHelper dbHelper;

        private String[] allColumns = { GroupSQLiteHelper.COLUMN_ID, GroupSQLiteHelper.COLUMN_NAME,
                                        GroupSQLiteHelper.COLUMN_PERIOD, GroupSQLiteHelper.COLUMN_DURATION,
                                        GroupSQLiteHelper.COLUMN_STOP, GroupSQLiteHelper.COLUMN_START };

        public GroupDatabaseHandler(Context context) {
            dbHelper = new GroupSQLiteHelper(context);
        }

        public void open() throws SQLException {
            database = dbHelper.getWritableDatabase();
        }

        public void close() {
            dbHelper.close();
        }

        public static List<Group> getGroups(String response) {
            ArrayList<Group> groups = new ArrayList<Group>();
            try {
                    JSONObject obj = new JSONObject(response);
                    String timestamp = obj.getString("datetime");
                    JSONArray groupArr = obj.getJSONArray("groups");
                    if (groupArr.length() != 0) {
                        for (int i = 0; i < groupArr.length(); i++) {
                            Group tempGroup = new Group();
                            JSONObject tempObj = (JSONObject) groupArr.get(i);
                            tempGroup.setName(tempObj.getString("n"));
                            tempGroup.setPeriod(tempObj.getInt("p"));
                            tempGroup.setDuration(tempObj.getInt("d"));
                            tempGroup.setStop(tempObj.getInt("t"));
                            tempGroup.setStart(tempObj.getInt("s"));
                            tempGroup.setLng(tempObj.getString("lng"));
                            tempGroup.setLat(tempObj.getString("lat"));
                            tempGroup.setRad(tempObj.getInt("r"));
                            groups.add(tempGroup);
                        }
                    }
            }
            catch (JSONException e) {
                //TODO: IDK??
            }
            Log.w(TAG, "Groups from json: "+groups.size());
            return groups;
        }

        public static String getTime(String response) {
            //Default to the start of time!!
            String timestamp = "0";
            try {
                JSONObject obj = new JSONObject(response);
                timestamp = obj.getString("datetime");
            }
            catch (JSONException e) {
                //TODO: IDK??
            }
            return timestamp;
        }

        public static int addGroups(List<Group> groups, Context con)
        {
            int events = 0;
            GroupSQLiteHelper gDB = new GroupSQLiteHelper(con);
            SQLiteDatabase db = gDB.getWritableDatabase();

            SQLiteStatement stmt = db.compileStatement("INSERT OR IGNORE INTO groups (name, period, duration, stop, start, conf, lat, lng, rad) VALUES (?, ?, ?, ?, ?, 0, ?, ?, ?)");
            SQLiteStatement stmt2 = db.compileStatement("UPDATE groups SET period = ?, duration = ?, stop = ?, start = ?, conf = 0, lat = ?, lng = ?, rad = ? WHERE name LIKE ? AND stop < ?");

            for( Group temp :  groups)
            {
                Log.d(TAG, "name: " + temp.getName() + " period: " + temp.getPeriod() + " duration: " + temp.getDuration() + " stop: " + temp.getStop() + " start: " + temp.getStart());
                stmt.bindString(1, temp.getName());
                stmt2.bindString(8, temp.getName());

                stmt.bindLong(2, temp.getPeriod());
                stmt2.bindLong(1, temp.getPeriod());

                stmt.bindLong(3, temp.getDuration());
                stmt2.bindLong(2, temp.getDuration());

                stmt.bindLong(4, temp.getStop());
                stmt2.bindLong(3, temp.getStop());
                if(temp.getStop() > System.currentTimeMillis()/1000)
                {
                    events++;
                }

                stmt.bindLong(5, temp.getStart());
                stmt2.bindLong(4, temp.getStart());

                stmt.bindString(6, temp.getLat());
                stmt2.bindString(5, temp.getLat());

                stmt.bindString(7, temp.getLng());
                stmt2.bindString(6, temp.getLng());

                stmt.bindLong(8, temp.getRad());
                stmt2.bindLong(7, temp.getRad());

                stmt2.bindLong(9, System.currentTimeMillis()/1000);

                try {
                    if (stmt.executeInsert() == -1) {
                        Log.w(TAG, "Insert Failed");
                        int val = stmt2.executeUpdateDelete();
                        Log.w(TAG, "Update: " + val);
                    }
                }
                catch(SQLException exception)
                {
                    exception.printStackTrace();
                }
            }

            db.close();
            return events;
        }

    //Returns true if group is in db and has conf set to true
    public static boolean getConfStatus(String name, Context con)
    {
        GroupSQLiteHelper gDB = new GroupSQLiteHelper(con);
        SQLiteDatabase db = gDB.getWritableDatabase();

        String[] projection = {
                GroupSQLiteHelper.COLUMN_CONFIRMED,
                GroupSQLiteHelper.COLUMN_NAME,
        };

        Cursor cursor = db.query(
                GroupSQLiteHelper.TABLE_GROUPS,
                projection,
                "name=?",
                new String[] { name },
                null,
                null,
                null
        );

        boolean isConf = false;
        try {
           if(cursor.moveToNext())
           {
               Log.w(TAG,"got one");
               isConf = cursor.getInt(cursor.getColumnIndex(GroupSQLiteHelper.COLUMN_CONFIRMED)) == 1;
               Log.w(TAG,"Think name is: "+cursor.getString(cursor.getColumnIndex(GroupSQLiteHelper.COLUMN_NAME)));
           }
        }
        finally {
            if(cursor != null && !cursor.isClosed())
            {
                cursor.close();
            }
            db.close();
        }
        Log.w(TAG,"conf: "+isConf);
        return isConf;
    }

    public static List<Group> selectGroups(Context con)
    {
        GroupSQLiteHelper gDB = new GroupSQLiteHelper(con);
        SQLiteDatabase db = gDB.getWritableDatabase();

        String[] projection = {
                GroupSQLiteHelper.COLUMN_NAME,
                GroupSQLiteHelper.COLUMN_START,
                GroupSQLiteHelper.COLUMN_STOP,
                GroupSQLiteHelper.COLUMN_PERIOD,
                GroupSQLiteHelper.COLUMN_DURATION,
                GroupSQLiteHelper.COLUMN_CONFIRMED,
                GroupSQLiteHelper.COLUMN_LAT,
                GroupSQLiteHelper.COLUMN_LONG,
                GroupSQLiteHelper.COLUMN_RAD
        };

        //for now grab everything!!

        //sort by earliest (lowest stop time)
        String sortOrder = GroupSQLiteHelper.COLUMN_STOP + " ASC";

        Cursor cursor = db.query(
                GroupSQLiteHelper.TABLE_GROUPS,
                projection,
                null,
                null,
                null,
                null,
                sortOrder
        );

        Log.w(TAG,"Queried");
        List<Group> groups = new ArrayList<Group>();
        try {
            while (cursor.moveToNext()) {
                Group tempGroup = new Group();
                tempGroup.setName(cursor.getString(cursor.getColumnIndex(GroupSQLiteHelper.COLUMN_NAME)));
                tempGroup.setPeriod(cursor.getInt(cursor.getColumnIndex(GroupSQLiteHelper.COLUMN_PERIOD)));
                tempGroup.setDuration(cursor.getInt(cursor.getColumnIndex(GroupSQLiteHelper.COLUMN_DURATION)));
                tempGroup.setStop(cursor.getInt(cursor.getColumnIndex(GroupSQLiteHelper.COLUMN_STOP)));
                tempGroup.setStart(cursor.getInt(cursor.getColumnIndex(GroupSQLiteHelper.COLUMN_START)));
                tempGroup.setConf(cursor.getInt(cursor.getColumnIndex(GroupSQLiteHelper.COLUMN_CONFIRMED)));
                tempGroup.setLat(cursor.getString(cursor.getColumnIndex(GroupSQLiteHelper.COLUMN_LAT)));
                tempGroup.setLng(cursor.getString(cursor.getColumnIndex(GroupSQLiteHelper.COLUMN_LONG)));
                tempGroup.setRad(cursor.getInt(cursor.getColumnIndex(GroupSQLiteHelper.COLUMN_RAD)));
                groups.add(tempGroup);
            }
        }
        finally {
            if(cursor != null && !cursor.isClosed())
            {
                cursor.close();
            }
            db.close();
        }

        return groups;
    }

    public static List<Group> selectGroupsPriority(Context con)
    {
        List<Group> actionGroups = new ArrayList<Group>();
        List<Group> groups = selectGroups(con);
        for(Group group : groups)
        {
            if(group.getStop() > System.currentTimeMillis()/1000)
            {
                actionGroups.add(group);
            }
        }
        return actionGroups;
    }

    public static List<Group> selectGroupsConfirmed(Context con)
    {
        List<Group> groups = new ArrayList<Group>();
        List<Group> groupsPriority = selectGroups(con);
        for(Group group : groupsPriority)
        {
            if(group.getConf() != 0)
            {
                groups.add(group);
            }
        }
        return groups;
    }

    public static void setConfirm(String name, Boolean confirm, Context con)
    {
        GroupSQLiteHelper gDB = new GroupSQLiteHelper(con);
        SQLiteDatabase db = gDB.getWritableDatabase();

        int value = 0;
        if(confirm)
        {
            value = 1;
        }

        Log.w(TAG,"setConfirm: "+confirm);

        SQLiteStatement stmt = db.compileStatement("UPDATE groups SET conf = ? WHERE name = ?");
        stmt.bindLong(1, value);
        stmt.bindString(2,name);
        stmt.executeUpdateDelete();

        db.close();
    }

    }
