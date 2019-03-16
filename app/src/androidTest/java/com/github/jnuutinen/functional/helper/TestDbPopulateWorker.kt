package com.github.jnuutinen.functional.helper

import android.content.Context
import android.util.Log
import androidx.work.ListenableWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.github.jnuutinen.functional.data.db.TaskDatabase
import com.github.jnuutinen.functional.data.db.entity.Task
import com.github.jnuutinen.functional.data.db.entity.TaskList
import com.github.jnuutinen.functional.util.TEST_LIST_DATA_FILENAME
import com.github.jnuutinen.functional.util.TEST_TASK_DATA_FILENAME
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader

class TestDbPopulateWorker(context: Context, workerParams: WorkerParameters): Worker(context, workerParams) {
    private val mTAG by lazy { TestDbPopulateWorker::class.java.simpleName }

    override fun doWork(): ListenableWorker.Result {
        val taskType = object : TypeToken<List<Task>>() {}.type
        val listType = object : TypeToken<List<TaskList>>() {}.type
        var jsonReader: JsonReader? = null

        return try {
            var inputStream = applicationContext.assets.open(TEST_LIST_DATA_FILENAME)
            jsonReader = JsonReader(inputStream.reader())
            val lists: List<TaskList> = Gson().fromJson(jsonReader, listType)

            inputStream = applicationContext.assets.open(TEST_TASK_DATA_FILENAME)
            jsonReader = JsonReader(inputStream.reader())
            val tasks: List<Task> = Gson().fromJson(jsonReader, taskType)

            val database = TaskDatabase.getInstance(applicationContext)
            database.taskListDao().insertAll(lists)
            database.taskDao().insertAll(tasks)
            ListenableWorker.Result.success()
        } catch (ex: Exception) {
            Log.e(mTAG, "Error seeding test tasks to database", ex)
            ListenableWorker.Result.failure()
        } finally {
            jsonReader?.close()
        }
    }
}