package ag.granular.tiff

import java.io.File
import java.io.IOException

/**
 * Test reading an argument provided TIFF file
 */
object TiffFileTester {

    /**
     * Main method, provide a single file path argument
     *
     * @param args
     * @throws IOException
     */
    @Throws(IOException::class)
    @JvmStatic
    fun main(args: Array<String>) {

        if (args.size != 1) {
            throw IllegalArgumentException(
                "Provide a single TIFF file path argument"
            )
        }

        val file = File(args[0])
        if (!file.exists()) {
            throw IllegalArgumentException("TIFF file does not exist: " + file.absolutePath)
        }

        val tiffImage = TiffReader(file.readBytes()).readTiff()

        println("TIFF Image: " + file.name)

        val fileDirectories = tiffImage.fileDirectories
        for (i in fileDirectories.indices) {
            val fileDirectory = fileDirectories[i]

            println()
            print("-- File Directory ")
            if (fileDirectories.size > 1) {
                print((i + 1).toString() + " ")
            }
            println("--")

            for ((fieldTag, fieldType, typeCount, values) in fileDirectory.getEntries()) {

                println()
                println(
                    fieldTag.toString() + " (" +
                            fieldTag.id + ")"
                )
                println(
                    fieldType.toString() + " (" +
                            fieldType.bytes + " bytes)"
                )
                println("Count: $typeCount")
                println("Values: $values")
            }

            val rasters = fileDirectory.readRasters()
            println()
            println("-- Rasters --")
            println()
            println("Width: " + rasters.width)
            println("Height: " + rasters.height)
            println("Number of Pixels: " + rasters.numPixels)
            println("Samples Per Pixel: " + rasters.samplesPerPixel)
            println("Bits Per Sample: " + rasters.getBitsPerSample())

            println()
            printPixel(rasters, 0, 0)
            printPixel(
                rasters, (rasters.width / 2.0).toInt(),
                (rasters.height / 2.0).toInt()
            )
            printPixel(rasters, rasters.width - 1, rasters.height - 1)

            println()
        }
    }

    /**
     * Print a pixel from the rasters
     *
     * @param rasters
     * rasters
     * @param x
     * x coordinate
     * @param y
     * y coordinate
     */
    private fun printPixel(rasters: Rasters, x: Int, y: Int) {
        print("Pixel x = $x, y = $y: [")
        val pixel = rasters.getPixel(x, y)
        for (i in pixel.indices) {
            if (i > 0) {
                print(", ")
            }
            print(pixel[i])
        }
        println("]")
    }
}
