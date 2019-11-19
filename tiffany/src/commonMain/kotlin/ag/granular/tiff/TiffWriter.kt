package ag.granular.tiff

import ag.granular.tiff.compression.CompressionEncoder
import ag.granular.tiff.compression.DeflateCompression
import ag.granular.tiff.compression.LZWCompression
import ag.granular.tiff.compression.PackbitsCompression
import ag.granular.tiff.compression.RawCompression
import ag.granular.tiff.io.ByteWriter
import ag.granular.tiff.util.TiffConstants
import ag.granular.tiff.util.TiffException
import ag.granular.io.ByteOrder
import ag.granular.io.IOException
import kotlin.math.min

/**
 * TIFF Writer.
 *
 * For a striped TIFF, the [FileDirectory.setStripOffsets] and
 * [FileDirectory.setStripByteCounts] methods are automatically set
 * or adjusted based upon attributes including:
 * [FileDirectory.getRowsPerStrip],
 * [FileDirectory.getImageHeight],
 * [FileDirectory.getPlanarConfiguration], and
 * [FileDirectory.getSamplesPerPixel].
 *
 * The [Rasters.calculateRowsPerStrip] and
 * [Rasters.calculateRowsPerStrip] methods provide a mechanism
 * for determining a [FileDirectory.getRowsPerStrip] setting.
 */
class TiffWriter {

//    /**
//     * Write a TIFF to bytes
//     *
//     * @param tiffImage
//     * TIFF image
//     * @return tiff bytes
//     * @throws IOException
//     * upon failure to write
//     */
//    @Throws(IOException::class)
    fun writeTiffToBytes(tiffImage: TIFFImage): ByteArray {
        val writer = ByteWriter()
        val bytes = writeTiffToBytes(writer, tiffImage)
        writer.close()
        return bytes
    }

    /**
     * Write a TIFF to bytes
     *
     * @param writer
     * byte writer
     * @param tiffImage
     * TIFF image
     * @return tiff bytes
     * @throws IOException
     * upon failure to write
     */
//    @Throws(IOException::class)
    fun writeTiffToBytes(writer: ByteWriter, tiffImage: TIFFImage): ByteArray {
        writeTiff(writer, tiffImage)
        return writer.bytes
    }

    /**
     * Write a TIFF to a byte writer
     *
     * @param writer
     * byte writer
     * @param tiffImage
     * TIFF image
     * @throws IOException
     * upon failure to write
     */
//    @Throws(IOException::class)
    fun writeTiff(writer: ByteWriter, tiffImage: TIFFImage) {

        // Write the byte order (bytes 0-1)
        val byteOrder = if (writer.byteOrder == ByteOrder.BIG_ENDIAN)
            TiffConstants.BYTE_ORDER_BIG_ENDIAN
        else
            TiffConstants.BYTE_ORDER_LITTLE_ENDIAN
        writer.writeString(byteOrder)

        // Write the TIFF file identifier (bytes 2-3)
        writer.writeUnsignedShort(TiffConstants.FILE_IDENTIFIER)

        // Write the first IFD offset (bytes 4-7), set to start right away at
        // byte 8
        writer.writeUnsignedInt(TiffConstants.HEADER_BYTES.toLong())

        // Write the TIFF Image
        writeImageFileDirectories(writer, tiffImage)
    }

    /**
     * Write the image file directories
     *
     * @param writer
     * byte writer
     * @param tiffImage
     * tiff image
     * @throws IOException
     */
//    @Throws(IOException::class)
    private fun writeImageFileDirectories(
        writer: ByteWriter,
        tiffImage: TIFFImage
    ) {

        // Write each file directory
        for (i in 0 until tiffImage.fileDirectories.size) {
            val fileDirectory = tiffImage.fileDirectories[i]

            // Populate strip entries with placeholder values so the sizes come
            // out correctly
            populateRasterEntries(fileDirectory)

            // Track of the starting byte of this directory
            val startOfDirectory = writer.size()
            val afterDirectory = startOfDirectory + fileDirectory.size()
            val afterValues = startOfDirectory + fileDirectory.sizeWithValues()

            // Write the number of directory entries
            writer.writeUnsignedShort(fileDirectory.numEntries())

            val entryValues = ArrayList<FileDirectoryEntry>()

            // Byte to write the next values
            var nextByte = afterDirectory

            val valueBytesCheck = ArrayList<Long>()

            // Write the raster bytes to temporary storage
            if (fileDirectory.isTiled) {
                throw TiffException("Tiled images are not supported")
            }

            // Create the raster bytes, written to the stream later
            val rastersBytes = writeRasters(
                writer.byteOrder,
                fileDirectory, afterValues
            )

            // Write each entry
            for (entry in fileDirectory.getEntries()) {
                writer.writeUnsignedShort(entry.fieldTag.id)
                writer.writeUnsignedShort(entry.fieldType.value)
                writer.writeUnsignedInt(entry.typeCount)
                val valueBytes = entry.fieldType.bytes * entry.typeCount
                if (valueBytes > 4) {
                    // Write the value offset
                    entryValues.add(entry)
                    writer.writeUnsignedInt(nextByte)
                    valueBytesCheck.add(nextByte)
                    nextByte += entry.sizeOfValues()
                } else {
                    // Write the value in the inline 4 byte space, left aligned
                    val bytesWritten = writeValues(writer, entry)
                    if (bytesWritten.toLong() != valueBytes) {
                        throw TiffException(
                            "Unexpected bytes written. Expected: " +
                                    valueBytes + ", Actual: " +
                                    bytesWritten
                        )
                    }
                    writeFillerBytes(writer, 4 - valueBytes)
                }
            }

            if (i + 1 == tiffImage.fileDirectories.size) {
                // Write 0's since there are not more file directories
                writeFillerBytes(writer, 4)
            } else {
                // Write the start address of the next file directory
                val nextFileDirectory = afterValues + rastersBytes.size
                writer.writeUnsignedInt(nextFileDirectory)
            }

            // Write the external entry values
            for (entryIndex in entryValues.indices) {
                val entry = entryValues[entryIndex]
                val entryValuesByte = valueBytesCheck[entryIndex]
                if (entryValuesByte != writer.size().toLong()) {
                    throw TiffException(
                        "Entry values byte does not match the write location. Entry Values Byte: " +
                                entryValuesByte + ", Current Byte: " +
                                writer.size()
                    )
                }
                val bytesWritten = writeValues(writer, entry)
                val valueBytes = entry.fieldType.bytes * entry.typeCount
                if (bytesWritten.toLong() != valueBytes) {
                    throw TiffException(
                        "Unexpected bytes written. Expected: " + valueBytes +
                                ", Actual: " + bytesWritten
                    )
                }
            }

            // Write the image bytes
            writer.writeBytes(rastersBytes)
        }
    }

    /**
     * Populate the raster entry values with placeholder values for correct size
     * calculations
     *
     * @param fileDirectory
     * file directory
     */
    private fun populateRasterEntries(fileDirectory: FileDirectory) {

        val rasters = fileDirectory.writeRasters ?: throw TiffException(
            "File Directory Writer Rasters is required to create a TIFF"
        )

        // Populate the raster entries
        if (!fileDirectory.isTiled) {
            populateStripEntries(fileDirectory)
        } else {
            throw TiffException("Tiled images are not supported")
        }
    }

    /**
     * Populate the strip entries with placeholder values
     *
     * @param fileDirectory
     * file directory
     */
    private fun populateStripEntries(fileDirectory: FileDirectory) {

        val rowsPerStrip = fileDirectory.rowsPerStrip?.toInt() ?: 0
        val imageHeight = fileDirectory.imageHeight.toInt()
        val stripsPerSample = (imageHeight + rowsPerStrip - 1) / rowsPerStrip
        var strips = stripsPerSample
        if (fileDirectory.getPlanarConfiguration() == TiffConstants.PLANAR_CONFIGURATION_PLANAR) {
            strips *= fileDirectory.samplesPerPixel
        }

        fileDirectory.setStripOffsetsAsLongs(
            List(strips) { 0L }
        )
        fileDirectory.setStripByteCounts(
            List(strips) { 0 }
        )
    }

    /**
     * Write the rasters as bytes
     *
     * @param byteOrder
     * byte order
     * @param fileDirectory
     * file directory
     * @param offset
     * byte offset
     * @return rasters bytes
     * @throws IOException
     */
//    @Throws(IOException::class)
    private fun writeRasters(
        byteOrder: ByteOrder,
        fileDirectory: FileDirectory,
        offset: Long
    ): ByteArray {

        val rasters = fileDirectory.writeRasters ?: throw TiffException(
            "File Directory Writer Rasters is required to create a TIFF"
        )

        // Get the compression encoder
        val encoder = getEncoder(fileDirectory)

        // Byte writer to write the raster
        val writer = ByteWriter(byteOrder)

        // Write the rasters
        if (!fileDirectory.isTiled) {
            writeStripRasters(writer, fileDirectory, offset, encoder)
        } else {
            throw TiffException("Tiled images are not supported")
        }

        // Return the rasters bytes
        val bytes = writer.bytes
        writer.close()

        return bytes
    }

    /**
     * Write the rasters as bytes
     *
     * @param writer
     * byte writer
     * @param fileDirectory
     * file directory
     * @param offset
     * byte offset
     * @param encoder
     * compression encoder
     * @throws IOException
     */
//    @Throws(IOException::class)
    private fun writeStripRasters(
        writer: ByteWriter,
        fileDirectory: FileDirectory,
        offset: Long,
        encoder: CompressionEncoder
    ) {
        var offset = offset

        val rasters = fileDirectory.writeRasters!!

        // Get the row and strip counts
        val rowsPerStrip = fileDirectory.rowsPerStrip!!.toInt()
        val maxY = fileDirectory.imageHeight.toInt()
        val stripsPerSample = (maxY + rowsPerStrip - 1) / rowsPerStrip
        var strips = stripsPerSample
        if (fileDirectory.getPlanarConfiguration() == TiffConstants.PLANAR_CONFIGURATION_PLANAR) {
            strips *= fileDirectory.samplesPerPixel
        }

        // Build the strip offsets and byte counts
        val stripOffsets = ArrayList<Long>()
        val stripByteCounts = ArrayList<Int>()

        // Write each strip
        for (strip in 0 until strips) {

            val startingY: Int
            var sample: Int? = null
            if (fileDirectory.getPlanarConfiguration() == TiffConstants.PLANAR_CONFIGURATION_PLANAR) {
                sample = strip / stripsPerSample
                startingY = strip % stripsPerSample * rowsPerStrip
            } else {
                startingY = strip * rowsPerStrip
            }

            // Write the strip of bytes
            val stripWriter = ByteWriter(writer.byteOrder)

            val endingY = min(startingY + rowsPerStrip, maxY)
            for (y in startingY until endingY) {
                // Get the row bytes and encode if needed
                var rowBytes: ByteArray? = null
                if (sample != null) {
                    rowBytes = rasters.getSampleRow(
                        y, sample,
                        writer.byteOrder
                    )
                } else {
                    rowBytes = rasters.getPixelRow(y, writer.byteOrder)
                }

                if (encoder.rowEncoding()) {
                    rowBytes = encoder.encode(rowBytes!!, writer.byteOrder)
                }

                // Write the row
                stripWriter.writeBytes(rowBytes!!)
            }

            // Get the strip bytes and encode if needed
            var stripBytes = stripWriter.bytes
            stripWriter.close()
            if (!encoder.rowEncoding()) {
                stripBytes = encoder.encode(stripBytes, writer.byteOrder)
            }

            // Write the strip bytes
            writer.writeBytes(stripBytes)

            // Add the strip byte count
            val bytesWritten = stripBytes.size
            stripByteCounts.add(bytesWritten)

            // Add the strip offset
            stripOffsets.add(offset)
            offset += bytesWritten.toLong()
        }

        // Set the strip offsets and byte counts
        fileDirectory.setStripOffsetsAsLongs(stripOffsets)
        fileDirectory.setStripByteCounts(stripByteCounts)
    }

    /**
     * Get the compression encoder
     *
     * @param fileDirectory
     * file directory
     * @return encoder
     */
    private fun getEncoder(fileDirectory: FileDirectory): CompressionEncoder {

        var encoder: CompressionEncoder? = null

        // Determine the encoder based upon the compression
        var compression = fileDirectory.compression
        if (compression == null) {
            compression = TiffConstants.COMPRESSION_NO
        }

        when (compression) {
            TiffConstants.COMPRESSION_NO -> encoder = RawCompression()
            TiffConstants.COMPRESSION_CCITT_HUFFMAN -> throw TiffException("CCITT Huffman compression not supported: $compression")
            TiffConstants.COMPRESSION_T4 -> throw TiffException("T4-encoding compression not supported: $compression")
            TiffConstants.COMPRESSION_T6 -> throw TiffException("T6-encoding compression not supported: $compression")
            TiffConstants.COMPRESSION_LZW -> encoder = LZWCompression()
            TiffConstants.COMPRESSION_JPEG_OLD, TiffConstants.COMPRESSION_JPEG_NEW -> throw TiffException(
                "JPEG compression not supported: $compression"
            )
            TiffConstants.COMPRESSION_DEFLATE, TiffConstants.COMPRESSION_PKZIP_DEFLATE -> encoder =
                DeflateCompression()
            TiffConstants.COMPRESSION_PACKBITS -> encoder =
                PackbitsCompression()
            else -> throw TiffException("Unknown compression method identifier: $compression")
        }

        return encoder
    }

    /**
     * Write filler 0 bytes
     *
     * @param writer
     * byte writer
     * @param count
     * number of 0 bytes to write
     */
    private fun writeFillerBytes(writer: ByteWriter, count: Long) {
        for (i in 0 until count) {
            writer.writeUnsignedByte(0.toShort())
        }
    }

    /**
     * Write file directory entry values
     *
     * @param writer
     * byte writer
     * @param entry
     * file directory entry
     * @return bytes written
     * @throws IOException
     */
//    @Throws(IOException::class)
    private fun writeValues(writer: ByteWriter, entry: FileDirectoryEntry): Int {

        val valuesList = if (entry.typeCount == 1L &&
            !entry.fieldTag.isArray &&
            !(entry.fieldType === FieldType.RATIONAL || entry
                .fieldType === FieldType.SRATIONAL)
        ) {
            ArrayList<Any>().apply {
                add(entry.values)
            }
        } else {
            entry.values as List<Any>
        }

        var bytesWritten = 0

        for (value in valuesList) {

            when (entry.fieldType) {
                FieldType.ASCII -> {
                    bytesWritten += writer.writeString(value as String)
                    if (bytesWritten < entry.typeCount) {
                        val fillerBytes = entry.typeCount - bytesWritten
                        writeFillerBytes(writer, fillerBytes)
                        bytesWritten += fillerBytes.toInt()
                    }
                }
                FieldType.BYTE, FieldType.UNDEFINED -> {
                    writer.writeUnsignedByte(value as Short)
                    bytesWritten += 1
                }
                FieldType.SBYTE -> {
                    writer.writeByte(value as Byte)
                    bytesWritten += 1
                }
                FieldType.SHORT -> {
                    writer.writeUnsignedShort(value as Int)
                    bytesWritten += 2
                }
                FieldType.SSHORT -> {
                    writer.writeShort(value as Short)
                    bytesWritten += 2
                }
                FieldType.LONG -> {
                    writer.writeUnsignedInt(value as Long)
                    bytesWritten += 4
                }
                FieldType.SLONG -> {
                    writer.writeInt(value as Int)
                    bytesWritten += 4
                }
                FieldType.RATIONAL -> {
                    writer.writeUnsignedInt(value as Long)
                    bytesWritten += 4
                }
                FieldType.SRATIONAL -> {
                    writer.writeInt(value as Int)
                    bytesWritten += 4
                }
                FieldType.FLOAT -> {
                    writer.writeFloat(value as Float)
                    bytesWritten += 4
                }
                FieldType.DOUBLE -> {
                    writer.writeDouble(value as Double)
                    bytesWritten += 8
                }
                else -> throw TiffException("Invalid field type: " + entry.fieldType)
            }
        }

        return bytesWritten
    }
}
