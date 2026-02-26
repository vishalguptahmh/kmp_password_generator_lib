# Quick Integration Guide

This guide provides step-by-step instructions for integrating the Password Generator library into Android and iOS apps.

## Android Integration

### Step 1: Add Dependency

**Published (Maven Central):**
```kotlin
// app/build.gradle.kts
dependencies {
    implementation("io.github.kotlin:library:1.0.0")
}
```

**Local Development:**
```kotlin
// settings.gradle.kts
include(":password-generator")
project(":password-generator").projectDir = file("../path/to/multiplatform-library-template-main/library")

// app/build.gradle.kts
dependencies {
    implementation(project(":password-generator"))
}
```

### Step 2: Initialize Repository and Use Cases

```kotlin
import io.github.kotlin.passwordgenerator.*

class MainActivity : AppCompatActivity() {
    private val repository = AndroidPasswordGeneratorRepository(this)
    private val passwordUseCase = PasswordGeneratorUseCase(repository)
    private val passphraseUseCase = PassphraseGeneratorUseCase(repository)
    
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        scope.launch {
            val password = passwordUseCase(
                length = 16,
                includeUppercase = true,
                includeLowercase = true,
                includeNumbers = true,
                includeSpecialChars = true
            )
            // Use password
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}
```

## iOS Integration

### Step 1: Build XCFramework

```bash
cd multiplatform-library-template-main
./gradlew :library:buildXCFramework
```

### Step 2: Add to Xcode Project

1. Open your iOS project in Xcode
2. File → Add Package Dependencies
3. Choose one:
   - **Local**: Add Local... → Select directory with `Package.swift`
   - **Remote**: Enter repository URL

### Step 3: Use in Swift Code

```swift
import PasswordGenerator

class PasswordService {
    private let repository = IosPasswordGeneratorRepository()
    private let passwordUseCase = PasswordGeneratorUseCase(repository: repository)
    
    func generatePassword() async throws -> String {
        return try await passwordUseCase.invoke(
            length: 16,
            includeUppercase: true,
            includeLowercase: true,
            includeNumbers: true,
            includeSpecialChars: true
        )
    }
}
```

## Common Patterns

### Regenerating with Saved Settings

**Android:**
```kotlin
scope.launch {
    val settings = repository.getPasswordSettings()
    val newPassword = passwordUseCase(
        length = 16,
        includeUppercase = settings.includeUppercase,
        includeLowercase = settings.includeLowercase,
        includeNumbers = settings.includeNumbers,
        includeSpecialChars = settings.includeSpecialChars,
        isPasswordRegenerate = true // Don't save again
    )
}
```

**iOS:**
```swift
let settings = try await repository.getPasswordSettings()
let newPassword = try await passwordUseCase.invoke(
    length: 16,
    includeUppercase: settings.includeUppercase,
    includeLowercase: settings.includeLowercase,
    includeNumbers: settings.includeNumbers,
    includeSpecialChars: settings.includeSpecialChars,
    isPasswordRegenerate: true
)
```

### Error Handling

**Android:**
```kotlin
scope.launch {
    try {
        val password = passwordUseCase(
            length = 16,
            includeUppercase = true,
            includeLowercase = true,
            includeNumbers = true,
            includeSpecialChars = true
        )
    } catch (e: IllegalArgumentException) {
        // Handle validation error
        Log.e("PasswordGen", "Invalid parameters: ${e.message}")
    }
}
```

**iOS:**
```swift
do {
    let password = try await passwordUseCase.invoke(
        length: 16,
        includeUppercase: true,
        includeLowercase: true,
        includeNumbers: true,
        includeSpecialChars: true
    )
} catch {
    // Handle error
    print("Error: \(error)")
}
```

## Next Steps

- See [README.md](README.md) for full API documentation
- See [ios-distribution-setup.md](ios-distribution-setup.md) for iOS distribution options
- Check the test files for usage examples
