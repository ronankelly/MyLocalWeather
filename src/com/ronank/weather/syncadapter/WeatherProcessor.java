package com.ronank.weather.syncadapter;

import java.util.Date;
import java.util.Locale;

import com.google.gson.Gson;
import com.ronank.weather.db.MyLocalWeatherContentProvider;
import com.ronank.weather.db.TableForecast;
import com.ronank.weather.geocode.GeocodeResponse;
import com.ronank.weather.openweather.dataapi.ForecastResponse;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.location.Location;
import android.util.Log;

/**
 * Handle the REST API calls between the two servers and parses and stores responses
 * 
 * @author rkelly
 */
public class WeatherProcessor extends Thread
{
    private final String TAG = getClass().getSimpleName();
    
    private Location currentLocation;
    private ContentResolver resolver;
    private String address;
    
    private static final String GOOGLE_GEOCODE_API_URL = "http://maps.googleapis.com/maps/api/geocode/json?latlng=%1$f,%2$f&sensor=true&language=";
    private static final String OPENWEATHER_FORECAST_API_URL = "http://api.openweathermap.org/data/2.5/weather?lat=%1$f&lon=%2$f&units=metric";
    
    public WeatherProcessor(ContentResolver res, Location loc)
    {
        resolver = res;
        currentLocation = loc;
        address = "Current Location";
    }
    
    @Override
    public void run()
    {
        // Do a reverse geocode lookup on the coordinates to get the current location address
        getAddress();
        
        // Get forecast from OpenWeather based on the coordinates
        getForecast();        
    }
    
    private void getAddress()
    {
        // Geocoder seems flakey on some versions of Android
        // (there's a bug raised about JellyBean versions see http://code.google.com/p/android/issues/detail?id=38009)
        // As I'm using a JellyBean phone I'm just using Google maps to get the address.
        
        String url = String.format(Locale.ENGLISH, GOOGLE_GEOCODE_API_URL + Locale.getDefault().getCountry(),
                currentLocation.getLatitude(), currentLocation.getLongitude());
        
        Log.d(TAG, "getAddress - url : " + url);
        
        HttpHandler httpHandler = new HttpHandler();
        String response = httpHandler.doHttpGET(url);
        
        GeocodeResponse gsonRsp = new Gson().fromJson(response, GeocodeResponse.class);
        
        if (gsonRsp != null && gsonRsp.getResults() != null && gsonRsp.getResults().size() > 0)
        {
            address =  gsonRsp.getResults().get(0).getFormatted_address();
        }

        Log.e(TAG, "Unable to parse Reverse Geocode JSON response");
    }
    
    private void getForecast()
    {
        String url = String.format(Locale.ENGLISH, OPENWEATHER_FORECAST_API_URL, currentLocation.getLatitude(),
                currentLocation.getLongitude());
        
        Log.d(TAG, "getForecast - url : " + url);
        
        HttpHandler httpHandler = new HttpHandler();
        String response = httpHandler.doHttpGET(url);
        
        if (response != null && response.length() > 0)
        {
            ForecastResponse gsonRsp = new Gson().fromJson(response, ForecastResponse.class);
            if (gsonRsp == null)
            {
                Log.e(TAG, "Unable to parse OpenWeather JSON response");
                
                return;
            }
            
            // Write the forecast to the db for later retrieval
            ContentValues values = new ContentValues();
            
            values.put(TableForecast.KEY_NAME, gsonRsp.getName());
            values.put(TableForecast.KEY_ADDRESS, address);
            values.put(TableForecast.KEY_TEMP, gsonRsp.getMain().getTemp());
            values.put(TableForecast.KEY_MAIN_TEXT, gsonRsp.getWeather().get(0).getMain());
            values.put(TableForecast.KEY_DESCRIPTION, gsonRsp.getWeather().get(0).getDescription());
            values.put(TableForecast.KEY_ICON, gsonRsp.getWeather().get(0).getIcon());
            values.put(TableForecast.KEY_CUR_DT, new Date().getTime() / 1000L);
            values.put(TableForecast.KEY_FORECAST_DT, gsonRsp.getDt());
            
            resolver.insert(MyLocalWeatherContentProvider.CONTENT_URI_FORECAST, values);

        }
    }
}
