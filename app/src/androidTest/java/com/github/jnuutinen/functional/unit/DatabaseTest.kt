package com.github.jnuutinen.functional.unit

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.github.jnuutinen.functional.data.db.TaskDatabase
import com.github.jnuutinen.functional.data.db.dao.TaskDao
import com.github.jnuutinen.functional.data.db.dao.TaskListDao
import com.github.jnuutinen.functional.data.db.entity.Task
import com.github.jnuutinen.functional.data.db.entity.TaskList
import java.util.Calendar
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.collection.IsEmptyCollection.empty
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@SmallTest
@RunWith(AndroidJUnit4::class)
class DatabaseTest {
    private lateinit var mTaskDatabase: TaskDatabase
    private lateinit var mTaskDao: TaskDao
    private lateinit var mTaskListDao: TaskListDao

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
        mTaskDatabase = Room.inMemoryDatabaseBuilder(context, TaskDatabase::class.java).build()
        mTaskDao = mTaskDatabase.taskDao()
        mTaskListDao = mTaskDatabase.taskListDao()

        val calendar = Calendar.getInstance()

        val myList = TaskList(1, "My list", calendar.time.time)
        val task1 = Task(1, "Task 1", calendar.time.time, 0, 0, 1)
        val task2 = Task(2, "Task 2", calendar.time.time, 0, 1, 1)

        mTaskListDao.insert(myList)
        mTaskDao.insert(task1)
        mTaskDao.insert(task2)
    }

    @After
    fun closeDb() {
        mTaskDatabase.close()
    }

    @Test
    fun deleteList() {
        mTaskListDao.delete(mTaskListDao.getAll().getValueBlocking()!![0])

        val lists = mTaskListDao.getAll().getValueBlocking()!!
        assertThat(lists, empty())

        val listsWithTasks = mTaskListDao.getAllListsWithTasks().getValueBlocking()!!
        assertThat(listsWithTasks, empty())

        val tasks = mTaskDao.getAll().getValueBlocking()!!
        assertThat(tasks, empty())
    }

    @Test
    fun deleteTask() {
        var tasks = mTaskDao.getAll().getValueBlocking()!!
        assertThat(tasks.size, `is`(2))

        var task = tasks[0]
        assertThat(task.contents, `is`("Task 1"))

        mTaskDao.delete(task)

        tasks = mTaskDao.getAll().getValueBlocking()!!
        assertThat(tasks.size, `is`(1))

        task = tasks[0]
        assertThat(task.contents, `is`("Task 2"))
    }

    @Test
    fun editList() {
        var list = mTaskListDao.getAll().getValueBlocking()!![0]
        assertThat(list.name, `is`("My list"))
        list.name = "Edited"
        mTaskListDao.update(list)

        list = mTaskListDao.getAll().getValueBlocking()!![0]
        assertThat(list.name, `is`("Edited"))

        val listWithTasks = mTaskListDao.getAllListsWithTasks().getValueBlocking()!![0]
        assertThat(listWithTasks.taskList.name, `is`("Edited"))
        assertThat(listWithTasks.tasks.size, `is`(2))
    }
}
