package io.github.kotlin.passwordgenerator

import java.security.SecureRandom as JavaSecureRandom

actual class SecureRandom {
    private val random = JavaSecureRandom()
    
    actual fun nextInt(bound: Int): Int {
        require(bound > 0) { "Bound must be positive" }
        return random.nextInt(bound)
    }
}