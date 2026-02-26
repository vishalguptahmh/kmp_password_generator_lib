package io.github.kotlin.passwordgenerator

import kotlinx.coroutines.test.runTest as kotlinxRunTest

actual fun runTest(block: suspend () -> Unit) {
    kotlinxRunTest {
        block()
    }
}
