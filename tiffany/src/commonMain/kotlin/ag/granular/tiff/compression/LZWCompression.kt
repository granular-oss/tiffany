package ag.granular.tiff.compression

import ag.granular.io.ByteArrayOutputStream
import ag.granular.io.ByteOrder
import ag.granular.tiff.util.TiffException
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.pow

class LZWCompression : CompressionDecoder,
    CompressionEncoder {

    /**
     * Table entries
     */
    private val table = mutableMapOf<Int, ByteArray>()

    /**
     * Current max table code
     */
    private var maxCode: Int = 0

    /**
     * Current byte length
     */
    private var byteLength: Int = 0

    /**
     * Current byte compression position
     */
    private var position: Int = 0

    /**
     * {@inheritDoc}
     */
    override fun decode(bytes: ByteArray, byteOrder: ByteOrder): ByteArray {

        val decodedStream = ByteArrayOutputStream()

        // Initialize the table, starting position, and old code
        initializeTable()
        position = 0
        var oldCode = 0

        // Read codes until end of input
        var code = getNextCode(bytes)
        while (code != EOI_CODE) {

            // If a clear code
            if (code == CLEAR_CODE) {

                // Reset the table
                initializeTable()

                // Read past clear codes
                code = getNextCode(bytes)
                while (code == CLEAR_CODE) {
                    code = getNextCode(bytes)
                }
                if (code == EOI_CODE) {
                    break
                }
                if (code > CLEAR_CODE) {
                    throw TiffException("Corrupted code at scan line: $code")
                }

                // Write the code value
                val value = table[code]
                decodedStream.write(value!!)
                oldCode = code
            } else {

                // If already in the table
                val value = table[code]
                if (value != null) {

                    // Write the code value
                    decodedStream.write(value)

                    // Create new value and add to table
                    val newValue = table[oldCode]!! + table[code]!![0]
                    addToTable(newValue)
                    oldCode = code
                } else {

                    // Create and write new value from old value
                    val oldValue = table[oldCode]
                    val newValue = oldValue!! + oldValue[0]
                    decodedStream.write(newValue)

                    // Write value to the table
                    addToTable(code, newValue)
                    oldCode = code
                }
            }

            // Get the next code
            code = getNextCode(bytes)
        }

        return decodedStream.toByteArray()
    }

    /**
     * Initialize the table and byte length
     */
    private fun initializeTable() {
        table.clear()
        for (i in 0..257) {
            table[i] = ByteArray(1) {
                i.toByte()
            }
        }
        maxCode = 257
        byteLength = MIN_BITS
    }

    /**
     * Check the byte length and increase if needed
     */
    private fun checkByteLength() {
        if (maxCode >= 2.0.pow(byteLength.toDouble()) - 2) {
            byteLength++
        }
    }

    /**
     * Add the value to the table
     *
     * @param value
     * value
     */
    private fun addToTable(value: ByteArray) {
        addToTable(maxCode + 1, value)
    }

    /**
     * Add the code and value to the table
     *
     * @param code
     * code
     * @param value
     * value
     */
    private fun addToTable(code: Int, value: ByteArray) {
        table[code] = value
        maxCode = max(maxCode, code)
        checkByteLength()
    }

    /**
     * Get the next code
     *
     * @param reader
     * byte reader
     * @return code
     */
    private fun getNextCode(bytes: ByteArray): Int {
        val nextByte = getByte(bytes)
        position += byteLength
        return nextByte
    }

    /**
     * Get the next byte
     *
     * @param bytes
     * byte bytes
     * @return byte
     */
    private fun getByte(bytes: ByteArray): Int {

        val d = position % 8
        val a = floor(position / 8.0).toInt()
        val de = 8 - d
        val ef = position + byteLength - (a + 1) * 8
        var fg = 8 * (a + 2) - (position + byteLength)
        val dg = (a + 2) * 8 - position
        fg = max(0, fg)
        if (a >= bytes.size) {
            println("End of data reached without an end of input code")
            return EOI_CODE
        }
        var chunk1 =
            readUnsignedByte(bytes, a).toInt() and (2.0.pow((8 - d).toDouble()) - 1).toInt()
        chunk1 = chunk1 shl byteLength - de
        var chunks = chunk1
        if (a + 1 < bytes.size) {
            var chunk2 = readUnsignedByte(bytes, a + 1).toInt().ushr(fg)
            chunk2 = chunk2 shl max(0, byteLength - dg)
            chunks += chunk2
        }
        if (ef > 8 && a + 2 < bytes.size) {
            val hi = (a + 3) * 8 - (position + byteLength)
            val chunk3 = readUnsignedByte(bytes, a + 2).toInt().ushr(hi)
            chunks += chunk3
        }
        return chunks
    }

    private fun readUnsignedByte(bytes: ByteArray, offset: Int): UByte = bytes[offset].toUByte()

    /**
     * {@inheritDoc}
     */
    override fun rowEncoding(): Boolean {
        return false
    }

    /**
     * {@inheritDoc}
     */
    override fun encode(bytes: ByteArray, byteOrder: ByteOrder): ByteArray {
        throw TiffException("LZW encoder is not yet implemented")
    }

    companion object {

        /**
         * Clear code
         */
        private val CLEAR_CODE = 256

        /**
         * End of information code
         */
        private val EOI_CODE = 257

        /**
         * Min bits
         */
        private val MIN_BITS = 9
    }
}
