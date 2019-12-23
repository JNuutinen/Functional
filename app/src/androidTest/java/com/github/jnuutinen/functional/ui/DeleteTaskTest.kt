package com.github.jnuutinen.functional.ui

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.swipeLeft
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import androidx.test.uiautomator.UiDevice
import com.github.jnuutinen.functional.R
import com.github.jnuutinen.functional.helper.RecyclerItemCountAssert.Companion.hasSize
import com.github.jnuutinen.functional.helper.RecyclerViewMatcher.Companion.withRecyclerView
import com.github.jnuutinen.functional.helper.TestDatabaseHelper.Companion.repopulateDb
import com.github.jnuutinen.functional.helper.TestDatabaseHelper.Companion.setActiveListSharedPref
import com.github.jnuutinen.functional.helper.UiTestHelper.Companion.openNavigationDrawer
import com.github.jnuutinen.functional.presentation.activity.TasksActivity
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class DeleteTaskTest {

    @Rule
    @JvmField
    val mActivityTestRule = object : ActivityTestRule<TasksActivity>(
        TasksActivity::class.java, false, false
    ) {
        override fun beforeActivityLaunched() {
            setActiveListSharedPref(InstrumentationRegistry.getInstrumentation().targetContext, 2)
            super.beforeActivityLaunched()
        }
    }

    @Before
    fun setUp() {
        repopulateDb()
        Thread.sleep(1000)
        mActivityTestRule.launchActivity(null)
        Thread.sleep(1000)
    }

    @Test
    fun deleteTask_First() {
        val secondTaskName = "First list, second task"

        // Delete the first task by swiping it.
        deleteTask(0)

        // The former second task should now be the first one.
        onView(withRecyclerView(R.id.task_recycler).atPosition(0))
            .check(matches(hasDescendant(withText(secondTaskName))))

        // The number of tasks should now be 2.
        onView(withId(R.id.task_recycler))
            .check(hasSize(2))

        // Ensure state remains after orientation change and Activity restart.
        with(UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())) {
            setOrientationLeft()
            setOrientationNatural()
            Thread.sleep(2000)
        }

        // Make the same checks as before.
        onView(withRecyclerView(R.id.task_recycler).atPosition(0))
            .check(matches(hasDescendant(withText(secondTaskName))))

        onView(withId(R.id.task_recycler))
            .check(hasSize(2))
    }

    @Test
    fun deleteTask_Middle() {
        val thirdTaskName = "First list, third task"

        // Delete the second task by swiping it.
        deleteTask(1)

        // The former third task should now be the second one.
        onView(withRecyclerView(R.id.task_recycler).atPosition(1))
            .check(matches(hasDescendant(withText(thirdTaskName))))

        // The number of tasks should now be 2.
        onView(withId(R.id.task_recycler))
            .check(hasSize(2))

        // Ensure state remains after orientation change and Activity restart.
        with(UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())) {
            setOrientationLeft()
            setOrientationNatural()
            Thread.sleep(2000)
        }

        // Make the same checks as before.
        onView(withRecyclerView(R.id.task_recycler).atPosition(1))
            .check(matches(hasDescendant(withText(thirdTaskName))))

        onView(withId(R.id.task_recycler))
            .check(hasSize(2))
    }

    @Test
    fun deleteTask_Last() {
        val secondTaskName = "First list, second task"

        // Delete the third task by swiping it.
        deleteTask(2)

        // The former second task should still be the second one.
        onView(withRecyclerView(R.id.task_recycler).atPosition(1))
            .check(matches(hasDescendant(withText(secondTaskName))))

        // The number of tasks should now be 2.
        onView(withId(R.id.task_recycler))
            .check(hasSize(2))

        // Ensure state remains after orientation change and Activity restart.
        with(UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())) {
            setOrientationLeft()
            setOrientationNatural()
            Thread.sleep(2000)
        }

        // Make the same checks as before.
        onView(withRecyclerView(R.id.task_recycler).atPosition(1))
            .check(matches(hasDescendant(withText(secondTaskName))))

        onView(withId(R.id.task_recycler))
            .check(hasSize(2))
    }

    @Test
    fun deleteTask_Middle_InAnotherList() {
        // Switch to the second test list.
        openNavigationDrawer(mActivityTestRule.activity)
        onView(withText("Second task list"))
            .perform(click())

        val thirdTaskName = "Second list, third task"

        // Delete the second task by swiping it.
        deleteTask(1)

        // The former third task should now be the second one.
        onView(withRecyclerView(R.id.task_recycler).atPosition(1))
            .check(matches(hasDescendant(withText(thirdTaskName))))

        // The number of tasks should now be 2.
        onView(withId(R.id.task_recycler))
            .check(hasSize(2))

        // Go back to the first test list.
        openNavigationDrawer(mActivityTestRule.activity)
        onView(withText("First task list"))
            .perform(click())

        // The first test list should be unaffected.
        onView(withId(R.id.task_recycler))
            .check(hasSize(3))

        // Return to the second list.
        openNavigationDrawer(mActivityTestRule.activity)
        onView(withText("Second task list"))
            .perform(click())

        // Make the same checks as before.
        onView(withRecyclerView(R.id.task_recycler).atPosition(1))
            .check(matches(hasDescendant(withText(thirdTaskName))))

        onView(withId(R.id.task_recycler))
            .check(hasSize(2))
    }

    @Test
    fun deleteTask_Middle_Undo() {
        val secondTaskName = "First list, second task"
        val thirdTaskName = "First list, third task"

        // Delete the second task by swiping it.
        deleteTask(1)

        // The former third task should now be the second one.
        onView(withRecyclerView(R.id.task_recycler).atPosition(1))
            .check(matches(hasDescendant(withText(thirdTaskName))))

        // The number of tasks should now be 2.
        onView(withId(R.id.task_recycler))
            .check(hasSize(2))

        Thread.sleep(1000)

        // Undo the deletion.
        onView(withText("UNDO"))
            .perform(click())

        // We should now be in the original state.
        onView(withRecyclerView(R.id.task_recycler).atPosition(1))
            .check(matches(hasDescendant(withText(secondTaskName))))
        onView(withRecyclerView(R.id.task_recycler).atPosition(2))
            .check(matches(hasDescendant(withText(thirdTaskName))))

        // The number of tasks should now be 3.
        onView(withId(R.id.task_recycler))
            .check(hasSize(3))
    }

    @Test
    fun deleteTwoTasks() {
        val firstTaskName = "First list, first task"

        // Delete the second task twice to delete the middle and last tasks.
        deleteTask(1)
        deleteTask(1)

        // Only the first task should remain.
        onView(withRecyclerView(R.id.task_recycler).atPosition(0))
            .check(matches(hasDescendant(withText(firstTaskName))))

        // The number of tasks should now be 1.
        onView(withId(R.id.task_recycler))
            .check(hasSize(1))
    }

    @Test
    fun deleteTwoTasks_Undo() {
        val firstTaskName = "First list, first task"
        val thirdTaskName = "First list, third task"

        // Delete the second task by swiping it.
        deleteTask(1)

        // Delete the first task by swiping it.
        deleteTask(0)

        // Undo the deletion of the first task.
        onView(withText(R.string.action_undo))
            .perform(click())

        // The first and third task should remain.
        onView(withRecyclerView(R.id.task_recycler).atPosition(0))
            .check(matches(hasDescendant(withText(firstTaskName))))
        onView(withRecyclerView(R.id.task_recycler).atPosition(1))
            .check(matches(hasDescendant(withText(thirdTaskName))))
    }

    private fun deleteTask(index: Int) {
        onView(withRecyclerView(R.id.task_recycler).atPosition(index))
            .perform(swipeLeft())
        Thread.sleep(1000)
    }
}
