package com.github.jnuutinen.functional.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.github.jnuutinen.functional.data.db.entity.TaskList

@Dao
interface TaskListDao {
    @Transaction
    @Query("SELECT * FROM todo_group ORDER BY group_date")
    fun getAllListsWithTasks(): LiveData<List<ListWithTasks>>

    @Query("SELECT * FROM todo_group ORDER BY group_date")
    fun getAll(): LiveData<List<TaskList>>

    @Insert
    fun insert(taskList: TaskList)

    @Insert
    fun insertAll(taskLists: List<TaskList>)

    @Query("DELETE FROM todo_group")
    fun deleteAll()

    @Delete
    fun delete(taskList: TaskList)

    @Query("DELETE FROM todo_group WHERE group_id = :listId")
    fun delete(listId: Int)

    @Update
    fun update(taskList: TaskList)

    @Query("UPDATE todo_group SET group_name = :updatedName WHERE group_id = :listId")
    fun update(listId: Int, updatedName: String)
}
