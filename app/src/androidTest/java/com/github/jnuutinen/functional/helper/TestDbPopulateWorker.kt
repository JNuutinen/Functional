package com.github.jnuutinen.functional.helper

import android.content.Context
import android.util.Log
import androidx.work.ListenableWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.github.jnuutinen.functional.data.db.TodoDatabase
import com.github.jnuutinen.functional.data.db.entity.Todo
import com.github.jnuutinen.functional.data.db.entity.TodoList
import com.github.jnuutinen.functional.util.TEST_LIST_DATA_FILENAME
import com.github.jnuutinen.functional.util.TEST_TODO_DATA_FILENAME
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader

class TestDbPopulateWorker(context: Context, workerParams: WorkerParameters): Worker(context, workerParams) {
    private val mTAG by lazy { TestDbPopulateWorker::class.java.simpleName }

    override fun doWork(): ListenableWorker.Result {
        val todoType = object : TypeToken<List<Todo>>() {}.type
        val listType = object : TypeToken<List<TodoList>>() {}.type
        var jsonReader: JsonReader? = null

        return try {
            var inputStream = applicationContext.assets.open(TEST_LIST_DATA_FILENAME)
            jsonReader = JsonReader(inputStream.reader())
            val lists: List<TodoList> = Gson().fromJson(jsonReader, listType)

            inputStream = applicationContext.assets.open(TEST_TODO_DATA_FILENAME)
            jsonReader = JsonReader(inputStream.reader())
            val todos: List<Todo> = Gson().fromJson(jsonReader, todoType)

            val database = TodoDatabase.getInstance(applicationContext)
            database.todoListDao().insertAll(lists)
            database.todoDao().insertAll(todos)
            ListenableWorker.Result.success()
        } catch (ex: Exception) {
            Log.e(mTAG, "Error seeding test to-dos to database", ex)
            ListenableWorker.Result.failure()
        } finally {
            jsonReader?.close()
        }
    }
}