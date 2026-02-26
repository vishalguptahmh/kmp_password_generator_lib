package io.github.kotlin.passwordgenerator

/**
 * Repository interface for persisting password and passphrase generator settings.
 * Platform-specific implementations should handle storage (e.g., SharedPreferences on Android, UserDefaults on iOS).
 */
interface PasswordGeneratorRepository {

    /**
     * Save password generator settings
     */
    suspend fun savePasswordSettings(settings: PasswordSettings)

    /**
     * Get password generator settings
     */
    suspend fun getPasswordSettings(): PasswordSettings

    /**
     * Save passphrase generator settings
     */
    suspend fun savePassphraseSettings(settings: PassphraseSettings)

    /**
     * Get passphrase generator settings
     */
    suspend fun getPassphraseSettings(): PassphraseSettings
}