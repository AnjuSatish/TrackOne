package com.example.trackone

import Weather
import WeatherResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface weatherMapApi {
    @GET("weather")
   fun getCurrentWeather(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("APPID") apiKey: String
    ): Call<WeatherResponse>
}