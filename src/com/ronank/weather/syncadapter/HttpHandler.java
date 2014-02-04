package com.ronank.weather.syncadapter;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.util.Log;

/**
 * Basic Http Handler which does a GET on a URL
 * 
 * @author rkelly
 *
 */
public class HttpHandler
{
    private final String TAG = getClass().getSimpleName();

    private DefaultHttpClient client = new DefaultHttpClient();
    
    public String doHttpGET(String url)
    {
        HttpGet get = new HttpGet(url);

        try
        {
            HttpResponse getResponse = client.execute(get);
            HttpEntity getResponseEntity = getResponse.getEntity();

            if (getResponseEntity != null)
            {
                String response = EntityUtils.toString(getResponseEntity);
                Log.d(TAG, response);
                return response;
            }
        }
        catch (IOException e)
        {
            Log.e(TAG, "Error retrieving url " + url, e);
        }

        return null;

    }
}
