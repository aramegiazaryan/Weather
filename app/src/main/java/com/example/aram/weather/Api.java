package com.example.aram.weather;




import com.example.aram.weather.googleMapModel.modelGoogleMap;
import com.example.aram.weather.weatherModel.modelWeather;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface Api {

    @GET("/maps/api/place/autocomplete/json")
    Call<modelGoogleMap> getCities(@Query("input") String city, @Query("types") String types, @Query("key") String key);

    @GET("/data/2.5/weather")
    Call<modelWeather> getWeather(@Query("q") String city, @Query("appid") String key);
}

