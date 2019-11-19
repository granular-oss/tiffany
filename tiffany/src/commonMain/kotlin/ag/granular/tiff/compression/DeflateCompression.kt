package ag.granular.tiff.compression

import ag.granular.io.ByteArrayOutputStream
import ag.granular.io.ByteOrder
import ag.granular.tiff.compression.zip.Deflater
import ag.granular.tiff.compression.zip.Inflater
import ag.granular.tiff.util.TiffException

/**
 * Deflate Compression
 */
class DeflateCompression : CompressionDecoder,
    CompressionEncoder {

    /**
     * {@inheritDoc}
     */
    override fun decode(bytes: ByteArray, byteOrder: ByteOrder): ByteArray {
        try {
            val inflater = Inflater()
            inflater.setInput(bytes)
            val outputStream = ByteArrayOutputStream(bytes.size)
            val buffer = ByteArray(1024)
            while (!inflater.finished()) {
                val count = inflater.inflate(buffer)
                outputStream.write(buffer, 0, count)
            }
            return outputStream.toByteArray()
        } catch (e: Exception) {
            throw TiffException("Failed to inflate", e)
        }
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
        try {
            val deflater = Deflater()
            deflater.setInput(bytes)
            val outputStream = ByteArrayOutputStream()
            deflater.finish()
            val buffer = ByteArray(1024)
            while (!deflater.finished()) {
                val count = deflater.deflate(buffer) // returns the generated code... index
                outputStream.write(buffer, 0, count)
            }
            return outputStream.toByteArray()
        } catch (e: Exception) {
            throw TiffException("Failed close encoded stream", e)
        }
    }
}
