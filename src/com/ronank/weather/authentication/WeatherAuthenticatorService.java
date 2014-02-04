package com.ronank.weather.authentication;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * The main authenticator service which facilitates integration with the Android
 * Account Manager framework.
 * 
 * @author rkelly
 */
public class WeatherAuthenticatorService extends Service
{
    private final String TAG = getClass().getSimpleName();

    private WeatherAuthenticator auth; // the authenticator used to manage Weather app account

    /** Creates a new authenticator service. */
    public WeatherAuthenticatorService()
    {
        super();

        auth = null;
    }

    /** Performs one-time initialisation of the authenticator service. */
    @Override
    public void onCreate()
    {
        super.onCreate();

        if (auth == null)
            auth = new WeatherAuthenticator(this);
    }

    /** Destroys the authenticator service, releasing any associated resources. */
    @Override
    public void onDestroy()
    {
        super.onDestroy();

        auth = null;
    }

    /**
     * Returns the actual authenticator that will be used to manage Mozu
     * accounts.
     */
    @Override
    public IBinder onBind(Intent intent)
    {
        if (!intent.getAction().equals(android.accounts.AccountManager.ACTION_AUTHENTICATOR_INTENT))
        {
            Log.e(TAG, "Unrecognised bind action: " + intent.getAction());
            return null;
        }

        if (auth == null)
        {
            Log.e(TAG, "Authenticator doesn't exist - client can't bind");
            return null;
        }

        // bind the client to the authenticator
        IBinder result = auth.getIBinder();
        return result;
    }
}
