package com.github.jnuutinen.functional.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import androidx.room.Update
import com.github.jnuutinen.functional.data.db.entity.Task

@Dao
interface TaskDao {
    @Query("SELECT * FROM todo ORDER BY todo_date")
    fun getAll(): LiveData<List<Task>>

    @Query("SELECT * FROM todo WHERE todo_group_id = :listId ORDER BY todo_date")
    fun getAllInList(listId: Int): LiveData<List<Task>>

    @Insert(onConflict = REPLACE)
    fun insert(task: Task)

    @Insert(onConflict = REPLACE)
    fun insertAll(tasks: List<Task>)

    @Query("DELETE FROM todo")
    fun deleteAll()

    @Delete
    fun delete(task: Task)

    @Update
    fun updateAll(tasks: List<Task>)
}
