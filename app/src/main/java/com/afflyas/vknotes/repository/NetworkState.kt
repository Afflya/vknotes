package com.afflyas.vknotes.repository

enum class Status {
    RUNNING,
    SUCCESS,
    FAILED
}

/**
 *
 * Contains status of request
 * and error message (when request failed)
 *
 */
@Suppress("DataClassPrivateConstructor")
data class NetworkState private constructor(
        val status: Status,
        val msg: String? = null) {
    companion object {
        val LOADED = NetworkState(Status.SUCCESS)
        val LOADING = NetworkState(Status.RUNNING)
        fun error(msg: String?) = NetworkState(Status.FAILED, msg)
    }
}