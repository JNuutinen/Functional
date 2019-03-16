package com.github.jnuutinen.functional.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.github.jnuutinen.functional.data.db.dao.TaskDao
import com.github.jnuutinen.functional.data.db.dao.TaskListDao
import com.github.jnuutinen.functional.data.db.entity.Task
import com.github.jnuutinen.functional.data.db.entity.TaskList
import com.github.jnuutinen.functional.util.DB_NAME

@Database(entities = [Task::class, TaskList::class], version = 2)
abstract class TaskDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun taskListDao(): TaskListDao

    companion object {
        @Volatile
        private var mInstance: TaskDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE todo ADD todo_order INTEGER DEFAULT 0 NOT NULL")
            }
        }

        fun getInstance(context: Context): TaskDatabase {
            return mInstance ?: synchronized(this) {
                mInstance ?: buildDatabase(context).also { mInstance = it }
            }
        }

        private fun buildDatabase(context: Context): TaskDatabase {
            return Room.databaseBuilder(context, TaskDatabase::class.java, DB_NAME)
                .addMigrations(MIGRATION_1_2)
                .build()
        }
    }
}