package io.github.kotlin.passwordgenerator

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class PassphraseGeneratorUseCaseTest {

    private val mockRepository = object : PasswordGeneratorRepository {
        private var savedSettings: PassphraseSettings? = null
        
        override suspend fun savePasswordSettings(settings: PasswordSettings) {}
        override suspend fun getPasswordSettings(): PasswordSettings = PasswordSettings()

        override suspend fun savePassphraseSettings(settings: PassphraseSettings) {
            savedSettings = settings
        }

        override suspend fun getPassphraseSettings(): PassphraseSettings {
            return savedSettings ?: PassphraseSettings()
        }
    }

    private val useCase = PassphraseGeneratorUseCase(mockRepository)

    @Test
    fun testPassphraseGenerationBasic() = runTest {
        val passphrase = useCase(
            wordCount = 4,
            separator = "-",
            includeUppercase = false,
            includeLowercase = true,
            includeNumbers = false,
            includeSpecialChars = false
        )

        val words = passphrase.split("-")
        assertEquals(4, words.size)
        assertTrue(words.all { it.isNotEmpty() })
    }

    @Test
    fun testPassphraseGenerationWithCustomSeparator() = runTest {
        val passphrase = useCase(
            wordCount = 3,
            separator = " ",
            includeUppercase = false,
            includeLowercase = true,
            includeNumbers = false,
            includeSpecialChars = false
        )

        val words = passphrase.split(" ")
        assertEquals(3, words.size)
    }

    @Test
    fun testPassphraseGenerationMinimumWordCount() = runTest {
        val passphrase = useCase(
            wordCount = PassphraseConstants.MIN_WORD_COUNT,
            separator = "-",
            includeUppercase = false,
            includeLowercase = true,
            includeNumbers = false,
            includeSpecialChars = false
        )

        val words = passphrase.split("-")
        assertEquals(PassphraseConstants.MIN_WORD_COUNT, words.size)
    }

    @Test
    fun testPassphraseGenerationMaximumWordCount() = runTest {
        val passphrase = useCase(
            wordCount = PassphraseConstants.MAX_WORD_COUNT,
            separator = "-",
            includeUppercase = false,
            includeLowercase = true,
            includeNumbers = false,
            includeSpecialChars = false
        )

        val words = passphrase.split("-")
        assertEquals(PassphraseConstants.MAX_WORD_COUNT, words.size)
    }

    @Test
    fun testPassphraseGenerationInvalidWordCountTooLow() = runTest {
        assertFailsWith<IllegalArgumentException> {
            useCase(
                wordCount = 0,
                separator = "-",
                includeUppercase = false,
                includeLowercase = true,
                includeNumbers = false,
                includeSpecialChars = false
            )
        }
    }

    @Test
    fun testPassphraseGenerationInvalidWordCountTooHigh() = runTest {
        assertFailsWith<IllegalArgumentException> {
            useCase(
                wordCount = 21,
                separator = "-",
                includeUppercase = false,
                includeLowercase = true,
                includeNumbers = false,
                includeSpecialChars = false
            )
        }
    }

    @Test
    fun testPassphraseGenerationSavesSettings() = runTest {
        useCase(
            wordCount = 5,
            separator = "_",
            includeUppercase = true,
            includeLowercase = false,
            includeNumbers = true,
            includeSpecialChars = true,
            isPasswordRegenerate = false
        )

        val settings = mockRepository.getPassphraseSettings()
        assertEquals(5, settings.wordCount)
        assertEquals("_", settings.separator)
        assertTrue(settings.includeUppercase)
        assertTrue(!settings.includeLowercase)
        assertTrue(settings.includeNumbers)
        assertTrue(settings.includeSpecialChars)
    }

    @Test
    fun testPassphraseRegenerateDoesNotSaveSettings() = runTest {
        useCase(
            wordCount = 6,
            separator = "-",
            includeUppercase = true,
            includeLowercase = true,
            includeNumbers = false,
            includeSpecialChars = false,
            isPasswordRegenerate = true
        )

        val settings = mockRepository.getPassphraseSettings()
        assertEquals(PassphraseSettings(), settings)
    }
}
