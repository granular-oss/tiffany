package ag.granular.tiff.compression.zip

expect class Deflater() {
    fun setInput(bytes: ByteArray)
    fun finish()
    fun finished(): Boolean
    fun deflate(buffer: ByteArray): Int
}