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
import com.github.jnuutinen.functional.helper.RecyclerViewMatcher.Companion.withRecyclerView
import com.github.jnuutinen.functional.helper.TestDatabaseHelper.Companion.repopulateDb
import com.github.jnuutinen.functional.helper.TestDatabaseHelper.Companion.setActiveListSharedPref
import com.github.jnuutinen.functional.presentation.activity.TasksActivity
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class EditTaskTest {

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
    fun editTask() {
        // Edit the second task.
        onView(withRecyclerView(R.id.task_recycler).atPosition(1))
            .perform(click())
        onView(withId(R.id.edit_task_add))
            .perform(replaceText("Edited task"), closeSoftKeyboard())
        onView(withText(R.string.action_save))
            .perform(click())

        // The second task should be updated.
        onView(withRecyclerView(R.id.task_recycler).atPosition(1))
            .check(matches(hasDescendant(withText("Edited task"))))
    }

    @Test
    fun editTask_withEmptyContents() {
        // Edit the second task.
        onView(withRecyclerView(R.id.task_recycler).atPosition(1))
            .perform(click())
        onView(withId(R.id.edit_task_add))
            .perform(replaceText("  "), closeSoftKeyboard())
        onView(withText(R.string.action_save))
            .perform(click())

        // Info Snackbar should be visible.
        onView(withId(R.id.main_coordinator))
            .check(matches(hasDescendant(withText(R.string.alert_task_empty))))

        // The second task should NOT be updated.
        onView(withRecyclerView(R.id.task_recycler).atPosition(1))
            .check(matches(hasDescendant(withText("First list, second task"))))
    }
}