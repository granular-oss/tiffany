/*
 * Copyright 2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ag.granular.io

@Suppress("NO_ACTUAL_FOR_EXPECT") // see KT-19848
expect abstract class Writer protected constructor() {
    open fun write(ch: Int)
    open fun write(str: String)
    abstract fun write(src: CharArray, off: Int, len: Int)
    abstract fun flush()
    abstract fun close()
}

expect open class PrintWriter(w: Writer) : Writer {
    open fun print(s: String)
    open fun print(ch: Char)
    open fun print(value: Float)
    open fun print(value: Double)
    open fun print(value: Boolean)
    open fun print(value: Int)
    open fun print(value: Long)
    open fun print(value: Any?)

    open fun println()
    open fun println(s: String)
    open fun println(ch: Char)
    open fun println(value: Float)
    open fun println(value: Double)
    open fun println(value: Boolean)
    open fun println(value: Int)
    open fun println(value: Long)
    open fun println(value: Any?)

    override fun write(src: CharArray, off: Int, len: Int)
    override fun flush()
    override fun close()
}

expect class StringWriter() : Writer {
    override fun toString(): String
    override fun write(src: CharArray, off: Int, len: Int)
    override fun flush()
    override fun close()
}

@Suppress("NO_ACTUAL_FOR_EXPECT") // see KT-19848
expect abstract class Reader protected constructor() {
    open fun read(): Int
    abstract fun read(dst: CharArray, off: Int, len: Int): Int
    abstract fun close()
}

expect class StringReader(str: String) : Reader {
    override fun read(): Int
    override fun read(dst: CharArray, off: Int, len: Int): Int
    override fun close()
}