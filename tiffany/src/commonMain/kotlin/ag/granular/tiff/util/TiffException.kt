package ag.granular.tiff.util

/**
 * TIFF exception
 */
class TiffException : RuntimeException {

    constructor(message: String) : super(message)
    constructor(message: String, throwable: Throwable) : super(message, throwable)
    constructor(throwable: Throwable) : super(throwable)

    companion object {

        /**
         * Serial version id
         */
        private const val serialVersionUID = 1L
    }
}
