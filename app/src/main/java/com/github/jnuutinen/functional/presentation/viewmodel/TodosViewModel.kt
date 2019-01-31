package com.github.jnuutinen.functional.presentation.viewmodel

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.github.jnuutinen.functional.data.TodoRepository
import com.github.jnuutinen.functional.data.db.dao.ListWithTodos
import com.github.jnuutinen.functional.data.db.entity.Todo
import com.github.jnuutinen.functional.data.db.entity.TodoList
import com.github.jnuutinen.functional.util.PREF_VALUE_DOES_NOT_EXIST_INT

class TodosViewModel internal constructor(private val mTodoRepository: TodoRepository) : ViewModel() {

    // Use MediatorLiveData so that we can force update it from TodosActivity with setValue(getValue).
    val listsWithTodos = MediatorLiveData<List<ListWithTodos>>()
    var activeList = PREF_VALUE_DOES_NOT_EXIST_INT

    init {
        listsWithTodos.addSource(mTodoRepository.getListsWithTodos()) { value -> listsWithTodos.value = value }
    }

    fun deleteTodo(todo: Todo) {
        mTodoRepository.deleteTodo(todo)
    }

    fun deleteTodoList(listId: Int) {
        mTodoRepository.deleteTodoList(listId)
    }

    fun insertTodo(todo: Todo) {
        mTodoRepository.insertTodo(todo)
    }

    fun insertTodoList(todoList: TodoList) {
        mTodoRepository.insertTodoList(todoList)
    }

    fun onTodoDelete(positionsToUpdate: List<Todo>, deletedTodo: Todo) {
        mTodoRepository.onTodoDelete(positionsToUpdate, deletedTodo)
    }

    fun onTodoDeleteUndo(positionsToUpdate: List<Todo>, insertedTodo: Todo) {
        mTodoRepository.onTodoDeleteUndo(positionsToUpdate, insertedTodo)
    }

    fun updateTodoList(listId: Int, updatedName: String) {
        mTodoRepository.updateTodoList(listId, updatedName)
    }

    fun updateTodos(todos: List<Todo>) {
        mTodoRepository.updateTodos(todos)
    }
}