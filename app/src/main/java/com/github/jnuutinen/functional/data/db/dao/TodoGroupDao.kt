package com.github.jnuutinen.functional.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.github.jnuutinen.functional.data.db.entity.TodoGroup

@Dao
interface TodoGroupDao {
    @Query("SELECT * FROM todo_group ORDER BY group_date")
    fun getGroupsWithTodos(): LiveData<List<GroupWithTodos>>

    @Query("SELECT * FROM todo_group ORDER BY group_date")
    fun getTodoGroups(): LiveData<List<TodoGroup>>

    @Insert
    fun insertTodoGroup(todoGroup: TodoGroup)

    @Delete
    fun deleteTodoGroup(todoGroup: TodoGroup)

    @Query("DELETE FROM todo_group WHERE group_id = :groupId")
    fun deleteTodoGroup(groupId: Int)

    @Update
    fun updateTodoGroup(todoGroup: TodoGroup)

    @Query("UPDATE todo_group SET group_name = :updatedName WHERE group_id = :groupId")
    fun updateTodoGroup(groupId: Int, updatedName: String)
}