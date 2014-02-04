package com.ronank.weather.db;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

/**
 * Simple ContentProvider implementation   
 * @author rkelly
 *
 */
public class MyLocalWeatherContentProvider extends ContentProvider
{
    private static final String CONTENT_FORECAST_ITEM_TYPE = "vnd.android.cursor.item/vnd.weather.forecast";
    private static final String CONTENT_FORECAST_TYPE_DIR  = "vnd.android.cursor.dir/vnd.weather.forecast";

    public static final String AUTHORITY = "com.ronank.forecast.provider";

    public static final Uri CONTENT_URI_FORECAST = Uri.parse("content://" + AUTHORITY + "/forecast");
    
    public static final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Paths
    public static final String PATH_FORECAST          = "forecast";
    public static final String PATH_FORECAST_ITEM     = "forecast/#";

    // Tokens
    public static final int PATH_FORECAST_TOKEN       = 100;
    public static final int PATH_FORECAST_ITEM_TOKEN  = 101;

    static
    {
        matcher.addURI(AUTHORITY, PATH_FORECAST, PATH_FORECAST_TOKEN);
        matcher.addURI(AUTHORITY, PATH_FORECAST_ITEM, PATH_FORECAST_ITEM_TOKEN);
    }

    // Content Provider stuff

    private MyLocalWeatherDBOpenHelper dbHelper;

    @Override
    public boolean onCreate()
    {
        Context ctx = getContext();
        dbHelper = MyLocalWeatherDBOpenHelper.getDB(ctx);
        return true;
    }

    @Override
    public String getType(Uri uri)
    {
        final int match = matcher.match(uri);
        switch (match)
        {
            case PATH_FORECAST_TOKEN:
                return CONTENT_FORECAST_ITEM_TYPE;
            case PATH_FORECAST_ITEM_TOKEN:
                return CONTENT_FORECAST_TYPE_DIR;
            default:
                throw new UnsupportedOperationException("URI " + uri + " is not supported.");
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)
    {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        final int match = matcher.match(uri);
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        switch (match)
        {
            case PATH_FORECAST_TOKEN:
                // retrieve forecast list
                builder.setTables(TableForecast.TABLE_NAME);
                return builder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
            case PATH_FORECAST_ITEM_TOKEN:
                // Retrieve specific forecast
                int locationId = (int) ContentUris.parseId(uri);
                builder.setTables(TableForecast.TABLE_NAME);
                builder.appendWhere(TableForecast.KEY_ID + "=" + locationId);
                return builder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
            default:
                return null;
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values)
    {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int token = matcher.match(uri);
        switch (token)
        {
            case PATH_FORECAST_TOKEN:
                // We only ever have 1 forecast, so update rather than insert
                int rowsUpdated = db.update(TableForecast.TABLE_NAME, values, TableForecast.KEY_ID + "=1", null);

                if (rowsUpdated != 0)
                {
                    Uri updatedUri = CONTENT_URI_FORECAST.buildUpon().appendPath("1").build();
                    
                    getContext().getContentResolver().notifyChange(updatedUri, null);
                    return updatedUri;
                }

                return null;
            default:
                throw new UnsupportedOperationException("URI: " + uri + " not supported.");
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs)
    {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int token = matcher.match(uri);
        int rowsDeleted = -1;
        switch (token)
        {
            case PATH_FORECAST_TOKEN:
                rowsDeleted = db.delete(TableForecast.TABLE_NAME, selection, selectionArgs);
                break;
            case PATH_FORECAST_ITEM_TOKEN:
                String whereClause = TableForecast.KEY_ID + "=" + uri.getLastPathSegment();
                if (!TextUtils.isEmpty(selection))
                    whereClause += " AND " + selection;
                rowsDeleted = db.delete(TableForecast.TABLE_NAME, whereClause, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        // Notifying the changes, if there are any
        if (rowsDeleted != -1)
            getContext().getContentResolver().notifyChange(uri, null);

        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs)
    {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int token = matcher.match(uri);
        int rowsUpdated = 0;
        switch (token)
        {
            case PATH_FORECAST_TOKEN:
                rowsUpdated = db.update(TableForecast.TABLE_NAME, values, selection, selectionArgs);
                break;
            case PATH_FORECAST_ITEM_TOKEN:
                String segment = uri.getPathSegments().get(1);
                rowsUpdated = db.update(TableForecast.TABLE_NAME, values, TableForecast.KEY_ID
                        + "=" + segment + (!TextUtils.isEmpty(selection) ? " AND ("
                        + selection + ')' : ""), selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        // Notifying the changes, if there are any
        if (rowsUpdated != 0)
            getContext().getContentResolver().notifyChange(uri, null);

        return rowsUpdated;
    }
}