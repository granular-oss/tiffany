package ag.granular.tiff.util

/**
 * TIFF Constants
 */
object TiffConstants {

    /**
     * Little Endian byte order string
     */
    const val BYTE_ORDER_LITTLE_ENDIAN = "II"

    /**
     * Big Endian byte order string
     */
    const val BYTE_ORDER_BIG_ENDIAN = "MM"

    /**
     * TIFF File Identifier
     */
    const val FILE_IDENTIFIER = 42

    /**
     * TIFF header bytes
     */
    const val HEADER_BYTES = 8

    /**
     * Image File Directory header / number of entries bytes
     */
    const val IFD_HEADER_BYTES = 2

    /**
     * Image File Directory offset to the next IFD bytes
     */
    const val IFD_OFFSET_BYTES = 4

    /**
     * Image File Directory entry bytes
     */
    const val IFD_ENTRY_BYTES = 12

    /**
     * Default max bytes per strip when writing strips
     */
    const val DEFAULT_MAX_BYTES_PER_STRIP = 8000

    // Compression constants
    const val COMPRESSION_NO = 1
    const val COMPRESSION_CCITT_HUFFMAN = 2
    const val COMPRESSION_T4 = 3
    const val COMPRESSION_T6 = 4
    const val COMPRESSION_LZW = 5
    const val COMPRESSION_JPEG_OLD = 6
    const val COMPRESSION_JPEG_NEW = 7
    const val COMPRESSION_DEFLATE = 8
    @Deprecated("")
    // PKZIP-style Deflate encoding (Obsolete).
    const val COMPRESSION_PKZIP_DEFLATE = 32946
    const val COMPRESSION_PACKBITS = 32773

    // Extra Samples constants
    const val EXTRA_SAMPLES_UNSPECIFIED = 0
    const val EXTRA_SAMPLES_ASSOCIATED_ALPHA = 1
    const val EXTRA_SAMPLES_UNASSOCIATED_ALPHA = 2

    // Fill Order constants
    const val FILL_ORDER_LOWER_COLUMN_HIGHER_ORDER = 1
    const val FILL_ORDER_LOWER_COLUMN_LOWER_ORDER = 2

    // Gray Response constants
    const val GRAY_RESPONSE_TENTHS = 1
    const val GRAY_RESPONSE_HUNDREDTHS = 2
    const val GRAY_RESPONSE_THOUSANDTHS = 3
    const val GRAY_RESPONSE_TEN_THOUSANDTHS = 4
    const val GRAY_RESPONSE_HUNDRED_THOUSANDTHS = 5

    // Orientation constants
    const val ORIENTATION_TOP_ROW_LEFT_COLUMN = 1
    const val ORIENTATION_TOP_ROW_RIGHT_COLUMN = 2
    const val ORIENTATION_BOTTOM_ROW_RIGHT_COLUMN = 3
    const val ORIENTATION_BOTTOM_ROW_LEFT_COLUMN = 4
    const val ORIENTATION_LEFT_ROW_TOP_COLUMN = 5
    const val ORIENTATION_RIGHT_ROW_TOP_COLUMN = 6
    const val ORIENTATION_RIGHT_ROW_BOTTOM_COLUMN = 7
    const val ORIENTATION_LEFT_ROW_BOTTOM_COLUMN = 8

    // Photometric Interpretation constants
    const val PHOTOMETRIC_INTERPRETATION_WHITE_IS_ZERO = 0
    const val PHOTOMETRIC_INTERPRETATION_BLACK_IS_ZERO = 1
    const val PHOTOMETRIC_INTERPRETATION_RGB = 2
    const val PHOTOMETRIC_INTERPRETATION_PALETTE = 3
    const val PHOTOMETRIC_INTERPRETATION_TRANSPARENCY = 4

    // Planar Configuration constants
    const val PLANAR_CONFIGURATION_CHUNKY = 1
    const val PLANAR_CONFIGURATION_PLANAR = 2

    // Resolution Unit constants
    const val RESOLUTION_UNIT_NO = 1
    const val RESOLUTION_UNIT_INCH = 2
    const val RESOLUTION_UNIT_CENTIMETER = 3

    // Sample Format constants
    const val SAMPLE_FORMAT_UNSIGNED_INT = 1
    const val SAMPLE_FORMAT_SIGNED_INT = 2
    const val SAMPLE_FORMAT_FLOAT = 3
    const val SAMPLE_FORMAT_UNDEFINED = 4

    // Subfile Type constants
    const val SUBFILE_TYPE_FULL = 1
    const val SUBFILE_TYPE_REDUCED = 2
    const val SAMPLE_FORMAT_SINGLE_PAGE_MULTI_PAGE = 3

    // Threshholding constants
    const val THRESHHOLDING_NO = 1
    const val THRESHHOLDING_ORDERED = 2
    const val THRESHHOLDING_RANDOM = 3
}
