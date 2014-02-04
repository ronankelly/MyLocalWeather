package com.ronank.weather.broadcast;

import com.ronank.weather.authentication.WeatherAuthenticatorService;
import com.ronank.weather.syncadapter.WeatherSyncService;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Broadcast Receiver which starts our services on boot complete.
 * 
 * @author rkelly
 *
 */
public class SyncBroadcastReceiver extends BroadcastReceiver
{
    private final String TAG = getClass().getSimpleName();
    
    @Override
    public void onReceive(Context context, Intent intent)
    {
        // we are booting up, start the service
        if("android.intent.action.BOOT_COMPLETED".equals(intent.getAction()))
            startSyncServices(context, intent);        
    }

    /**
     * @param context The context to use to start the service
     * @param intent The intent that triggered the services to be started
     */
    private void startSyncServices(Context context, Intent intent)
    {
        ComponentName authName = new ComponentName(context.getPackageName(), WeatherAuthenticatorService.class.getName());
        ComponentName authService = context.startService(new Intent().setComponent(authName));
        if (authService == null)
        {
            Log.e(TAG, "SyncBroadcastReceiver: Could not start weather authentication service " + authName.toString());
        }
        
        ComponentName syncName = new ComponentName(context.getPackageName(), WeatherSyncService.class.getName());
        ComponentName syncService = context.startService(new Intent().setComponent(syncName));
        if (syncService == null)
        {
            Log.e(TAG, "SyncBroadcastReceiver: Could not start weather sync adapter service " + syncName.toString());
        }
    }
}
