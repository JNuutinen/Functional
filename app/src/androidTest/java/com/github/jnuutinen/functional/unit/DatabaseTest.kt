package com.github.jnuutinen.functional.unit

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.github.jnuutinen.functional.data.db.TodoDatabase
import com.github.jnuutinen.functional.data.db.dao.TodoDao
import com.github.jnuutinen.functional.data.db.dao.TodoListDao
import com.github.jnuutinen.functional.data.db.entity.Todo
import com.github.jnuutinen.functional.data.db.entity.TodoList
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.collection.IsEmptyCollection.empty
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@SmallTest
@RunWith(AndroidJUnit4::class)
class DatabaseTest {
    private lateinit var mTodoDatabase: TodoDatabase
    private lateinit var mTodoDao: TodoDao
    private lateinit var mTodoListDao: TodoListDao

    @Rule
    @JvmField
    var mInstantTaskExecutorRule = InstantTaskExecutorRule()

    @Throws(InterruptedException::class)
    fun <T> LiveData<T>.getValueBlocking(): T? {
        var value: T? = null
        val latch = CountDownLatch(1)
        val innerObserver = Observer<T> {
            value = it
            latch.countDown()
        }
        observeForever(innerObserver)
        latch.await(2, TimeUnit.SECONDS)
        return value
    }

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        mTodoDatabase = Room.inMemoryDatabaseBuilder(context, TodoDatabase::class.java).build()
        mTodoDao = mTodoDatabase.todoDao()
        mTodoListDao = mTodoDatabase.todoListDao()

        val calendar = Calendar.getInstance()

        val myList = TodoList(1, "My list", calendar.time.time)
        val todo1 = Todo(1, "Todo 1", calendar.time.time, 0, 0, 1)
        val todo2 = Todo(2, "Todo 2", calendar.time.time, 0, 1, 1)

        mTodoListDao.insert(myList)
        mTodoDao.insert(todo1)
        mTodoDao.insert(todo2)
    }

    @After
    fun closeDb() {
        mTodoDatabase.close()
    }

    @Test
    fun deleteList() {
        mTodoListDao.delete(mTodoListDao.getAll().getValueBlocking()!![0])

        val lists = mTodoListDao.getAll().getValueBlocking()!!
        assertThat(lists, empty())

        val listsWithTodos = mTodoListDao.getAllListsWithTodos().getValueBlocking()!!
        assertThat(listsWithTodos, empty())

        val todos = mTodoDao.getAll().getValueBlocking()!!
        assertThat(todos, empty())
    }

    @Test
    fun deleteTodo() {
        var todos = mTodoDao.getAll().getValueBlocking()!!
        assertThat(todos.size, `is`(2))

        var todo = todos[0]
        assertThat(todo.contents, `is`("Todo 1"))

        mTodoDao.delete(todo)

        todos = mTodoDao.getAll().getValueBlocking()!!
        assertThat(todos.size, `is`(1))

        todo = todos[0]
        assertThat(todo.contents, `is`("Todo 2"))
    }

    @Test
    fun editList() {
        var list = mTodoListDao.getAll().getValueBlocking()!![0]
        assertThat(list.name, `is`("My list"))
        list.name = "Edited"
        mTodoListDao.update(list)

        list = mTodoListDao.getAll().getValueBlocking()!![0]
        assertThat(list.name, `is`("Edited"))

        val listWithTodos = mTodoListDao.getAllListsWithTodos().getValueBlocking()!![0]
        assertThat(listWithTodos.todoList.name, `is`("Edited"))
        assertThat(listWithTodos.todos.size, `is`(2))
    }
}