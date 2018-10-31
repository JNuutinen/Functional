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
import com.github.jnuutinen.functional.data.db.dao.TodoGroupDao
import com.github.jnuutinen.functional.data.db.entity.Todo
import com.github.jnuutinen.functional.data.db.entity.TodoGroup
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
    private lateinit var todoDatabase: TodoDatabase
    private lateinit var todoDao: TodoDao
    private lateinit var todoGroupDao: TodoGroupDao

    @get:Rule var instantTaskExecutorRule = InstantTaskExecutorRule()

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

    @Before fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        todoDatabase = Room.inMemoryDatabaseBuilder(context, TodoDatabase::class.java).build()
        todoDao = todoDatabase.todoDao()
        todoGroupDao = todoDatabase.todoGroupDao()

        val calendar = Calendar.getInstance()

        val myGroup = TodoGroup(1, "My group", calendar.time.time)
        val todo1 = Todo(1, "Todo 1", calendar.time.time, 0, 1)
        val todo2 = Todo(2, "Todo 2", calendar.time.time, 0, 1)

        todoGroupDao.insertTodoGroup(myGroup)
        todoDao.insertTodo(todo1)
        todoDao.insertTodo(todo2)
    }

    @After fun closeDb() {
        todoDatabase.close()
    }

    @Test fun deleteGroup() {
        todoGroupDao.deleteTodoGroup(todoGroupDao.getTodoGroups().getValueBlocking()!![0])

        val groups = todoGroupDao.getTodoGroups().getValueBlocking()!!
        assertThat(groups, empty())

        val groupsWithTodos = todoGroupDao.getGroupsWithTodos().getValueBlocking()!!
        assertThat(groupsWithTodos, empty())

        val todos = todoDao.getTodos().getValueBlocking()!!
        assertThat(todos, empty())
    }

    @Test fun deleteTodo() {
        var todos = todoDao.getTodos().getValueBlocking()!!
        assertThat(todos.size, `is`(2))

        var todo = todos[0]
        assertThat(todo.contents, `is`("Todo 1"))

        todoDao.deleteTodo(todo)

        todos = todoDao.getTodos().getValueBlocking()!!
        assertThat(todos.size, `is`(1))

        todo = todos[0]
        assertThat(todo.contents, `is`("Todo 2"))
    }

    @Test fun editGroup() {
        var group = todoGroupDao.getTodoGroups().getValueBlocking()!![0]
        assertThat(group.name, `is`("My group"))
        group.name = "Edited"
        todoGroupDao.updateTodoGroup(group)

        group = todoGroupDao.getTodoGroups().getValueBlocking()!![0]
        assertThat(group.name, `is`("Edited"))

        val groupWithTodos = todoGroupDao.getGroupsWithTodos().getValueBlocking()!![0]
        assertThat(groupWithTodos.todoGroup.name, `is`("Edited"))
        assertThat(groupWithTodos.todos.size, `is`(2))
    }
}