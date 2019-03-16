package com.github.jnuutinen.functional.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.github.jnuutinen.functional.data.TaskRepository

class TasksViewModelFactory(private val taskRepository: TaskRepository) : ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T: ViewModel> create(modelClass: Class<T>): T {
        return TasksViewModel(taskRepository) as T
    }
}