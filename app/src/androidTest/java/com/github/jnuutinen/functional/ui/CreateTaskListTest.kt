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
class CreateTaskListTest {

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
    fun createList() {
        // Create a new task list.
        openNavigationDrawer(mActivityTestRule.activity)
        onView(withText(R.string.action_add_list))
            .perform(click())
        onView(withId(R.id.edit_list_add))
            .perform(replaceText("A new task list"), closeSoftKeyboard())
        onView(withText(R.string.action_add_task))
            .perform(click())

        // The created task list should be active (toolbar title should have changed).
        onView(withId(R.id.toolbar))
            .check(matches(hasDescendant(withText("A new task list"))))

        // The created task list should exist in the Navigation Drawer.
        openNavigationDrawer(mActivityTestRule.activity)
        onView(withId(R.id.nav_view))
            .check(matches(hasDescendant(withText("A new task list"))))
    }

    @Test
    fun createList_WithEmptyContents() {
        // Try to create a new task list with empty name.
        openNavigationDrawer(mActivityTestRule.activity)
        onView(withText(R.string.action_add_list))
            .perform(click())
        onView(withId(R.id.edit_list_add))
            .perform(replaceText(" "), closeSoftKeyboard())
        onView(withText(R.string.action_add_task))
            .perform(click())

        // Info Snackbar should be visible.
        onView(withId(R.id.main_coordinator))
            .check(matches(hasDescendant(withText(R.string.alert_list_name_empty))))

        // The active task list should not change.
        onView(withId(R.id.toolbar))
            .check(matches(hasDescendant(withText("First task list"))))
    }
}