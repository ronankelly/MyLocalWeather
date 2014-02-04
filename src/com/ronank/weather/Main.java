package com.ronank.weather;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.ContentResolver;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.ronank.weather.R;
import com.ronank.weather.db.MyLocalWeatherContentProvider;
import com.ronank.weather.db.TableForecast;

/**
 * Main (and only) Activity for this app
 * 
 * @author rkelly
 *
 */
public class Main extends Activity
{
    private final String TAG = getClass().getSimpleName();
    
    public static final String ACCOUNT_TYPE = "com.ronank.weatherapp";
    public static final String ACCOUNT_NAME = "My Local Weather";
    private Account connectedAccount;
    private AccountManager accountManager;
    
    private UpdateUITask updateTask = null;
    
    protected TextView dateTime;
    protected TextView weatherfrom;
    protected TextView maintext;
    protected TextView description;
    protected TextView temperature;
    protected TextView curlocation;
    protected ImageView thumbnail_image;
    
    // Main app icon is App gadu Icon - 
    // http://www.iconarchive.com/show/crystal-clear-icons-by-everaldo/App-gadu-icon.html
    
    // Forecast Icons are from the free Weezle Icon Pack by d3stroy - 
    // http://d3stroy.deviantart.com/art/Weezle-Weather-Icons-187306753
    
    // ContentObserver for our forecast db
    ContentObserver forecastObserver = new ContentObserver(null)
    {
        @Override
        public void onChange(boolean selfChange)
        {
            callUpdateUITask();
        }
    };
    
    private void callUpdateUITask()
    {
        if (updateTask != null)
        {
            updateTask.setActivity(this);
            if (updateTask.isFinished())
            {
                updateTask.updateUI();
                updateTask = null;
            }
        }
        else
        {
            updateTask = new UpdateUITask();
            updateTask.setActivity(this);
            updateTask.setContentResolver(getContentResolver());
            updateTask.execute();
        }
    }

    // To get around issues using AsyncTask in an Activity, I'm
    // using the pattern mentioned in http://www.shanekirk.com/2012/04/asynctask-missteps/ 
    static class UpdateUITask extends AsyncTask<Void, Void, Cursor>
    {
        private ContentResolver resolver = null;
        private Main activity = null;
        private boolean isFinished  = false;
        private Cursor cur= null;
        
        // HashMap provides our mapping from OpenWeather icons to these icons
        private static final HashMap<String, Integer> iconMappings = new HashMap<String, Integer>();
        static
        {
            iconMappings.put("01d", R.drawable.weezle_sun);
            iconMappings.put("01n", R.drawable.weezle_fullmoon);
            iconMappings.put("02d", R.drawable.weezle_cloud_sun);
            iconMappings.put("02n", R.drawable.weezle_moon_cloud);
            iconMappings.put("03d", R.drawable.weezle_medium_cloud);
            iconMappings.put("03n", R.drawable.weezle_moon_cloud_medium);
            iconMappings.put("04d", R.drawable.weezle_max_cloud);
            iconMappings.put("04n", R.drawable.weezle_moon_cloud_medium);
            iconMappings.put("09d", R.drawable.weezle_medium_rain);
            iconMappings.put("09n", R.drawable.weezle_night_rain);
            iconMappings.put("10d", R.drawable.weezle_medium_rain);
            iconMappings.put("10n", R.drawable.weezle_medium_rain);
            iconMappings.put("11d", R.drawable.weezle_cloud_thunder_rain);
            iconMappings.put("11n", R.drawable.weezle_night_thunder_rain);
            iconMappings.put("13d", R.drawable.weezle_much_snow);
            iconMappings.put("13n", R.drawable.weezle_night_and_snow);
            iconMappings.put("50d", R.drawable.weezle_fog);
            iconMappings.put("50n", R.drawable.weezle_night_fog);
        }
        
        @Override
        protected Cursor doInBackground(Void... params)
        {
            // Query the table and get the Cursor
            if (resolver != null)
                return resolver.query(MyLocalWeatherContentProvider.CONTENT_URI_FORECAST, null, null, null, null);

            return null;
        }
 
        @Override
        protected void onPostExecute(Cursor result)
        {
            cur = result;
            isFinished = true;
            updateUI();
        }
        
        public void setContentResolver(ContentResolver value)
        {
            resolver = value;
        }
        
        public void setActivity(Main value)
        {
            activity = value;
        }
        
        public boolean isFinished()
        {
            return isFinished;
        }
 
        // Update the UI with the values retrieved from the db
        public void updateUI()
        {
            // If there's nothing there...
            if (cur == null || !cur.moveToFirst())
                return;

            if (activity != null)
            {
                try
                {
                    activity.dateTime.setText(convertTime(cur.getLong(cur.getColumnIndex(TableForecast.KEY_CUR_DT))));
                    activity.weatherfrom.setText(cur.getString(cur.getColumnIndex(TableForecast.KEY_NAME)));
                    activity.maintext.setText(cur.getString(cur.getColumnIndex(TableForecast.KEY_MAIN_TEXT)));
                    activity.description.setText(cur.getString(cur.getColumnIndex(TableForecast.KEY_DESCRIPTION)));
                    activity.temperature.setText(cur.getString(cur.getColumnIndex(TableForecast.KEY_TEMP)) + "°C");
                    activity.curlocation.setText(cur.getString(cur.getColumnIndex(TableForecast.KEY_ADDRESS)));
                    activity.thumbnail_image.setImageResource(getIconResourceId(cur.getString(cur.getColumnIndex(TableForecast.KEY_ICON))));
                }
                finally
                {
                    if (cur != null)
                    {
                        cur.close();
                        cur = null;
                    }
                }
            }            
        }
        
        private String convertTime(long time)
        {
            Date date = new Date(time * 1000);
            Format format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.ENGLISH);

            return format.format(date).toString();
        }
        
        private int getIconResourceId(String icon_id)
        {
            // We need to map the icon_id returned by the open weather api to the icons we've got pre installed.
            
            if (iconMappings.containsKey(icon_id))
                return iconMappings.get(icon_id);
            
            return R.drawable.ic_launcher;
        }
    }
   
    @Override
    protected void onResume()
    {
        super.onResume();
        
        // Register our observer on the Forecast table
        getContentResolver().registerContentObserver(MyLocalWeatherContentProvider.CONTENT_URI_FORECAST, true, forecastObserver);
    }
    
    @Override
    public Object onRetainNonConfigurationInstance() 
    {
        return updateTask;
    }

    @Override
    protected void onPause()
    {
        // Unregister our observer on the Forecast table
        getContentResolver().unregisterContentObserver(forecastObserver);

        super.onStop();
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);
        accountManager = AccountManager.get(this);

        // Create an account if needed.
        checkAccountCreateIfNeeded();
        
        // Find views and assign them to member variables.
        dateTime = (TextView) findViewById(R.id.date_time);
        weatherfrom = (TextView) findViewById(R.id.weatherfrom);
        maintext = (TextView) findViewById(R.id.maintext);
        description = (TextView) findViewById(R.id.description);
        temperature = (TextView) findViewById(R.id.temperature);
        curlocation = (TextView) findViewById(R.id.curlocation);
        thumbnail_image = (ImageView) findViewById(R.id.thumbnail_image);

        findViewById(R.id.btnUpdate).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Bundle bundle = new Bundle();
                
                bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true); 
                bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);

                ContentResolver.requestSync(connectedAccount, MyLocalWeatherContentProvider.AUTHORITY, bundle);
            }
        });
        
        updateTask = (UpdateUITask) getLastNonConfigurationInstance();
        callUpdateUITask();
    }

    private void checkAccountCreateIfNeeded()
    {
        // Check to see if there are any accounts for this ACCOUNT_TYPE already
        Account availableAccounts[] = accountManager.getAccountsByType(ACCOUNT_TYPE);
        
        if (availableAccounts.length == 0)
        {
            // None available, so create a new one
            connectedAccount = new Account(ACCOUNT_NAME, ACCOUNT_TYPE);
            
            if (!accountExists() && !accountManager.addAccountExplicitly(connectedAccount, null, null))
            {
                Log.e(TAG, "Failed to explicitly add weather account " + ACCOUNT_NAME);
                return;
            }

            Log.i(TAG, "Successfully added weather account " + connectedAccount.name);
        }
        else
        {
            // If there's more than one, use the first one found.
            connectedAccount = availableAccounts[0];
        }

        // Setting the syncable state of the sync adapter
        ContentResolver.setIsSyncable(connectedAccount, MyLocalWeatherContentProvider.AUTHORITY, 1);
        
        // Setting the autosync state of the sync adapter
        ContentResolver.setSyncAutomatically(connectedAccount, MyLocalWeatherContentProvider.AUTHORITY, true);
    }
    
    private boolean accountExists()
    {
        Account[] accounts = accountManager.getAccountsByType(ACCOUNT_TYPE);

        for (Account account : accounts)
        {
            if ((account.name).equals(connectedAccount.name))
            {
                // account exists
                return true;
            }
        }

        return false;
    }
}
