package ag.granular.tiff.compression.zip

typealias JDeflater = java.util.zip.Deflater

actual class Deflater {
    private val jd = JDeflater()
    actual fun setInput(bytes: ByteArray) = jd.setInput(bytes)
    actual fun finish() = jd.finish()
    actual fun finished(): Boolean = jd.finished()
    actual fun deflate(buffer: ByteArray): Int = jd.deflate(buffer)
}