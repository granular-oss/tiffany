# Tiffany

#### A library for parsing Tagged Image File Format (Tiff) files on any platform (currently JVM and native iOS) ####

The library inspired by the [TIFF Java](https://github.com/ngageoint/tiff-java/) developed at the [National Geospatial-Intelligence Agency (NGA)](http://www.nga.mil/) in collaboration with [BIT Systems](http://www.bit-sys.com/). The software use, modification, and distribution rights are stipulated within the [MIT license](http://choosealicense.com/licenses/mit/).

### Pull Requests ###
If you'd like to contribute to this project, please make a pull request. We'll review the pull request and discuss the changes. All pull request contributions to this project will be released under the MIT license.

### About ###

`Tiffany` is a Kotlin Multiplatform library for reading and writing Tagged Image File Format files. Implementation is based on the [TIFF specification](https://partners.adobe.com/public/developer/en/tiff/TIFF6.pdf) and ported from TIFF Java implementation: https://github.com/ngageoint/tiff-java/

### Usage ###

View the latest [Javadoc](/) TODO

#### Read ####

```kotlin

//val data: ByteArray = ...

val tiffFile: TIFFImage = TiffReader(data).readTiff()
val fileDirectories: List<FileDirectory> = tiffFile?.fileDirectories

// read the first directory in tiff file
val fileDirectory = fileDirectories[0]
val rasters: TypedRasters = fileDirectory.readTypedRasters()

// get the width and height
val rasterWidth = rasters.width
val rasterHeight = rasters.height

val samples1: ShortArray = (rasters.samples[0] as TypedSample.ShortSample).data
val samples2: ShortArray = (rasters.samples[1] as TypedSample.ShortSample).data

```

#### Write ####

```kotlin

val tiffImage: TiffImage = ...
val tiffBytes: ByteArray = TiffWriter().writeTiffToBytes(tiffImage)

```

### Installation ###

TODO provide link to the maven once published

### Build ###

Build this repository using Gradle:

```bash

    ./gradlew build
```

Publish to local maven for local testing:

```bash

    ./gradlew publishToMavenLocal
```