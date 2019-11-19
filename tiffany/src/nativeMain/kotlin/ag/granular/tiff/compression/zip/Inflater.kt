package ag.granular.tiff.compression.zip

import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.free
import kotlinx.cinterop.nativeHeap
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import platform.zlib.Z_OK
import platform.zlib.Z_STREAM_END
import platform.zlib.Z_SYNC_FLUSH
import platform.zlib.inflate
import platform.zlib.inflateEnd
import platform.zlib.inflateInit
import platform.zlib.z_stream

/**
 * See https://www.zlib.net/manual.html for details of using [z_stream]
 */
actual class Inflater {

    private var finished = false
    private lateinit var z: z_stream

    actual fun setInput(bytes: ByteArray) {
        this.z = bytes.usePinned {
            // needs to be released when finished
            nativeHeap.alloc<z_stream>().apply {
                next_in = it.addressOf(0).reinterpret()
                avail_in = bytes.size.toUInt()
                total_out = 0u
                // zalloc = Z_NULL
                // zfree = Z_NULL
            }
        }

        if (inflateInit(z.ptr) != Z_OK) throw Error("inflateInit(z_stream) != Z_OK")
    }

    actual fun finished(): Boolean = finished

    actual fun inflate(buffer: ByteArray): Int = buffer.usePinned { pinned ->
        z.next_out = pinned.addressOf(0).reinterpret()
        z.avail_out = buffer.size.toUInt()

        // returns count of inflated bytes
        when (val status = inflate(z.ptr, Z_SYNC_FLUSH)) {
            Z_STREAM_END -> {
                if (inflateEnd(z.ptr) != Z_OK) throw Error("inflateEnd (z.ptr) != Z_OK")
                finished = true
                buffer.size - z.avail_out.toInt()
            }
            Z_OK -> {
                // need to read more
                buffer.size - z.avail_out.toInt()
            }
            else -> throw Error("z_stream: status != Z_OK, status: $status")
        }
    }.also {
        // release memory pointer if finished
        if (finished) {
            nativeHeap.free(z.ptr)
        }
    }
}