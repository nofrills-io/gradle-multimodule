# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.7.0] - 2020-12-23
### Changed
- added minimum supported Android Build Tools version to 4.1.0
- update minimum supported Dokka version to 1.4.10

## [0.6.0] - 2020-07-13
### Changed
- added support for Android Build Tools 4.0.0
- update supported Dokka version

## [0.5.2] - 2020-04-09
### Changed
- `JacocoConfig`, `JavaConfig`, `KotlinConfig` and `PublishConfig` do not inherit from a `Project`;
    they still have the `project` property

## [0.5.1] - 2020-04-02
### Fixed
- the `project` field would be always set to the root project

## [0.5.0] - 2020-04-01
### Added
- `multimodule` extension has a `project` property, which can be used to access either the root project,
    or the sub-project (when the configuration is applied to the sub-project)

## [0.4.1] - 2020-03-27
### Changed/Fixed
- minimum required version of Android build tools plugin is 3.6.0 (needed for publishing configuration)

## [0.4.0] - 2020-03-27
### Changed
- Kotlin Android extensions plugin is now disabled by default
- Jacoco configuration: you can configure both the plugin, and tasks

### Fixed
- kotlin libraries were incorrectly added as dependencies

### Added
- more tests

## [0.3.1] - 2020-03-25
### Changed
- publishing: workaround when attaching doc for dokka-android

## [0.3.0] - 2020-03-23
### Added
- Option to include plugins: kotlin android extensions and kapt 
- Option to include kotlin libraries: coroutines, kotlin-stdlib, kotlin-reflect

### Removed
- Some options from kotlin configuration were removed

## [0.2.1] - 2020-03-17
### Changed
- Allow the clients of the plugin to overwrite version numbers of the plugins used in multimodule (i.e kotlin, dokka and android gradle plugin)

## [0.2.0] - 2020-03-11
### Added
- Sub-projects can now decide to opt-out of dokka, jacoco or publishing
    by configuring the "submodule" extension

## [0.1.0] - 2020-03-10
### Added
- Ability to specify a common configuration in the root project,
    that can later be applied to any sub-project
- Common configuration can include android, dokka, jacoco, java, kotlin and publishing

[Unreleased]: https://github.com/nofrills-io/gradle-multimodule/compare/v0.5.2...HEAD
[0.5.2]: https://github.com/nofrills-io/gradle-multimodule/compare/v0.5.1...v0.5.2
[0.5.1]: https://github.com/nofrills-io/gradle-multimodule/compare/v0.5.0...v0.5.1
[0.5.0]: https://github.com/nofrills-io/gradle-multimodule/compare/v0.4.1...v0.5.0
[0.4.1]: https://github.com/nofrills-io/gradle-multimodule/compare/v0.4.0...v0.4.1
[0.4.0]: https://github.com/nofrills-io/gradle-multimodule/compare/v0.3.1...v0.4.0
[0.3.1]: https://github.com/nofrills-io/gradle-multimodule/compare/v0.3.0...v0.3.1
[0.3.0]: https://github.com/nofrills-io/gradle-multimodule/compare/v0.2.1...v0.3.0
[0.2.1]: https://github.com/nofrills-io/gradle-multimodule/compare/v0.2.0...v0.2.1
[0.2.0]: https://github.com/nofrills-io/gradle-multimodule/compare/v0.1.0...v0.2.0
[0.1.0]: https://github.com/nofrills-io/gradle-multimodule/releases/tag/v0.1.0