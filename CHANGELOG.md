# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

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

[Unreleased]: https://github.com/nofrills-io/gradle-multimodule/compare/v0.2.0...HEAD
[0.2.0]: https://github.com/nofrills-io/gradle-multimodule/compare/v0.1.0...v0.2.0
[0.1.0]: https://github.com/nofrills-io/gradle-multimodule/releases/tag/v0.1.0