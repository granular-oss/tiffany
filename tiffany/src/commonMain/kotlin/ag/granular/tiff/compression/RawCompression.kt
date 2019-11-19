package ag.granular.tiff.compression

import ag.granular.io.ByteOrder

/**
 * Raw / no compression
 */
class RawCompression : CompressionDecoder,
    CompressionEncoder {

    /**
     * {@inheritDoc}
     */
    override fun decode(bytes: ByteArray, byteOrder: ByteOrder): ByteArray {
        return bytes
    }

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
        return bytes
    }
}
