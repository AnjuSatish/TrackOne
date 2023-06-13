package com.example.trackone.userDatas

import WeatherResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface weatherMapApi {
    @GET("data/2.5/weather")
   fun getCurrentWeather(

        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("APPID") apiKey: String
    ): Call<WeatherResponse>
}