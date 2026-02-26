// swift-tools-version: 5.9
// The swift-tools-version declares the minimum version of Swift required to build this package.

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
