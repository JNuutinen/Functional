package com.github.jnuutinen.functional.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.github.jnuutinen.functional.R
import com.github.jnuutinen.functional.data.db.dao.TodoDao
import com.github.jnuutinen.functional.data.db.dao.TodoGroupDao
import com.github.jnuutinen.functional.data.db.entity.Todo
import com.github.jnuutinen.functional.data.db.entity.TodoGroup
import com.github.jnuutinen.functional.util.DB_NAME
import com.github.jnuutinen.functional.util.runOnIoThread
import java.util.*

@Database(entities = [Todo::class, TodoGroup::class], version = 1)
abstract class TodoDatabase : RoomDatabase() {
    abstract fun todoDao(): TodoDao
    abstract fun todoGroupDao(): TodoGroupDao

    companion object {
        @Volatile
        private var instance: TodoDatabase? = null

        fun getInstance(context: Context): TodoDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): TodoDatabase {
            return Room.databaseBuilder(context, TodoDatabase::class.java, DB_NAME)
                    /*
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        val todoGroup = TodoGroup(1, context.resources.getString(R.string.group_default_name), Calendar.getInstance().time.time)
                        runOnIoThread {
                            getInstance(context).todoGroupDao().insertTodoGroup(todoGroup)
                        }
                    }
                })*/
                .build()
        }
    }
}