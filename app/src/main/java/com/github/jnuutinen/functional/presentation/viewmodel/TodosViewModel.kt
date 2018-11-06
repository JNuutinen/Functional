package com.github.jnuutinen.functional.presentation.viewmodel

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.github.jnuutinen.functional.data.TodoRepository
import com.github.jnuutinen.functional.data.db.dao.GroupWithTodos
import com.github.jnuutinen.functional.data.db.entity.Todo
import com.github.jnuutinen.functional.data.db.entity.TodoGroup
import com.github.jnuutinen.functional.util.PREF_VALUE_DOES_NOT_EXIST_INT

class TodosViewModel internal constructor(private val mTodoRepository: TodoRepository) : ViewModel() {

    // Use MediatorLiveData so that we can force update it from TodosActivity with setValue(getValue).
    val groupsWithTodos = MediatorLiveData<List<GroupWithTodos>>()
    var activeGroup = PREF_VALUE_DOES_NOT_EXIST_INT

    init {
        groupsWithTodos.addSource(mTodoRepository.getGroupsWithTodos()) { value -> groupsWithTodos.value = value }
    }

    fun deleteTodo(todo: Todo) {
        mTodoRepository.deleteTodo(todo)
    }

    fun deleteTodoGroup(groupId: Int) {
        mTodoRepository.deleteTodoGroup(groupId)
    }

    fun insertTodo(todo: Todo) {
        mTodoRepository.insertTodo(todo)
    }

    fun insertTodoGroup(todoGroup: TodoGroup) {
        mTodoRepository.insertTodoGroup(todoGroup)
    }

    fun updateTodoGroup(groupId: Int, updatedName: String) {
        mTodoRepository.updateTodoGroup(groupId, updatedName)
    }
}