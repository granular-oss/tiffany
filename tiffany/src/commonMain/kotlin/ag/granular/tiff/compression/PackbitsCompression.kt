package ag.granular.tiff.compression

import ag.granular.tiff.io.ByteReader
import ag.granular.io.ByteArrayOutputStream
import ag.granular.io.ByteOrder
import ag.granular.tiff.util.TiffException

/**
 * Packbits Compression
 */
class PackbitsCompression : CompressionDecoder,
    CompressionEncoder {

    /**
     * {@inheritDoc}
     */
    override fun decode(bytes: ByteArray, byteOrder: ByteOrder): ByteArray {

        val reader = ByteReader(bytes, byteOrder)

        val decodedStream = ByteArrayOutputStream()

        while (reader.hasByte()) {
            var header = reader.readByte().toInt()
            if (header != -128) {
                if (header < 0) {
                    val next = reader.readUnsignedByte().toInt()
                    header = -header
                    for (i in 0..header) {
                        decodedStream.write(next)
                    }
                } else {
                    for (i in 0..header) {
                        decodedStream.write(reader.readUnsignedByte().toInt())
                    }
                }
            }
        }

        return decodedStream.toByteArray()
    }

    /**
     * {@inheritDoc}
     */
    override fun rowEncoding(): Boolean {
        return true
    }

    /**
     * {@inheritDoc}
     */
    override fun encode(bytes: ByteArray, byteOrder: ByteOrder): ByteArray {
        throw TiffException("Packbits encoder is not yet implemented")
    }
}
