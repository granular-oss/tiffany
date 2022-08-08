package ag.granular.tiff

import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSBundle
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.getBytes

actual fun getTestData(fileName: String): ByteArray {
    val path = NSBundle.mainBundle.resourcePath!!
    val contentsAtPath = NSFileManager.defaultManager.contentsAtPath("$path/$fileName")
    return contentsAtPath?.copyToByteArray() ?: throw Error()
}

fun NSData.copyToByteArray(): ByteArray = ByteArray(length.toInt()).apply {
    usePinned {
        getBytes(it.addressOf(0), length)
    }
}