package com.github.jnuutinen.functional.helper

import android.content.Context
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.github.jnuutinen.functional.util.PREF_KEY_ACTIVE_LIST_ID
import com.github.jnuutinen.functional.workers.DatabaseClearWorker

class TestDatabaseHelper {

    companion object {

        /**
         * Set the active list shared preference value.
         *
         * Call this before launching the Activity under test.
         *
         * @param context The application target context.
         * @param id The id of the task list to be set as active.
         */
        fun setActiveListSharedPref(context: Context, id: Int) {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            prefs.edit { putInt(PREF_KEY_ACTIVE_LIST_ID, id) }
        }

        /**
         * First clear the existing database, and then populate it with test data.
         *
         * Call this before launching the Activity under test.
         */
        fun repopulateDb() {
            val workManager = WorkManager.getInstance()
            val clear = OneTimeWorkRequestBuilder<DatabaseClearWorker>().build()
            val populate = OneTimeWorkRequestBuilder<TestDbPopulateWorker>().build()
            workManager.beginWith(clear).then(populate).enqueue()
        }
    }
}