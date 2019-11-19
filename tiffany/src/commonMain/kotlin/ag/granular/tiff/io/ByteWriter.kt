package ag.granular.tiff.io

import ag.granular.io.ByteArrayOutputStream
import ag.granular.io.ByteBuffer
import ag.granular.io.ByteOrder
import ag.granular.io.IOException

class ByteWriter(
    val byteOrder: ByteOrder = ByteOrder.LITTLE_ENDIAN
) {

    private val os = ByteArrayOutputStream()

    val bytes: ByteArray
        get() = os.toByteArray()

    fun close() = try {
        os.close()
    } catch (e: IOException) {
    }

    fun size(): Int = os.size()

    //    @Throws(IOException::class)
    @UseExperimental(ExperimentalStdlibApi::class)
    fun writeString(value: String): Int {
        val valueBytes = value.encodeToByteArray()
        os.write(valueBytes)
        return valueBytes.size
    }

    fun writeByte(value: Byte) {
        os.write(value.toInt())
    }

    fun writeUnsignedByte(value: Short) {
        os.write(value.toUByte().toInt())
    }

    //    @Throws(IOException::class)
    fun writeBytes(value: ByteArray) {
        os.write(value)
    }

    //    @Throws(IOException::class)
    fun writeShort(value: Short) {
//        val valueBytes = ByteArray(2)
        val byteBuffer = ByteBuffer.allocate(2).order(byteOrder)
            .putShort(value)
        byteBuffer.flip()
//        byteBuffer.get(valueBytes, 0, 2)
        os.write(byteBuffer.array())
    }

    //    @Throws(IOException::class)
    fun writeUnsignedShort(value: Int) {
//        val valueBytes = ByteArray(2)
        val byteBuffer = ByteBuffer.allocate(2).order(byteOrder)
            .putShort((value and 0xffff).toShort())
        byteBuffer.flip()
//        byteBuffer.get(valueBytes)
        os.write(byteBuffer.array())
    }

    //    @Throws(IOException::class)
    fun writeInt(value: Int) {
//        val valueBytes = ByteArray(4)
        val byteBuffer = ByteBuffer.allocate(4).order(byteOrder)
            .putInt(value)
        byteBuffer.flip()
//        byteBuffer.get(valueBytes)
        os.write(byteBuffer.array())
    }

    //    @Throws(IOException::class)
    fun writeUnsignedInt(value: Long) {
//        val valueBytes = ByteArray(4)
        val byteBuffer = ByteBuffer.allocate(4).order(byteOrder)
            .putInt((value and 0xffffffffL).toInt())
        byteBuffer.flip()
//        byteBuffer.get(valueBytes)
        os.write(byteBuffer.array())
    }

    //    @Throws(IOException::class)
    fun writeFloat(value: Float) {
//        val valueBytes = ByteArray(4)
        val byteBuffer = ByteBuffer.allocate(4).order(byteOrder)
            .putFloat(value)
        byteBuffer.flip()
//        byteBuffer.get(valueBytes)
        os.write(byteBuffer.array())
    }

    //    @Throws(IOException::class)
    fun writeDouble(value: Double) {
//        val valueBytes = ByteArray(8)
        val byteBuffer = ByteBuffer.allocate(8).order(byteOrder)
            .putDouble(value)
        byteBuffer.flip()
//        byteBuffer.get(valueBytes)
        os.write(byteBuffer.array())
    }
}