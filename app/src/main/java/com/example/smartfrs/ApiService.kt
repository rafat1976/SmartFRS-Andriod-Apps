package com.example.smartfrs

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {

    @POST("/login")
    fun loginUser(
        @Body request: LoginRequest
    ): Call<LoginResponse>

    @POST("/register")
    fun registerUser(
        @Body request: RegisterRequest
    ): Call<RegisterResponse>

    @GET("/search")
    fun searchFlights(
        @Query("from") from: String,
        @Query("to") to: String,
        @Query("date") date: String,
        @Query("class") flightClass: String
    ): Call<SearchFlightResponse>

    @POST("/book")
    fun bookFlight(
        @Body request: BookingRequest
    ): Call<BookingResponse>

    @POST("/cancel")
    fun cancelTicket(
        @Query("bookingId") bookingId: String
    ): Call<BookingResponse>

}