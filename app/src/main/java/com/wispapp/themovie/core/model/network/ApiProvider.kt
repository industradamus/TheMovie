package com.wispapp.themovie.core.model.network

import retrofit2.Retrofit

class ApiProvider(private val retrofit: Retrofit) {
    fun provide(): ApiInterface = retrofit.create(ApiInterface::class.java)
}