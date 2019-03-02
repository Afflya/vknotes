package com.afflyas.vknotes.vo

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import java.sql.Date
import java.text.SimpleDateFormat
import java.util.*

/**
 * Object that contains general information about message from VK
 *
 * Implemented Parcelable to be able to put objects into Bundle and pass it
 * as argument when navigating
 *
 */
class VKNote : Parcelable {

    @SerializedName("id") val id: Long

    @SerializedName("date") val date: Long

    @SerializedName("text") var text: String?

    constructor(parcel: Parcel) {
        id = parcel.readLong()
        date = parcel.readLong()
        text = parcel.readString()
    }

    constructor(id: Long, date: Long, text: String?) {
        this.id = id
        this.date = date
        this.text = text
    }

    /**
     * Convert unix date time to string
     */
    fun getDateString(): String{

        val date = Date(this.date * 1000)

        val df = SimpleDateFormat("dd MMMM yyyy, HH:mm:ss", Locale.getDefault())

        return df.format(date)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeLong(date)
        parcel.writeString(text)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<VKNote> {
        override fun createFromParcel(parcel: Parcel): VKNote {
            return VKNote(parcel)
        }

        override fun newArray(size: Int): Array<VKNote?> {
            return arrayOfNulls(size)
        }
    }

}