package com.ronank.weather.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * The table used to store the current forecase data in
 * 
 * @author rkelly
 *
 */
public class TableForecast
{
    private final String TAG = getClass().getSimpleName();

    // table name
    protected static final String TABLE_NAME = "tcurforecast";
    protected SQLiteDatabase db;

    // column names
    public static final String KEY_ID          = "_id";
    public static final String KEY_TEMP        = "temp";
    public static final String KEY_MAIN_TEXT   = "main";
    public static final String KEY_DESCRIPTION = "desc";
    public static final String KEY_ICON        = "icon";
    public static final String KEY_NAME        = "name";
    public static final String KEY_ADDRESS     = "addr";
    public static final String KEY_CUR_DT      = "cur_dt";
    public static final String KEY_FORECAST_DT = "for_dt";

    // create string
    private static final String CREATE_TABLE = "create table if not exists " + TABLE_NAME + " ("
            + KEY_ID + " integer primary key autoincrement, " 
            + KEY_TEMP + " integer not null,"
            + KEY_MAIN_TEXT + " text not null,"
            + KEY_DESCRIPTION + " text not null,"
            + KEY_ICON + " text not null,"
            + KEY_NAME + " text not null,"
            + KEY_ADDRESS + " text not null,"
            + KEY_CUR_DT + " integer not null,"
            + KEY_FORECAST_DT + " integer not null);";

    // Initial values
    public static final int DEFAULT_VALUE = 0;
    public static final String DEFAULT_TEXT = "-default-";

    private static TableForecast instance;

    private TableForecast(SQLiteDatabase aDb, boolean create)
    {
        db = aDb;

        try
        {
            if (create)
            {
                db.execSQL(CREATE_TABLE);
            }
        }
        catch (SQLException e)
        {
            Log.e(TAG, "Failed to create table " + TABLE_NAME, e);
        }
    }

    public static TableForecast getTable(SQLiteDatabase db)
    {
        if (instance == null)
            instance = new TableForecast(db, true);

        return instance;
    }

    public static void upgradeTable(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        getTable(db);        
    }

    public void init()
    {
        Cursor cur = null;

        try
        {
            cur = db.query(TABLE_NAME, null, null, null, null, null, null, null);

            if (cur != null && cur.getCount() == 0)
            {
                // the db is not intialised => initialise it.
                ContentValues initialValues = new ContentValues();

                initialValues.put(KEY_TEMP, DEFAULT_VALUE);
                initialValues.put(KEY_MAIN_TEXT, DEFAULT_TEXT);
                initialValues.put(KEY_DESCRIPTION, DEFAULT_TEXT);
                initialValues.put(KEY_ICON, DEFAULT_TEXT);
                initialValues.put(KEY_NAME, DEFAULT_TEXT);
                initialValues.put(KEY_ADDRESS, DEFAULT_TEXT);
                initialValues.put(KEY_CUR_DT, DEFAULT_VALUE);
                initialValues.put(KEY_FORECAST_DT, DEFAULT_VALUE);

                db.insert(TABLE_NAME, null, initialValues);
            }
        }
        finally
        {
            if (cur != null)
                cur.close();
        }
    }
}
