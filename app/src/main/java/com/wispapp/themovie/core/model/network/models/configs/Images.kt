package com.wispapp.themovie.core.model.network.models.configs

import com.google.gson.annotations.SerializedName

data class Images(
    @SerializedName("base_url") val baseUrl: String,
    @SerializedName("secure_base_url") val secureBaseUrl: String,
    @SerializedName("poster_sizes") val posterSizes: List<String>,
    @SerializedName("backdrop_sizes") val backdropSizes: List<String>,
    @SerializedName("logo_sizes") val logoSizes: List<String>,
    @SerializedName("still_sizes") val stillSizes: List<String>,
    @SerializedName("profile_sizes") val profileSizes: List<String>
)
