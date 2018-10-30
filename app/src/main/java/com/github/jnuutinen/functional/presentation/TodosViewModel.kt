package com.github.jnuutinen.functional.presentation

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.github.jnuutinen.functional.data.TodoRepository
import com.github.jnuutinen.functional.data.db.dao.GroupWithTodos
import com.github.jnuutinen.functional.data.db.entity.Todo
import com.github.jnuutinen.functional.data.db.entity.TodoGroup

class TodosViewModel internal constructor(private val todoRepository: TodoRepository) : ViewModel() {
    val groupsWithTodos = MediatorLiveData<List<GroupWithTodos>>()
    var activeGroup = -1

    init {
        groupsWithTodos.addSource(todoRepository.getGroupsWithTodos()) { value -> groupsWithTodos.value = value }
    }

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

    fun updateTodoGroup(groupId: Int, updatedName: String) {
        todoRepository.updateTodoGroup(groupId, updatedName)
    }
}