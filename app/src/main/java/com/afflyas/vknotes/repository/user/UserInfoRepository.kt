package com.afflyas.vknotes.repository.user

import androidx.lifecycle.MutableLiveData
import com.afflyas.vknotes.api.SolidApiResponse
import com.afflyas.vknotes.api.VKApiService
import com.afflyas.vknotes.core.App
import com.afflyas.vknotes.repository.NetworkState
import com.afflyas.vknotes.repository.SolidRepoResponse
import com.afflyas.vknotes.vo.VKUser
import com.vk.sdk.VKAccessToken
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

/**
 *
 * Repository that handles user info.
 *
 */
class UserInfoRepository @Inject constructor(private val apiService: VKApiService){


    val userInfo = MutableLiveData<VKUser>()
    val networkState = MutableLiveData<NetworkState>()

    fun loadUserInfo(): SolidRepoResponse<VKUser> {

        reload()

        return SolidRepoResponse<VKUser>(
                userInfo = userInfo,
                networkState = networkState,
                reload = {
                    reload()
                }
        )
    }

    private fun reload(){
        networkState.postValue(NetworkState.LOADING)

        val request = apiService.getProfileInfo(
                VKAccessToken.currentToken().accessToken
        )

        request.enqueue(object : Callback<SolidApiResponse<VKUser>>{

            override fun onResponse(call: Call<SolidApiResponse<VKUser>>, response: Response<SolidApiResponse<VKUser>>) {
                userInfo.postValue(response.body()?.response?.get(0))

                if (response.isSuccessful) {
                    networkState.postValue(NetworkState.LOADED)
                }else{
                    networkState.postValue(
                            NetworkState.error("error code: ${response.code()}"))
                }
            }

            override fun onFailure(call: Call<SolidApiResponse<VKUser>>?, t: Throwable?) {
                networkState.postValue(NetworkState.error(t?.message
                        ?: "unknown error"))
            }
        })
    }

}