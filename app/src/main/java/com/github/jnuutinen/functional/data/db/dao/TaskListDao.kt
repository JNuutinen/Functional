package com.github.jnuutinen.functional.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.github.jnuutinen.functional.data.db.entity.TaskList
import com.github.jnuutinen.functional.util.MiscUtil

@Dao
interface TaskListDao {
    @Query(
        "INSERT INTO todo (todo_contents, todo_date, todo_color, todo_order, todo_group_id) " +
            "SELECT todo_contents, todo_date, todo_color, todo_order, :destinationListId " +
            "FROM todo " +
            "WHERE todo_group_id = :sourceListId"
    )
    fun copyTasksInList(sourceListId: Int, destinationListId: Int)

    @Transaction
    fun createCopy(listId: Int, newListName: String) {
        val newId = insert(TaskList(0, newListName, MiscUtil.getCurrentTime()))
        copyTasksInList(listId, newId.toInt())
    }

    @Transaction
    @Query("SELECT * FROM todo_group ORDER BY group_date")
    fun getAllListsWithTasks(): LiveData<List<ListWithTasks>>

    @Query("SELECT * FROM todo_group ORDER BY group_date")
    fun getAll(): LiveData<List<TaskList>>

    @Insert
    fun insert(taskList: TaskList): Long

    @Insert
    fun insertAll(taskLists: List<TaskList>): List<Long>

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
