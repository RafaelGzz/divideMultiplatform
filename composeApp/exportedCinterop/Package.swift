// swift-tools-version: 5.9
import PackageDescription

let package = Package(
  name: "exportedCinterop",
  platforms: [.iOS("16.0"), .macOS("10.13"), .tvOS("12.0"), .watchOS("4.0")],
  products: [
    .library(
      name: "exportedCinterop",
      type: .static,
      targets: ["exportedCinterop"])
  ],
  dependencies: [
    .package(url: "https://github.com/firebase/firebase-ios-sdk.git", exact: "11.8.0"),
    .package(url: "https://github.com/google/GoogleSignIn-iOS", exact: "9.1.0"),
  ],
  targets: [
    .target(
      name: "exportedCinterop",
      dependencies: [
        .product(name: "FirebaseCore", package: "firebase-ios-sdk"),
        .product(name: "FirebaseAnalytics", package: "firebase-ios-sdk"),
        .product(name: "FirebaseCrashlytics", package: "firebase-ios-sdk"),
        .product(name: "FirebaseAuth", package: "firebase-ios-sdk"),
        .product(name: "FirebaseDatabase", package: "firebase-ios-sdk"),
        .product(name: "FirebasePerformance", package: "firebase-ios-sdk"),
        .product(name: "FirebaseStorage", package: "firebase-ios-sdk"),
        .product(name: "GoogleSignIn", package: "GoogleSignIn-iOS"),
      ],
      path: "Sources"

    )

  ]
)
