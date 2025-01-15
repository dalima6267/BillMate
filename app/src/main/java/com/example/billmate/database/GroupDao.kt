package com.example.billmate.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.billmate.models.Group

@Dao
interface GroupDao {

    // Insert a new group into the database
    @Insert
    suspend fun insertGroup(group: Group)

    // Get all groups from the database
    @Query("SELECT * FROM group_table")
    suspend fun getAllGroups(): List<Group>

    // Get a specific group by its ID
    @Query("SELECT * FROM group_table WHERE id = :groupId")
    suspend fun getGroupById(groupId: Int): Group?

    // Update an existing group
    @Update
    suspend fun updateGroup(group: Group)

    // Update the total expense for a specific group
    @Query("UPDATE group_table SET totalExpense = :totalExpense WHERE id = :groupId")
    suspend fun updateGroupExpense(groupId: Int, totalExpense: Double)

    // Update the notes for a specific group
    @Query("UPDATE group_table SET notes = :notes WHERE id = :groupId")
    suspend fun updateGroupNotes(groupId: Int, notes: String)

    // Delete a specific group from the database
    @Delete
    suspend fun deleteGroup(group: Group)
    @Delete
    suspend fun deleteMultiple(groups: List<Group>)
    @Query("SELECT * FROM group_table ORDER BY " +
            "CASE WHEN :orderBy = 'name' THEN name END ASC, " +
            "CASE WHEN :orderBy = 'totalExpense' THEN totalExpense END DESC")
    suspend fun getGroupsSortedBy(orderBy: String): List<Group>
    @Query("SELECT * FROM group_table WHERE name LIKE :query")
    suspend fun searchGroups(query: String): List<Group>



    // Delete all groups from the database
    @Query("DELETE FROM group_table")
    suspend fun deleteAllGroups()
}
