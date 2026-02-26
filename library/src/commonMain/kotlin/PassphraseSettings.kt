package io.github.kotlin.passwordgenerator

/**
 * Settings for passphrase generation.
 * 
 * @param wordCount Number of words in the passphrase
 * @param separator String used to separate words
 * @param includeUppercase Whether to randomly capitalize words
 * @param includeLowercase Whether to keep words lowercase
 * @param includeNumbers Whether to add numbers to words
 * @param includeSpecialChars Whether to add special characters to words
 */
data class PassphraseSettings(
    val wordCount: Int = PassphraseConstants.DEFAULT_WORD_COUNT,
    val separator: String = "-",
    val includeUppercase: Boolean = false,
    val includeLowercase: Boolean = true,
    val includeNumbers: Boolean = false,
    val includeSpecialChars: Boolean = false
)
