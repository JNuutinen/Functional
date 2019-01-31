package com.github.jnuutinen.functional.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.github.jnuutinen.functional.data.db.dao.TodoDao
import com.github.jnuutinen.functional.data.db.dao.TodoListDao
import com.github.jnuutinen.functional.data.db.entity.Todo
import com.github.jnuutinen.functional.data.db.entity.TodoList
import com.github.jnuutinen.functional.util.DB_NAME

@Database(entities = [Todo::class, TodoList::class], version = 2)
abstract class TodoDatabase : RoomDatabase() {
    abstract fun todoDao(): TodoDao
    abstract fun todoListDao(): TodoListDao

    companion object {
        @Volatile
        private var mInstance: TodoDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE todo ADD todo_order INTEGER DEFAULT 0 NOT NULL")
            }
        }

        fun getInstance(context: Context): TodoDatabase {
            return mInstance ?: synchronized(this) {
                mInstance ?: buildDatabase(context).also { mInstance = it }
            }
        }

        private fun buildDatabase(context: Context): TodoDatabase {
            return Room.databaseBuilder(context, TodoDatabase::class.java, DB_NAME)
                .addMigrations(MIGRATION_1_2)
                .build()
        }
    }
}