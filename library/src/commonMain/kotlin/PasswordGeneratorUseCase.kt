package io.github.kotlin.passwordgenerator

/**
 * Use case for generating secure passwords with customizable character sets.
 * 
 * @param repository Repository for persisting password generation settings
 */
class PasswordGeneratorUseCase(private val repository: PasswordGeneratorRepository) {

    /**
     * Generates a secure password with the specified parameters.
     * 
     * @param length Password length (must be between 1 and 128)
     * @param includeUppercase Include uppercase letters (A-Z)
     * @param includeLowercase Include lowercase letters (a-z)
     * @param includeNumbers Include numbers (0-9)
     * @param includeSpecialChars Include special characters
     * @param excludeCharacters Characters to exclude from the password
     * @param isPasswordRegenerate If true, doesn't save settings to repository
     * @return Generated password
     * @throws IllegalArgumentException if length is invalid or all character sets are excluded
     */
    suspend operator fun invoke(
        length: Int,
        includeUppercase: Boolean,
        includeLowercase: Boolean,
        includeNumbers: Boolean,
        includeSpecialChars: Boolean,
        excludeCharacters: String = "",
        isPasswordRegenerate: Boolean = false
    ): String {
        require(length in 1..128) { "Password length must be between 1 and 128" }
        // Save settings if not regenerating
        if (!isPasswordRegenerate) {
            repository.savePasswordSettings(
                PasswordSettings(
                    includeUppercase = includeUppercase,
                    includeLowercase = includeLowercase,
                    includeNumbers = includeNumbers,
                    includeSpecialChars = includeSpecialChars
                )
            )
        }
        // Define character sets
        val uppercase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val lowercase = "abcdefghijklmnopqrstuvwxyz"
        val numbers = "0123456789"
        val specialChars = "!#$%&()*+-./:;<=>?@[]^_{|}~\'"


        val charSet = StringBuilder()
        if (includeUppercase) charSet.append(uppercase)
        if (includeLowercase) charSet.append(lowercase)
        if (includeNumbers) charSet.append(numbers)
        if (includeSpecialChars) charSet.append(specialChars)

        // Ensure at least one character set is available
        if (charSet.isEmpty()) {
            charSet.append(lowercase)
        }

        // Exclude specified characters from charset
        excludeCharacters.replace(",", "").toCharArray().forEach { char ->
            val index = charSet.indexOf(char)
            if (index >= 0) {
                charSet.deleteAt(index)
            }
        }
        
        require(charSet.isNotEmpty()) { 
            "Cannot generate password: all character sets excluded or invalid excludeCharacters specified" 
        }

        val random = SecureRandom()
        val password = StringBuilder()
        val requiredChars = mutableListOf<Char>()



        // Ensure at least one character from each selected set (if not already satisfied)
        if (includeUppercase && uppercase.isNotEmpty() && requiredChars.none { it in uppercase }) {
            requiredChars.add(uppercase[random.nextInt(uppercase.length)])
        }
        if (includeLowercase && lowercase.isNotEmpty() && requiredChars.none { it in lowercase }) {
            requiredChars.add(lowercase[random.nextInt(lowercase.length)])
        }

        if (includeNumbers && numbers.isNotEmpty() && requiredChars.none { it in numbers }) {
            requiredChars.add(numbers[random.nextInt(numbers.length)])
        }

        if (includeSpecialChars && specialChars.isNotEmpty() && requiredChars.none { it in specialChars }) {
            requiredChars.add(specialChars[random.nextInt(specialChars.length)])
        }

        // Fill the rest with random characters
        val remainingLength = length - requiredChars.size
        if (remainingLength > 0) {
            repeat(remainingLength) {
                password.append(charSet[random.nextInt(charSet.length)])
            }
        }

        // Insert required characters at random positions
        requiredChars.forEach { char ->
            val position = if (password.isEmpty()) 0 else random.nextInt(password.length + 1)
            password.insert(position, char)
        }

        // Trim to exact length
        return password.take(length).toString()
    }

}