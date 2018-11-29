package org.helpapaw.helpapaw.db

import androidx.room.Database
import androidx.room.RoomDatabase
import org.helpapaw.helpapaw.data.models.Signal

@Database(entities = arrayOf(Signal::class), version = 1)
abstract class SignalsDatabase : RoomDatabase() {
    abstract fun signalDao(): SignalDao
}