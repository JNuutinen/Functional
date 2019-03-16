package com.github.jnuutinen.functional.util

import android.content.Context
import com.github.jnuutinen.functional.data.TaskRepository
import com.github.jnuutinen.functional.data.db.TaskDatabase
import com.github.jnuutinen.functional.presentation.viewmodel.TasksViewModelFactory

object InjectorUtils {
    private fun getTaskRepository(context: Context): TaskRepository {
        val db = TaskDatabase.getInstance(context)
        return TaskRepository.getInstance(db.taskDao(), db.taskListDao())
    }

    fun provideTasksViewModelFactory(context: Context): TasksViewModelFactory {
        val repository = getTaskRepository(context)
        return TasksViewModelFactory(repository)
    }
}