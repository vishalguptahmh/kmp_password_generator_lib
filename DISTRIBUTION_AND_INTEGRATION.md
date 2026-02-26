# Distribution and Integration Guide

This comprehensive guide covers how to create distribution bundles and integrate the Password Generator Kotlin Multiplatform library into various project types.

## Table of Contents

1. [Creating Distribution Bundles](#creating-distribution-bundles)
2. [Android Integration](#android-integration)
3. [iOS Integration](#ios-integration)
4. [Kotlin Multiplatform Integration](#kotlin-multiplatform-integration)
5. [Troubleshooting](#troubleshooting)
6. [Best Practices](#best-practices)

---

## Creating Distribution Bundles

### Prerequisites

Before building distribution bundles, ensure you have:

- **JDK 11 or higher** (JDK 21 recommended)
- **Android SDK** (for Android builds)
- **Xcode** (for iOS builds, macOS only)
- **Gradle** (included via Gradle Wrapper)

### 1. Android Distribution (JAR/AAR)

#### Building AAR for Android

The Android Archive (AAR) includes compiled classes, resources, and manifest files for Android projects.

```bash
# Build release AAR
./gradlew :library:assembleRelease

# Build debug AAR
./gradlew :library:assembleDebug

# Build both variants
./gradlew :library:assemble
```

**Output Location:**
```
library/build/outputs/aar/
├── library-release.aar
└── library-debug.aar
```

#### Building JAR for JVM

For pure JVM projects (non-Android), build the JVM JAR:

```bash
# Build JVM JAR
./gradlew :library:jvmJar

# Build with sources
./gradlew :library:jvmSourcesJar
```

**Output Location:**
```
library/build/libs/
├── library-jvm-1.0.0.jar
└── library-jvm-1.0.0-sources.jar
```

#### Publishing to Maven Central

To publish artifacts to Maven Central:

```bash
# Publish to Maven Central (requires credentials)
./gradlew publishToMavenCentral --no-configuration-cache
```

**Required Environment Variables:**
- `ORG_GRADLE_PROJECT_mavenCentralUsername` - Sonatype username
- `ORG_GRADLE_PROJECT_mavenCentralPassword` - Sonatype password
- `ORG_GRADLE_PROJECT_signingInMemoryKeyId` - GPG key ID
- `ORG_GRADLE_PROJECT_signingInMemoryKeyPassword` - GPG key password
- `ORG_GRADLE_PROJECT_signingInMemoryKey` - GPG private key contents

**Published Artifacts:**
- `io.github.kotlin:library:1.0.0` (Android AAR)
- `io.github.kotlin:library-jvm:1.0.0` (JVM JAR)
- Sources JARs
- Javadoc JARs

### 2. iOS Distribution (XCFramework)

#### Building XCFramework

The XCFramework bundles iOS frameworks for multiple architectures (device and simulator).

```bash
# Build XCFramework
./gradlew :library:buildXCFramework

# If you encounter configuration cache issues:
./gradlew :library:buildXCFramework --no-configuration-cache
```

**Output Location:**
```
library/build/XCFrameworks/PasswordGenerator.xcframework/
├── Info.plist
├── ios-arm64/
│   └── PasswordGenerator.framework
└── ios-arm64-simulator/
    └── PasswordGenerator.framework
```

**Architectures Included:**
- `ios-arm64` - Physical iOS devices (iPhone, iPad)
- `ios-arm64-simulator` - Apple Silicon simulators (M1/M2 Macs)

**Note:** Intel-based simulators (`ios-x86_64-simulator`) are excluded to avoid conflicts.

#### Creating ZIP Bundle for Distribution

For GitHub Releases or other distribution methods, create a ZIP archive:

```bash
# Create ZIP archive
cd library/build/XCFrameworks
zip -r PasswordGenerator.xcframework.zip PasswordGenerator.xcframework

# Generate checksum (required for SPM)
swift package compute-checksum PasswordGenerator.xcframework.zip
```

**Output:**
- `PasswordGenerator.xcframework.zip` - Ready for distribution
- Checksum string - Use in `Package.swift` for remote distribution

### 3. Complete Build (All Platforms)

To build all distribution artifacts at once:

```bash
# Build everything
./gradlew build

# Build and publish (if configured)
./gradlew publishToMavenCentral --no-configuration-cache
```

---

## Android Integration

### Option 1: Published Artifact (Maven Central)

#### Step 1: Add Repository

Add Maven Central to your `build.gradle.kts` or `build.gradle`:

```kotlin
// Project-level build.gradle.kts
repositories {
    mavenCentral()
    google()
    maven { url = uri("https://jitpack.io") }
}
```

#### Step 2: Add Dependency

Add the library dependency to your app module:

```kotlin
// app/build.gradle.kts
dependencies {
    implementation("io.github.kotlin:library:1.0.0")
    
    // If using coroutines (recommended)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
}
```

**For Groovy (`build.gradle`):**
```groovy
dependencies {
    implementation 'io.github.kotlin:library:1.0.0'
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1'
}
```

#### Step 3: Sync and Use

After syncing Gradle, import and use the library:

```kotlin
import io.github.kotlin.passwordgenerator.AndroidPasswordGeneratorRepository
import io.github.kotlin.passwordgenerator.PasswordGeneratorUseCase
import io.github.kotlin.passwordgenerator.PassphraseGeneratorUseCase
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {
    private val repository = AndroidPasswordGeneratorRepository(this)
    private val passwordUseCase = PasswordGeneratorUseCase(repository)
    private val passphraseUseCase = PassphraseGeneratorUseCase(repository)
    
    // Use lifecycleScope for automatic cancellation
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Generate password
        lifecycleScope.launch {
            val password = passwordUseCase(
                length = 16,
                includeUppercase = true,
                includeLowercase = true,
                includeNumbers = true,
                includeSpecialChars = true,
                excludeCharacters = "0O1lI" // Exclude ambiguous characters
            )
            Log.d("PasswordGen", "Generated: $password")
        }
    }
}
```

### Option 2: Local AAR File

#### Step 1: Build AAR

```bash
./gradlew :library:assembleRelease
```

#### Step 2: Copy AAR to Project

Copy the AAR file to your project's `libs` directory:

```bash
cp library/build/outputs/aar/library-release.aar your-app/libs/
```

#### Step 3: Add Dependency

```kotlin
// app/build.gradle.kts
dependencies {
    implementation(files("libs/library-release.aar"))
    
    // Or using fileTree
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.aar"))))
}
```

**Note:** When using AAR files directly, ensure transitive dependencies are included manually.

### Option 3: Local Module Dependency (Development)

For local development or when modifying the library:

#### Step 1: Add Module to Settings

```kotlin
// settings.gradle.kts
include(":password-generator")
project(":password-generator").projectDir = file("../multiplatform-library-template-main/library")
```

**For Groovy (`settings.gradle`):**
```groovy
include ':password-generator'
project(':password-generator').projectDir = new File('../multiplatform-library-template-main/library')
```

#### Step 2: Add Dependency

```kotlin
// app/build.gradle.kts
dependencies {
    implementation(project(":password-generator"))
}
```

### Complete Android Example

```kotlin
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import io.github.kotlin.passwordgenerator.*
import kotlinx.coroutines.launch

class PasswordActivity : AppCompatActivity() {
    private lateinit var repository: AndroidPasswordGeneratorRepository
    private lateinit var passwordUseCase: PasswordGeneratorUseCase
    private lateinit var passphraseUseCase: PassphraseGeneratorUseCase
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_password)
        
        // Initialize
        repository = AndroidPasswordGeneratorRepository(this)
        passwordUseCase = PasswordGeneratorUseCase(repository)
        passphraseUseCase = PassphraseGeneratorUseCase(repository)
        
        // Generate password
        lifecycleScope.launch {
            try {
                val password = passwordUseCase(
                    length = 20,
                    includeUppercase = true,
                    includeLowercase = true,
                    includeNumbers = true,
                    includeSpecialChars = true,
                    excludeCharacters = "0O1lI"
                )
                updatePasswordDisplay(password)
            } catch (e: IllegalArgumentException) {
                Log.e("PasswordGen", "Invalid parameters: ${e.message}")
                showError(e.message ?: "Invalid password settings")
            }
        }
        
        // Generate passphrase
        lifecycleScope.launch {
            try {
                val passphrase = passphraseUseCase(
                    wordCount = 4,
                    separator = "-",
                    includeUppercase = true,
                    includeLowercase = true,
                    includeNumbers = true,
                    includeSpecialChars = false
                )
                updatePassphraseDisplay(passphrase)
            } catch (e: IllegalArgumentException) {
                Log.e("PasswordGen", "Invalid parameters: ${e.message}")
                showError(e.message ?: "Invalid passphrase settings")
            }
        }
        
        // Load saved settings
        lifecycleScope.launch {
            val passwordSettings = repository.getPasswordSettings()
            val passphraseSettings = repository.getPassphraseSettings()
            
            // Pre-fill UI with saved settings
            restoreSettings(passwordSettings, passphraseSettings)
        }
    }
    
    private fun updatePasswordDisplay(password: String) {
        // Update UI
        runOnUiThread {
            findViewById<TextView>(R.id.passwordText).text = password
        }
    }
    
    private fun updatePassphraseDisplay(passphrase: String) {
        // Update UI
        runOnUiThread {
            findViewById<TextView>(R.id.passphraseText).text = passphrase
        }
    }
    
    private fun showError(message: String) {
        // Show error to user
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun restoreSettings(
        passwordSettings: PasswordSettings,
        passphraseSettings: PassphraseSettings
    ) {
        // Restore UI state from saved settings
        // ...
    }
    
    // Regenerate with saved settings
    fun regeneratePassword() {
        lifecycleScope.launch {
            val settings = repository.getPasswordSettings()
            val password = passwordUseCase(
                length = 16,
                includeUppercase = settings.includeUppercase,
                includeLowercase = settings.includeLowercase,
                includeNumbers = settings.includeNumbers,
                includeSpecialChars = settings.includeSpecialChars,
                isPasswordRegenerate = true // Don't save settings again
            )
            updatePasswordDisplay(password)
        }
    }
}
```

### Using with ViewModel

```kotlin
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.kotlin.passwordgenerator.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PasswordViewModel(
    private val context: Context
) : ViewModel() {
    private val repository = AndroidPasswordGeneratorRepository(context)
    private val passwordUseCase = PasswordGeneratorUseCase(repository)
    private val passphraseUseCase = PassphraseGeneratorUseCase(repository)
    
    private val _password = MutableStateFlow<String?>(null)
    val password: StateFlow<String?> = _password
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    
    fun generatePassword(
        length: Int = 16,
        includeUppercase: Boolean = true,
        includeLowercase: Boolean = true,
        includeNumbers: Boolean = true,
        includeSpecialChars: Boolean = true,
        excludeCharacters: String = ""
    ) {
        viewModelScope.launch {
            try {
                val result = passwordUseCase(
                    length = length,
                    includeUppercase = includeUppercase,
                    includeLowercase = includeLowercase,
                    includeNumbers = includeNumbers,
                    includeSpecialChars = includeSpecialChars,
                    excludeCharacters = excludeCharacters
                )
                _password.value = result
                _error.value = null
            } catch (e: IllegalArgumentException) {
                _error.value = e.message
                _password.value = null
            }
        }
    }
    
    fun generatePassphrase(
        wordCount: Int = 4,
        separator: String = "-",
        includeUppercase: Boolean = true,
        includeLowercase: Boolean = true,
        includeNumbers: Boolean = true,
        includeSpecialChars: Boolean = false
    ) {
        viewModelScope.launch {
            try {
                val result = passphraseUseCase(
                    wordCount = wordCount,
                    separator = separator,
                    includeUppercase = includeUppercase,
                    includeLowercase = includeLowercase,
                    includeNumbers = includeNumbers,
                    includeSpecialChars = includeSpecialChars
                )
                _password.value = result
                _error.value = null
            } catch (e: IllegalArgumentException) {
                _error.value = e.message
                _password.value = null
            }
        }
    }
}
```

---

## iOS Integration

### Option 1: Swift Package Manager (SPM) - Remote Distribution

#### Step 1: Build and Package XCFramework

```bash
# Build XCFramework
./gradlew :library:buildXCFramework --no-configuration-cache

# Create ZIP archive
cd library/build/XCFrameworks
zip -r PasswordGenerator.xcframework.zip PasswordGenerator.xcframework

# Generate checksum
swift package compute-checksum PasswordGenerator.xcframework.zip
```

#### Step 2: Upload to GitHub Releases

1. Create a GitHub release tag (e.g., `v1.0.0`)
2. Upload `PasswordGenerator.xcframework.zip` as a release asset
3. Copy the download URL (e.g., `https://github.com/username/repo/releases/download/v1.0.0/PasswordGenerator.xcframework.zip`)

#### Step 3: Update Package.swift

Update the `Package.swift` file in your repository:

```swift
// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "PasswordGenerator",
    platforms: [
        .iOS(.v13)
    ],
    products: [
        .library(
            name: "PasswordGenerator",
            targets: ["PasswordGenerator"]
        ),
    ],
    targets: [
        .binaryTarget(
            name: "PasswordGenerator",
            url: "https://github.com/YOUR_USERNAME/YOUR_REPO/releases/download/v1.0.0/PasswordGenerator.xcframework.zip",
            checksum: "PASTE_CHECKSUM_HERE"
        )
    ]
)
```

#### Step 4: Add to Xcode Project

1. Open your iOS project in Xcode
2. Go to **File → Add Package Dependencies**
3. Enter your repository URL: `https://github.com/YOUR_USERNAME/YOUR_REPO.git`
4. Select the version/tag (e.g., `1.0.0`)
5. Click **Add Package**

#### Step 5: Import and Use

```swift
import PasswordGenerator
import Foundation

class PasswordService {
    private let repository = IosPasswordGeneratorRepository()
    private let passwordUseCase = PasswordGeneratorUseCase(repository: repository)
    private let passphraseUseCase = PassphraseGeneratorUseCase(repository: repository)
    
    func generatePassword() async throws -> String {
        return try await passwordUseCase.invoke(
            length: 16,
            includeUppercase: true,
            includeLowercase: true,
            includeNumbers: true,
            includeSpecialChars: true,
            excludeCharacters: "0O1lI"
        )
    }
    
    func generatePassphrase() async throws -> String {
        return try await passphraseUseCase.invoke(
            wordCount: 4,
            separator: "-",
            includeUppercase: true,
            includeLowercase: true,
            includeNumbers: true,
            includeSpecialChars: false
        )
    }
}
```

### Option 2: Swift Package Manager (SPM) - Local Development

#### Step 1: Build XCFramework

```bash
./gradlew :library:buildXCFramework --no-configuration-cache
```

#### Step 2: Update Package.swift for Local Path

Ensure `Package.swift` uses a local path:

```swift
// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "PasswordGenerator",
    platforms: [
        .iOS(.v13)
    ],
    products: [
        .library(
            name: "PasswordGenerator",
            targets: ["PasswordGenerator"]
        ),
    ],
    targets: [
        .binaryTarget(
            name: "PasswordGenerator",
            path: "library/build/XCFrameworks/PasswordGenerator.xcframework"
        )
    ]
)
```

#### Step 3: Add Local Package to Xcode

1. Open your iOS project in Xcode
2. Go to **File → Add Package Dependencies**
3. Click **Add Local...**
4. Navigate to the directory containing `Package.swift`
5. Click **Add Package**

### Option 3: Manual Framework Integration

#### Step 1: Build XCFramework

```bash
./gradlew :library:buildXCFramework --no-configuration-cache
```

#### Step 2: Add Framework to Xcode

1. Open your iOS project in Xcode
2. Select your project in the navigator
3. Select your target
4. Go to **General → Frameworks, Libraries, and Embedded Content**
5. Click **+** button
6. Click **Add Other... → Add Files...**
7. Navigate to `library/build/XCFrameworks/PasswordGenerator.xcframework`
8. Select the XCFramework and click **Open**
9. Ensure **Embed & Sign** is selected

#### Step 3: Import and Use

```swift
import PasswordGenerator

// Use the same code as SPM integration
```

### Complete iOS Example (SwiftUI)

```swift
import SwiftUI
import PasswordGenerator

struct PasswordView: View {
    @StateObject private var viewModel = PasswordViewModel()
    @State private var password: String = ""
    @State private var errorMessage: String?
    
    var body: some View {
        VStack(spacing: 20) {
            Text(password.isEmpty ? "Tap to generate" : password)
                .font(.monospaced(.body)())
                .padding()
                .frame(maxWidth: .infinity)
                .background(Color.gray.opacity(0.1))
                .cornerRadius(8)
            
            if let error = errorMessage {
                Text(error)
                    .foregroundColor(.red)
                    .font(.caption)
            }
            
            Button("Generate Password") {
                Task {
                    do {
                        password = try await viewModel.generatePassword()
                        errorMessage = nil
                    } catch {
                        errorMessage = error.localizedDescription
                    }
                }
            }
            .buttonStyle(.borderedProminent)
            
            Button("Generate Passphrase") {
                Task {
                    do {
                        password = try await viewModel.generatePassphrase()
                        errorMessage = nil
                    } catch {
                        errorMessage = error.localizedDescription
                    }
                }
            }
            .buttonStyle(.bordered)
        }
        .padding()
    }
}

@MainActor
class PasswordViewModel: ObservableObject {
    private let repository = IosPasswordGeneratorRepository()
    private let passwordUseCase = PasswordGeneratorUseCase(repository: repository)
    private let passphraseUseCase = PassphraseGeneratorUseCase(repository: repository)
    
    func generatePassword() async throws -> String {
        return try await passwordUseCase.invoke(
            length: 16,
            includeUppercase: true,
            includeLowercase: true,
            includeNumbers: true,
            includeSpecialChars: true,
            excludeCharacters: "0O1lI"
        )
    }
    
    func generatePassphrase() async throws -> String {
        return try await passphraseUseCase.invoke(
            wordCount: 4,
            separator: "-",
            includeUppercase: true,
            includeLowercase: true,
            includeNumbers: true,
            includeSpecialChars: false
        )
    }
    
    func regenerateWithSavedSettings() async throws -> String {
        let settings = try await repository.getPasswordSettings()
        return try await passwordUseCase.invoke(
            length: 16,
            includeUppercase: settings.includeUppercase,
            includeLowercase: settings.includeLowercase,
            includeNumbers: settings.includeNumbers,
            includeSpecialChars: settings.includeSpecialChars,
            isPasswordRegenerate: true
        )
    }
}
```

### Complete iOS Example (UIKit)

```swift
import UIKit
import PasswordGenerator

class PasswordViewController: UIViewController {
    @IBOutlet weak var passwordLabel: UILabel!
    @IBOutlet weak var generateButton: UIButton!
    
    private let repository = IosPasswordGeneratorRepository()
    private lazy var passwordUseCase = PasswordGeneratorUseCase(repository: repository)
    private lazy var passphraseUseCase = PassphraseGeneratorUseCase(repository: repository)
    
    override func viewDidLoad() {
        super.viewDidLoad()
        setupUI()
    }
    
    private func setupUI() {
        passwordLabel.text = "Tap to generate"
        passwordLabel.font = .monospacedSystemFont(ofSize: 16, weight: .regular)
        generateButton.addTarget(self, action: #selector(generatePassword), for: .touchUpInside)
    }
    
    @objc private func generatePassword() {
        Task {
            do {
                let password = try await passwordUseCase.invoke(
                    length: 16,
                    includeUppercase: true,
                    includeLowercase: true,
                    includeNumbers: true,
                    includeSpecialChars: true,
                    excludeCharacters: "0O1lI"
                )
                await MainActor.run {
                    passwordLabel.text = password
                }
            } catch {
                await MainActor.run {
                    let alert = UIAlertController(
                        title: "Error",
                        message: error.localizedDescription,
                        preferredStyle: .alert
                    )
                    alert.addAction(UIAlertAction(title: "OK", style: .default))
                    present(alert, animated: true)
                }
            }
        }
    }
    
    @IBAction func generatePassphrase(_ sender: UIButton) {
        Task {
            do {
                let passphrase = try await passphraseUseCase.invoke(
                    wordCount: 4,
                    separator: "-",
                    includeUppercase: true,
                    includeLowercase: true,
                    includeNumbers: true,
                    includeSpecialChars: false
                )
                await MainActor.run {
                    passwordLabel.text = passphrase
                }
            } catch {
                await MainActor.run {
                    let alert = UIAlertController(
                        title: "Error",
                        message: error.localizedDescription,
                        preferredStyle: .alert
                    )
                    alert.addAction(UIAlertAction(title: "OK", style: .default))
                    present(alert, animated: true)
                }
            }
        }
    }
}
```

---

## Kotlin Multiplatform Integration

### Option 1: Published Artifact (Maven Central)

#### Step 1: Add Repository

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        google()
    }
}
```

#### Step 2: Add Dependency

```kotlin
// shared/build.gradle.kts (or your multiplatform module)
kotlin {
    android()
    ios()
    
    sourceSets {
        commonMain.dependencies {
            implementation("io.github.kotlin:library:1.0.0")
        }
    }
}
```

#### Step 3: Use in Common Code

```kotlin
// shared/src/commonMain/kotlin/PasswordGenerator.kt
import io.github.kotlin.passwordgenerator.*

class PasswordManager(
    private val repository: PasswordGeneratorRepository
) {
    private val passwordUseCase = PasswordGeneratorUseCase(repository)
    private val passphraseUseCase = PassphraseGeneratorUseCase(repository)
    
    suspend fun generatePassword(
        length: Int = 16,
        includeUppercase: Boolean = true,
        includeLowercase: Boolean = true,
        includeNumbers: Boolean = true,
        includeSpecialChars: Boolean = true
    ): String {
        return passwordUseCase(
            length = length,
            includeUppercase = includeUppercase,
            includeLowercase = includeLowercase,
            includeNumbers = includeNumbers,
            includeSpecialChars = includeSpecialChars
        )
    }
    
    suspend fun generatePassphrase(
        wordCount: Int = 4,
        separator: String = "-"
    ): String {
        return passphraseUseCase(
            wordCount = wordCount,
            separator = separator,
            includeUppercase = true,
            includeLowercase = true,
            includeNumbers = true,
            includeSpecialChars = false
        )
    }
}
```

#### Step 4: Platform-Specific Implementations

**Android:**
```kotlin
// shared/src/androidMain/kotlin/PlatformPasswordManager.kt
import android.content.Context
import io.github.kotlin.passwordgenerator.AndroidPasswordGeneratorRepository

actual class PlatformPasswordManager(private val context: Context) {
    actual val repository = AndroidPasswordGeneratorRepository(context)
}
```

**iOS:**
```kotlin
// shared/src/iosMain/kotlin/PlatformPasswordManager.kt
import io.github.kotlin.passwordgenerator.IosPasswordGeneratorRepository

actual class PlatformPasswordManager {
    actual val repository = IosPasswordGeneratorRepository()
}
```

**Common:**
```kotlin
// shared/src/commonMain/kotlin/PlatformPasswordManager.kt
expect class PlatformPasswordManager {
    val repository: PasswordGeneratorRepository
}
```

### Option 2: Local Module Dependency

#### Step 1: Add Module to Settings

```kotlin
// settings.gradle.kts
include(":password-generator")
project(":password-generator").projectDir = file("../multiplatform-library-template-main/library")
```

#### Step 2: Add Dependency

```kotlin
// shared/build.gradle.kts
kotlin {
    android()
    ios()
    
    sourceSets {
        commonMain.dependencies {
            implementation(project(":password-generator"))
        }
    }
}
```

### Complete KMP Example

```kotlin
// shared/src/commonMain/kotlin/PasswordFeature.kt
import io.github.kotlin.passwordgenerator.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PasswordFeature(
    repository: PasswordGeneratorRepository
) {
    private val passwordUseCase = PasswordGeneratorUseCase(repository)
    private val passphraseUseCase = PassphraseGeneratorUseCase(repository)
    
    private val _password = MutableStateFlow<String?>(null)
    val password: StateFlow<String?> = _password.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    suspend fun generatePassword(
        length: Int = 16,
        includeUppercase: Boolean = true,
        includeLowercase: Boolean = true,
        includeNumbers: Boolean = true,
        includeSpecialChars: Boolean = true,
        excludeCharacters: String = ""
    ) {
        try {
            val result = passwordUseCase(
                length = length,
                includeUppercase = includeUppercase,
                includeLowercase = includeLowercase,
                includeNumbers = includeNumbers,
                includeSpecialChars = includeSpecialChars,
                excludeCharacters = excludeCharacters
            )
            _password.value = result
            _error.value = null
        } catch (e: IllegalArgumentException) {
            _error.value = e.message
            _password.value = null
        }
    }
    
    suspend fun generatePassphrase(
        wordCount: Int = 4,
        separator: String = "-",
        includeUppercase: Boolean = true,
        includeLowercase: Boolean = true,
        includeNumbers: Boolean = true,
        includeSpecialChars: Boolean = false
    ) {
        try {
            val result = passphraseUseCase(
                wordCount = wordCount,
                separator = separator,
                includeUppercase = includeUppercase,
                includeLowercase = includeLowercase,
                includeNumbers = includeNumbers,
                includeSpecialChars = includeSpecialChars
            )
            _password.value = result
            _error.value = null
        } catch (e: IllegalArgumentException) {
            _error.value = e.message
            _password.value = null
        }
    }
}
```

---

## Troubleshooting

### Android Issues

#### Issue: "Could not find io.github.kotlin:library:1.0.0"

**Solution:**
1. Ensure Maven Central is in your repositories:
   ```kotlin
   repositories {
       mavenCentral()
   }
   ```
2. Check internet connection
3. Verify the version exists on Maven Central
4. Try invalidating caches: **File → Invalidate Caches / Restart**

#### Issue: "Unresolved reference: AndroidPasswordGeneratorRepository"

**Solution:**
1. Ensure the dependency is added correctly
2. Sync Gradle: **File → Sync Project with Gradle Files**
3. Clean and rebuild: **Build → Clean Project**, then **Build → Rebuild Project**
4. Check that you're importing from the correct package:
   ```kotlin
   import io.github.kotlin.passwordgenerator.AndroidPasswordGeneratorRepository
   ```

#### Issue: "AAR file not found" or "Failed to resolve AAR"

**Solution:**
1. Verify the AAR file exists at the specified path
2. Ensure the path in `build.gradle.kts` is correct (relative to module root)
3. Try using absolute path for testing:
   ```kotlin
   implementation(files("/absolute/path/to/library-release.aar"))
   ```

#### Issue: CoroutineScope not found

**Solution:**
Add coroutines dependency:
```kotlin
dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
}
```

### iOS Issues

#### Issue: "No such module 'PasswordGenerator'"

**Solution:**
1. Ensure XCFramework is built:
   ```bash
   ./gradlew :library:buildXCFramework --no-configuration-cache
   ```
2. Verify `Package.swift` path is correct
3. In Xcode: **File → Packages → Reset Package Caches**
4. Clean build folder: **Product → Clean Build Folder** (Shift+Cmd+K)
5. Restart Xcode

#### Issue: "Could not find 'PasswordGenerator' in scope"

**Solution:**
1. Ensure the package is added to your target:
   - Select your project → Select target → **General → Frameworks, Libraries, and Embedded Content**
   - Verify `PasswordGenerator` is listed
2. Check import statement:
   ```swift
   import PasswordGenerator
   ```
3. Rebuild: **Product → Clean Build Folder**, then **Product → Build**

#### Issue: "XCFramework validation failed" or architecture mismatch

**Solution:**
1. Ensure you're using the correct XCFramework:
   - Device builds require `ios-arm64`
   - Simulator builds require `ios-arm64-simulator` (Apple Silicon) or `ios-x86_64-simulator` (Intel)
2. Rebuild XCFramework:
   ```bash
   ./gradlew clean :library:buildXCFramework --no-configuration-cache
   ```
3. Verify XCFramework contains required architectures:
   ```bash
   xcrun xcodebuild -checkFirstLaunchStatus
   xcrun simctl list devices
   ```

#### Issue: "Checksum mismatch" (SPM)

**Solution:**
1. Regenerate checksum:
   ```bash
   swift package compute-checksum PasswordGenerator.xcframework.zip
   ```
2. Update `Package.swift` with the new checksum
3. Reset package caches in Xcode

#### Issue: Configuration cache errors

**Solution:**
Use `--no-configuration-cache` flag:
```bash
./gradlew :library:buildXCFramework --no-configuration-cache
```

### Kotlin Multiplatform Issues

#### Issue: "Unresolved reference" in common code

**Solution:**
1. Ensure the dependency is in `commonMain`:
   ```kotlin
   sourceSets {
       commonMain.dependencies {
           implementation("io.github.kotlin:library:1.0.0")
       }
   }
   ```
2. Sync Gradle
3. Clean and rebuild

#### Issue: Platform-specific code not compiling

**Solution:**
1. Ensure platform targets are configured:
   ```kotlin
   kotlin {
       android()
       ios()
   }
   ```
2. Check that platform-specific source sets exist:
   - `androidMain`
   - `iosMain`
   - `commonMain`

### General Issues

#### Issue: Build fails with "Task failed"

**Solution:**
1. Check Gradle version compatibility
2. Clean build:
   ```bash
   ./gradlew clean
   ```
3. Check for dependency conflicts:
   ```bash
   ./gradlew dependencies
   ```
4. Review build logs for specific error messages

#### Issue: Version conflicts

**Solution:**
1. Check dependency tree:
   ```bash
   ./gradlew :library:dependencies
   ```
2. Use dependency resolution strategy:
   ```kotlin
   configurations.all {
       resolutionStrategy {
           force("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
       }
   }
   ```

---

## Best Practices

### Distribution

1. **Versioning**: Use semantic versioning (MAJOR.MINOR.PATCH)
   - Increment MAJOR for breaking changes
   - Increment MINOR for new features (backward compatible)
   - Increment PATCH for bug fixes

2. **Release Notes**: Document changes in release notes
   - Breaking changes
   - New features
   - Bug fixes
   - Migration guides if needed

3. **Testing**: Test distribution artifacts before release
   - Test AAR in a sample Android app
   - Test XCFramework in a sample iOS app
   - Test JVM JAR in a sample JVM project

4. **Checksums**: Always provide checksums for binary distributions
   - Required for SPM
   - Helps verify integrity

5. **Documentation**: Keep documentation up to date
   - API changes
   - Integration examples
   - Migration guides

### Android Integration

1. **Use Lifecycle-Aware Scopes**: Prefer `lifecycleScope` or `viewModelScope` over manual `CoroutineScope`
   ```kotlin
   // Good
   lifecycleScope.launch { ... }
   
   // Avoid (unless necessary)
   val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
   ```

2. **Error Handling**: Always handle `IllegalArgumentException` from use cases
   ```kotlin
   try {
       val password = passwordUseCase(...)
   } catch (e: IllegalArgumentException) {
       // Handle validation error
   }
   ```

3. **Repository Lifecycle**: Create repository instances appropriately
   - Activity: Use activity context
   - ViewModel: Pass application context
   - Service: Use service context

4. **Settings Persistence**: Use `isPasswordRegenerate = true` when regenerating with saved settings to avoid unnecessary writes

### iOS Integration

1. **Async/Await**: Use Swift's async/await for suspend functions
   ```swift
   // Good
   let password = try await passwordUseCase.invoke(...)
   
   // Avoid (unless necessary)
   passwordUseCase.invoke(...) { result in ... }
   ```

2. **Main Actor**: Update UI on main thread
   ```swift
   await MainActor.run {
       passwordLabel.text = password
   }
   ```

3. **Error Handling**: Use do-catch for error handling
   ```swift
   do {
       let password = try await passwordUseCase.invoke(...)
   } catch {
       // Handle error
   }
   ```

4. **Package Management**: Prefer SPM over manual framework integration for easier updates

### Kotlin Multiplatform Integration

1. **Common Code**: Keep platform-specific code minimal
   - Use `expect/actual` for platform differences
   - Share business logic in common code

2. **Repository Pattern**: Use the provided repository implementations
   - `AndroidPasswordGeneratorRepository` for Android
   - `IosPasswordGeneratorRepository` for iOS
   - Or implement custom repository if needed

3. **Dependency Management**: Use version catalogs for dependency versions
   ```kotlin
   // libs.versions.toml
   [versions]
   passwordGenerator = "1.0.0"
   
   [libraries]
   passwordGenerator = { group = "io.github.kotlin", name = "library", version.ref = "passwordGenerator" }
   ```

4. **Testing**: Write tests in common test source set when possible

### Security

1. **Random Generation**: The library uses platform-native secure random generators
   - No additional configuration needed
   - For extra security, consider implementing encrypted storage via custom repository

2. **Settings Storage**: Default implementations use platform-standard storage
   - Android: SharedPreferences
   - iOS: UserDefaults
   - For sensitive data, implement encrypted storage

3. **Input Validation**: Always validate user input before calling use cases
   - Length ranges
   - Character set combinations
   - Exclude character validation

### Performance

1. **Coroutine Scope**: Use appropriate coroutine scopes
   - `lifecycleScope` for Activities/Fragments
   - `viewModelScope` for ViewModels
   - `GlobalScope` only when necessary (rarely)

2. **Settings Caching**: Cache repository instances when possible
   ```kotlin
   // Good - reuse repository
   private val repository = AndroidPasswordGeneratorRepository(context)
   
   // Avoid - creating new instances repeatedly
   fun generate() {
       val repo = AndroidPasswordGeneratorRepository(context) // Don't do this
   }
   ```

3. **Batch Operations**: When generating multiple passwords, use coroutines efficiently
   ```kotlin
   // Good
   val passwords = (1..10).map {
       async { passwordUseCase(...) }
   }.awaitAll()
   ```

---

## Additional Resources

- [README.md](README.md) - Full API documentation
- [INTEGRATION_GUIDE.md](INTEGRATION_GUIDE.md) - Quick integration reference
- [ios-distribution-setup.md](ios-distribution-setup.md) - iOS-specific distribution details
- [Kotlin Multiplatform Documentation](https://kotlinlang.org/docs/multiplatform.html)
- [Android Gradle Plugin Documentation](https://developer.android.com/studio/build)
- [Swift Package Manager Documentation](https://swift.org/package-manager/)

---

## Support

For issues, questions, or contributions:
- Open an issue on GitHub
- Check existing issues and discussions
- Review the codebase and tests for examples
