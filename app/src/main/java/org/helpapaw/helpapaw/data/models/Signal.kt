@file:JvmName("Signal")

package org.helpapaw.helpapaw.data.models

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import java.util.*

@Entity(tableName = "signals")
@Parcelize
data class Signal(
        @PrimaryKey
        @ColumnInfo(name = "signal_id")
        var id: String = "",
        @ColumnInfo(name = "title")
        var title: String = "",
        @Ignore
        var dateSubmitted: Date = Date(),
        @ColumnInfo(name = "authorName")
        var authorName: String? = "",
        @ColumnInfo(name = "authorPhone")
        var authorPhone: String? = "",
        @ColumnInfo(name = "photoUrl")
        var photoUrl: String = "",
        @ColumnInfo(name = "status")
        var status: Int = 0,
        @ColumnInfo(name = "latitude")
        var latitude: Double = 0.toDouble(),
        @ColumnInfo(name = "longitude")
        var longitude: Double = 0.toDouble(),
        @ColumnInfo(name = "seen")
        var seen: Boolean = false) : Parcelable {

    companion object {
        @Ignore
        const val KEY_FOCUSED_SIGNAL_ID = "FocusSignalId"
        @Ignore
        const val SOLVED = 2
        @Ignore
        const val SOMEBODY_ON_THE_WAY = 1
        @Ignore
        const val HELP_IS_NEEDED = 0
    }


    @Ignore
    constructor(id: String,
                title: String,
                dateSubmitted: Date,
                status: Int,
                authorName: String,
                authorPhone: String,
                latitude: Double,
                longitude: Double,
                seen: Boolean) : this(title, dateSubmitted, status, authorName, authorPhone, latitude, longitude) {
        this.id = id
        this.seen = seen
    }

    @Ignore
    constructor (title: String,
                 dateSubmitted: Date,
                 status: Int,
                 authorName: String,
                 authorPhone: String,
                 latitude: Double,
                 longitude: Double) :
            this(title, dateSubmitted, status, latitude, longitude) {
        this.authorName = authorName
        this.authorPhone = authorPhone
    }


    @Ignore
    constructor(title: String,
                dateSubmitted: Date,
                status: Int,
                latitude: Double,
                longitude: Double) :
            this("", title = title, dateSubmitted = dateSubmitted, status = status, latitude = latitude, longitude = longitude)


}