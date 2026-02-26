package io.github.kotlin.passwordgenerator

/**
 * Settings for password generation.
 * 
 * @param includeUppercase Include uppercase letters (A-Z)
 * @param includeLowercase Include lowercase letters (a-z)
 * @param includeNumbers Include numbers (0-9)
 * @param includeSpecialChars Include special characters
 */
data class PasswordSettings(
    val includeUppercase: Boolean = false,
    val includeLowercase: Boolean = true,
    val includeNumbers: Boolean = false,
    val includeSpecialChars: Boolean = false
)
