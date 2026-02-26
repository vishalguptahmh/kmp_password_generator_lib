package io.github.kotlin.passwordgenerator

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Android implementation of PasswordGeneratorRepository using SharedPreferences.
 * 
 * Example usage:
 * ```
 * val repository = AndroidPasswordGeneratorRepository(context)
 * val useCase = PasswordGeneratorUseCase(repository)
 * ```
 */
class AndroidPasswordGeneratorRepository(
    private val context: Context,
    private val preferencesName: String = "password_generator_prefs"
) : PasswordGeneratorRepository {

    private val prefs: SharedPreferences
        get() = context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE)

    override suspend fun savePasswordSettings(settings: PasswordSettings) {
        withContext(Dispatchers.IO) {
            prefs.edit().apply {
                putBoolean(KEY_PASSWORD_UPPERCASE, settings.includeUppercase)
                putBoolean(KEY_PASSWORD_LOWERCASE, settings.includeLowercase)
                putBoolean(KEY_PASSWORD_NUMBERS, settings.includeNumbers)
                putBoolean(KEY_PASSWORD_SPECIAL_CHARS, settings.includeSpecialChars)
                apply()
            }
        }
    }

    override suspend fun getPasswordSettings(): PasswordSettings = withContext(Dispatchers.IO) {
        PasswordSettings(
            includeUppercase = prefs.getBoolean(KEY_PASSWORD_UPPERCASE, false),
            includeLowercase = prefs.getBoolean(KEY_PASSWORD_LOWERCASE, true),
            includeNumbers = prefs.getBoolean(KEY_PASSWORD_NUMBERS, false),
            includeSpecialChars = prefs.getBoolean(KEY_PASSWORD_SPECIAL_CHARS, false)
        )
    }

    override suspend fun savePassphraseSettings(settings: PassphraseSettings) {
        withContext(Dispatchers.IO) {
            prefs.edit().apply {
                putInt(KEY_PASSPHRASE_WORD_COUNT, settings.wordCount)
                putString(KEY_PASSPHRASE_SEPARATOR, settings.separator)
                putBoolean(KEY_PASSPHRASE_UPPERCASE, settings.includeUppercase)
                putBoolean(KEY_PASSPHRASE_LOWERCASE, settings.includeLowercase)
                putBoolean(KEY_PASSPHRASE_NUMBERS, settings.includeNumbers)
                putBoolean(KEY_PASSPHRASE_SPECIAL_CHARS, settings.includeSpecialChars)
                apply()
            }
        }
    }

    override suspend fun getPassphraseSettings(): PassphraseSettings = withContext(Dispatchers.IO) {
        PassphraseSettings(
            wordCount = prefs.getInt(KEY_PASSPHRASE_WORD_COUNT, PassphraseConstants.DEFAULT_WORD_COUNT),
            separator = prefs.getString(KEY_PASSPHRASE_SEPARATOR, "-") ?: "-",
            includeUppercase = prefs.getBoolean(KEY_PASSPHRASE_UPPERCASE, false),
            includeLowercase = prefs.getBoolean(KEY_PASSPHRASE_LOWERCASE, true),
            includeNumbers = prefs.getBoolean(KEY_PASSPHRASE_NUMBERS, false),
            includeSpecialChars = prefs.getBoolean(KEY_PASSPHRASE_SPECIAL_CHARS, false)
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
