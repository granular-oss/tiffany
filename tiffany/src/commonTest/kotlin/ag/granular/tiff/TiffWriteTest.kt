package ag.granular.tiff

import ag.granular.tiff.TiffTestConstants.FILE_STRIPPED
import ag.granular.tiff.util.TiffConstants.COMPRESSION_NO
import ag.granular.tiff.util.TiffConstants.PLANAR_CONFIGURATION_CHUNKY
import ag.granular.tiff.util.TiffConstants.PLANAR_CONFIGURATION_PLANAR
import kotlin.test.Ignore
import kotlin.test.Test

@Ignore
class TiffWriteTest {

    @Test
    fun `Test writing and reading a stripped TIFF chunky file`() {

        val strippedTiff = TiffReader(getTestData(FILE_STRIPPED)).readTiff()

        val fileDirectory = strippedTiff.fileDirectories.first()
        val rasters = fileDirectory.readRasters()
        val rastersInterleaved = fileDirectory.readInterleavedRasters()

        fileDirectory.writeRasters = rasters
        fileDirectory.setCompression(COMPRESSION_NO)
        fileDirectory.setPlanarConfiguration(PLANAR_CONFIGURATION_CHUNKY)
        val rowsPerStrip = rasters.calculateRowsPerStrip(
            fileDirectory.getPlanarConfiguration()!!
        )
        fileDirectory.setRowsPerStrip(rowsPerStrip)
        val tiffBytes = TiffWriter().writeTiffToBytes(strippedTiff)

        val readTiffImage = TiffReader(tiffBytes).readTiff()
        val fileDirectory2 = readTiffImage.fileDirectories.first()
        val rasters2 = fileDirectory2.readRasters()
        val rasters2Interleaved = fileDirectory2.readInterleavedRasters()

        compareRastersSampleValues(rasters, rasters2)
        compareRastersInterleaveValues(
            rastersInterleaved,
            rasters2Interleaved
        )
    }

    @Test
    fun `Test writing and reading a stripped TIFF planar file`() {

        val strippedTiff = TiffReader(getTestData(FILE_STRIPPED)).readTiff()

        val fileDirectory = strippedTiff.fileDirectories.first()
        val rasters = fileDirectory.readRasters()
        val rastersInterleaved = fileDirectory.readInterleavedRasters()

        fileDirectory.writeRasters = rasters
        fileDirectory.setCompression(COMPRESSION_NO)
        fileDirectory
            .setPlanarConfiguration(PLANAR_CONFIGURATION_PLANAR)
        val rowsPerStrip = rasters.calculateRowsPerStrip(
            fileDirectory.getPlanarConfiguration()!!
        )
        fileDirectory.setRowsPerStrip(rowsPerStrip)
        val tiffBytes = TiffWriter().writeTiffToBytes(strippedTiff)

        val readTiffImage = TiffReader(tiffBytes).readTiff()
        val fileDirectory2 = readTiffImage.fileDirectories.first()
        val rasters2 = fileDirectory2.readRasters()
        val rasters2Interleaved = fileDirectory2.readInterleavedRasters()

        compareRastersSampleValues(rasters, rasters2)
        compareRastersInterleaveValues(
            rastersInterleaved,
            rasters2Interleaved
        )
    }
}
