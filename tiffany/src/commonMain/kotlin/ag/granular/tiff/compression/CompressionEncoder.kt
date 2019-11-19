package ag.granular.tiff.compression

import ag.granular.io.ByteOrder

/**
 * Compression encoder interface. Encode either on a per row or block basis
 */
interface CompressionEncoder {

    /**
     * True to encode on a per row basis, false to encode on a per block / strip
     * basis
     *
     * @return true for row encoding
     */
    fun rowEncoding(): Boolean

    /**
     * Encode the bytes
     *
     * @param bytes
     * bytes to encode
     * @param byteOrder
     * byte order
     * @return encoded block of bytes
     */
    fun encode(bytes: ByteArray, byteOrder: ByteOrder): ByteArray
}
