# iOS Distribution Setup Guide

This guide explains how to distribute the Password Generator library for iOS via Swift Package Manager (SPM).

## Building XCFramework

1. Build the XCFramework:
   ```bash
   ./gradlew :library:buildXCFramework
   ```
   
   **Note**: If you encounter configuration cache issues, use:
   ```bash
   ./gradlew :library:buildXCFramework --no-configuration-cache
   ```

2. The XCFramework will be generated at:
   ```
   library/build/XCFrameworks/PasswordGenerator.xcframework
   ```
   
   **Note**: The XCFramework includes:
   - `ios-arm64` (physical iOS devices)
   - `ios-arm64-simulator` (Apple Silicon simulators)
   
   Intel-based simulators (ios-x86_64-simulator) are not included to avoid conflicts.

## Distribution Options

### Option 1: GitHub Releases (Recommended)

1. Create a GitHub release tag (e.g., `v1.0.0`)
2. Upload the XCFramework as a release asset
3. Update `Package.swift` to point to the release URL:
   ```swift
   .binaryTarget(
       name: "PasswordGenerator",
       url: "https://github.com/YOUR_USERNAME/YOUR_REPO/releases/download/v1.0.0/PasswordGenerator.xcframework.zip",
       checksum: "CHECKSUM_HERE"
   )
   ```

### Option 2: Local File Path

For local development, use the `Package.swift` file as-is. It references the local XCFramework path.

### Option 3: Git Repository

1. Commit the XCFramework to your repository
2. Update `Package.swift` to use a git-based source:
   ```swift
   dependencies: [
       .package(url: "https://github.com/YOUR_USERNAME/YOUR_REPO.git", from: "1.0.0")
   ]
   ```

## Generating Checksum

To generate a checksum for the XCFramework zip file:
```bash
swift package compute-checksum PasswordGenerator.xcframework.zip
```

## Testing SPM Integration

1. Create a test iOS project in Xcode
2. File → Add Package Dependencies
3. Enter your repository URL or local path
4. Import and use:
   ```swift
   import PasswordGenerator
   ```
