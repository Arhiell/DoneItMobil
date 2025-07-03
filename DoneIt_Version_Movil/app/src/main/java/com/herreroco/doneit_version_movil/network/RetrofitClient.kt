package com.herreroco.doneit_version_movil.network

import android.content.Context
import com.herreroco.doneit_version_movil.util.SessionManager
import okhttp3.OkHttpClient
import okhttp3.Interceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "http://192.168.1.12:5212/"

    fun getApiService(context: Context): ApiService {
        val sessionManager = SessionManager(context)

        val authInterceptor = Interceptor { chain ->
            val originalRequest = chain.request()
            val token = sessionManager.fetchAuthToken()

            val requestBuilder = originalRequest.newBuilder()
            token?.let {
                requestBuilder.addHeader("Authorization", "Bearer $it")
            }

            val requestWithHeaders = requestBuilder.build()
            chain.proceed(requestWithHeaders)
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(ApiService::class.java)
    }
}
