package com.github.jnuutinen.functional.ui

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
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
class CreateTaskTest {

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
    fun createTask() {
        // Create a new task.
        onView(withId(R.id.button_add_task))
            .perform(click())
        onView(withId(R.id.edit_task_add))
            .perform(replaceText("New task"), closeSoftKeyboard())
        onView(withText(R.string.action_add_task))
            .perform(click())

        // The new task should be appended to the end of the tasks list.
        onView(withRecyclerView(R.id.task_recycler).atPosition(3))
            .check(matches(hasDescendant(withText("New task"))))
    }

    @Test
    fun createTask_WithEmptyContents() {
        // Try to create an empty task.
        onView(withId(R.id.button_add_task))
            .perform(click())
        onView(withId(R.id.edit_task_add))
            .perform(replaceText(""), closeSoftKeyboard())
        onView(withText(R.string.action_add_task))
            .perform(click())

        // Info Snackbar should be visible.
        onView(withId(R.id.main_coordinator))
            .check(matches(hasDescendant(withText(R.string.alert_task_empty))))

        // The number of tasks should be 3.
        onView(withId(R.id.task_recycler))
            .check(hasSize(3))
    }

    @Test
    fun createTask_InAnotherList() {
        // Switch to the second test list.
        openNavigationDrawer(mActivityTestRule.activity)
        onView(withText("Second task list"))
            .perform(click())

        // Create a new task
        onView(withId(R.id.button_add_task))
            .perform(click())
        onView(withId(R.id.edit_task_add))
            .perform(replaceText("New task"), closeSoftKeyboard())
        onView(withText(R.string.action_add_task))
            .perform(click())

        // The new task should be appended to the end of the tasks list.
        onView(withRecyclerView(R.id.task_recycler).atPosition(3))
            .check(matches(hasDescendant(withText("New task"))))

        // There should now be 4 tasks.
        onView(withId(R.id.task_recycler))
            .check(hasSize(4))

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

        // Check that the new task still exists.
        onView(withRecyclerView(R.id.task_recycler).atPosition(3))
            .check(matches(hasDescendant(withText("New task"))))
        onView(withId(R.id.task_recycler))
            .check(hasSize(4))
    }
}