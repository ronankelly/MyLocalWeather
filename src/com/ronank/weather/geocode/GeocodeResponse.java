package com.ronank.weather.geocode;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * GSON classes for JSON response from Google Geocode API call
 * This class only contains a subset of the possible data returned.
 * 
 * @author rkelly
 *
 */
public class GeocodeResponse
{
    @SerializedName("status")
    private String status;
    
    @SerializedName("results")
    private List<Result> results = new ArrayList<Result>();

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public void setResults(List<Result> results)
    {
        this.results = results;
    }

    public List<Result> getResults()
    {
        return results;
    }
}
