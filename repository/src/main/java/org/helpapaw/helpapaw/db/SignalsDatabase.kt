package org.helpapaw.helpapaw.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import org.helpapaw.helpapaw.models.Signal

@Database(entities = [Signal::class], version = 1)
abstract class SignalsDatabase : RoomDatabase() {

    abstract fun signalDao(): SignalDao

    companion object {
        private var INSTANCE: SignalsDatabase? = null

        fun getDatabase(context: Context): SignalsDatabase? {
            if (INSTANCE == null) {
                // allowMainThreadQueries should not be used, it is added so the query can be executed in
                // the main thread, now we know that it is quick enough and works for now, but should be refactored!!!
                synchronized(SignalsDatabase::class) {
                    INSTANCE = Room.databaseBuilder(context, SignalsDatabase::class.java, "signals_db").allowMainThreadQueries().build()
                }
            }
            return INSTANCE
        }

        fun destroyInstance() {
            INSTANCE = null
        }
    }
}
