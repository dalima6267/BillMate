package com.example.billmate.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Delete
import androidx.room.Update

@Dao
interface BillDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBill(bill: Bill)

    @Query("SELECT * FROM bills")
    suspend fun getAllBills(): List<Bill>

    // Method to delete a specific bill
    @Delete
    suspend fun delete(bill: Bill)

    // Method to delete multiple bills


    // Method to update a bill
    @Update
    suspend fun updateBill(bill: Bill)
}
