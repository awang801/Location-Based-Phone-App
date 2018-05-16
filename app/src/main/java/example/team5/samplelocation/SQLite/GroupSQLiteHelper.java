package example.team5.samplelocation.SQLite;

import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;
import android.content.Context;
import android.util.Log;

/**
 * Created by magnu_000 on 2/10/2017.
 */

public class GroupSQLiteHelper extends SQLiteOpenHelper {
    public static final String TABLE_GROUPS = "groups";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_PERIOD = "period";
    public static final String COLUMN_DURATION = "duration";
    public static final String COLUMN_STOP = "stop";
    public static final String COLUMN_START = "start";
    public static final String COLUMN_CONFIRMED = "conf";
    public static final String COLUMN_LAT = "lat";
    public static final String COLUMN_LONG = "lng";
    public static final String COLUMN_RAD = "rad";

    private static final String DATABASE_NAME = "groups.db";
    private static final int DATABASE_VERSION = 1;

    // Database creation sql statement
    private static final String DATABASE_CREATE = "create table "
            + TABLE_GROUPS + "( " + COLUMN_ID  + " integer primary key autoincrement, "
            + COLUMN_NAME + " text not null unique, "
            + COLUMN_PERIOD + " integer not null, "
            + COLUMN_DURATION + " integer not null, "
            + COLUMN_CONFIRMED + " integer not null, "
            + COLUMN_LAT + " text not null, "
            + COLUMN_LONG + " text not null, "
            + COLUMN_RAD + " integer not null, "
            + COLUMN_STOP + " integer not null, "
            + COLUMN_START + " integer not null);";

    public GroupSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d("Upgrade","Upgrade");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GROUPS);
        onCreate(db);
    }
}
