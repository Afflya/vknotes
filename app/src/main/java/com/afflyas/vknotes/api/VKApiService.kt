package com.afflyas.vknotes.api

import com.afflyas.vknotes.vo.VKNote
import com.afflyas.vknotes.vo.VKUser
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface VKApiService {

    companion object {
        const val API_VERSION = "5.80"

        const val DEFAULT_PAGE_SIZE = 20

        const val BASE_URL = "https://api.vk.com/method/"
    }

    /**
     *
     * Initial load messages using pagination
     *
     * @param token - access token
     * @param count - number of items to load
     * @param userId - user's id
     * @return PagingApiResponse<VKNote> with list of VKNote
     *
     */
    @GET("messages.getHistory?v=$API_VERSION")
    fun getNotes(
            @Query("access_token") token: String,
            @Query("count") count: Int,
            @Query("user_id") userId: Long
    ): Call<PagingApiResponse<VKNote>>

    /**
     *
     * Load page of messages
     *
     * @param token - access token
     * @param count - number of items to load
     * @param userId - user's id
     * @param startMessageId - id of message to start load
     * @return PagingApiResponse<VKNote> with list of VKNote
     *
     */
    @GET("messages.getHistory?v=$API_VERSION&offset=1")
    fun getNotesAfter(
            @Query("access_token") token: String,
            @Query("count") count: Int,
            @Query("user_id") userId: Long,
            @Query("start_message_id") startMessageId: Long
    ): Call<PagingApiResponse<VKNote>>

    /**
     * load profile info of current user
     *
     * @param token - access token
     * @return SolidApiResponse<VKUser> with user's info
     */
    @GET("users.get?v=$API_VERSION&fields=photo_200")
    fun getProfileInfo(
            @Query("access_token") token: String
    ): Call<SolidApiResponse<VKUser>>

    /**
     *
     * Delete messages from VK by their id
     *
     *
     */
    @POST("messages.delete?v=$API_VERSION")
    fun deleteNotes(
            @Query("access_token") token: String,
            @Query("message_ids") messageIds: String
    ): Call<ResponseBody>

    /**
     *
     * Post new message to VK
     *
     */
    @POST("messages.send?v=$API_VERSION")
    fun createNote(
            @Query("access_token") token: String,
            @Query("message") message: String,
            @Query("user_id") userId: String

    ): Call<ResponseBody>

    /**
     *
     * Search for messages by query string with pagination
     *
     * @param token - access token
     * @param count - number of items to load
     * @param userId - current user's id
     * @param offset - offset required to retrieve a specific subset of messages
     * @param query - string to search
     * @return PagingApiResponse<VKNote> with list of VKNote
     */
    @GET("messages.search?v=$API_VERSION")
    fun searchNotes(
            @Query("access_token") token: String,
            @Query("count") count: Int,
            @Query("offset") offset: Int,
            @Query("peer_id") userId: Long,
            @Query("q") query: String
    ): Call<PagingApiResponse<VKNote>>

}