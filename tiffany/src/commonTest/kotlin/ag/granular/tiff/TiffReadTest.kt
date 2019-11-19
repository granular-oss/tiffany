package ag.granular.tiff

import ag.granular.io.measureTime
import ag.granular.tiff.TiffTestConstants.FILE_FLOAT32
import ag.granular.tiff.TiffTestConstants.FILE_FLOAT64
import ag.granular.tiff.TiffTestConstants.FILE_INT32
import ag.granular.tiff.TiffTestConstants.FILE_INTERLEAVE
import ag.granular.tiff.TiffTestConstants.FILE_LZW
import ag.granular.tiff.TiffTestConstants.FILE_PACKBITS
import ag.granular.tiff.TiffTestConstants.FILE_STRIPPED
import ag.granular.tiff.TiffTestConstants.FILE_TILED
import ag.granular.tiff.TiffTestConstants.FILE_TILED_PLANAR
import ag.granular.tiff.TiffTestConstants.FILE_UINT32
import kotlin.test.Ignore
import kotlin.test.Test

@Ignore
class TiffReadTest {

    @Test
    fun `Test the stripped TIFF file vs the same data tiled`() {
        val strippedTiff = measureTime("strippedTiff") { TiffReader(getTestData(FILE_STRIPPED)).readTiff() }
        val tiledTiff = measureTime("tiledTiff") { TiffReader(getTestData(FILE_TILED)).readTiff() }
        measureTime("compareTIFFImages") { compareTIFFImages(strippedTiff, tiledTiff) }
    }

    @Test
    fun `Test the stripped TIFF file vs the same data as int 32`() {
        val strippedTiff = TiffReader(getTestData(FILE_STRIPPED)).readTiff()
        val int32Tiff = TiffReader(getTestData(FILE_INT32)).readTiff()
        compareTIFFImages(strippedTiff, int32Tiff, true, false)
    }

    @Test
    fun `Test the stripped TIFF file vs the same data as unsigned int 32`() {
        val strippedTiff = TiffReader(getTestData(FILE_STRIPPED)).readTiff()
        val uint32Tiff = TiffReader(getTestData(FILE_UINT32)).readTiff()
        compareTIFFImages(strippedTiff, uint32Tiff, false, false)
    }

    @Test
    fun `Test the stripped TIFF file vs the same data as float 32`() {
        val strippedTiff = TiffReader(getTestData(FILE_STRIPPED)).readTiff()
        val float32Tiff = TiffReader(getTestData(FILE_FLOAT32)).readTiff()
        compareTIFFImages(strippedTiff, float32Tiff, false, false)
    }

    @Test
    fun `Test the stripped TIFF file vs the same data as float 64`() {
        val strippedTiff = TiffReader(getTestData(FILE_STRIPPED)).readTiff()
        val float64Tiff = TiffReader(getTestData(FILE_FLOAT64)).readTiff()
        compareTIFFImages(strippedTiff, float64Tiff, false, false)
    }

    @Test
    fun `Test the stripped TIFF file vs the same data compressed as LZW`() {
        val strippedTiff = TiffReader(getTestData(FILE_STRIPPED)).readTiff()
        val lzwTiff = TiffReader(getTestData(FILE_LZW)).readTiff()
        compareTIFFImages(strippedTiff, lzwTiff)
    }

    @Test
    fun `Test the stripped TIFF file vs the same data compressed as Packbits`() {
        val strippedTiff = TiffReader(getTestData(FILE_LZW)).readTiff()
        val packbitsTiff = TiffReader(getTestData(FILE_PACKBITS)).readTiff()
        compareTIFFImages(strippedTiff, packbitsTiff)
    }

    @Test
    fun `Test the stripped TIFF file vs the same data as interleaved`() {
        val strippedTiff = TiffReader(getTestData(FILE_STRIPPED)).readTiff()
        val interleaveTiff = TiffReader(getTestData(FILE_INTERLEAVE)).readTiff()
        compareTIFFImages(strippedTiff, interleaveTiff)
    }

    @Test
    fun `Test the stripped TIFF file vs the same data as tiled planar`() {
        val strippedTiff = TiffReader(getTestData(FILE_STRIPPED)).readTiff()
        val tiledPlanarTiff = TiffReader(getTestData(FILE_TILED_PLANAR)).readTiff()
        compareTIFFImages(strippedTiff, tiledPlanarTiff)
    }
}
