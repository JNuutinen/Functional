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
import com.github.jnuutinen.functional.presentation.activity.TodosActivity
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class CreateTodoTest {

    @Rule
    @JvmField
    val mActivityTestRule = object : ActivityTestRule<TodosActivity>(
        TodosActivity::class.java, false, false
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
    fun createTodo() {
        // Create a new to-do.
        onView(withId(R.id.button_add_todo))
            .perform(click())
        onView(withId(R.id.edit_todo_add))
            .perform(replaceText("New to-do"), closeSoftKeyboard())
        onView(withText("CREATE"))
            .perform(click())

        // The new to-do should be appended to the end of the to-dos list.
        onView(withRecyclerView(R.id.todo_recycler).atPosition(3))
            .check(matches(hasDescendant(withText("New to-do"))))
    }

    @Test
    fun createTodo_WithEmptyContents() {
        // Try to create an empty to-do.
        onView(withId(R.id.button_add_todo))
            .perform(click())
        onView(withId(R.id.edit_todo_add))
            .perform(replaceText(""), closeSoftKeyboard())
        onView(withText("CREATE"))
            .perform(click())

        // Info Snackbar should be visible.
        onView(withId(R.id.main_coordinator))
            .check(matches(hasDescendant(withText(R.string.alert_todo_empty))))

        // The number of to-dos should be 3.
        onView(withId(R.id.todo_recycler))
            .check(hasSize(3))
    }

    @Test
    fun createTodo_InAnotherList() {
        // Switch to the second test list.
        openNavigationDrawer(mActivityTestRule.activity)
        onView(withText("Second to-do list"))
            .perform(click())

        // Create a new to-do
        onView(withId(R.id.button_add_todo))
            .perform(click())
        onView(withId(R.id.edit_todo_add))
            .perform(replaceText("New to-do"), closeSoftKeyboard())
        onView(withText("CREATE"))
            .perform(click())

        // The new to-do should be appended to the end of the to-dos list.
        onView(withRecyclerView(R.id.todo_recycler).atPosition(3))
            .check(matches(hasDescendant(withText("New to-do"))))

        // There should now be 4 to-dos.
        onView(withId(R.id.todo_recycler))
            .check(hasSize(4))

        // Go back to the first test list.
        openNavigationDrawer(mActivityTestRule.activity)
        onView(withText("First to-do list"))
            .perform(click())

        // The first test list should be unaffected.
        onView(withId(R.id.todo_recycler))
            .check(hasSize(3))

        // Return to the second list.
        openNavigationDrawer(mActivityTestRule.activity)
        onView(withText("Second to-do list"))
            .perform(click())

        // Check that the new to-do still exists.
        onView(withRecyclerView(R.id.todo_recycler).atPosition(3))
            .check(matches(hasDescendant(withText("New to-do"))))
        onView(withId(R.id.todo_recycler))
            .check(hasSize(4))
    }
}