package com.github.jnuutinen.functional.ui

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
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
class EditTaskListTest {

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
    fun editList() {
        // Edit the second list.
        openNavigationDrawer(mActivityTestRule.activity)
        onView(withText("Second task list"))
            .perform(click())
        onView(withId(R.id.action_edit_list))
            .perform(click())
        onView(withId(R.id.edit_list_edit))
            .perform(replaceText("Edited task list"), closeSoftKeyboard())
        onView(withText(R.string.action_save))
            .perform(click())

        // Toolbar title should have updated.
        onView(withId(R.id.toolbar))
            .check(matches(hasDescendant(withText("Edited task list"))))

        // Visit another list and come back to check that the navigation drawer text is updated.
        openNavigationDrawer(mActivityTestRule.activity)
        onView(withText("First task list"))
            .perform(click())
        onView(withId(R.id.toolbar))
            .check(matches(hasDescendant(withText("First task list"))))
        openNavigationDrawer(mActivityTestRule.activity)
        onView(withText("Edited task list"))
            .perform(click())
        onView(withId(R.id.toolbar))
            .check(matches(hasDescendant(withText("Edited task list"))))
    }

    @Test
    fun editList_WithEmptyContents() {
        // Edit the second list.
        openNavigationDrawer(mActivityTestRule.activity)
        onView(withText("Second task list"))
            .perform(click())
        onView(withId(R.id.action_edit_list))
            .perform(click())
        onView(withId(R.id.edit_list_edit))
            .perform(replaceText(""), closeSoftKeyboard())
        onView(withText(R.string.action_save))
            .perform(click())

        // Info Snackbar should be visible.
        onView(withId(R.id.main_coordinator))
            .check(matches(hasDescendant(withText(R.string.alert_list_name_empty))))

        // Toolbar title should NOT have updated.
        onView(withId(R.id.toolbar))
            .check(matches(hasDescendant(withText("Second task list"))))
    }
}
