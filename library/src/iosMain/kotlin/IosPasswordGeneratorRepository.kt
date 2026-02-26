package io.github.kotlin.passwordgenerator

import platform.Foundation.NSUserDefaults

/**
 * iOS implementation of PasswordGeneratorRepository using NSUserDefaults.
 * 
 * Example usage:
 * ```
 * let repository = IosPasswordGeneratorRepository()
 * let useCase = PasswordGeneratorUseCase(repository: repository)
 * ```
 */
class IosPasswordGeneratorRepository(
    private val userDefaults: NSUserDefaults = NSUserDefaults.standardUserDefaults
) : PasswordGeneratorRepository {

    override suspend fun savePasswordSettings(settings: PasswordSettings) {
        userDefaults.setBool(settings.includeUppercase, forKey = KEY_PASSWORD_UPPERCASE)
        userDefaults.setBool(settings.includeLowercase, forKey = KEY_PASSWORD_LOWERCASE)
        userDefaults.setBool(settings.includeNumbers, forKey = KEY_PASSWORD_NUMBERS)
        userDefaults.setBool(settings.includeSpecialChars, forKey = KEY_PASSWORD_SPECIAL_CHARS)
        userDefaults.synchronize()
    }

    override suspend fun getPasswordSettings(): PasswordSettings {
        return PasswordSettings(
            includeUppercase = userDefaults.boolForKey(KEY_PASSWORD_UPPERCASE),
            includeLowercase = userDefaults.boolForKey(KEY_PASSWORD_LOWERCASE) ?: true,
            includeNumbers = userDefaults.boolForKey(KEY_PASSWORD_NUMBERS),
            includeSpecialChars = userDefaults.boolForKey(KEY_PASSWORD_SPECIAL_CHARS)
        )
    }

    override suspend fun savePassphraseSettings(settings: PassphraseSettings) {
        userDefaults.setInteger(settings.wordCount.toLong(), forKey = KEY_PASSPHRASE_WORD_COUNT)
        userDefaults.setObject(settings.separator, forKey = KEY_PASSPHRASE_SEPARATOR)
        userDefaults.setBool(settings.includeUppercase, forKey = KEY_PASSPHRASE_UPPERCASE)
        userDefaults.setBool(settings.includeLowercase, forKey = KEY_PASSPHRASE_LOWERCASE)
        userDefaults.setBool(settings.includeNumbers, forKey = KEY_PASSPHRASE_NUMBERS)
        userDefaults.setBool(settings.includeSpecialChars, forKey = KEY_PASSPHRASE_SPECIAL_CHARS)
        userDefaults.synchronize()
    }

    override suspend fun getPassphraseSettings(): PassphraseSettings {
        val wordCount = userDefaults.integerForKey(KEY_PASSPHRASE_WORD_COUNT).toInt()
        val separator = userDefaults.stringForKey(KEY_PASSPHRASE_SEPARATOR) ?: "-"
        return PassphraseSettings(
            wordCount = if (wordCount > 0) wordCount else PassphraseConstants.DEFAULT_WORD_COUNT,
            separator = separator,
            includeUppercase = userDefaults.boolForKey(KEY_PASSPHRASE_UPPERCASE),
            includeLowercase = userDefaults.boolForKey(KEY_PASSPHRASE_LOWERCASE) ?: true,
            includeNumbers = userDefaults.boolForKey(KEY_PASSPHRASE_NUMBERS),
            includeSpecialChars = userDefaults.boolForKey(KEY_PASSPHRASE_SPECIAL_CHARS)
        )
    }

    companion object {
        private const val KEY_PASSWORD_UPPERCASE = "password_uppercase"
        private const val KEY_PASSWORD_LOWERCASE = "password_lowercase"
        private const val KEY_PASSWORD_NUMBERS = "password_numbers"
        private const val KEY_PASSWORD_SPECIAL_CHARS = "password_special_chars"
        private const val KEY_PASSPHRASE_WORD_COUNT = "passphrase_word_count"
        private const val KEY_PASSPHRASE_SEPARATOR = "passphrase_separator"
        private const val KEY_PASSPHRASE_UPPERCASE = "passphrase_uppercase"
        private const val KEY_PASSPHRASE_LOWERCASE = "passphrase_lowercase"
        private const val KEY_PASSPHRASE_NUMBERS = "passphrase_numbers"
        private const val KEY_PASSPHRASE_SPECIAL_CHARS = "passphrase_special_chars"
    }
}
