package com.github.jnuutinen.functional.ui

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.swipeLeft
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
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
import com.github.jnuutinen.functional.presentation.activity.TodosActivity
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class DeleteTodoTest {

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
        mActivityTestRule.launchActivity(null)
        Thread.sleep(1000)

        // Test the setup.
        val firstTodoName = "First list, first to-do"
        val secondTodoName = "First list, second to-do"
        val thirdTodoName = "First list, third to-do"
        onView(withRecyclerView(R.id.todo_recycler).atPosition(0))
            .check(matches(hasDescendant(withText(firstTodoName))))
        onView(withRecyclerView(R.id.todo_recycler).atPosition(1))
            .check(matches(hasDescendant(withText(secondTodoName))))
        onView(withRecyclerView(R.id.todo_recycler).atPosition(2))
            .check(matches(hasDescendant(withText(thirdTodoName))))
    }

    @Test
    fun deleteTodo_First() {
        val secondTodoName = "First list, second to-do"

        // Delete the first to-do by swiping it.
        deleteTodo(1)

        // The former second to-do should now be the first one.
        onView(withRecyclerView(R.id.todo_recycler).atPosition(0))
            .check(matches(hasDescendant(withText(secondTodoName))))

        // The number of to-dos should now be 2.
        onView(withId(R.id.todo_recycler))
            .check(hasSize(2))

        // Ensure state remains after orientation change and Activity restart.
        with(UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())) {
            setOrientationLeft()
            setOrientationNatural()
            Thread.sleep(2000)
        }

        // Make the same checks as before.
        onView(withRecyclerView(R.id.todo_recycler).atPosition(0))
            .check(matches(hasDescendant(withText(secondTodoName))))

        onView(withId(R.id.todo_recycler))
            .check(hasSize(2))

    }

    @Test
    fun deleteTodo_Middle() {
        val thirdTodoName = "First list, third to-do"

        // Delete the second to-do by swiping it.
        deleteTodo(1)

        // The former third to-do should now be the second one.
        onView(withRecyclerView(R.id.todo_recycler).atPosition(1))
            .check(matches(hasDescendant(withText(thirdTodoName))))

        // The number of to-dos should now be 2.
        onView(withId(R.id.todo_recycler))
            .check(hasSize(2))

        // Ensure state remains after orientation change and Activity restart.
        with(UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())) {
            setOrientationLeft()
            setOrientationNatural()
            Thread.sleep(2000)
        }

        // Make the same checks as before.
        onView(withRecyclerView(R.id.todo_recycler).atPosition(1))
            .check(matches(hasDescendant(withText(thirdTodoName))))

        onView(withId(R.id.todo_recycler))
            .check(hasSize(2))
    }

    @Test
    fun deleteTodo_Last() {
        val secondTodoName = "First list, second to-do"

        // Delete the third to-do by swiping it.
        deleteTodo(1)

        // The former second to-do should still be the second one.
        onView(withRecyclerView(R.id.todo_recycler).atPosition(1))
            .check(matches(hasDescendant(withText(secondTodoName))))

        // The number of to-dos should now be 2.
        onView(withId(R.id.todo_recycler))
            .check(hasSize(2))

        // Ensure state remains after orientation change and Activity restart.
        with(UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())) {
            setOrientationLeft()
            setOrientationNatural()
            Thread.sleep(2000)
        }

        // Make the same checks as before.
        onView(withRecyclerView(R.id.todo_recycler).atPosition(1))
            .check(matches(hasDescendant(withText(secondTodoName))))

        onView(withId(R.id.todo_recycler))
            .check(hasSize(2))
    }

    @Test
    fun deleteTodo_Middle_InAnotherList() {
        // Switch to the second test list.
        openNavigationDrawer(mActivityTestRule.activity)
        onView(withText("Second to-do list"))
            .perform(click())

        val thirdTodoName = "Second list, third to-do"

        // Delete the second to-do by swiping it.
        deleteTodo(1)

        // The former third to-do should now be the second one.
        onView(withRecyclerView(R.id.todo_recycler).atPosition(1))
            .check(matches(hasDescendant(withText(thirdTodoName))))

        // The number of to-dos should now be 2.
        onView(withId(R.id.todo_recycler))
            .check(hasSize(2))

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

        // Make the same checks as before.
        onView(withRecyclerView(R.id.todo_recycler).atPosition(1))
            .check(matches(hasDescendant(withText(thirdTodoName))))

        onView(withId(R.id.todo_recycler))
            .check(hasSize(2))
    }

    @Test
    fun deleteTodo_Middle_Undo() {
        val secondTodoName = "First list, second to-do"
        val thirdTodoName = "First list, third to-do"

        // Delete the second to-do by swiping it.
        deleteTodo(1)

        // The former third to-do should now be the second one.
        onView(withRecyclerView(R.id.todo_recycler).atPosition(1))
            .check(matches(hasDescendant(withText(thirdTodoName))))

        // The number of to-dos should now be 2.
        onView(withId(R.id.todo_recycler))
            .check(hasSize(2))

        Thread.sleep(1000)

        // Undo the deletion.
        onView(withText("UNDO"))
            .perform(click())

        // We should now be in the original state.
        onView(withRecyclerView(R.id.todo_recycler).atPosition(1))
            .check(matches(hasDescendant(withText(secondTodoName))))
        onView(withRecyclerView(R.id.todo_recycler).atPosition(2))
            .check(matches(hasDescendant(withText(thirdTodoName))))

        // The number of to-dos should now be 3.
        onView(withId(R.id.todo_recycler))
            .check(hasSize(3))
    }

    @Test
    fun deleteTwoTodos() {
        val firstTodoName = "First list, first to-do"

        // Delete the second to-do twice to delete the middle and last to-dos.
        deleteTodo(1)
        deleteTodo(1)

        // Only the first to-do should remain.
        onView(withRecyclerView(R.id.todo_recycler).atPosition(0))
            .check(matches(hasDescendant(withText(firstTodoName))))

        // The number of to-dos should now be 1.
        onView(withId(R.id.todo_recycler))
            .check(hasSize(1))
    }

    @Test
    fun deleteTwoTodos_Undo() {
        val firstTodoName = "First list, first to-do"
        val thirdTodoName = "First list, third to-do"

        // Delete the second to-do by swiping it.
        deleteTodo(1)

        // Delete the first to-do by swiping it.
        deleteTodo(0)

        // Undo the deletion of the first to-do.
        onView(withText("UNDO"))
            .perform(click())

        // The first and third to-do should remain.
        onView(withRecyclerView(R.id.todo_recycler).atPosition(0))
            .check(matches(hasDescendant(withText(firstTodoName))))
        onView(withRecyclerView(R.id.todo_recycler).atPosition(1))
            .check(matches(hasDescendant(withText(thirdTodoName))))
    }

    private fun deleteTodo(index: Int) {
        onView(withRecyclerView(R.id.todo_recycler).atPosition(index))
            .perform(swipeLeft())
        Thread.sleep(1000)
    }
}