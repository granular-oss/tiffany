package ag.granular.tiff.compression.zip

typealias JInflater = java.util.zip.Inflater

actual class Inflater {
    private val ji = JInflater()
    actual fun setInput(bytes: ByteArray) = ji.setInput(bytes)
    actual fun finished(): Boolean = ji.finished()
    actual fun inflate(buffer: ByteArray): Int = ji.inflate(buffer)
}
