package com.github.jnuutinen.functional.util

import android.content.Context
import com.github.jnuutinen.functional.data.TodoRepository
import com.github.jnuutinen.functional.data.db.TodoDatabase
import com.github.jnuutinen.functional.presentation.viewmodel.TodosViewModelFactory

object InjectorUtils {
    private fun getTodoRepository(context: Context): TodoRepository {
        val db = TodoDatabase.getInstance(context)
        return TodoRepository.getInstance(db.todoDao(), db.todoListDao())
    }

    fun provideTodosViewModelFactory(context: Context): TodosViewModelFactory {
        val repository = getTodoRepository(context)
        return TodosViewModelFactory(repository)
    }
}