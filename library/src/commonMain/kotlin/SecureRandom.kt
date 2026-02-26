package io.github.kotlin.passwordgenerator

/**
 * Platform-agnostic secure random number generator.
 * Uses platform-specific cryptographically secure implementations.
 */
expect class SecureRandom() {
    /**
     * Returns a random integer between 0 (inclusive) and bound (exclusive).
     * 
     * @param bound Upper bound (exclusive), must be positive
     * @return Random integer in range [0, bound)
     * @throws IllegalArgumentException if bound <= 0
     */
    fun nextInt(bound: Int): Int
}