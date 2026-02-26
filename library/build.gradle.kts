import com.android.build.api.dsl.androidLibrary
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.Framework
import org.jetbrains.kotlin.gradle.tasks.FatFrameworkTask
import java.util.zip.ZipEntry

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.vanniktech.mavenPublish)
}

group = "io.github.kotlin"
version = "1.0.0"

kotlin {
    jvm()
    androidLibrary {
        namespace = "org.jetbrains.kotlinx.multiplatform.library.template"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        withJava() // enable java compilation support
        withHostTestBuilder {}.configure {}
        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }

        compilations.configureEach {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_11)
                }
            }
        }
    }
    
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    
    // Configure iOS framework for SPM distribution
    targets.filterIsInstance<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget>().forEach { target ->
        target.binaries.framework {
            baseName = "PasswordGenerator"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
        }
        
        androidMain.dependencies {
            implementation("androidx.core:core-ktx:1.15.0")
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
        }
    }
}

// Task to build XCFramework for iOS SPM distribution
// Note: This task is not compatible with configuration cache due to xcodebuild execution
tasks.register("buildXCFramework") {
    group = "build"
    description = "Builds an XCFramework for iOS Swift Package Manager distribution"
    
    // Use only iosArm64 (device) and iosSimulatorArm64 (Apple Silicon simulator)
    // Skip iosX64 to avoid conflict - both iosX64 and iosSimulatorArm64 target simulators
    val iosTargets = listOf("iosArm64", "iosSimulatorArm64")
    val frameworks = iosTargets.map { targetName ->
        kotlin.targets.getByName<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget>(targetName)
            .binaries.getFramework("RELEASE")
    }
    
    val outputDir = layout.buildDirectory.dir("XCFrameworks")
    val xcframeworkPath = outputDir.map { it.asFile.resolve("PasswordGenerator.xcframework") }
    val projectDirPath = layout.projectDirectory.asFile.absolutePath
    
    dependsOn(frameworks.map { it.linkTaskProvider })
    
    // Mark as not compatible with configuration cache
    notCompatibleWithConfigurationCache("Uses xcodebuild which requires execution-time project access")
    
    doLast {
        val outputDirFile = outputDir.get().asFile
        val xcframeworkPathFile = xcframeworkPath.get()
        
        outputDirFile.deleteRecursively()
        outputDirFile.mkdirs()
        
        // Verify all frameworks are built
        val frameworkFiles = frameworks.mapNotNull { framework ->
            val frameworkFile = framework.outputFile
            if (frameworkFile.exists()) {
                frameworkFile
            } else {
                throw GradleException("Framework not found: ${frameworkFile.absolutePath}")
            }
        }
        
        // Create XCFramework using xcodebuild
        val frameworkArgs = frameworkFiles.flatMap { listOf("-framework", it.absolutePath) }
        val commandLine = listOf("xcodebuild", "-create-xcframework") + frameworkArgs + 
            listOf("-output", xcframeworkPathFile.absolutePath)
        
        // Execute xcodebuild command
        @Suppress("DEPRECATION")
        val execResult = project.exec {
            commandLine(commandLine)
            workingDir = file(projectDirPath)
        }
        
        execResult.assertNormalExitValue()
        
        if (xcframeworkPathFile.exists()) {
            println("✓ XCFramework created successfully at: ${xcframeworkPathFile.absolutePath}")
        } else {
            throw GradleException("Failed to create XCFramework at ${xcframeworkPathFile.absolutePath}")
        }
    }
}

// Task to create distribution bundle with all artifacts
tasks.register<Zip>("createDistributionBundle") {
    group = "distribution"
    description = "Creates a portable distribution bundle with all platform artifacts"
    archiveBaseName.set("password-generator")
    archiveVersion.set(project.version.toString())
    archiveClassifier.set("all-platforms")
    
    dependsOn("build", "buildXCFramework", "jvmJar")
    
    // Not compatible with configuration cache due to buildXCFramework dependency
    notCompatibleWithConfigurationCache("Depends on buildXCFramework which uses xcodebuild")
    
    val distDir = layout.buildDirectory.dir("distributions").get().asFile
    destinationDirectory.set(file(distDir))
    
    from(projectDir.parentFile) {
        include("README.md")
        include("LICENSE")
        include("Package.swift")
        into(".")
    }
    
    // Include JVM JAR
    from(tasks.named("jvmJar")) {
        into("jvm")
    }
    
    // Include Android AAR (generated during build)
    from(layout.buildDirectory.dir("outputs/aar")) {
        into("android")
        include("**/*.aar")
    }
    
    // Include iOS XCFramework
    from(layout.buildDirectory.dir("XCFrameworks")) {
        into("ios")
        include("**/*.xcframework/**")
    }
    
    doLast {
        println("✓ Distribution bundle created: ${archiveFile.get().asFile.absolutePath}")
        println("  Contains:")
        println("    - JVM JAR")
        println("    - Android AAR")
        println("    - iOS XCFramework")
        println("    - Documentation")
    }
}

// Task to create a simple JAR bundle for JVM-only distribution
tasks.register<Jar>("createJarBundle") {
    group = "distribution"
    description = "Creates a JAR file for JVM distribution"
    archiveBaseName.set("password-generator")
    archiveVersion.set(project.version.toString())
    archiveClassifier.set("jvm")
    
    dependsOn("jvmJar")
    
    from(tasks.named("jvmJar")) {
        into(".")
    }
    
    manifest {
        attributes(
            "Implementation-Title" to "Password Generator Library",
            "Implementation-Version" to project.version.toString(),
            "Implementation-Vendor" to "io.github.kotlin"
        )
    }
    
    doLast {
        println("✓ JAR bundle created: ${archiveFile.get().asFile.absolutePath}")
    }
}

// Task to create iOS distribution bundle
tasks.register<Zip>("createIosBundle") {
    group = "distribution"
    description = "Creates a bundle for iOS distribution (XCFramework + Package.swift)"
    archiveBaseName.set("password-generator")
    archiveVersion.set(project.version.toString())
    archiveClassifier.set("ios")
    
    dependsOn("buildXCFramework")
    
    val distDir = layout.buildDirectory.dir("distributions").get().asFile
    destinationDirectory.set(file(distDir))
    
    from(projectDir.parentFile) {
        include("Package.swift")
        include("README.md")
        include("LICENSE")
        into(".")
    }
    
    from(layout.buildDirectory.dir("XCFrameworks")) {
        into("library/build/XCFrameworks")
        include("**/*.xcframework/**")
    }
    
    doLast {
        println("✓ iOS bundle created: ${archiveFile.get().asFile.absolutePath}")
        println("  Contains:")
        println("    - XCFramework")
        println("    - Package.swift")
        println("    - Documentation")
    }
}

mavenPublishing {
    publishToMavenCentral()

    signAllPublications()

    coordinates(group.toString(), "library", version.toString())

    pom {
        name = "Password Generator Library"
        description = "A Kotlin Multiplatform library for generating secure passwords and passphrases"
        inceptionYear = "2024"
        url = "https://github.com/kotlin/multiplatform-library-template/"
        licenses {
            license {
                name = "XXX"
                url = "YYY"
                distribution = "ZZZ"
            }
        }
        developers {
            developer {
                id = "XXX"
                name = "YYY"
                url = "ZZZ"
            }
        }
        scm {
            url = "XXX"
            connection = "YYY"
            developerConnection = "ZZZ"
        }
    }
}
