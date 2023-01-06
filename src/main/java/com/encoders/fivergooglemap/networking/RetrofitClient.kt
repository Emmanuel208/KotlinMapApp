package com.example.mvvmkotlinexample.retrofit

import com.encoders.fivergooglemap.BuildConfig
import com.encoders.myapplication.Networking.APIs.Companion.BASE_URL
import com.google.gson.GsonBuilder
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit

object RetrofitClient {
    val retrofitClient: Retrofit.Builder by lazy {

        val levelType: Level = if (BuildConfig.BUILD_TYPE.contentEquals("debug"))
            Level.BODY else Level.NONE

        val logging = HttpLoggingInterceptor()
        logging.setLevel(levelType)

        var logInter = HttpLoggingInterceptor()
        val client = OkHttpClient.Builder().apply {
            this.addInterceptor(logging)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)

        }.build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
    }

    val apiInterface: ApiInterface by lazy {
        retrofitClient
            .build()
            .create(ApiInterface::class.java)
    }

    val interceptor = HttpLoggingInterceptor().apply {

        this.level = HttpLoggingInterceptor.Level.BODY

    }

    var logInter = HttpLoggingInterceptor()

    val token_client = OkHttpClient.Builder().apply {
        this.addInterceptor(HeaderInterceptor())
            .addNetworkInterceptor(interceptor)
            .addNetworkInterceptor(logInter)
            .protocols(Arrays.asList(Protocol.HTTP_1_1))
            .connectTimeout(180, TimeUnit.SECONDS)
            .readTimeout(180, TimeUnit.SECONDS)
            .writeTimeout(180, TimeUnit.SECONDS)

    }.build()

    val apiInterface_Header: ApiInterface by lazy {

        retrofitClient
            .baseUrl(BASE_URL)
            .client(token_client)
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
            .build()
            .create(ApiInterface::class.java)
    }


    class HeaderInterceptor : Interceptor {
        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response {
            val request: Request = chain.request()
                .newBuilder()
                .addHeader("Authorization", "Bearer ")
                .build()
            return chain.proceed(request)
        }
    }

}
