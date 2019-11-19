package ag.granular.tiff

import ag.granular.tiff.util.TiffConstants
import ag.granular.tiff.util.TiffException

/**
 * Field Types
 */
enum class FieldType(
    /**
     * Number of bytes per field value
     */
    val bytes: Int
) {

    /**
     * 8-bit unsigned integer
     */
    BYTE(1),

    /**
     * 8-bit byte that contains a 7-bit ASCII code; the last byte must be NUL
     * (binary zero)
     */
    ASCII(1),

    /**
     * 16-bit (2-byte) unsigned integer
     */
    SHORT(2),

    /**
     * 32-bit (4-byte) unsigned integer
     */
    LONG(4),

    /**
     * Two LONGs: the first represents the numerator of a fraction; the second,
     * the denominator
     */
    RATIONAL(8),

    /**
     * An 8-bit signed (twos-complement) integer
     */
    SBYTE(1),

    /**
     * An 8-bit byte that may contain anything, depending on the definition of
     * the field
     */
    UNDEFINED(1),

    /**
     * A 16-bit (2-byte) signed (twos-complement) integer
     */
    SSHORT(2),

    /**
     * A 32-bit (4-byte) signed (twos-complement) integer
     */
    SLONG(4),

    /**
     * Two SLONG’s: the first represents the numerator of a fraction, the second
     * the denominator
     */
    SRATIONAL(8),

    /**
     * Single precision (4-byte) IEEE format
     */
    FLOAT(4),

    /**
     * Double precision (8-byte) IEEE format
     */
    DOUBLE(8);

    /**
     * Get the field type value
     *
     * @return field type value
     */
    val value: Int
        get() = ordinal + 1

    /**
     * Get the number of bits per value
     *
     * @return number of bits
     * @since 2.0.0
     */
    val bits: Int
        get() = bytes * 8

    companion object {

        /**
         * Get the field type
         *
         * @param fieldType
         * field type number
         * @return field type
         */
        fun getFieldType(fieldType: Int): FieldType = values()[fieldType - 1]

        /**
         * Get the field type of the sample format and bits per sample
         *
         * @param sampleFormat
         * sample format
         * @param bitsPerSample
         * bits per sample
         * @return field type
         * @since 2.0.0
         */
        fun getFieldType(sampleFormat: Int, bitsPerSample: Int): FieldType {
            val throwError: () -> FieldType = {
                throw TiffException(
                    "Unsupported field type for sample format: " + sampleFormat +
                            ", bits per sample: " + bitsPerSample
                )
            }

            return when (sampleFormat) {
                TiffConstants.SAMPLE_FORMAT_UNSIGNED_INT -> when (bitsPerSample) {
                    8 -> BYTE
                    16 -> SHORT
                    32 -> LONG
                    else -> throwError()
                }
                TiffConstants.SAMPLE_FORMAT_SIGNED_INT -> when (bitsPerSample) {
                    8 -> SBYTE
                    16 -> SSHORT
                    32 -> SLONG
                    else -> throwError()
                }
                TiffConstants.SAMPLE_FORMAT_FLOAT -> when (bitsPerSample) {
                    32 -> FLOAT
                    64 -> DOUBLE
                    else -> throwError()
                }
                else -> throwError()
            }
        }

        /**
         * Get the sample format of the field type
         *
         * @param fieldType
         * field type
         * @return sample format
         * @since 2.0.0
         */
        fun getSampleFormat(fieldType: FieldType): Int = when (fieldType) {
            BYTE, SHORT, LONG -> TiffConstants.SAMPLE_FORMAT_UNSIGNED_INT
            SBYTE, SSHORT, SLONG -> TiffConstants.SAMPLE_FORMAT_SIGNED_INT
            FLOAT, DOUBLE -> TiffConstants.SAMPLE_FORMAT_FLOAT
            else -> throw TiffException(
                "Unsupported sample format for field type: $fieldType"
            )
        }
    }
}
