package com.github.jnuutinen.functional.ui

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openContextualActionModeOverflowMenu
import androidx.test.espresso.action.ViewActions.click
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
import org.hamcrest.CoreMatchers.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class DeleteTaskListTest {

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
    fun deleteList() {
        // Delete the first task list.
        openContextualActionModeOverflowMenu()
        onView(withText(R.string.action_delete_list))
            .perform(click())
        onView(withText(R.string.action_delete))
            .perform(click())

        // Ensure current task list changed to "Second task list".
        onView(withId(R.id.toolbar))
            .check(matches(hasDescendant(withText("Second task list"))))
        onView(withId(R.id.task_recycler))
            .check(hasSize(3))
        onView(withRecyclerView(R.id.task_recycler).atPosition(0))
            .check(matches(hasDescendant(withText("Second list, first task"))))

        // Ensure deleted list is no longer in the Navigation drawer.
        openNavigationDrawer(mActivityTestRule.activity)
        onView(withId(R.id.nav_view))
            .check(matches(not(hasDescendant(withText("First task list")))))
        onView(withId(R.id.nav_view))
    }

    @Test
    fun deleteAllLists_DefaultListShouldBeCreated() {
        // Delete all three of the test lists.
        for (i in 1..3) {
            openContextualActionModeOverflowMenu()
            onView(withText(R.string.action_delete_list))
                .perform(click())
            onView(withText(R.string.action_delete))
                .perform(click())
        }

        // Check the toolbar title.
        onView(withId(R.id.toolbar))
            .check(matches(hasDescendant(withText(R.string.list_default_name))))

        // Ensure only the default list is in the Navigation drawer.
        openNavigationDrawer(mActivityTestRule.activity)
        onView(withId(R.id.nav_view))
            .check(matches(hasDescendant(withText(R.string.list_default_name))))
        onView(withId(R.id.nav_view))
            .check(matches(not(hasDescendant(withText("First task list")))))
        onView(withId(R.id.nav_view))
            .check(matches(not(hasDescendant(withText("Second task list")))))
        onView(withId(R.id.nav_view))
            .check(matches(not(hasDescendant(withText("Third task list")))))
    }
}