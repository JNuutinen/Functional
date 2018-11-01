package com.github.jnuutinen.functional.presentation

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.github.jnuutinen.functional.data.TodoRepository
import com.github.jnuutinen.functional.data.db.dao.GroupWithTodos
import com.github.jnuutinen.functional.data.db.entity.Todo
import com.github.jnuutinen.functional.data.db.entity.TodoGroup
import com.github.jnuutinen.functional.util.PREF_VALUE_DOES_NOT_EXIST_INT

class TodosViewModel internal constructor(private val todoRepository: TodoRepository) : ViewModel() {

    // Use MediatorLiveData so that we can force update it from TodosActivity with setValue(getValue).
    val groupsWithTodos = MediatorLiveData<List<GroupWithTodos>>()
    var activeGroup = PREF_VALUE_DOES_NOT_EXIST_INT

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