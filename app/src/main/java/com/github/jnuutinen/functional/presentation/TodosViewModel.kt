package com.github.jnuutinen.functional.presentation

import androidx.lifecycle.ViewModel
import com.github.jnuutinen.functional.data.TodoRepository
import com.github.jnuutinen.functional.data.db.entity.Todo
import com.github.jnuutinen.functional.data.db.entity.TodoGroup

class TodosViewModel internal constructor(private val todoRepository: TodoRepository) : ViewModel() {
    val todoGroups = todoRepository.getTodoGroups()
    var activeGroup = 1

    fun getTodosInGroup(groupId: Int) = todoRepository.getTodosInGroup(groupId)

    fun deleteTodo(todo: Todo) {
        todoRepository.deleteTodo(todo)
    }

    fun deleteTodoGroup(groupId: Int) {
        todoRepository.deleteTodoGroup(groupId)
    }

    fun insertTodo(todo: Todo) {
        todoRepository.insertTodo(todo)
    }

    fun insertTodoGroup(todoGroup: TodoGroup) {
        todoRepository.insertTodoGroup(todoGroup)
    }
}