package ag.granular.tiff

data class TypedRasters(
    val width: Int,
    val height: Int,
    val samples: List<TypedSample>
)

sealed class TypedSample {
    data class ByteSample(val fieldType: FieldType, val data: ByteArray) : TypedSample()
    data class ShortSample(val fieldType: FieldType, val data: ShortArray) : TypedSample()
    data class IntSample(val fieldType: FieldType, val data: IntArray) : TypedSample()
    data class FloatSample(val fieldType: FieldType, val data: FloatArray) : TypedSample()
    data class DoubleSample(val fieldType: FieldType, val data: DoubleArray) : TypedSample()
}