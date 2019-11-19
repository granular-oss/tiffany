package ag.granular.tiff

import ag.granular.io.ByteBuffer
import ag.granular.io.ByteOrder
import ag.granular.tiff.util.TiffConstants
import ag.granular.tiff.util.TiffException
import kotlin.math.max

/**
 * Raster image values
 */
class Rasters(
    val width: Int,
    val height: Int,
    /**
     * Field type for each sample
     */
    val fieldTypes: Array<FieldType>,
    /**
     * Values separated by sample
     */
    private var sampleValues: Array<ByteBuffer>?,
    /**
     * Interleaved pixel sample values
     */
    private var interleaveValues: ByteBuffer? = null
) {
    // Calculated values

    /**
     * Calculated pixel size in bytes
     */
    private var pixelSize: Int? = null

    /**
     * @see .getBitsPerSample
     */
    private var bitsPerSample: MutableList<Int>? = null

    /**
     * @see .getSampleFormat
     */
    private var sampleFormat: MutableList<Int>? = null

    /**
     * Return the number of pixels
     *
     * @return number of pixels
     */
    val numPixels: Int
        get() = width * height

    /**
     * Get the number of samples per pixel
     *
     * @return samples per pixel
     */
    val samplesPerPixel: Int
        get() = fieldTypes.size

//    constructor(
//        width: Int, height: Int, fieldTypes: Array<FieldType>,
//        interleaveValues: ByteBuffer
//    ) : this(width, height, fieldTypes, null, interleaveValues) {
//    }

    init {
        validateValues()
    }

//    constructor(
//        width: Int, height: Int, samplesPerPixel: Int,
//        fieldType: FieldType
//    ) : this(width, height, createFieldTypeArray(samplesPerPixel, fieldType)) {
//    }

//    constructor(
//        width: Int, height: Int, samplesPerPixel: Int,
//        fieldType: FieldType, order: ByteOrder
//    ) : this(
//        width, height, createFieldTypeArray(samplesPerPixel, fieldType),
//        order
//    ) {
//    }

//    constructor(
//        width: Int, height: Int, bitsPerSamples: IntArray,
//        sampleFormats: IntArray
//    ) : this(width, height, createFieldTypeArray(bitsPerSamples, sampleFormats)) {
//    }

//    constructor(
//        width: Int, height: Int, bitsPerSamples: IntArray,
//        sampleFormats: IntArray, order: ByteOrder
//    ) : this(
//        width, height,
//        createFieldTypeArray(bitsPerSamples, sampleFormats), order
//    ) {
//    }

//    constructor(
//        width: Int, height: Int, samplesPerPixel: Int,
//        bitsPerSample: Int, sampleFormat: Int
//    ) : this(
//        width, height, samplesPerPixel, FieldType.getFieldType(
//            sampleFormat, bitsPerSample
//        )
//    ) {
//    }

//    constructor(
//        width: Int, height: Int, samplesPerPixel: Int,
//        bitsPerSample: Int, sampleFormat: Int, order: ByteOrder
//    ) : this(
//        width, height, samplesPerPixel, FieldType.getFieldType(
//            sampleFormat, bitsPerSample
//        ), order
//    ) {
//    }

//    constructor(
//        width: Int, height: Int, fieldTypes: Array<FieldType>,
//        order: ByteOrder = ByteOrder.nativeOrder()
//    ) : this(width, height, fieldTypes, arrayOfNulls<ByteBuffer>(fieldTypes.size)) {
//        for (i in sampleValues!!.indices) {
//            sampleValues[i] = ByteBuffer.allocateDirect(
//                width * height * fieldTypes[i].bytes
//            ).order(order)
//        }
//    }

    /**
     * Validate that either sample or interleave values exist
     */
    private fun validateValues() {
        if (sampleValues == null && interleaveValues == null) {
            throw TiffException(
                "Results must be sample and/or interleave based"
            )
        }
    }

    private fun createFieldTypeArray(
        samplesPerPixel: Int,
        fieldType: FieldType
    ): Array<FieldType> = Array(samplesPerPixel) {
        fieldType
    }

    /**
     * Create [FieldType] array for the bits per samples and sample
     * formats
     *
     * @param bitsPerSamples
     * bits per samples
     * @param sampleFormats
     * sample formats
     * @return field type array
     */
    private fun createFieldTypeArray(
        bitsPerSamples: IntArray,
        sampleFormats: IntArray
    ): Array<FieldType> {
        if (bitsPerSamples.size != sampleFormats.size) {
            throw TiffException(
                "Equal number of bits per samples and sample formats expected. " +
                        "Bits Per Samples: " + bitsPerSamples +
                        ", Sample Formats: " + sampleFormats
            )
        }
        return Array(bitsPerSamples.size) { i ->
            FieldType.getFieldType(
                sampleFormats[i],
                bitsPerSamples[i]
            )
        }
    }

    /**
     * True if the results are stored by samples
     *
     * @return true if results exist
     */
    fun hasSampleValues(): Boolean {
        return sampleValues != null
    }

    /**
     * True if the results are stored interleaved
     *
     * @return true if results exist
     */
    fun hasInterleaveValues(): Boolean {
        return interleaveValues != null
    }

    /**
     * Updates sample to given value in buffer.
     *
     * @param buffer
     * A buffer to be updated.
     * @param bufferIndex
     * Position in buffer where to update.
     * @param sampleIndex
     * Sample index in sampleFieldTypes. Needed for determining
     * sample size.
     * @param value
     * A Number value to be put in buffer. Has to be same size as
     * sampleFieldTypes[sampleIndex].
     */
    private fun updateSampleInByteBuffer(
        buffer: ByteBuffer?,
        bufferIndex: Int,
        sampleIndex: Int,
        value: Number
    ) {
        if (bufferIndex < 0 || bufferIndex >= buffer!!.capacity()) {
            throw IndexOutOfBoundsException(
                "index: " + bufferIndex +
                        ". Buffer capacity: " + buffer!!.capacity()
            )
        }

        buffer.position(bufferIndex)
        writeSample(buffer, fieldTypes[sampleIndex], value)
    }

    /**
     * Reads sample from given buffer.
     *
     * @param buffer
     * A buffer to read from
     * @param index
     * Position in buffer where to read from
     * @param sampleIndex
     * Index of sample type to read
     * @return Number read from buffer
     */
    private fun getSampleFromByteBuffer(
        buffer: ByteBuffer?,
        index: Int,
        sampleIndex: Int
    ): Number {

        if (index < 0 || index >= buffer!!.capacity()) {
            throw IndexOutOfBoundsException(
                "Requested index: " + index +
                        ", but size of buffer is: " + buffer!!.capacity()
            )
        }

        buffer.position(index)
        return readSample(buffer, fieldTypes[sampleIndex])
    }

    /**
     * Add a value to the sample results
     *
     * @param sampleIndex
     * sample index
     * @param coordinate
     * coordinate location
     * @param value
     * value
     */
    fun addToSample(sampleIndex: Int, coordinate: Int, value: Number) {
        updateSampleInByteBuffer(
            sampleValues!![sampleIndex],
            coordinate * fieldTypes[sampleIndex].bytes,
            sampleIndex,
            value
        )
    }

    /**
     * Add a value to the interleaved results
     *
     * @param sampleIndex
     * sample index
     * @param coordinate
     * coordinate location
     * @param value
     * value
     * @since 2.0.0
     */
    fun addToInterleave(sampleIndex: Int, coordinate: Int, value: Number) {
        var bufferPos = coordinate * sizePixel()
        for (i in 0 until sampleIndex) {
            bufferPos += fieldTypes[i].bytes
        }

        updateSampleInByteBuffer(
            interleaveValues, bufferPos, sampleIndex,
            value
        )
    }

    /**
     * Get the bits per sample
     *
     * @return bits per sample
     */
    fun getBitsPerSample(): List<Int> {
        var result: MutableList<Int>? = bitsPerSample
        if (result == null) {
            result = ArrayList(fieldTypes.size)
            for (fieldType in fieldTypes) {
                result.add(fieldType.bits)
            }
            bitsPerSample = result
        }
        return result
    }

    /**
     * Returns list of sample types constants
     *
     * Returns list of sample types constants (SAMPLE_FORMAT_UNSIGNED_INT,
     * SAMPLE_FORMAT_SIGNED_INT or SAMPLE_FORMAT_FLOAT) for each sample in
     * sample list @see getFieldTypes(). @see [TiffConstants]
     *
     * @return list of sample type constants
     * @since 2.0.0
     */
    fun getSampleFormat(): List<Int> {
        var result: MutableList<Int>? = sampleFormat
        if (result == null) {
            result = ArrayList(fieldTypes.size)
            for (fieldType in fieldTypes) {
                result.add(FieldType.getSampleFormat(fieldType))
            }
            sampleFormat = result
        }
        return result
    }

    /**
     * Get the results stored by samples
     *
     * @return sample values
     * @since 2.0.0
     */
    fun getSampleValues(): Array<ByteBuffer> {
        for (i in sampleValues!!.indices) {
            sampleValues!![i].rewind()
        }
        return sampleValues!!
    }

    /**
     * Set the results stored by samples
     *
     * @param sampleValues
     * sample values
     * @since 2.0.0
     */
    fun setSampleValues(sampleValues: Array<ByteBuffer>) {
        this.sampleValues = sampleValues
        this.sampleFormat = null
        this.bitsPerSample = null
        this.pixelSize = null
        validateValues()
    }

    /**
     * Get the results stored as interleaved pixel samples
     *
     * @return interleaved values
     * @since 2.0.0
     */
    fun getInterleaveValues(): ByteBuffer {
        interleaveValues!!.rewind()
        return interleaveValues!!
    }

    /**
     * Set the results stored as interleaved pixel samples
     *
     * @param interleaveValues
     * interleaved values
     * @since 2.0.0
     */
    fun setInterleaveValues(interleaveValues: ByteBuffer) {
        this.interleaveValues = interleaveValues
        validateValues()
    }

    /**
     * Get the pixel sample values
     *
     * @param x
     * x coordinate (>= 0 && < [.getWidth])
     * @param y
     * y coordinate (>= 0 && < [.getHeight])
     * @return pixel sample values
     */
    fun getPixel(x: Int, y: Int): Array<Number> {

        validateCoordinates(x, y)

        // Get the pixel values from each sample
        return if (sampleValues != null) {
            val sampleIndex = getSampleIndex(x, y)
            Array<Number>(samplesPerPixel) { i ->
                val bufferIndex = sampleIndex * fieldTypes[i].bytes
                getSampleFromByteBuffer(
                    sampleValues!![i],
                    bufferIndex, i
                )
            }
        } else {
            var interleaveIndex = getInterleaveIndex(x, y)
            Array<Number>(samplesPerPixel) { i ->
                val s = getSampleFromByteBuffer(
                    interleaveValues,
                    interleaveIndex, i
                )
                interleaveIndex += fieldTypes[i].bytes
                s
            }
        }
    }

    /**
     * Set the pixel sample values
     *
     * @param x
     * x coordinate (>= 0 && < [.getWidth])
     * @param y
     * y coordinate (>= 0 && < [.getHeight])
     * @param values
     * pixel values
     */
    fun setPixel(x: Int, y: Int, values: Array<Number>) {

        validateCoordinates(x, y)
        validateSample(values.size + 1)

        // Set the pixel values from each sample
        if (sampleValues != null) {
            for (i in 0 until samplesPerPixel) {
                val bufferIndex = getSampleIndex(x, y) * fieldTypes[i].bytes
                updateSampleInByteBuffer(
                    sampleValues!![i], bufferIndex, i,
                    values[i]
                )
            }
        } else {
            var interleaveIndex = getSampleIndex(x, y) * sizePixel()
            for (i in 0 until samplesPerPixel) {
                updateSampleInByteBuffer(
                    interleaveValues, interleaveIndex, i,
                    values[i]
                )
                interleaveIndex += fieldTypes[i].bytes
            }
        }
    }

    /**
     * Returns byte array of pixel row.
     *
     * @param y
     * Row index
     * @param newOrder
     * Desired byte order of result byte array
     * @return Byte array of pixel row
     * @since 2.0.0
     */
    fun getPixelRow(y: Int, newOrder: ByteOrder): ByteArray {
        val outBuffer = ByteBuffer.allocate(width * sizePixel())
        outBuffer.order(newOrder)

        if (sampleValues != null) {
            for (i in 0 until samplesPerPixel) {
                sampleValues!![i].position(
                    y * width
                            * fieldTypes[i].bytes
                )
            }
            for (i in 0 until width) {
                for (j in 0 until samplesPerPixel) {
                    writeSample(outBuffer, sampleValues!![j], fieldTypes[j])
                }
            }
        } else {
            interleaveValues!!.position(y * width * sizePixel())

            for (i in 0 until width) {
                for (j in 0 until samplesPerPixel) {
                    writeSample(outBuffer, interleaveValues!!, fieldTypes[j])
                }
            }
        }

        return outBuffer.array()
    }

    /**
     * Returns byte array of sample row.
     *
     * @param y
     * Row index
     * @param sample
     * Sample index
     * @param newOrder
     * Desired byte order of resulting byte array
     * @return Byte array of sample row
     * @since 2.0.0
     */
    fun getSampleRow(y: Int, sample: Int, newOrder: ByteOrder): ByteArray {
        val outBuffer = ByteBuffer.allocate(width * fieldTypes[sample].bytes)
        outBuffer.order(newOrder)

        if (sampleValues != null) {
            sampleValues!![sample].position(
                y * width
                        * fieldTypes[sample].bytes
            )
            for (x in 0 until width) {
                writeSample(outBuffer, sampleValues!![sample], fieldTypes[sample])
            }
        } else {
            var sampleOffset = 0
            for (i in 0 until sample) {
                sampleOffset += fieldTypes[sample].bytes
            }

            for (i in 0 until width) {
                interleaveValues!!.position((y * width + i) * sizePixel() + sampleOffset)
                writeSample(outBuffer, interleaveValues!!, fieldTypes[sample])
            }
        }

        return outBuffer.array()
    }

    /**
     * Get a pixel sample value
     *
     * @param sample
     * sample index (>= 0 && < [.getSamplesPerPixel])
     * @param x
     * x coordinate (>= 0 && < [.getWidth])
     * @param y
     * y coordinate (>= 0 && < [.getHeight])
     * @return pixel sample
     */
    fun getPixelSample(sample: Int, x: Int, y: Int): Number {

        validateCoordinates(x, y)
        validateSample(sample)

        // Pixel sample value
        var pixelSample: Number? = null

        // Get the pixel sample
        if (sampleValues != null) {
            val bufferPos = getSampleIndex(x, y) * fieldTypes[sample].bytes
            pixelSample = getSampleFromByteBuffer(
                sampleValues!![sample],
                bufferPos, sample
            )
        } else {
            var bufferPos = getInterleaveIndex(x, y)
            for (i in 0 until sample) {
                bufferPos += fieldTypes[sample].bytes
            }

            pixelSample = getSampleFromByteBuffer(
                interleaveValues, bufferPos,
                sample
            )
        }

        return pixelSample
    }

    /**
     * Set a pixel sample value
     *
     * @param sample
     * sample index (>= 0 && < [.getSamplesPerPixel])
     * @param x
     * x coordinate (>= 0 && < [.getWidth])
     * @param y
     * y coordinate (>= 0 && < [.getHeight])
     * @param value
     * pixel value
     */
    fun setPixelSample(sample: Int, x: Int, y: Int, value: Number) {

        validateCoordinates(x, y)
        validateSample(sample)

        // Set the pixel sample
        if (sampleValues != null) {
            val sampleIndex = getSampleIndex(x, y) * fieldTypes[sample].bytes
            updateSampleInByteBuffer(
                sampleValues!![sample], sampleIndex, sample,
                value
            )
        }
        if (interleaveValues != null) {
            var interleaveIndex = getSampleIndex(x, y) * sizePixel()
            for (i in 0 until sample) {
                interleaveIndex += fieldTypes[sample].bytes
            }
            updateSampleInByteBuffer(
                interleaveValues, interleaveIndex, sample,
                value
            )
        }
    }

    /**
     * Get the first pixel sample value, useful for single sample pixels
     * (grayscale)
     *
     * @param x
     * x coordinate (>= 0 && < [.getWidth])
     * @param y
     * y coordinate (>= 0 && < [.getHeight])
     * @return first pixel sample
     */
    fun getFirstPixelSample(x: Int, y: Int): Number? {
        return getPixelSample(0, x, y)
    }

    /**
     * Set the first pixel sample value, useful for single sample pixels
     * (grayscale)
     *
     * @param x
     * x coordinate (>= 0 && < [.getWidth])
     * @param y
     * y coordinate (>= 0 && < [.getHeight])
     * @param value
     * pixel value
     */
    fun setFirstPixelSample(x: Int, y: Int, value: Number) {
        setPixelSample(0, x, y, value)
    }

    /**
     * Get the sample index location
     *
     * @param x
     * x coordinate
     * @param y
     * y coordinate
     * @return sample index
     */
    fun getSampleIndex(x: Int, y: Int): Int {
        return y * width + x
    }

    /**
     * Get the interleave index location
     *
     * @param x
     * x coordinate
     * @param y
     * y coordinate
     * @return interleave index
     */
    fun getInterleaveIndex(x: Int, y: Int): Int {
        return y * width * sizePixel() + x * sizePixel()
    }

    /**
     * Size in bytes of the image
     *
     * @return bytes
     */
    fun size(): Int {
        return numPixels * sizePixel()
    }

    /**
     * Size in bytes of a pixel
     *
     * @return bytes
     */
    fun sizePixel(): Int {
        if (pixelSize != null) {
            return pixelSize!!
        }

        var size = 0
        for (i in 0 until samplesPerPixel) {
            size += fieldTypes[i].bytes
        }
        pixelSize = size
        return size
    }

    /**
     * Validate the coordinates range
     *
     * @param x
     * x coordinate
     * @param y
     * y coordinate
     */
    private fun validateCoordinates(x: Int, y: Int) {
        if (x < 0 || x >= width || y < 0 || y > height) {
            throw TiffException(
                "Pixel oustide of raster range. Width: " +
                        width + ", Height: " + height + ", x: " + x + ", y: " + y
            )
        }
    }

    /**
     * Validate the sample index
     *
     * @param sample
     * sample index
     */
    private fun validateSample(sample: Int) {
        if (sample < 0 || sample >= samplesPerPixel) {
            throw TiffException(
                "Pixel sample out of bounds. sample: " +
                        sample + ", samples per pixel: " + samplesPerPixel
            )
        }
    }

    /**
     * Calculate the rows per strip to write
     *
     * @param planarConfiguration
     * chunky or planar
     * @param maxBytesPerStrip
     * attempted max bytes per strip
     * @return rows per strip
     */

    fun calculateRowsPerStrip(
        planarConfiguration: Int,
        maxBytesPerStrip: Int = TiffConstants.DEFAULT_MAX_BYTES_PER_STRIP
    ): Int {

        var rowsPerStrip: Int? = null

        if (planarConfiguration == TiffConstants.PLANAR_CONFIGURATION_CHUNKY) {
            rowsPerStrip = rowsPerStrip(sizePixel(), maxBytesPerStrip)
        } else {

            for (sample in 0 until samplesPerPixel) {
                val rowsPerStripForSample = rowsPerStrip(
                    fieldTypes[sample].bytes, maxBytesPerStrip
                )
                if (rowsPerStrip == null || rowsPerStripForSample < rowsPerStrip) {
                    rowsPerStrip = rowsPerStripForSample
                }
            }
        }

        return rowsPerStrip!!
    }

    /**
     * Get the rows per strip based upon the bits per pixel and max bytes per
     * strip
     *
     * @param bytesPerPixel
     * bytes per pixel
     * @param maxBytesPerStrip
     * max bytes per strip
     * @return rows per strip
     */
    private fun rowsPerStrip(bytesPerPixel: Int, maxBytesPerStrip: Int): Int {

        val bytesPerRow = bytesPerPixel * width

        return max(1, maxBytesPerStrip / bytesPerRow)
    }

    /**
     * Reads sample from given buffer
     *
     * @param buffer
     * A buffer to read from. @note Make sure position is set.
     * @param fieldType
     * field type to be read
     * @return Sample from buffer
     */
    private fun readSample(buffer: ByteBuffer, fieldType: FieldType): Number {
        return when (fieldType) {
            FieldType.BYTE -> (buffer.get().toInt() and 0xff).toShort()
            FieldType.SHORT -> buffer.getShort().toInt() and 0xffff
            FieldType.LONG -> buffer.getInt() and 0xfffffff // ???
            FieldType.SBYTE -> buffer.get()
            FieldType.SSHORT -> buffer.getShort()
            FieldType.SLONG -> buffer.getInt()
            FieldType.FLOAT -> buffer.getFloat()
            FieldType.DOUBLE -> buffer.getDouble()
            else -> throw TiffException("Unsupported raster field type: $fieldType")
        }
    }

    /**
     * Writes sample into given buffer.
     *
     * @param buffer
     * A buffer to write to. @note Make sure buffer position is set.
     * @param fieldType
     * field type to be written.
     * @param value
     * Actual value to write.
     */
    private fun writeSample(
        buffer: ByteBuffer,
        fieldType: FieldType,
        value: Number
    ) {
        when (fieldType) {
            FieldType.BYTE, FieldType.SBYTE -> buffer.put(value.toByte())
            FieldType.SHORT, FieldType.SSHORT -> buffer.putShort(value.toShort())
            FieldType.LONG, FieldType.SLONG -> buffer.putInt(value.toInt())
            FieldType.FLOAT -> buffer.putFloat(value.toFloat())
            FieldType.DOUBLE -> buffer.putDouble(value.toDouble())
            else -> throw TiffException("Unsupported raster field type: $fieldType")
        }
    }

    /**
     * Writes sample from input buffer to given output buffer.
     *
     * @param outBuffer
     * A buffer to write to. @note Make sure buffer position is set.
     * @param inBuffer
     * A buffer to read from. @note Make sure buffer position is set.
     * @param fieldType
     * Field type to be read.
     */
    private fun writeSample(
        outBuffer: ByteBuffer,
        inBuffer: ByteBuffer,
        fieldType: FieldType
    ) {
        when (fieldType) {
            FieldType.BYTE, FieldType.SBYTE -> outBuffer.put(inBuffer.get())
            FieldType.SHORT, FieldType.SSHORT -> outBuffer.putShort(inBuffer.getShort())
            FieldType.LONG, FieldType.SLONG -> outBuffer.putInt(inBuffer.getInt())
            FieldType.FLOAT -> outBuffer.putFloat(inBuffer.getFloat())
            FieldType.DOUBLE -> outBuffer.putDouble(inBuffer.getDouble())
            else -> throw TiffException("Unsupported raster field type: $fieldType")
        }
    }
}