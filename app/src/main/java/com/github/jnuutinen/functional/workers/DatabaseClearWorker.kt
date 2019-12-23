package com.github.jnuutinen.functional.workers

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.github.jnuutinen.functional.data.db.TaskDatabase

class DatabaseClearWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {
    private val mTAG by lazy { DatabaseClearWorker::class.java.simpleName }

    override fun doWork(): Result {
        return try {
            val database = TaskDatabase.getInstance(applicationContext)
            database.taskDao().deleteAll()
            database.taskListDao().deleteAll()
            Result.success()
        } catch (ex: Exception) {
            Log.e(mTAG, "Error clearing database", ex)
            Result.failure()
        }
    }
}
