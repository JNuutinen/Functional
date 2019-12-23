package com.github.jnuutinen.functional.helper

import android.app.Activity
import androidx.appcompat.widget.Toolbar
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import com.github.jnuutinen.functional.R

class UiTestHelper {

    companion object {
        fun openNavigationDrawer(activity: Activity) {
            val navContDesc = activity.findViewById<Toolbar>(R.id.toolbar)
                .navigationContentDescription as String
            onView(withContentDescription(navContDesc))
                .perform(click())
        }
    }
}
