package br.com.igorbag.githubsearch.domain

import com.google.gson.annotations.SerializedName

data class Repository(

    @SerializedName("name")
    val name: String,

    @SerializedName("html_url")
    val htmlUrl: String
)