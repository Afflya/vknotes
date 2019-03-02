package com.afflyas.vknotes.vo

import com.google.gson.annotations.SerializedName

/**
 * Object that contains general info about current user
 */
data class VKUser (
        @SerializedName("id") val id: Long,
        @SerializedName("first_name") val firstName: String?,
        @SerializedName("last_name") val lastName: String?,
        @SerializedName("photo_200") val photo: String?
){
    fun getFullName(): String{
        return "$firstName $lastName"
    }
}