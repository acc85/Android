package org.helpapaw.helpapaw.db

import android.content.Context
import androidx.room.*
import org.helpapaw.helpapaw.data.models.Signal

@Database(entities = arrayOf(Signal::class), version = 1)
abstract class SignalsDatabase: RoomDatabase() {

    companion object {
        private var INSTANCE: SignalsDatabase? = null
        fun getDatabase(context: Context): SignalsDatabase {
            if (INSTANCE == null) {
                // allowMainThreadQueries should not be used, it is added so the query can be executed in
                // the main thread, now we know that it is quick enough and works for now, but should be refactored!!!
                INSTANCE = Room.databaseBuilder(context, SignalsDatabase::class.java, "signals.db").allowMainThreadQueries().build()
            }
            return INSTANCE!!
        }
    }

    abstract fun signalDao(): SignalDao



    fun destroyInstance() {
        INSTANCE = null
    }

}