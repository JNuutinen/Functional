package com.github.jnuutinen.functional.data.db.dao

import androidx.room.Embedded
import androidx.room.Relation
import com.github.jnuutinen.functional.data.db.entity.Todo
import com.github.jnuutinen.functional.data.db.entity.TodoList

class ListWithTodos(@Embedded val todoList: TodoList,
                    @Relation(parentColumn = "group_id", entityColumn = "todo_group_id", entity = Todo::class) val todos: List<Todo>)
