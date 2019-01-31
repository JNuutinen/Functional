package com.github.jnuutinen.functional.unit

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.impl.WorkDatabaseMigrations.MIGRATION_1_2
import com.github.jnuutinen.functional.data.db.TodoDatabase
import com.github.jnuutinen.functional.util.DB_NAME
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.hasItemInArray
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class DatabaseMigrationTest {
    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        TodoDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    @Throws(IOException::class)
    fun migrate1To2() {
        // FIXME: Does not work, see:
        // https://stackoverflow.com/questions/54390911/what-causes-no-such-table-alarminfo-in-room-database-migration-tests
        helper.createDatabase(DB_NAME, 1).apply {
            execSQL("INSERT INTO todo_group VALUES (1, 'list1', 42)")
            execSQL("INSERT INTO todo VALUES (1, 'todo1', 42, 42, 1)")
            execSQL("INSERT INTO todo VALUES(2, 'todo2', 75, 42, 1)")
            close()
        }

        helper.runMigrationsAndValidate(DB_NAME, 2, true, MIGRATION_1_2).apply {
            // Check to-do list.
            var cursor = query("SELECT * FROM todo_group")
            assertThat(cursor.columnCount, `is`(3))
            assertThat(cursor.count, `is`(1))

            var columnNames = cursor.columnNames
            assertThat(columnNames, hasItemInArray("group_id"))
            assertThat(columnNames, hasItemInArray("group_name"))
            assertThat(columnNames, hasItemInArray("group_date"))

            cursor.moveToNext()
            assertThat(cursor.getInt(0), `is`(1))
            assertThat(cursor.getString(1), `is`("list1"))
            assertThat(cursor.getLong(2), `is`(42L))

            // Check to-dos.
            cursor = query("SELECT * FROM todo")
            assertThat(cursor.columnCount, `is`(6))
            assertThat(cursor.count, `is`(2))

            columnNames = cursor.columnNames
            assertThat(columnNames, hasItemInArray("todo_id"))
            assertThat(columnNames, hasItemInArray("todo_contents"))
            assertThat(columnNames, hasItemInArray("todo_date"))
            assertThat(columnNames, hasItemInArray("todo_color"))
            assertThat(columnNames, hasItemInArray("todo_order"))
            assertThat(columnNames, hasItemInArray("todo_group_id"))

            cursor.moveToNext()
            assertThat(cursor.getInt(0), `is`(1))
            assertThat(cursor.getString(1), `is`("todo1"))
            assertThat(cursor.getLong(2), `is`(42L))
            assertThat(cursor.getInt(3), `is`(42))
            assertThat(cursor.getInt(4), `is`(0)) // the added order column
            assertThat(cursor.getInt(5), `is`(1))

            cursor.moveToNext()
            assertThat(cursor.getInt(0), `is`(2))
            assertThat(cursor.getString(1), `is`("todo2"))
            assertThat(cursor.getLong(2), `is`(75L))
            assertThat(cursor.getInt(3), `is`(42))
            assertThat(cursor.getInt(4), `is`(1)) // the added order column
            assertThat(cursor.getInt(5), `is`(1))
            close()
        }
    }
}