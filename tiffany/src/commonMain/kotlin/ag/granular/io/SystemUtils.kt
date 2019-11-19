package ag.granular.io

expect fun getTimeMillis(): Long

fun <T> measureTime(message: String, block: () -> T): T {
    val start = getTimeMillis()
    val r = block()
    println("$message took: ${getTimeMillis() - start} ms")
    return r
}