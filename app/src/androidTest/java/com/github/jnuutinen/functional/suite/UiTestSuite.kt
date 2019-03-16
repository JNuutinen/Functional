package com.github.jnuutinen.functional.suite

import com.github.jnuutinen.functional.ui.*
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
    CreateTaskTest::class,
    DeleteTaskTest::class,
    EditTaskTest::class,
    EditTaskListTest::class,
    DeleteTaskListTest::class,
    CreateTaskListTest::class
)
class UiTestSuite