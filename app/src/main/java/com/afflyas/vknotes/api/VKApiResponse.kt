package com.afflyas.vknotes.api

import com.google.gson.annotations.SerializedName

/**
 * Response data returned by Api for requests without paging
 */
data class SolidApiResponse<T>(
        @SerializedName("response") val response: List<T>
)

/**
 * Response data returned by Api for requests that support paging
 */
data class PagingApiResponse<T>(
        @SerializedName("response") val response: NotesApiResponse<T>
)

/**
 * response data of the PagingApiResponse<>
 *
 * contains list of items for this page and total number of items
 */
data class NotesApiResponse<T>(
        @SerializedName("count") val count: Int,
        @SerializedName("items") val items: List<T>
)