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
    fun getAll(): LiveData<List<Todo>>

    @Query("SELECT * FROM todo WHERE todo_group_id = :listId ORDER BY todo_date")
    fun getAllInList(listId: Int): LiveData<List<Todo>>

    @Insert(onConflict = REPLACE)
    fun insert(todo: Todo)

    @Insert(onConflict = REPLACE)
    fun insertAll(todos: List<Todo>)

    @Query("DELETE FROM todo")
    fun deleteAll()

    @Delete
    fun delete(todo: Todo)
}