package com.ronank.weather.db;


import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * DB Helper class
 * 
 * @author rkelly
 *
 */
public class MyLocalWeatherDBOpenHelper extends SQLiteOpenHelper
{
    private static final String DATABASE_NAME = "mylocalweather.db";
    private static final int DATABASE_VERSION = 1;

    // TABLES
    private TableForecast tableCurrentForecast;

    private static MyLocalWeatherDBOpenHelper instance = null;
    private SQLiteDatabase mDb = null;

    public MyLocalWeatherDBOpenHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static MyLocalWeatherDBOpenHelper getDB(Context ctx) 
    {
        if (instance == null)
        {
            instance = new MyLocalWeatherDBOpenHelper(ctx);
            instance.open();
        }

        return instance;
    }
    
    private void open() throws SQLException
    {
        if (mDb == null)
            mDb = getWritableDatabase();

        tableCurrentForecast.init();
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        tableCurrentForecast = TableForecast.getTable(db);    
    }

    @Override
    public void onOpen(SQLiteDatabase db)
    {
        tableCurrentForecast = TableForecast.getTable(db);

        super.onOpen(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        TableForecast.upgradeTable(db, oldVersion, newVersion);
    }
}
