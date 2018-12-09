package com.github.jnuutinen.functional.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.github.jnuutinen.functional.data.db.entity.TodoList

@Dao
interface TodoListDao {
    @Transaction
    @Query("SELECT * FROM todo_group ORDER BY group_date")
    fun getAllListsWithTodos(): LiveData<List<ListWithTodos>>

    @Query("SELECT * FROM todo_group ORDER BY group_date")
    fun getAll(): LiveData<List<TodoList>>

    @Insert
    fun insert(todoList: TodoList)

    @Insert
    fun insertAll(todoLists: List<TodoList>)

    @Query("DELETE FROM todo_group")
    fun deleteAll()

    @Delete
    fun delete(todoList: TodoList)

    @Query("DELETE FROM todo_group WHERE group_id = :listId")
    fun delete(listId: Int)

    @Update
    fun update(todoList: TodoList)

    @Query("UPDATE todo_group SET group_name = :updatedName WHERE group_id = :listId")
    fun update(listId: Int, updatedName: String)
}