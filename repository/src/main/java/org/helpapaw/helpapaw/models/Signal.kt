package org.helpapaw.helpapaw.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import android.os.Parcelable
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.helpapaw.helpapaw.repository.BR
import kotlinx.android.parcel.Parcelize

import java.util.Date

/**
 * Created by iliyan on 7/28/16
 * edit: Alex-11.11.17
 */

const val KEY_FOCUSED_SIGNAL_ID = "FocusSignalId"

const val SOLVED = 2

const val SOMEBODY_ON_THE_WAY = 1

const val HELP_IS_NEEDED = 0

@Parcelize
@Entity(tableName = "signals")
data class Signal(
        @PrimaryKey
        @ColumnInfo(name = "signal_id")
        var id: String = "",
        @ColumnInfo(name = "title")
        var title: String? = null,
        @Ignore
        var dateSubmitted: Date? = Date(),
        @ColumnInfo(name = "authorName")
        var authorName: String? = null,
        @ColumnInfo(name = "authorPhone")
        var authorPhone: String? = null,
        @ColumnInfo(name = "photoUrl")
        var photoUrl: String? = null,
        @ColumnInfo(name = "status")
        var status: Int = 0,
        @ColumnInfo(name = "latitude")
        var latitude: Double = 0.toDouble(),
        @ColumnInfo(name = "longitude")
        var longitude: Double = 0.toDouble(),
        @ColumnInfo(name = "seen")
        var seen: Boolean = false
) : Parcelable, BaseObservable() {

    var _title: String?
        @Bindable get() = title
        set(_) {
            title = _title
            notifyPropertyChanged(BR._title)
        }

    var _status : Int
        @Bindable get() = status
        set(value){
            status = _status
            notifyPropertyChanged(BR._status)
        }

    var _photoUri: String?
        @Bindable get() = photoUrl
        set(_){
            photoUrl = _photoUri
            notifyPropertyChanged(BR._photoUri)
        }

}



