package com.github.jnuutinen.functional.suite

import com.github.jnuutinen.functional.ui.*
import com.github.jnuutinen.functional.unit.DatabaseTest
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
    DatabaseTest::class,
    CreateTodoTest::class,
    DeleteTodoTest::class,
    EditTodoTest::class,
    EditTodoListTest::class,
    DeleteTodoListTest::class
)
class FullTestSuite