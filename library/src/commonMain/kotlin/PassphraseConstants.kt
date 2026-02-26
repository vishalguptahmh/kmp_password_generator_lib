package io.github.kotlin.passwordgenerator

/**
 * Constants for passphrase generation configuration
 */
object PassphraseConstants {
    const val ATTACH_FRONT = 0
    const val ATTACH_REAR = 1
    const val SKIP_ACTION = 0
    const val INCLUDE_UPPERCASE = 1
    const val INCLUDE_LOWERCASE = 2
    const val INCLUDE_NUMBERS = 3
    const val INCLUDE_SPECIAL_CHARACTERS = 4
    
    const val MIN_WORD_COUNT = 1
    const val MAX_WORD_COUNT = 20
    const val DEFAULT_WORD_COUNT = 4
}
