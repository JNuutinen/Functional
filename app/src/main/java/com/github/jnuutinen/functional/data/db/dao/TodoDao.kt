package com.github.jnuutinen.functional.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import com.github.jnuutinen.functional.data.db.entity.Todo

@Dao
interface TodoDao {
    @Query("SELECT * FROM todo ORDER BY todo_date")
    fun getTodos(): LiveData<List<Todo>>

    @Query("SELECT * FROM todo WHERE todo_group_id = :groupId ORDER BY todo_date")
    fun getTodosInGroup(groupId: Int): LiveData<List<Todo>>

    @Insert(onConflict = REPLACE)
    fun insertTodo(todo: Todo)

    @Insert(onConflict = REPLACE)
    fun insertAllTodos(todos: List<Todo>)

    @Query("DELETE FROM todo")
    fun deleteAllTodos()

    @Delete
    fun deleteTodo(todo: Todo)
}