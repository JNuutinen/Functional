package com.github.jnuutinen.functional.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.github.jnuutinen.functional.data.TodoRepository

class TodosViewModelFactory(private val todoRepository: TodoRepository) : ViewModelProvider.NewInstanceFactory() {
    override fun <T: ViewModel> create(modelClass: Class<T>): T {
        return TodosViewModel(todoRepository) as T
    }
}