package ag.granular.tiff

actual fun getTestData(fileName: String): ByteArray =
    TiffTestConstants::class.java.getResource("/$fileName")
        .readBytes()