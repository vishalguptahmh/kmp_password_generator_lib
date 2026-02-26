package io.github.kotlin.passwordgenerator

import platform.posix.arc4random_uniform

actual class SecureRandom {
    actual fun nextInt(bound: Int): Int {
        require(bound > 0) { "Bound must be positive" }
        return arc4random_uniform(bound.toUInt()).toInt()
    }
}