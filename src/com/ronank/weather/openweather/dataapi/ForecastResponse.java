package com.ronank.weather.openweather.dataapi;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * GSON classes for JSON response from OpenWeather API call
 * This class only contains a subset of the possible data returned.
 * 
 * @author rkelly
 *
 */
public class ForecastResponse
{
    @SerializedName("name")
    private String name;
    
    @SerializedName("main")
    private Main main;
    
    @SerializedName("dt")
    private long dt;
    
    @SerializedName("weather")
    private List<Weather> weather = new ArrayList<Weather>();

    public void setName(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }
    
    public void setMain(Main main)
    {
        this.main = main;
    }

    public Main getMain()
    {
        return main;
    }
    
    public void setDt(long dt)
    {
        this.dt = dt;
    }

    public long getDt()
    {
        return dt;
    }
    
    public void setWeather(List<Weather> weather)
    {
        this.weather = weather;
    }

    public List<Weather> getWeather()
    {
        return weather;
    }
}
