package com.example.printfulsockettest.network

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import kotlinx.coroutines.Deferred
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import retrofit2.http.QueryMap

private const val BASE_URL = "http://213.175.74.120/"

/**
 * Use the Retrofit builder to build a retrofit object using a Moshi converter with our Moshi
 * object.
 */
private val retrofit = Retrofit.Builder()
        .addConverterFactory(ScalarsConverterFactory.create())
        .addCallAdapterFactory(CoroutineCallAdapterFactory())
        .baseUrl(BASE_URL)
        .build()

interface PrintfulAPIService{

    @GET(" ")
    fun getLocationOfUsersAsync(@QueryMap params:Map<String, String>):
            Deferred<String>
}

/**
 * A public Api object that exposes the lazy-initialized Retrofit service
 */
object PrintfulAPI{
    val retrofitService: PrintfulAPIService by lazy {
        retrofit.create(PrintfulAPIService::class.java)
    }
}

