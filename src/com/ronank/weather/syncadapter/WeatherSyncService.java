package com.ronank.weather.syncadapter;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class WeatherSyncService extends Service
{
    // Object to use as a thread-safe lock
    private static final Object syncAdapterLock = new Object();

    // Storage for an instance of the sync adapter
    private static WeatherSyncAdapter syncAdapter = null;

    // Instantiate the sync adapter object.
    @Override
    public void onCreate()
    {
        // Create the sync adapter as a singleton. Set the sync adapter as
        // syncable Disallow parallel syncs
        synchronized (syncAdapterLock)
        {
            if (syncAdapter == null)
                syncAdapter = new WeatherSyncAdapter(getApplicationContext(), true);
        }
    }

    // Return an object that allows the system to invoke the sync adapter.
    @Override
    public IBinder onBind(Intent intent)
    {
        // Get the object that allows external processes to call onPerformSync().
        // The object is created in the base class code when the SyncAdapter
        // constructors call super()
        return syncAdapter.getSyncAdapterBinder();
    }
}
