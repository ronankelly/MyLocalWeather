package com.ronank.weather.openweather.dataapi;

import com.google.gson.annotations.SerializedName;

public class Main
{
    @SerializedName("temp")
    private double temp;
    
    public void setTemp(double temp)
    {
        this.temp = temp;
    }

    public double getTemp()
    {
        return temp;
    }
}
