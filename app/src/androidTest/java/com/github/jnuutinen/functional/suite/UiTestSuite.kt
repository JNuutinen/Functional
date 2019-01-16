package com.github.jnuutinen.functional.suite

import com.github.jnuutinen.functional.ui.*
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
    CreateTodoTest::class,
    DeleteTodoTest::class,
    EditTodoTest::class,
    EditTodoListTest::class,
    DeleteTodoListTest::class,
    CreateTodoListTest::class
)
class UiTestSuite