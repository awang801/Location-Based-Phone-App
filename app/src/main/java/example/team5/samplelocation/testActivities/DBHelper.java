package example.team5.samplelocation.testActivities;

/**
 * Created by Philip on 10/17/2016.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;


public class DBHelper extends SQLiteOpenHelper {

    private static String DATABASE = "team5_db";
    private static String TABLE = "lnglat";
    private static String ID = "id";
    private static String LNG = "longitude";
    private static String LAT = "latitude";


    //public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
    public DBHelper(Context context) {
        super(context, DATABASE, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table if not exists " + TABLE + " (" + ID + " integer primary key," + LNG + " double, " + LAT + " double)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    // Insert a new row into the db
    public boolean insertSimpleCoordinate(SimpleCoordinate simpleCoordinate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        try {
            // The id value will be created automatically by the db
            contentValues.put(LNG, simpleCoordinate.getLongitude());
            contentValues.put(LAT, simpleCoordinate.getLatitude());
            db.insert(TABLE, null, contentValues);

            return true;
        }catch (Exception e) {
            return false;
        }
    }

    // Get all the values currently in the db
    public ArrayList<SimpleCoordinate> getAllSimpleCoordinates() {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<SimpleCoordinate> coordinates = new ArrayList<SimpleCoordinate>();


        try {
            Cursor cur = db.rawQuery(" select * from " + TABLE, null);
            cur.moveToFirst();

            // Go through each value in the db and add the values to the array that is to be returned
            while(cur.isAfterLast() == false) {
                SimpleCoordinate tempCoordinate = new SimpleCoordinate();
                tempCoordinate.setId(cur.getInt(cur.getColumnIndex(ID)));
                tempCoordinate.setLongitude(cur.getDouble(cur.getColumnIndex(LNG)));
                tempCoordinate.setLatitude(cur.getDouble(cur.getColumnIndex(LAT)));
                coordinates.add(tempCoordinate);
                cur.moveToNext();
            }

            return coordinates;
        } catch (Exception e) {
            return null;
        }
    }

    // Get the most recent value from the db
    public SimpleCoordinate getRecentSimpleCoordinate() {
        SQLiteDatabase db = this.getReadableDatabase();
        SimpleCoordinate retCoord = new SimpleCoordinate();

        try {
            Cursor cur = db.rawQuery("select * from " + TABLE, null);
            cur.moveToLast();

            retCoord.setId(cur.getInt(cur.getColumnIndex(ID)));
            retCoord.setLongitude(cur.getDouble(cur.getColumnIndex(LNG)));
            retCoord.setLatitude(cur.getDouble(cur.getColumnIndex(LAT)));

            return retCoord;
        } catch (Exception e) {
            return null;
        }
    }
}
