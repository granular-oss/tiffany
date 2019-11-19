package ag.granular.tiff

import ag.granular.tiff.util.TiffConstants

/**
 * TIFF Image containing the File Directories
 */
data class TIFFImage(
    val fileDirectories: List<FileDirectory>
) {

    /**
     * Size in bytes of the TIFF header and file directories with their entries
     *
     * @return size in bytes
     */
    fun sizeHeaderAndDirectories(): Long {
        var size = TiffConstants.HEADER_BYTES.toLong()
        for (directory in fileDirectories) {
            size += directory.size()
        }
        return size
    }

    /**
     * Size in bytes of the TIFF header and file directories with their entries
     * and entry values
     *
     * @return size in bytes
     */
    fun sizeHeaderAndDirectoriesWithValues(): Long {
        var size = TiffConstants.HEADER_BYTES.toLong()
        for (directory in fileDirectories) {
            size += directory.sizeWithValues()
        }
        return size
    }
}
