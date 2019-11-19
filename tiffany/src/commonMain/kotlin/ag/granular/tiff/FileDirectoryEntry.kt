package ag.granular.tiff

import ag.granular.tiff.util.TiffConstants

/**
 * TIFF File Directory Entry
 */
data class FileDirectoryEntry(
    val fieldTag: FieldTagType,
    val fieldType: FieldType,
    val typeCount: Long,
    val values: Any
) : Comparable<FileDirectoryEntry> {

    /**
     * Size in bytes of the image file directory entry and its values (not
     * contiguous bytes)
     *
     * @return size in bytes
     */
    fun sizeWithValues(): Long = TiffConstants.IFD_ENTRY_BYTES + sizeOfValues()

    /**
     * Size of the values not included in the directory entry bytes
     *
     * @return size in bytes
     */
    fun sizeOfValues(): Long {
        var size: Long = 0
        val valueBytes = fieldType.bytes * typeCount
        if (valueBytes > 4) {
            size = valueBytes
        }
        return size
    }

    override fun compareTo(other: FileDirectoryEntry): Int = fieldTag.id - other.fieldTag.id

    override fun hashCode(): Int = fieldTag.id

    override fun equals(obj: Any?): Boolean {
        if (this === obj)
            return true
        if (obj == null)
            return false
        if (obj !is FileDirectoryEntry)
            return false
        return fieldTag === obj.fieldTag
    }
}
