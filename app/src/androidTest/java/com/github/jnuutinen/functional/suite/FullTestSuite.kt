package com.github.jnuutinen.functional.suite

import com.github.jnuutinen.functional.ui.CreateTaskListTest
import com.github.jnuutinen.functional.ui.CreateTaskTest
import com.github.jnuutinen.functional.ui.DeleteTaskListTest
import com.github.jnuutinen.functional.ui.DeleteTaskTest
import com.github.jnuutinen.functional.ui.EditTaskListTest
import com.github.jnuutinen.functional.ui.EditTaskTest
import com.github.jnuutinen.functional.unit.DatabaseMigrationTest
import com.github.jnuutinen.functional.unit.DatabaseTest
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
    DatabaseTest::class,
    DatabaseMigrationTest::class,
    CreateTaskTest::class,
    DeleteTaskTest::class,
    EditTaskTest::class,
    EditTaskListTest::class,
    DeleteTaskListTest::class,
    CreateTaskListTest::class
)
class FullTestSuite
