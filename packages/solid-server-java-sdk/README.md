# Solid Server - Java SDK
- [Overview](#overview)
- [Build](#build)
- [Use](#use)

## Overview

This SDK is the fundamental building block for the Solid specification concepts targeted at Server implementations.  
It requires Java 8 or later.

## Build
### Binaries
To build a standalone jar usable in projects:
```bash
./gradlew build
```
Jar is produced in `build/libs/`

### Test
To run the tests:
```bash
./gradlew test
```
Detailed results are produced and saved at `build/reports/tests/test/index.html`

### Javadoc
To produce the javadoc:
```bash
./gradlew javadoc
```
Then go to `build/docs/javadoc/`

## Use
To use in other projects, either build the binary jar and include in your classpath, or import the project directly.

### Gradle
In `settings.gradle`:
```groovy
include(':sdk')
project(':sdk').projectDir = file('/path/to/this/sdk')
```
In `build.gradle`:
```groovy
dependencies {
    api project(':sdk')
    
    // Other dependencies...
}
```
