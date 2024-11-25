package com.example.billmate.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Delete
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BillDao {

    // Insert a single bill into the database. If a bill already exists, replace it.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBill(bill: Bill)

    // Fetch all bills from the database
    @Query("SELECT * FROM bills")
    suspend fun getAllBills(): List<Bill>

    // Fetch all bills from the database as a Flow (useful for observing changes)
    @Query("SELECT * FROM bills")
    fun getAllBillsAsFlow(): Flow<List<Bill>>

    // Delete a single bill
    @Delete
    suspend fun delete(bill: Bill)

    // Delete multiple bills at once
    @Delete
    suspend fun deleteMultiple(bills: List<Bill>)

    // Update a bill's data in the database
    @Update
    suspend fun updateBill(bill: Bill)
}
