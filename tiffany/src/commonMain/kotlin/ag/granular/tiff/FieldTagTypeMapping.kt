package ag.granular.tiff

import ag.granular.tiff.compression.CompressionDecoder
import ag.granular.tiff.compression.DeflateCompression
import ag.granular.tiff.io.ByteReader
import ag.granular.tiff.compression.LZWCompression
import ag.granular.tiff.compression.PackbitsCompression
import ag.granular.tiff.compression.RawCompression
import ag.granular.tiff.util.TiffConstants
import ag.granular.tiff.util.TiffException

class FieldTagTypeMapping(
    /**
     * File directory entries in sorted tag id order
     */
    private val entries: LinkedHashSet<FileDirectoryEntry>
) {

    /**
     * Mapping between tags and entries
     */
    private val fieldTagTypeMapping = HashMap<FieldTagType, FileDirectoryEntry>()

    /**
     * Is this a tiled image
     */
    val isTiled: Boolean

    /**
     * Planar configuration
     */
    val planarConfiguration: Int

    /**
     * Get the compression decoder
     *
     * @return compression decoder
     */
    val decoder: CompressionDecoder

    /**
     * Last block index, index of single block cache
     */
    private var lastBlockIndex = -1

    /**
     * Last block, single block cache when caching is not enabled
     */
    private var lastBlock: ByteArray? = null

    /**
     * Get the image width
     *
     * @return image width
     */
    val imageWidth: Number
        get() = getNumberEntryValue(FieldTagType.ImageWidth)!!

    /**
     * Get the image height
     *
     * @return image height
     */
    val imageHeight: Number
        get() = getNumberEntryValue(FieldTagType.ImageLength)!!

    var bitsPerSample: List<Int>?
        get() = getIntegerListEntryValue(FieldTagType.BitsPerSample)
        set(bitsPerSample) = setUnsignedIntegerListEntryValue(
            FieldTagType.BitsPerSample,
            bitsPerSample!!
        )

    /**
     * Get the max bits per sample
     */
    val maxBitsPerSample: Int?
        get() = getMaxIntegerEntryValue(FieldTagType.BitsPerSample)

    /**
     * Get the compression
     */
    val compression: Int?
        get() = getIntegerEntryValue(FieldTagType.Compression)

    /**
     * Get the photometric interpretation
     */
    val photometricInterpretation: Int?
        get() = getIntegerEntryValue(FieldTagType.PhotometricInterpretation)

    /**
     * Get the strip offsets
     */
    val stripOffsets: List<Number>?
        get() = getNumberListEntryValue(FieldTagType.StripOffsets)

    // if SamplesPerPixel tag is missing, use default value defined by
    // TIFF standard
    var samplesPerPixel: Int
        get() {
            var samplesPerPixel = getIntegerEntryValue(FieldTagType.SamplesPerPixel)
            if (samplesPerPixel == null) {
                samplesPerPixel = 1
            }
            return samplesPerPixel
        }
        set(samplesPerPixel) = setUnsignedIntegerEntryValue(
            FieldTagType.SamplesPerPixel,
            samplesPerPixel
        )

    val rowsPerStrip: Number?
        get() = getNumberEntryValue(FieldTagType.RowsPerStrip)

    val stripByteCounts: List<Number>?
        get() = getNumberListEntryValue(FieldTagType.StripByteCounts)

    var xResolution: List<Long>?
        get() = getLongListEntryValue(FieldTagType.XResolution)
        set(xResolution) = setUnsignedLongListEntryValue(FieldTagType.XResolution, xResolution!!)

    /**
     * Get the y resolution
     *
     * @return y resolution
     */
    /**
     * Set the y resolution
     *
     * @param yResolution
     * y resolution
     */
    var yResolution: List<Long>?
        get() = getLongListEntryValue(FieldTagType.YResolution)
        set(yResolution) = setUnsignedLongListEntryValue(FieldTagType.YResolution, yResolution!!)

    /**
     * Get the resolution unit
     *
     * @return resolution unit
     */
    val resolutionUnit: Int?
        get() = getIntegerEntryValue(FieldTagType.ResolutionUnit)

    /**
     * Get the color map
     *
     * @return color map
     */
    /**
     * Set the color map
     *
     * @param colorMap
     * color map
     */
    var colorMap: List<Int>?
        get() = getIntegerListEntryValue(FieldTagType.ColorMap)
        set(colorMap) = setUnsignedIntegerListEntryValue(FieldTagType.ColorMap, colorMap!!)

    /**
     * Get the tile width
     *
     * @return tile width
     */
    val tileWidth: Number?
        get() = if (isTiled)
            getNumberEntryValue(FieldTagType.TileWidth)
        else
            imageWidth

    /**
     * Get the tile height
     *
     * @return tile height
     */
    val tileHeight: Number?
        get() = if (isTiled)
            getNumberEntryValue(FieldTagType.TileLength)
        else
            rowsPerStrip

    /**
     * Get the tile offsets
     *
     * @return tile offsets
     */
    /**
     * Set the tile offsets
     *
     * @param tileOffsets
     * tile offsets
     */
    var tileOffsets: List<Long>?
        get() = getLongListEntryValue(FieldTagType.TileOffsets)
        set(tileOffsets) = setUnsignedLongListEntryValue(FieldTagType.TileOffsets, tileOffsets!!)

    /**
     * Get the tile byte counts
     *
     * @return tile byte counts
     */
    val tileByteCounts: List<Number>?
        get() = getNumberListEntryValue(FieldTagType.TileByteCounts)

    /**
     * Get the sample format
     *
     * @return sample format
     */
    /**
     * Set the sample format
     *
     * @param sampleFormat
     * sample format
     */
    var sampleFormat: List<Int>?
        get() = getIntegerListEntryValue(FieldTagType.SampleFormat)
        set(sampleFormat) = setUnsignedIntegerListEntryValue(
            FieldTagType.SampleFormat,
            sampleFormat!!
        )

    /**
     * Get the max sample format
     *
     * @return max sample format
     */
    val maxSampleFormat: Int?
        get() = getMaxIntegerEntryValue(FieldTagType.SampleFormat)

    /**
     * Calculates the number of bytes for each pixel across all samples. Only
     * full bytes are supported, an exception is thrown when this is not the
     * case.
     *
     * @return the bytes per pixel
     */
    private val bytesPerPixel: Int
        get() {
            var bitsPerSample = 0
            val bitsPerSamples = this.bitsPerSample!!
            for (i in bitsPerSamples.indices) {
                val bits = bitsPerSamples[i]
                if (bits % 8 != 0) {
                    throw TiffException(
                        "Sample bit-width of " + bits +
                                " is not supported"
                    )
                } else if (bits != bitsPerSamples[0]) {
                    throw TiffException(
                        "Differing size of samples in a pixel are not supported. sample 0 = " +
                                bitsPerSamples[0] + ", sample " + i +
                                " = " + bits
                    )
                }
                bitsPerSample += bits
            }
            return bitsPerSample / 8
        }

    init {
        for (entry in entries) {
            fieldTagTypeMapping[entry.fieldTag] = entry
        }

        // Determine if tiled
        isTiled = rowsPerStrip == null

        // Determine and validate the planar configuration
        val pc = getPlanarConfiguration()
        planarConfiguration = pc ?: TiffConstants.PLANAR_CONFIGURATION_CHUNKY
        if (planarConfiguration != TiffConstants.PLANAR_CONFIGURATION_CHUNKY && planarConfiguration != TiffConstants.PLANAR_CONFIGURATION_PLANAR) {
            throw TiffException("Invalid planar configuration: $planarConfiguration")
        }

        // Determine the decoder based upon the compression
        var compression = compression ?: TiffConstants.COMPRESSION_NO

        decoder = when (compression) {
            TiffConstants.COMPRESSION_NO -> RawCompression()
            TiffConstants.COMPRESSION_CCITT_HUFFMAN -> throw TiffException("CCITT Huffman compression not supported: $compression")
            TiffConstants.COMPRESSION_T4 -> throw TiffException("T4-encoding compression not supported: $compression")
            TiffConstants.COMPRESSION_T6 -> throw TiffException("T6-encoding compression not supported: $compression")
            TiffConstants.COMPRESSION_LZW -> LZWCompression()
            TiffConstants.COMPRESSION_JPEG_OLD, TiffConstants.COMPRESSION_JPEG_NEW -> throw TiffException(
                "JPEG compression not supported: $compression"
            )
            TiffConstants.COMPRESSION_DEFLATE, TiffConstants.COMPRESSION_PKZIP_DEFLATE ->
                DeflateCompression()
            TiffConstants.COMPRESSION_PACKBITS -> PackbitsCompression()
            else -> throw TiffException("Unknown compression method identifier: $compression")
        }
    }

    // 	/**
    // 	 * Constructor, for writing TIFF files
    // 	 */
    // 	public FileDirectory() {
    // 		this(null);
    // 	}

    // 	/**
    // 	 * Constructor, for writing TIFF files
    // 	 *
    // 	 * @param rasters
    // 	 *            image rasters to write
    // 	 */
    // 	public FileDirectory(Rasters rasters) {
    // 		this(new TreeSet<FileDirectoryEntry>(), rasters);
    // 	}

    // 	/**
    // 	 * Constructor, for writing TIFF files
    // 	 *
    // 	 * @param entries
    // 	 *            file directory entries
    // 	 * @param rasters
    // 	 *            image rasters to write
    // 	 */
    // 	public FileDirectory(SortedSet<FileDirectoryEntry> entries, Rasters rasters) {
    // 		this.entries = entries;
    // 		for (FileDirectoryEntry entry : entries) {
    // 			fieldTagTypeMapping.put(entry.getFieldTag(), entry);
    // 		}
    // 		this.writeRasters = rasters;
    // 	}

    /**
     * Add an entry
     *
     * @param entry
     * file directory entry
     */
    fun addEntry(entry: FileDirectoryEntry) {
        entries.remove(entry)
        entries.add(entry)
        fieldTagTypeMapping[entry.fieldTag] = entry
    }

    /**
     * Get the number of entries
     *
     * @return entry count
     */
    fun numEntries(): Int {
        return entries.size
    }

    /**
     * Get a file directory entry from the field tag type
     *
     * @param fieldTagType
     * field tag type
     * @return file directory entry
     */
    operator fun get(fieldTagType: FieldTagType): FileDirectoryEntry {
        return fieldTagTypeMapping[fieldTagType]!!
    }

    /**
     * Get the field tag type to file directory entry mapping
     *
     * @return field tag type mapping
     */
    fun getFieldTagTypeMapping(): Map<FieldTagType, FileDirectoryEntry> {
        return fieldTagTypeMapping
    }

    /**
     * Set the image width
     *
     * @param width
     * image width
     */
    fun setImageWidth(width: Int) {
        setUnsignedIntegerEntryValue(FieldTagType.ImageWidth, width)
    }

    /**
     * Set the image width
     *
     * @param width
     * image width
     */
    fun setImageWidthAsLong(width: Long) {
        setUnsignedLongEntryValue(FieldTagType.ImageWidth, width)
    }

    /**
     * Set the image height
     *
     * @param height
     * image height
     */
    fun setImageHeight(height: Int) {
        setUnsignedIntegerEntryValue(FieldTagType.ImageLength, height)
    }

    /**
     * Set the image height
     *
     * @param height
     * image height
     */
    fun setImageHeightAsLong(height: Long) {
        setUnsignedLongEntryValue(FieldTagType.ImageLength, height)
    }

    /**
     * Set a single value bits per sample
     *
     * @param bitsPerSample
     * bits per sample
     */
    fun setBitsPerSample(bitsPerSample: Int) {
        this.bitsPerSample = createSingleIntegerList(bitsPerSample)
    }

    /**
     * Set the compression
     *
     * @param compression
     * compression
     */
    fun setCompression(compression: Int) {
        setUnsignedIntegerEntryValue(FieldTagType.Compression, compression)
    }

    /**
     * Set the photometric interpretation
     *
     * @param photometricInterpretation
     * photometric interpretation
     */
    fun setPhotometricInterpretation(photometricInterpretation: Int) {
        setUnsignedIntegerEntryValue(
            FieldTagType.PhotometricInterpretation,
            photometricInterpretation
        )
    }

    /**
     * Set the strip offsets
     *
     * @param stripOffsets
     * strip offsets
     */
    fun setStripOffsets(stripOffsets: List<Int>) {
        setUnsignedIntegerListEntryValue(
            FieldTagType.StripOffsets,
            stripOffsets
        )
    }

    /**
     * Set the strip offsets
     *
     * @param stripOffsets
     * strip offsets
     */
    fun setStripOffsetsAsLongs(stripOffsets: List<Long>) {
        setUnsignedLongListEntryValue(FieldTagType.StripOffsets, stripOffsets)
    }

    /**
     * Set a single value strip offset
     *
     * @param stripOffset
     * strip offset
     */
    fun setStripOffsets(stripOffset: Int) {
        setStripOffsets(createSingleIntegerList(stripOffset))
    }

    /**
     * Set a single value strip offset
     *
     * @param stripOffset
     * strip offset
     */
    fun setStripOffsets(stripOffset: Long) {
        setStripOffsetsAsLongs(createSingleLongList(stripOffset))
    }

    /**
     * Set the rows per strip
     *
     * @param rowsPerStrip
     * rows per strip
     */
    fun setRowsPerStrip(rowsPerStrip: Int) {
        setUnsignedIntegerEntryValue(FieldTagType.RowsPerStrip, rowsPerStrip)
    }

    /**
     * Set the rows per strip
     *
     * @param rowsPerStrip
     * rows per strip
     */
    fun setRowsPerStripAsLong(rowsPerStrip: Long) {
        setUnsignedLongEntryValue(FieldTagType.RowsPerStrip, rowsPerStrip)
    }

    /**
     * Set the strip byte counts
     *
     * @param stripByteCounts
     * strip byte counts
     */
    fun setStripByteCounts(stripByteCounts: List<Int>) {
        setUnsignedIntegerListEntryValue(
            FieldTagType.StripByteCounts,
            stripByteCounts
        )
    }

    /**
     * Set the strip byte counts
     *
     * @param stripByteCounts
     * strip byte counts
     */
    fun setStripByteCountsAsLongs(stripByteCounts: List<Long>) {
        setUnsignedLongListEntryValue(
            FieldTagType.StripByteCounts,
            stripByteCounts
        )
    }

    /**
     * Set a single value strip byte count
     *
     * @param stripByteCount
     * strip byte count
     */
    fun setStripByteCounts(stripByteCount: Int) {
        setStripByteCounts(createSingleIntegerList(stripByteCount))
    }

    /**
     * Set a single value strip byte count
     *
     * @param stripByteCount
     * strip byte count
     */
    fun setStripByteCounts(stripByteCount: Long) {
        setStripByteCountsAsLongs(createSingleLongList(stripByteCount))
    }

    /**
     * Set a single value x resolution
     *
     * @param xResolution
     * x resolution
     */
    fun setXResolution(xResolution: Long) {
        this.xResolution = createSingleLongList(xResolution)
    }

    /**
     * Set a single value y resolution
     *
     * @param yResolution
     * y resolution
     */
    fun setYResolution(yResolution: Long) {
        this.yResolution = createSingleLongList(yResolution)
    }

    /**
     * Get the planar configuration
     *
     * @return planar configuration
     */
    fun getPlanarConfiguration(): Int? {
        return getIntegerEntryValue(FieldTagType.PlanarConfiguration)
    }

    /**
     * Set the planar configuration
     *
     * @param planarConfiguration
     * planar configuration
     */
    fun setPlanarConfiguration(planarConfiguration: Int) {
        setUnsignedIntegerEntryValue(
            FieldTagType.PlanarConfiguration,
            planarConfiguration
        )
    }

    /**
     * Set the resolution unit
     *
     * @param resolutionUnit
     * resolution unit
     */
    fun setResolutionUnit(resolutionUnit: Int) {
        setUnsignedIntegerEntryValue(
            FieldTagType.ResolutionUnit,
            resolutionUnit
        )
    }

    /**
     * Set a single value color map
     *
     * @param colorMap
     * color map
     */
    fun setColorMap(colorMap: Int) {
        this.colorMap = createSingleIntegerList(colorMap)
    }

    /**
     * Set the tile width
     *
     * @param tileWidth
     * tile width
     */
    fun setTileWidth(tileWidth: Int) {
        setUnsignedIntegerEntryValue(FieldTagType.TileWidth, tileWidth)
    }

    /**
     * Set the tile width
     *
     * @param tileWidth
     * tile width
     */
    fun setTileWidthAsLong(tileWidth: Long) {
        setUnsignedLongEntryValue(FieldTagType.TileWidth, tileWidth)
    }

    /**
     * Set the tile height
     *
     * @param tileHeight
     * tile height
     */
    fun setTileHeight(tileHeight: Int) {
        setUnsignedIntegerEntryValue(FieldTagType.TileLength, tileHeight)
    }

    /**
     * Set the tile height
     *
     * @param tileHeight
     * tile height
     */
    fun setTileHeightAsLong(tileHeight: Long) {
        setUnsignedLongEntryValue(FieldTagType.TileLength, tileHeight)
    }

    /**
     * Set a single value tile offset
     *
     * @param tileOffset
     * tile offset
     */
    fun setTileOffsets(tileOffset: Long) {
        tileOffsets = createSingleLongList(tileOffset)
    }

    /**
     * Set the tile byte counts
     *
     * @param tileByteCounts
     * tile byte counts
     */
    fun setTileByteCounts(tileByteCounts: List<Int>) {
        setUnsignedIntegerListEntryValue(
            FieldTagType.TileByteCounts,
            tileByteCounts
        )
    }

    /**
     * Set the tile byte counts
     *
     * @param tileByteCounts
     * tile byte counts
     */
    fun setTileByteCountsAsLongs(tileByteCounts: List<Long>) {
        setUnsignedLongListEntryValue(
            FieldTagType.TileByteCounts,
            tileByteCounts
        )
    }

    /**
     * Set a single value tile byte count
     *
     * @param tileByteCount
     * tile byte count
     */
    fun setTileByteCounts(tileByteCount: Int) {
        setTileByteCounts(createSingleIntegerList(tileByteCount))
    }

    /**
     * Set a single value tile byte count
     *
     * @param tileByteCount
     * tile byte count
     */
    fun setTileByteCounts(tileByteCount: Long) {
        setTileByteCountsAsLongs(createSingleLongList(tileByteCount))
    }

    /**
     * Set a single value sample format
     *
     * @param sampleFormat
     * sample format
     */
    fun setSampleFormat(sampleFormat: Int) {
        this.sampleFormat = createSingleIntegerList(sampleFormat)
    }

    /**
     * Read the value from the reader according to the field type
     *
     * @param reader
     * byte reader
     * @param fieldType
     * field type
     * @return value
     */
    private fun readValue(reader: ByteReader, fieldType: FieldType): Number {

        var value: Number? = null

        when (fieldType) {
            FieldType.BYTE -> value = reader.readUnsignedByte()
            FieldType.SHORT -> value = reader.readUnsignedShort()
            FieldType.LONG -> value = reader.readUnsignedInt()
            FieldType.SBYTE -> value = reader.readByte()
            FieldType.SSHORT -> value = reader.readShort()
            FieldType.SLONG -> value = reader.readInt()
            FieldType.FLOAT -> value = reader.readFloat()
            FieldType.DOUBLE -> value = reader.readDouble()
            else -> throw TiffException("Unsupported raster field type: $fieldType")
        }

        return value
    }

    /**
     * Get the field type for the sample
     *
     * @param sampleIndex
     * sample index
     * @return field type
     */
    fun getFieldTypeForSample(sampleIndex: Int): FieldType {

        val sampleFormatList = sampleFormat
        val sampleFormat = if (sampleFormatList == null)
            TiffConstants.SAMPLE_FORMAT_UNSIGNED_INT
        else
            sampleFormatList[if (sampleIndex < sampleFormatList.size)
                sampleIndex
            else
                0]
        val bitsPerSample = bitsPerSample!![sampleIndex]

        return FieldType.getFieldType(
            sampleFormat,
            bitsPerSample
        )
    }

    /**
     * Get the sample byte size
     *
     * @param sampleIndex
     * sample index
     * @return byte size
     */
    fun getSampleByteSize(sampleIndex: Int): Int {
        val bitsPerSample = bitsPerSample
        if (sampleIndex >= bitsPerSample!!.size) {
            throw TiffException(
                "Sample index " + sampleIndex +
                        " is out of range"
            )
        }
        val bits = bitsPerSample[sampleIndex]
        if (bits % 8 != 0) {
            throw TiffException(
                "Sample bit-width of " + bits +
                        " is not supported"
            )
        }
        return bits / 8
    }

    /**
     * Get an integer entry value
     *
     * @param fieldTagType
     * field tag type
     * @return integer value
     * @since 2.0.0
     */
    fun getIntegerEntryValue(fieldTagType: FieldTagType): Int? {
        return getEntryValue<Int>(fieldTagType)
    }

    /**
     * Set an unsigned integer entry value for the field tag type
     *
     * @param fieldTagType
     * field tag type
     * @param value
     * unsigned integer value (16 bit)
     * @since 2.0.0
     */
    fun setUnsignedIntegerEntryValue(
        fieldTagType: FieldTagType,
        value: Int
    ) {
        setEntryValue(fieldTagType, FieldType.SHORT, 1, value)
    }

    /**
     * Get an number entry value
     *
     * @param fieldTagType
     * field tag type
     * @return number value
     * @since 2.0.0
     */
    fun getNumberEntryValue(fieldTagType: FieldTagType): Number? {
        return getEntryValue<Number>(fieldTagType)
    }

    /**
     * Set an unsigned long entry value for the field tag type
     *
     * @param fieldTagType
     * field tag type
     * @param value
     * unsigned long value (32 bit)
     * @since 2.0.0
     */
    fun setUnsignedLongEntryValue(fieldTagType: FieldTagType, value: Long) {
        setEntryValue(fieldTagType, FieldType.LONG, 1, value)
    }

    /**
     * Get a string entry value for the field tag type
     *
     * @param fieldTagType
     * field tag type
     * @return string value
     * @since 2.0.0
     */
    fun getStringEntryValue(fieldTagType: FieldTagType): String? {
        var value: String? = null
        val values = getEntryValue<List<String>>(fieldTagType)
        if (values != null && !values.isEmpty()) {
            value = values[0]
        }
        return value
    }

    /**
     * Set string value for the field tag type
     *
     * @param fieldTagType
     * field tag type
     * @param value
     * string value
     * @since 2.0.0
     */
    fun setStringEntryValue(fieldTagType: FieldTagType, value: String) {
        val values = ArrayList<String>()
        values.add(value)
        setEntryValue(fieldTagType, FieldType.ASCII, (value.length + 1).toLong(), values)
    }

    /**
     * Get an integer list entry value
     *
     * @param fieldTagType
     * field tag type
     * @return integer list value
     * @since 2.0.0
     */
    fun getIntegerListEntryValue(fieldTagType: FieldTagType): List<Int>? {
        return getEntryValue<List<Int>>(fieldTagType)
    }

    /**
     * Set an unsigned integer list of values for the field tag type
     *
     * @param fieldTagType
     * field tag type
     * @param value
     * integer list value
     * @since 2.0.0
     */
    fun setUnsignedIntegerListEntryValue(
        fieldTagType: FieldTagType,
        value: List<Int>
    ) {
        setEntryValue(fieldTagType, FieldType.SHORT, value.size.toLong(), value)
    }

    /**
     * Get the max integer from integer list entry values
     *
     * @param fieldTagType
     * field tag type
     * @return max integer value
     * @since 2.0.0
     */
    fun getMaxIntegerEntryValue(fieldTagType: FieldTagType): Int? {
        var maxValue: Int? = null
        val values = getIntegerListEntryValue(fieldTagType)
        if (values != null) {
            maxValue = values.maxOrNull()
        }
        return maxValue
    }

    /**
     * Get a number list entry value
     *
     * @param fieldTagType
     * field tag type
     * @return long list value
     * @since 2.0.0
     */
    fun getNumberListEntryValue(fieldTagType: FieldTagType): List<Number>? {
        return getEntryValue<List<Number>>(fieldTagType)
    }

    /**
     * Get a long list entry value
     *
     * @param fieldTagType
     * field tag type
     * @return long list value
     * @since 2.0.0
     */
    fun getLongListEntryValue(fieldTagType: FieldTagType): List<Long>? {
        return getEntryValue<List<Long>>(fieldTagType)
    }

    /**
     * Set an unsigned long list of values for the field tag type
     *
     * @param fieldTagType
     * field tag type
     * @param value
     * long list value
     * @since 2.0.0
     */
    fun setUnsignedLongListEntryValue(
        fieldTagType: FieldTagType,
        value: List<Long>
    ) {
        setEntryValue(fieldTagType, FieldType.LONG, value.size.toLong(), value)
    }

    /**
     * Get an entry value
     *
     * @param fieldTagType
     * field tag type
     * @return value
     */
    private fun <T> getEntryValue(fieldTagType: FieldTagType): T? {
        var value: T? = null
        val entry = fieldTagTypeMapping[fieldTagType]
        if (entry != null) {
            value = entry.values as T
        }
        return value
    }

    /**
     * Create and set the entry value
     *
     * @param fieldTagType
     * field tag type
     * @param fieldType
     * field type
     * @param typeCount
     * type count
     * @param values
     * entry values
     */
    private fun setEntryValue(
        fieldTagType: FieldTagType,
        fieldType: FieldType,
        typeCount: Long,
        values: Any
    ) {
        val entry = FileDirectoryEntry(
            fieldTagType,
            fieldType, typeCount, values
        )
        addEntry(entry)
    }

    /**
     * Sum the list integer values in the provided range
     *
     * @param values
     * integer values
     * @param start
     * inclusive start index
     * @param end
     * exclusive end index
     * @return sum
     */
    private fun sum(values: List<Int>?, start: Int, end: Int): Int {
        var sum = 0
        for (i in start until end) {
            sum += values!![i]
        }
        return sum
    }

    /**
     * Create a single integer list with the value
     *
     * @param value
     * int value
     * @return single value list
     */
    private fun createSingleIntegerList(value: Int): List<Int> {
        val valueList = ArrayList<Int>()
        valueList.add(value)
        return valueList
    }

    /**
     * Create a single long list with the value
     *
     * @param value
     * long value
     * @return single value list
     */
    private fun createSingleLongList(value: Long): List<Long> {
        val valueList = ArrayList<Long>()
        valueList.add(value)
        return valueList
    }

    /**
     * Size in bytes of the Image File Directory (all contiguous)
     *
     * @return size in bytes
     */
    fun size(): Long {
        return (TiffConstants.IFD_HEADER_BYTES +
                entries.size * TiffConstants.IFD_ENTRY_BYTES +
                TiffConstants.IFD_OFFSET_BYTES).toLong()
    }

    /**
     * Size in bytes of the image file directory including entry values (not
     * contiguous bytes)
     *
     * @return size in bytes
     */
    fun sizeWithValues(): Long {
        var size = (TiffConstants.IFD_HEADER_BYTES + TiffConstants.IFD_OFFSET_BYTES).toLong()
        for (entry in entries) {
            size += entry.sizeWithValues()
        }
        return size
    }
}