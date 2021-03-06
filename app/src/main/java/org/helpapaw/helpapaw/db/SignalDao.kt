package org.helpapaw.helpapaw.db

import androidx.room.*
import org.helpapaw.helpapaw.data.models.Signal

@Dao
interface SignalDao{

    @Query("SELECT * FROM signals")
    fun getAll(): List<Signal>

    @Query("SELECT * FROM signals where signal_id = :signal_id")
    fun getSignal(signal_id: String): List<Signal>

    @Query("SELECT * FROM signals WHERE signal_id IN (:signal_ids)")
    fun getSignals(signal_ids: Array<String>): List<Signal>

    @Insert
    fun insertAll(vararg signals: Signal)

    @Delete
    fun delete(signal: Signal)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveSignal(signal: Signal)
}