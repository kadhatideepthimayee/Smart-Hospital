package com.example.medplus.data.network

import android.content.Context
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    // 10.0.2.2 points to localhost of host machine in Android emulator
    private const val BASE_URL = "http://127.0.0.1:5000/"

    private var retrofit: Retrofit? = null

    private fun getClient(context: Context): OkHttpClient {
        val sessionManager = SessionManager.getInstance(context)

        val authInterceptor = Interceptor { chain ->
            val originalRequest = chain.request()
            val token = sessionManager.getToken()

            val requestBuilder = originalRequest.newBuilder()
            if (token != null) {
                requestBuilder.addHeader("Authorization", "Bearer $token")
            }

            chain.proceed(requestBuilder.build())
        }

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    fun getApiService(context: Context): ApiService {
        if (retrofit == null) {
            val gson = com.google.gson.GsonBuilder()
                .registerTypeAdapter(com.google.firebase.Timestamp::class.java, TimestampTypeAdapter())
                .create()

            retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(getClient(context))
                .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create(gson))
                .build()
        }
        return retrofit!!.create(ApiService::class.java)
    }
}
