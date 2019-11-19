package ag.granular.tiff

import ag.granular.tiff.io.ByteReader
import ag.granular.tiff.util.TiffConstants
import ag.granular.tiff.util.TiffException
import ag.granular.io.ByteOrder
/**
 * TIFF reader
 */
class TiffReader(
    bytes: ByteArray,
    private val cache: Boolean = false
) {

    private val reader = ByteReader(bytes, ByteOrder.LITTLE_ENDIAN)

    fun <T> readTiff(block: TIFFImage.() -> T): T {
        val tiffImage = readTiff()
        val res = block(tiffImage)
        // TODO release all
        return res
    }

    // TODO make private after fixing tests
    fun readTiff(): TIFFImage {

        // Read the 2 bytes of byte order
        var byteOrderString: String = try {
            reader.readString(2)
                ?: throw TiffException("Failed to read byte order, byteOrderString is null")
        } catch (e: Exception) {
            throw TiffException("Failed to read byte order", e)
        }

        // Determine the byte order
        var byteOrder: ByteOrder = when (byteOrderString) {
            TiffConstants.BYTE_ORDER_LITTLE_ENDIAN -> ByteOrder.LITTLE_ENDIAN
            TiffConstants.BYTE_ORDER_BIG_ENDIAN -> ByteOrder.BIG_ENDIAN
            else -> throw TiffException("Invalid byte order: $byteOrderString")
        }
        reader.byteReaderOrder = byteOrder

        // Validate the TIFF file identifier
        val tiffIdentifier = reader.readUnsignedShort()
        if (tiffIdentifier != TiffConstants.FILE_IDENTIFIER) {
            throw TiffException("Invalid file identifier, not a TIFF")
        }

        // Get the offset in bytes of the first image file directory (IFD)
        val byteOffset = reader.readUnsignedInt().toInt()

        // Get the TIFF Image
        return parseTIFFImage(reader, byteOffset, cache)
    }

    /**
     * Parse the TIFF Image with file directories
     *
     * @param reader
     * byte reader
     * @param byteOffset
     * byte offset
     * @param cache
     * true to cache tiles and strips
     * @return TIFF image
     */
    private fun parseTIFFImage(
        reader: ByteReader,
        byteOffset: Int,
        cache: Boolean
    ): TIFFImage {
        var byteOffset = byteOffset

        val fileDirectories = mutableListOf<FileDirectory>()
        // Continue until the byte offset no longer points to another file
        // directory
        while (byteOffset != 0) {

            // Set the next byte to read from
            reader.nextByte = byteOffset

            // Create the new directory
            val entries = LinkedHashSet<FileDirectoryEntry>()

            // Read the number of directory entries
            val numDirectoryEntries = reader.readUnsignedShort()

            // Read each entry and the values
            for (entryCount in 0 until numDirectoryEntries) {

                // Read the field tag, field type, and type count
                val fieldTagValue = reader.readUnsignedShort()
                val fieldTag = FieldTagType.getById(fieldTagValue)
                val fieldTypeValue = reader.readUnsignedShort()
                val fieldType = FieldType.getFieldType(fieldTypeValue)
                val typeCount = reader.readUnsignedInt()

                // Save off the next byte to read location
                val nextByte = reader.nextByte

                // Read the field values
                val values = readFieldValues(
                    reader, fieldTag, fieldType,
                    typeCount
                )

                // Create and add a file directory
                val entry = FileDirectoryEntry(
                    fieldTag,
                    fieldType, typeCount, values!!
                )
                entries.add(entry)

                // Restore the next byte to read location
                reader.nextByte = nextByte + 4
            }

            // Add the file directory
            val fileDirectory = FileDirectory(
                entries, reader,
                cache
            )
            fileDirectories.add(fileDirectory)

            // Read the next byte offset location
            byteOffset = reader.readUnsignedInt().toInt()
        }

        return TIFFImage(fileDirectories = fileDirectories)
    }

    /**
     * Read the field values
     *
     * @param reader
     * byte reader
     * @param fieldTag
     * field tag type
     * @param fieldType
     * field type
     * @param typeCount
     * type count
     * @return values
     */
    private fun readFieldValues(
        reader: ByteReader,
        fieldTag: FieldTagType,
        fieldType: FieldType,
        typeCount: Long
    ): Any? {

        // If the value is larger and not stored inline, determine the offset
        if (fieldType.bytes * typeCount > 4) {
            val valueOffset = reader.readUnsignedInt().toInt()
            reader.nextByte = valueOffset
        }

        // Read the directory entry values
        val valuesList = getValues(reader, fieldType, typeCount)

        // Get the single or array values
        var values: Any? = null
        if (typeCount == 1L &&
            !fieldTag.isArray &&
            !(fieldType === FieldType.RATIONAL || fieldType === FieldType.SRATIONAL)
        ) {
            values = valuesList[0]
        } else {
            values = valuesList
        }

        return values
    }

    /**
     * Get the directory entry values
     *
     * @param reader
     * byte reader
     * @param fieldType
     * field type
     * @param typeCount
     * type count
     * @return values
     */
    private fun getValues(
        reader: ByteReader,
        fieldType: FieldType,
        typeCount: Long
    ): List<Any> {

        var values = mutableListOf<Any?>()

        for (i in 0 until typeCount) {

            when (fieldType) {
                FieldType.ASCII -> try {
                    values.add(reader.readString(1))
                } catch (e: Exception) {
                    throw TiffException("Failed to read ASCII character", e)
                }

                FieldType.BYTE, FieldType.UNDEFINED -> values.add(reader.readUnsignedByte())
                FieldType.SBYTE -> values.add(reader.readByte())
                FieldType.SHORT -> values.add(reader.readUnsignedShort())
                FieldType.SSHORT -> values.add(reader.readShort())
                FieldType.LONG -> values.add(reader.readUnsignedInt())
                FieldType.SLONG -> values.add(reader.readInt())
                FieldType.RATIONAL -> {
                    values.add(reader.readUnsignedInt())
                    values.add(reader.readUnsignedInt())
                }
                FieldType.SRATIONAL -> {
                    values.add(reader.readInt())
                    values.add(reader.readInt())
                }
                FieldType.FLOAT -> values.add(reader.readFloat())
                FieldType.DOUBLE -> values.add(reader.readDouble())
                else -> throw TiffException("Invalid field type: $fieldType")
            }
        }

        // If ASCII characters, combine the strings
        if (fieldType === FieldType.ASCII) {
            val stringValues = mutableListOf<Any>()
            var builder = StringBuilder()
            for (value in values) {
                if (value == null) {
                    if (builder.isNotEmpty()) {
                        stringValues.add(builder.toString())
                        builder = StringBuilder()
                    }
                } else {
                    builder.append(value.toString())
                }
            }
            return stringValues
        }

        return values.filterNotNull()
    }
}
