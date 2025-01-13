package com.example.billmate.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.billmate.models.Group

@Dao
interface GroupDao {

    // Insert a new group into the database
    @Insert
    suspend fun insertGroup(group: Group)

    // Get all groups from the database
    @Query("SELECT * FROM group_table")
    suspend fun getAllGroups(): List<Group>

    // Delete a specific group from the database
    @Delete
    suspend fun deleteGroup(group: Group)
}
