package io.github.kotlin.passwordgenerator

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class PasswordGeneratorUseCaseTest {

    private val mockRepository = object : PasswordGeneratorRepository {
        private var savedSettings: PasswordSettings? = null
        
        override suspend fun savePasswordSettings(settings: PasswordSettings) {
            savedSettings = settings
        }

        override suspend fun getPasswordSettings(): PasswordSettings {
            return savedSettings ?: PasswordSettings()
        }

        override suspend fun savePassphraseSettings(settings: PassphraseSettings) {}
        override suspend fun getPassphraseSettings(): PassphraseSettings = PassphraseSettings()
    }

    private val useCase = PasswordGeneratorUseCase(mockRepository)

    @Test
    fun testPasswordGenerationWithAllCharacterSets() = runTest {
        val password = useCase(
            length = 16,
            includeUppercase = true,
            includeLowercase = true,
            includeNumbers = true,
            includeSpecialChars = true
        )

        assertEquals(16, password.length)
        assertTrue(password.any { it.isUpperCase() })
        assertTrue(password.any { it.isLowerCase() })
        assertTrue(password.any { it.isDigit() })
        assertTrue(password.any { "!#$%&()*+-./:;<=>?@[]^_{|}~\'".contains(it) })
    }

    @Test
    fun testPasswordGenerationOnlyLowercase() = runTest {
        val password = useCase(
            length = 10,
            includeUppercase = false,
            includeLowercase = true,
            includeNumbers = false,
            includeSpecialChars = false
        )

        assertEquals(10, password.length)
        assertTrue(password.all { it.isLowerCase() })
    }

    @Test
    fun testPasswordGenerationExcludeCharacters() = runTest {
        val password = useCase(
            length = 20,
            includeUppercase = true,
            includeLowercase = true,
            includeNumbers = true,
            includeSpecialChars = true,
            excludeCharacters = "0O1lI"
        )

        assertEquals(20, password.length)
        assertTrue(!password.contains('0'))
        assertTrue(!password.contains('O'))
        assertTrue(!password.contains('1'))
        assertTrue(!password.contains('l'))
        assertTrue(!password.contains('I'))
    }

    @Test
    fun testPasswordGenerationMinimumLength() = runTest {
        val password = useCase(
            length = 1,
            includeUppercase = false,
            includeLowercase = true,
            includeNumbers = false,
            includeSpecialChars = false
        )

        assertEquals(1, password.length)
    }

    @Test
    fun testPasswordGenerationMaximumLength() = runTest {
        val password = useCase(
            length = 128,
            includeUppercase = true,
            includeLowercase = true,
            includeNumbers = true,
            includeSpecialChars = true
        )

        assertEquals(128, password.length)
    }

    @Test
    fun testPasswordGenerationInvalidLengthTooShort() = runTest {
        assertFailsWith<IllegalArgumentException> {
            useCase(
                length = 0,
                includeUppercase = true,
                includeLowercase = true,
                includeNumbers = true,
                includeSpecialChars = true
            )
        }
    }

    @Test
    fun testPasswordGenerationInvalidLengthTooLong() = runTest {
        assertFailsWith<IllegalArgumentException> {
            useCase(
                length = 129,
                includeUppercase = true,
                includeLowercase = true,
                includeNumbers = true,
                includeSpecialChars = true
            )
        }
    }

    @Test
    fun testPasswordGenerationAllExcluded() = runTest {
        assertFailsWith<IllegalArgumentException> {
            useCase(
                length = 10,
                includeUppercase = false,
                includeLowercase = true,
                includeNumbers = false,
                includeSpecialChars = false,
                excludeCharacters = "abcdefghijklmnopqrstuvwxyz"
            )
        }
    }

    @Test
    fun testPasswordRegenerateDoesNotSaveSettings() = runTest {
        useCase(
            length = 10,
            includeUppercase = true,
            includeLowercase = true,
            includeNumbers = false,
            includeSpecialChars = false,
            isPasswordRegenerate = true
        )

        val settings = mockRepository.getPasswordSettings()
        assertEquals(PasswordSettings(), settings)
    }

    @Test
    fun testPasswordGenerationSavesSettings() = runTest {
        useCase(
            length = 10,
            includeUppercase = true,
            includeLowercase = false,
            includeNumbers = true,
            includeSpecialChars = true,
            isPasswordRegenerate = false
        )

        val settings = mockRepository.getPasswordSettings()
        assertTrue(settings.includeUppercase)
        assertTrue(!settings.includeLowercase)
        assertTrue(settings.includeNumbers)
        assertTrue(settings.includeSpecialChars)
    }

    @Test
    fun testPasswordGenerationDefaultFallback() = runTest {
        val password = useCase(
            length = 8,
            includeUppercase = false,
            includeLowercase = false,
            includeNumbers = false,
            includeSpecialChars = false
        )

        assertEquals(8, password.length)
        assertTrue(password.all { it.isLowerCase() })
    }
}
