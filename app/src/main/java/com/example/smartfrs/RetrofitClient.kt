package com.example.smartfrs

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    // Android Emulator থেকে লোকাল পিসির Flask সার্ভার (port 5000) কানেক্ট করার লিংক
    private const val BASE_URL = "http://10.0.2.2:5000/"

    // Ngrok ব্রাউজার ওয়ার্নিং বাইপাস করার জন্য OkHttpClient যোগ করা হলো
    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("ngrok-skip-browser-warning", "true")
                .build()
            chain.proceed(request)
        }
        .build()

    val instance: ApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client) // OkHttpClient যুক্ত করা হলো
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(ApiService::class.java)
    }
}