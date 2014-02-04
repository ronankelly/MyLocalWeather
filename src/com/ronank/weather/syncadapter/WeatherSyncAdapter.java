package com.ronank.weather.syncadapter;

import java.util.List;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

/**
 * Handle the transfer of data between a server and an app, using the Android
 * sync adapter framework.
 * 
 * @author rkelly
 */
public class WeatherSyncAdapter extends AbstractThreadedSyncAdapter
{
    private ContentResolver resolver;
    private LocationManager locationManager;
    
    public WeatherSyncAdapter(Context context, boolean autoInitialize)
    {
        super(context, autoInitialize, false);
        
        // Acquire a reference to the ContentResolver
        resolver = context.getContentResolver();

        // Acquire a reference to the system Location Manager
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider,
            SyncResult syncResult)
    {
        // Start a new thread to get the forecast and store in db
        new WeatherProcessor(resolver, getCurrentLocation()).start();
    }
    
    private Location getCurrentLocation()
    {
        // Setup the current location with the best last known location.
        List<String> providers = locationManager.getAllProviders();

        Location currentLocation = null;
        for (String it : providers)
        {
            Location location = locationManager.getLastKnownLocation(it);
            if (location != null)
            {
                if (currentLocation == null || location.getAccuracy() < currentLocation.getAccuracy())
                {
                    currentLocation = location;
                }
            }
        }
        
        return currentLocation;
    }
}
