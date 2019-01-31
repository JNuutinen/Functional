package com.github.jnuutinen.functional.workers

import android.content.Context
import android.util.Log
import androidx.work.ListenableWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.github.jnuutinen.functional.data.db.TodoDatabase

class DatabaseClearWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    private val mTAG by lazy { DatabaseClearWorker::class.java.simpleName }

    override fun doWork(): ListenableWorker.Result {
        return try {
            val database = TodoDatabase.getInstance(applicationContext)
            database.todoDao().deleteAll()
            database.todoListDao().deleteAll()
            ListenableWorker.Result.success()
        } catch (ex: Exception) {
            Log.e(mTAG, "Error clearing database", ex)
            ListenableWorker.Result.failure()
        }
    }
}