package ag.granular.tiff

import ag.granular.io.measureTime
import kotlin.test.Test
import kotlin.test.assertEquals

class GranularTiffTest {

    @Test
    fun `test 80ac field mask`() = measureTime("test 80ac mask") {
        val tiff = TiffReader(getTestData("mask_albright_80.tif")).readTiff()
        assertEquals(1, tiff.fileDirectories.size)
        val rasters = measureTime("read untyped") { tiff.fileDirectories.first().readRasters() }
        assertEquals(963, rasters.width)
        assertEquals(781, rasters.height)
        val sampleValues = rasters.getSampleValues()
        assertEquals(1, sampleValues.size)
        assertEquals(752103, sampleValues.first().capacity())
    }

    @Test
    fun `test 80ac field mask compared to typed samples`() {
        val tiff = TiffReader(getTestData("mask_albright_80.tif")).readTiff()
        val untypedRasters = measureTime("read untyped") { tiff.fileDirectories.first().readRasters() }
        val untypedSamples = untypedRasters.getSampleValues()[0]
        val typedRasters = measureTime("read typed") { tiff.fileDirectories.first().readTypedRasters() }
        val typedSamples = typedRasters.samples[0] as TypedSample.ByteSample

        val size = untypedRasters.width * untypedRasters.height
        for (i in 0 until size) {
            assertEquals(untypedSamples.get(), typedSamples.data[i], "Failed for index: $i")
        }
    }

    @Test
    fun `test 800ac field mask`() = measureTime("test 800ac mask") {
        val tiff = TiffReader(getTestData("mask_brossman_800.tif")).readTiff()
        assertEquals(1, tiff.fileDirectories.size)
        val rasters = tiff.fileDirectories.first().readRasters()
        assertEquals(246, rasters.width)
        assertEquals(428, rasters.height)
        val sampleValues = rasters.getSampleValues()
        assertEquals(1, sampleValues.size)
        assertEquals(105288, sampleValues.first().capacity())
    }

    @Test
    fun `test 80ac field imagery`() = measureTime("test imagery_albright_80.tif") {
        val tiff = TiffReader(getTestData("imagery_albright_80.tif")).readTiff()
        assertEquals(1, tiff.fileDirectories.size)
        val rasters = tiff.fileDirectories.first().readRasters()
        assertEquals(246, rasters.width)
        assertEquals(428, rasters.height)
        val sampleValues = rasters.getSampleValues()
        assertEquals(4, sampleValues.size)
        for (i in 0..3) {
            assertEquals(210576, sampleValues[i].capacity())
        }
    }

    @Test
    fun `test 800ac field imagery`() = measureTime("test imagery_brossman_800.tif") {
        val tiff = TiffReader(getTestData("imagery_brossman_800.tif")).readTiff()
        assertEquals(1, tiff.fileDirectories.size)
        val rasters = measureTime("read untyped") { tiff.fileDirectories.first().readRasters() }
        assertEquals(963, rasters.width)
        assertEquals(781, rasters.height)
        val sampleValues = rasters.getSampleValues()
        assertEquals(4, sampleValues.size)
        for (i in 0..3) {
            assertEquals(1504206, sampleValues[i].capacity())
        }
    }

    @Test
    fun `test 800ac field imagery compared with typed`() {
        val tiff = TiffReader(getTestData("imagery_brossman_800.tif")).readTiff()
        assertEquals(1, tiff.fileDirectories.size)
        val untypedRasters = measureTime("read untyped") { tiff.fileDirectories.first().readRasters() }
        val typedRasters = measureTime("read typed") { tiff.fileDirectories.first().readTypedRasters() }

        assertEquals(untypedRasters.width, typedRasters.width)
        assertEquals(untypedRasters.height, typedRasters.height)

        val untypedSamples = untypedRasters.getSampleValues()
        val typedSamples = typedRasters.samples
        assertEquals(untypedSamples.size, typedSamples.size)
        val size = untypedRasters.width * untypedRasters.height
        for (i in 0 until untypedSamples.size) {
            for (j in 0 until size) {
                assertEquals(untypedSamples[i].getShort(), (typedSamples[i] as TypedSample.ShortSample).data[j])
            }
        }
    }
}