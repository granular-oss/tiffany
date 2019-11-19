package ag.granular.tiff

import ag.granular.io.measureTime
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

fun compareTIFFImages(
    tiffImage1: TIFFImage,
    tiffImage2: TIFFImage,
    exactType: Boolean = true,
    sameBitsPerSample: Boolean = true
) {

    assertNotNull(tiffImage1)
    assertNotNull(tiffImage2)
    assertEquals(
        tiffImage1.fileDirectories.size,
        tiffImage2.fileDirectories.size
    )
    for (i in 0 until tiffImage1.fileDirectories.size) {
        val fileDirectory1 = tiffImage1.fileDirectories[i]
        val fileDirectory2 = tiffImage2.fileDirectories[i]

        val sampleRasters1 = measureTime("readRasters 1") { fileDirectory1.readRasters() }
        compareFileDirectoryAndRastersMetadata(
            fileDirectory1,
            sampleRasters1
        )
        val sampleRasters2 = measureTime("readRasters 2") { fileDirectory2.readRasters() }
        compareFileDirectoryAndRastersMetadata(
            fileDirectory2,
            sampleRasters2
        )
        compareRastersSampleValues(
            sampleRasters1, sampleRasters2,
            exactType, sameBitsPerSample
        )

        val interleaveRasters1 = fileDirectory1
            .readInterleavedRasters()
        compareFileDirectoryAndRastersMetadata(
            fileDirectory1,
            interleaveRasters1
        )
        val interleaveRasters2 = fileDirectory2
            .readInterleavedRasters()
        compareFileDirectoryAndRastersMetadata(
            fileDirectory2,
            interleaveRasters2
        )
        compareRastersInterleaveValues(
            interleaveRasters1,
            interleaveRasters2, exactType, sameBitsPerSample
        )

        compareRasters(
            fileDirectory1, sampleRasters1, fileDirectory2,
            interleaveRasters2, exactType, sameBitsPerSample
        )
        compareRasters(
            fileDirectory1, interleaveRasters1, fileDirectory2,
            sampleRasters2, exactType, sameBitsPerSample
        )
    }
}

private fun compareFileDirectoryAndRastersMetadata(
    fileDirectory: FileDirectory,
    rasters: Rasters
) {
    assertEquals(fileDirectory.imageWidth, rasters.width)
    assertEquals(
        fileDirectory.imageHeight,
        rasters.height
    )
    assertEquals(
        fileDirectory.samplesPerPixel,
        rasters.samplesPerPixel
    )
    assertEquals(
        fileDirectory.bitsPerSample!!.size, rasters
            .getBitsPerSample().size
    )
    for (i in 0 until fileDirectory.bitsPerSample!!.size) {
        assertEquals(
            fileDirectory.bitsPerSample!![i],
            rasters.getBitsPerSample()[i]
        )
    }
}

fun compareRastersSampleValues(
    rasters1: Rasters,
    rasters2: Rasters,
    exactType: Boolean = true,
    sameBitsPerSample: Boolean = true
) {

    compareRastersMetadata(rasters1, rasters2, sameBitsPerSample)

    assertNotNull(rasters1.getSampleValues())
    assertNotNull(rasters2.getSampleValues())
    assertEquals(
        rasters1.getSampleValues().size,
        rasters2.getSampleValues().size
    )

    for (i in 0 until rasters1.getSampleValues().size) {
        assertEquals(
            rasters1.getSampleValues()[i].capacity() / rasters1.fieldTypes[i].bytes,
            rasters2.getSampleValues()[i].capacity() / rasters2.fieldTypes[i].bytes
        )

        for (x in 0 until rasters1.width) {
            for (y in 0 until rasters1.height) {
                compareNumbers(
                    rasters1.getPixelSample(i, x, y),
                    rasters2.getPixelSample(i, x, y), exactType
                )
            }
        }
    }
}

fun compareRastersInterleaveValues(
    rasters1: Rasters,
    rasters2: Rasters,
    exactType: Boolean = true,
    sameBitsPerSample: Boolean = true
) {

    compareRastersMetadata(rasters1, rasters2, sameBitsPerSample)

    assertNotNull(rasters1.getInterleaveValues())
    assertNotNull(rasters2.getInterleaveValues())
    assertEquals(
        rasters1.getInterleaveValues().capacity() / rasters1.sizePixel(), rasters2.getInterleaveValues()
            .capacity() / rasters2.sizePixel()
    )

    for (i in 0 until rasters1.samplesPerPixel) {
        for (x in 0 until rasters1.width) {
            for (y in 0 until rasters1.height) {
                compareNumbers(
                    rasters1.getPixelSample(i, x, y),
                    rasters2.getPixelSample(i, x, y), exactType
                )
            }
        }
    }
}

fun compareRasters(
    fileDirectory1: FileDirectory,
    rasters1: Rasters,
    fileDirectory2: FileDirectory,
    rasters2: Rasters,
    exactType: Boolean,
    sameBitsPerSample: Boolean
) {

    compareRastersMetadata(rasters1, rasters2, sameBitsPerSample)

    val randomX = (Random.nextFloat() * rasters1.width).toInt()
    val randomY = (Random.nextFloat() * rasters1.height).toInt()

    for (x in 0 until rasters1.width) {
        for (y in 0 until rasters1.height) {

            val pixel1 = rasters1.getPixel(x, y)
            val pixel2 = rasters2.getPixel(x, y)

            var rasters3: Rasters? = null
            var rasters4: Rasters? = null
            if (x == 0 && y == 0 ||
                x == rasters1.width - 1 && y == rasters1
                    .height - 1 ||
                x == randomX && y == randomY
            ) {
                val window = ImageWindow.fromXY(x, y)
                rasters3 = fileDirectory1.readRasters(window)
                assertEquals(1, rasters3.numPixels)
                rasters4 = fileDirectory2.readInterleavedRasters(window)
                assertEquals(1, rasters4.numPixels)
            }

            for (sample in 0 until rasters1.samplesPerPixel) {
                val sample1 = rasters1.getPixelSample(sample, x, y)
                val sample2 = rasters2.getPixelSample(sample, x, y)
                compareNumbers(sample1, sample2, exactType)
                compareNumbers(pixel1[sample], sample1, exactType)
                compareNumbers(pixel1[sample], pixel2[sample], exactType)

                if (rasters3 != null) {
                    val sample3 = rasters3.getPixelSample(sample, 0, 0)
                    val sample4 = rasters4!!.getPixelSample(sample, 0, 0)
                    compareNumbers(pixel1[sample], sample3, exactType)
                    compareNumbers(pixel1[sample], sample4, exactType)
                }
            }
        }
    }
}

private fun compareRastersMetadata(
    rasters1: Rasters,
    rasters2: Rasters,
    sameBitsPerSample: Boolean
) {
    assertNotNull(rasters1)
    assertNotNull(rasters2)
    assertEquals(rasters1.width, rasters2.width)
    assertEquals(rasters1.height, rasters2.height)
    assertEquals(rasters1.numPixels, rasters2.numPixels)
    assertEquals(
        rasters1.getBitsPerSample().size, rasters2
            .getBitsPerSample().size
    )
    if (sameBitsPerSample) {
        for (i in 0 until rasters1.getBitsPerSample().size) {
            assertEquals(
                rasters1.getBitsPerSample()[i],
                rasters2.getBitsPerSample()[i]
            )
        }
    }
}

private fun compareNumbers(
    number1: Number,
    number2: Number,
    exactType: Boolean
) {
    if (exactType) {
        assertEquals(number1, number2)
    } else {
        assertEquals(number1.toDouble(), number2.toDouble())
    }
}

expect fun getTestData(fileName: String): ByteArray