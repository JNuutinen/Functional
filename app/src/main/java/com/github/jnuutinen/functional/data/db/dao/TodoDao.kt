package com.github.jnuutinen.functional.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.github.jnuutinen.functional.data.db.entity.Todo

@Dao
interface TodoDao {
    @Query("SELECT * FROM todo ORDER BY todo_date")
    fun getTodos(): LiveData<List<Todo>>

    @Query("SELECT * FROM todo WHERE todo_group_id = :groupId ORDER BY todo_date")
    fun getTodosInGroup(groupId: Int): LiveData<List<Todo>>

    @Insert
    fun insertTodo(todo: Todo)

    @Delete
    fun deleteTodo(todo: Todo)
}