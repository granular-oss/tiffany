package ag.granular.io
// TODO restore or delete this test once kotlinx-io library supports kotlin 1.3.50 or whichever is defined in dependencies
// import kotlinx.io.core.BytePacketBuilder
// import kotlinx.io.core.ByteReadPacket
// import kotlinx.io.core.readBytes
// import kotlin.random.Random
// import kotlin.test.Test
// import kotlin.test.assertEquals
//
// class ByteBufferTest {
//     @Test
//     fun longsAndDoubles() {
//         ByteBuffer.allocate(8).putDouble(199.0).print() shouldBe "64,104,-32,0,0,0,0,0"
//         ByteBuffer.allocate(8).putLong(Int.MAX_VALUE.toLong() + 2).print() shouldBe "0,0,0,0,-128,0,0,1"
//         ByteBuffer.from("64,104,-32,0,0,0,0,0").getDouble() shouldBe 199.0
//         ByteBuffer.from("0,0,0,0,-128,0,0,1").getLong() shouldBe Int.MAX_VALUE.toLong() + 2
//     }
//
//     @Test
//     fun byteOrder() {
//         val bb = ByteBuffer.allocate(4)
//
//         // reading test
//         bb.order(ByteOrder.BIG_ENDIAN)
//         bb.put(0)
//         bb.put(0)
//         bb.put(5)
//         bb.put(57)
//         bb.flip()
//         assertEquals(1337, bb.getInt())
//         bb.flip()
//         bb.order(ByteOrder.LITTLE_ENDIAN)
//         assertEquals(956628992, bb.getInt())
//         bb.flip()
//         bb.order(ByteOrder.BIG_ENDIAN)
//         assertEquals(1337, bb.getInt())
//
//         // writing test
//         bb.clear()
//         bb.order(ByteOrder.BIG_ENDIAN)
//         bb.putInt(1337)
//         bb.flip()
//         assertEquals(0, bb.get())
//         assertEquals(0, bb.get())
//         assertEquals(5, bb.get())
//         assertEquals(57, bb.get())
//
//         bb.clear()
//         bb.order(ByteOrder.LITTLE_ENDIAN)
//         bb.putInt(1337)
//         bb.flip()
//         assertEquals(57, bb.get())
//         assertEquals(5, bb.get())
//         assertEquals(0, bb.get())
//         assertEquals(0, bb.get())
//     }
//
//     val size = 10_000_000
//
//     @Test
//     fun `performance test short`() = measureTime("Buffer perf short test") {
//         val bb = ByteBuffer.allocate(2 * size)
//         measureTime("writing") {
//             for (i in 0 until size) {
//                 val s = Short.MAX_VALUE * Random.nextFloat()
//                 bb.putShort(s.toShort())
//             }
//         }
//
//         val array = bb.array()
//
//         val readBuffer = ByteBuffer.wrap(array, 0, array.size)
//         measureTime("reading") {
//             for (i in 0 until size) {
//                 readBuffer.getShort()
//             }
//         }
//     }
//
//     @Test
//     fun `performance test buffer Float`() = measureTime("perf test buffer Float") {
//         val bb = ByteBuffer.allocate(4 * size)
//         measureTime("writing") {
//             for (i in 0 until size) {
//                 bb.putFloat(Random.nextFloat())
//             }
//         }
//
//         val array = bb.array()
//
//         val readBuffer = ByteBuffer.wrap(array, 0, array.size)
//         measureTime("reading") {
//             for (i in 0 until size) {
//                 val v = readBuffer.getFloat()
//             }
//         }
//     }
//
//     @Test
//     fun `performance test short byte packet`() = measureTime("Byte packet short") {
//         val bb = BytePacketBuilder()
//         measureTime("writing") {
//             for (i in 0 until size) {
//                 val s = Short.MAX_VALUE * Random.nextFloat()
//                 bb.writeShort(s.toShort())
//             }
//         }
//
//         val array = bb.build().readBytes()
//
//         val readBuffer = ByteReadPacket(array)
//         measureTime("reading") {
//             for (i in 0 until size) {
//                 val v = readBuffer.readShort()
//             }
//         }
//     }
//
//     @Test
//     fun `performance test byte packet Int`() = measureTime("Byte packet Int") {
//         val bb = BytePacketBuilder()
//         measureTime("writing") {
//             for (i in 0 until size) {
//                 bb.writeInt(Random.nextInt())
//             }
//         }
//
//         val array = bb.build().readBytes()
//
//         val readBuffer = ByteReadPacket(array)
//         measureTime("reading") {
//             for (i in 0 until size) {
//                 readBuffer.readInt()
//             }
//         }
//     }
//
//     @Test
//     fun `performance test byte packet Float`() = measureTime("Byte packet Float") {
//         val bb = BytePacketBuilder()
//         measureTime("writing") {
//             for (i in 0 until size) {
//                 bb.writeFloat(Random.nextFloat())
//             }
//         }
//
//         val array = bb.build().readBytes()
//
//         val readBuffer = ByteReadPacket(array)
//         measureTime("reading") {
//             for (i in 0 until size) {
//                 val v = readBuffer.readFloat()
//             }
//         }
//     }
//
//     @Test
//     fun `just iteration`() = measureTime("just iteration") {
//         //        val bb = ByteBuffer.allocate(2 * size)
//         for (i in 0 until size) {
// //            val s = Short.MAX_VALUE * Random.nextFloat()
// //            bb.putShort(s.toShort())
//         }
//
// //        val array = bb.array()
//
// //        val readBuffer = ByteBuffer.wrap(array, 0, array.size)
//         for (i in 0 until size) {
// //            readBuffer.getShort()
//         }
//     }
//
//     private fun ByteBuffer.print() = this.array().joinToString(",")
//
//     private fun ByteBuffer.Companion.from(source: String): ByteBuffer {
//         val array = source.split(",").map { it.toByte() }.toByteArray()
//         return ByteBuffer.allocate(array.size).put(array).flip()
//     }
// }
//
// infix fun <T> T.shouldBe(expected: T) = assertEquals(expected, this)
