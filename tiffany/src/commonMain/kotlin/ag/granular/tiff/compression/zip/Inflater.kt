package ag.granular.tiff.compression.zip

expect class Inflater() {
    fun setInput(bytes: ByteArray)
    fun finished(): Boolean
    fun inflate(buffer: ByteArray): Int
}