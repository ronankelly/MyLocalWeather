package com.ronank.weather.geocode;

import com.google.gson.annotations.SerializedName;

public class Result
{
    @SerializedName("formatted_address")
    private String formatted_address;

    public void setFormatted_address(String formatted_address) {
        this.formatted_address = formatted_address;
    }

    public String getFormatted_address() {
        return formatted_address;
    }
}
