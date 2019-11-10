package com.wispapp.themovie.core.model.network.models.configs

import com.google.gson.annotations.SerializedName

data class ConfigResponse(
    @SerializedName("imagesConfig") val images: Images,
    @SerializedName("change_keys") val changeKeys: List<String>
)
