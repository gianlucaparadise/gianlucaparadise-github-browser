package com.gianlucaparadise.githubbrowser.data

import com.google.gson.annotations.SerializedName

data class AccessTokenModel(
    @SerializedName("access_token") val accessToken: String?,
    @SerializedName("token_type") val tokenType: String?,
    @SerializedName("scope") val scope: String?
)