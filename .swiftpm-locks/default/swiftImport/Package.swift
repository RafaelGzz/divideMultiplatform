// swift-tools-version: 5.9
import PackageDescription
let package = Package(
  name: "KotlinMultiplatformLinkedPackage",
  platforms: [
    .iOS("16.0")
  ],
  products: [
    .library(
      name: "KotlinMultiplatformLinkedPackage",
      type: .none,
      targets: ["KotlinMultiplatformLinkedPackage"]
    )
  ],
  dependencies: [
    .package(path: "subpackages/_composeApp"),
    .package(path: "subpackages/io_github_mirzemehdi_kmpauth_firebase_core_3_0_0_alpha04"),
    .package(path: "subpackages/io_github_mirzemehdi_kmpauth_firebase_google_3_0_0_alpha04"),
    .package(path: "subpackages/io_github_mirzemehdi_kmpauth_google_3_0_0_alpha04")
  ],
  targets: [
    .target(
      name: "KotlinMultiplatformLinkedPackage",
      dependencies: [
        .product(name: "_composeApp", package: "_composeApp"),
        .product(name: "io_github_mirzemehdi_kmpauth_firebase_core_3_0_0_alpha04", package: "io_github_mirzemehdi_kmpauth_firebase_core_3_0_0_alpha04"),
        .product(name: "io_github_mirzemehdi_kmpauth_firebase_google_3_0_0_alpha04", package: "io_github_mirzemehdi_kmpauth_firebase_google_3_0_0_alpha04"),
        .product(name: "io_github_mirzemehdi_kmpauth_google_3_0_0_alpha04", package: "io_github_mirzemehdi_kmpauth_google_3_0_0_alpha04")
      ]
    )
  ]
)
