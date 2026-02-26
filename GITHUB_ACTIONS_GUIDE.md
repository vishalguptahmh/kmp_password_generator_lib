# GitHub Actions Guide

This guide explains how to use GitHub Actions workflows for building and releasing your multiplatform library.

## Table of Contents

1. [What is GitHub Actions?](#what-is-github-actions)
2. [Cost and Availability](#cost-and-availability)
3. [How Workflows Work](#how-workflows-work)
4. [Available Workflows](#available-workflows)
5. [Using the Workflows](#using-the-workflows)
6. [Accessing Artifacts](#accessing-artifacts)
7. [Troubleshooting](#troubleshooting)

---

## What is GitHub Actions?

GitHub Actions is a continuous integration and continuous deployment (CI/CD) platform that allows you to automate your build, test, and deployment pipeline directly from your GitHub repository. Workflows are defined in YAML files stored in the `.github/workflows/` directory.

## Cost and Availability

### ✅ **FREE for Public Repositories**

GitHub Actions is **completely free** for public repositories with unlimited minutes and unlimited concurrent jobs. This makes it perfect for open-source projects!

### Pricing for Private Repositories

- **Free tier**: 2,000 minutes/month
- **Additional minutes**: $0.008 per minute (Linux) or $0.016 per minute (macOS/Windows)

For most open-source projects, GitHub Actions provides everything you need at no cost.

## How Workflows Work

### Workflow Structure

A workflow consists of:

1. **Triggers**: Events that start the workflow (push, pull request, manual dispatch, etc.)
2. **Jobs**: Individual tasks that run on runners (virtual machines)
3. **Steps**: Commands or actions executed within a job
4. **Artifacts**: Files produced by the workflow that can be downloaded

### Workflow File Location

Workflows are stored in `.github/workflows/*.yml` files. Each file defines one or more workflows.

### Example Workflow Structure

```yaml
name: My Workflow

on:
  push:
    branches: [main]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Build
        run: ./gradlew build
```

## Available Workflows

This repository includes two main workflows:

### 1. Build Artifacts (`build-artifacts.yml`)

**Purpose**: Builds artifacts for all platforms (JVM, Android, iOS) on every push or pull request.

**Triggers**:
- Push to `main` or `master` branch
- Pull requests to `main` or `master` branch
- Manual dispatch (workflow_dispatch)

**What it builds**:
- **JVM**: JAR file for Java/Kotlin projects
- **Android**: AAR files (both debug and release variants)
- **iOS**: XCFramework and bundle for Swift Package Manager
- **Distribution Bundle**: Complete bundle with all platform artifacts

**Jobs**:
- `build-jvm`: Builds JVM JAR artifacts
- `build-android`: Builds Android AAR artifacts using the `build` task
- `build-ios`: Builds iOS XCFramework and bundle
- `create-distribution`: Creates a distribution bundle with all artifacts
- `build-summary`: Provides a summary of all builds

### 2. Release (`release.yml`)

**Purpose**: Creates a GitHub Release with all artifacts when a version tag is pushed.

**Triggers**:
- Push of a tag matching `v*.*.*` (e.g., `v1.0.0`, `v2.1.3`)
- Manual dispatch with tag input

**What it does**:
1. Builds all artifacts (JVM, Android, iOS)
2. Creates a GitHub Release
3. Attaches all artifacts to the release
4. Generates release notes (if not provided)

## Using the Workflows

### Automatic Builds

Workflows run automatically when:
- You push code to `main` or `master`
- You open or update a pull request
- You push a version tag (for releases)

### Manual Workflow Dispatch

You can manually trigger workflows from the GitHub Actions tab:

1. Go to your repository on GitHub
2. Click on the **Actions** tab
3. Select the workflow you want to run (e.g., "Build Artifacts")
4. Click **Run workflow**
5. Configure any inputs (if available)
6. Click **Run workflow**

#### Build Artifacts Manual Options

- `build_all`: Build all platforms (default: true)
- `build_jvm`: Build JVM artifacts (default: true)
- `build_android`: Build Android artifacts (default: true)
- `build_ios`: Build iOS artifacts (default: true)

#### Release Manual Options

- `tag`: Release tag (e.g., `v1.0.0`) - **Required**
- `release_name`: Custom release name (optional)
- `release_notes`: Release notes in Markdown (optional)
- `prerelease`: Mark as prerelease (default: false)

### Creating a Release

#### Method 1: Push a Tag

```bash
# Create and push a version tag
git tag v1.0.0
git push origin v1.0.0
```

The release workflow will automatically:
- Build all artifacts
- Create a GitHub Release
- Attach artifacts to the release

#### Method 2: Manual Dispatch

1. Go to **Actions** → **Release** workflow
2. Click **Run workflow**
3. Enter the tag name (e.g., `v1.0.0`)
4. Optionally add release notes
5. Click **Run workflow**

## Accessing Artifacts

### Viewing Workflow Runs

1. Go to your repository on GitHub
2. Click the **Actions** tab
3. Select a workflow run from the list
4. Click on a job to see its steps and logs

### Downloading Artifacts

Artifacts are available for **90 days** after the workflow run completes.

#### From Workflow Run

1. Go to **Actions** → Select a workflow run
2. Scroll down to the **Artifacts** section
3. Click on an artifact name to download it
4. Artifacts are downloaded as ZIP files

#### From GitHub Release

1. Go to **Releases** in your repository
2. Click on a release version
3. Scroll down to **Assets**
4. Download any attached artifact files

### Available Artifacts

#### Build Artifacts Workflow

- `jvm-jar`: JVM JAR file
- `android-aar`: Android AAR files (debug and release)
- `ios-xcframework`: iOS XCFramework
- `ios-bundle`: iOS distribution bundle
- `distribution-bundle-all-platforms`: Complete distribution bundle

#### Release Workflow

- `release-artifacts`: All artifacts packaged together
  - JVM JAR
  - Android AAR files
  - iOS XCFramework
  - iOS bundle
  - Distribution bundle

## Troubleshooting

### Workflow Fails to Start

**Problem**: Workflow doesn't trigger automatically.

**Solutions**:
- Check that workflow files are in `.github/workflows/` directory
- Verify YAML syntax is correct (use a YAML validator)
- Ensure the trigger conditions match your event (branch name, tag format, etc.)
- Check repository settings → Actions → ensure workflows are enabled

### Build Failures

#### Gradle Build Fails

**Problem**: `./gradlew build` fails with errors.

**Solutions**:
- Check the workflow logs for specific error messages
- Verify Gradle wrapper is present and valid
- Ensure all dependencies are available
- Check Java version compatibility (workflow uses JDK 17)
- Verify Android SDK is properly set up (for Android builds)

#### Android Build Issues

**Problem**: Android AAR build fails.

**Solutions**:
- Verify Android SDK is installed (workflow uses `android-actions/setup-android@v3`)
- Check that `compileSdk` and `minSdk` versions are valid
- Ensure Android dependencies are resolvable
- Verify the `build` task is correct for multiplatform Android library

#### iOS Build Issues

**Problem**: iOS XCFramework build fails.

**Solutions**:
- iOS builds require macOS runners (workflow uses `macos-latest`)
- Verify Xcode is available (GitHub Actions macOS runners include Xcode)
- Check that iOS targets are correctly configured
- Ensure `xcodebuild` command is available
- Verify Kotlin/Native dependencies are cached properly

### Artifact Not Found

**Problem**: Artifact upload fails with "if-no-files-found: error".

**Solutions**:
- Check that build actually produced the expected files
- Verify file paths in the upload step match actual build output locations
- Check build logs to see where files were created
- Ensure build completed successfully before artifact upload

**Common paths**:
- JVM JAR: `library/build/libs/*.jar`
- Android AAR: `library/build/outputs/aar/*.aar`
- iOS XCFramework: `library/build/XCFrameworks/**/*.xcframework`

### Cache Issues

**Problem**: Builds are slow or cache isn't working.

**Solutions**:
- Caches are keyed by file hashes - if dependencies change, cache will rebuild
- Check cache hit/miss in workflow logs
- Verify cache paths are correct
- Clear cache manually if needed (delete cache entries in Actions settings)

### Permission Errors

**Problem**: Workflow fails with permission errors.

**Solutions**:
- Check workflow `permissions` section
- For releases, ensure `contents: write` permission is set
- Verify GitHub token has necessary permissions
- Check repository settings → Actions → Workflow permissions

### Configuration Cache Issues

**Problem**: Gradle configuration cache errors.

**Solutions**:
- Workflows use `--no-configuration-cache` flag to avoid issues
- Some tasks (like `buildXCFramework`) are not compatible with configuration cache
- If you see cache errors, ensure the flag is present

### Release Creation Fails

**Problem**: Release workflow runs but doesn't create a release.

**Solutions**:
- Verify `GITHUB_TOKEN` has write permissions
- Check that tag format matches `v*.*.*` pattern
- Ensure tag doesn't already exist
- Verify release notes don't contain invalid characters
- Check Actions logs for specific error messages

### Timeout Issues

**Problem**: Workflow times out.

**Solutions**:
- GitHub Actions has a 6-hour limit per job
- iOS builds on macOS can be slow - this is normal
- Check if builds are hanging on specific steps
- Consider splitting large jobs into smaller ones
- Use caching to speed up subsequent runs

## Best Practices

1. **Test Locally First**: Run `./gradlew build` locally before pushing
2. **Monitor Workflow Runs**: Check Actions tab regularly for failures
3. **Use Meaningful Commit Messages**: Helps identify what triggered builds
4. **Tag Releases Properly**: Use semantic versioning (e.g., `v1.0.0`)
5. **Keep Workflows Updated**: Update action versions periodically
6. **Review Logs**: Check logs when builds fail to understand issues
7. **Use Caching**: Workflows include caching to speed up builds
8. **Document Changes**: Update this guide when adding new workflows

## Additional Resources

- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Gradle Build Action](https://github.com/gradle/gradle-build-action)
- [Android Setup Action](https://github.com/android-actions/setup-android)
- [GitHub Actions Marketplace](https://github.com/marketplace?type=actions)

---

**Need Help?** If you encounter issues not covered here, check the workflow logs in the Actions tab for detailed error messages and stack traces.
