package com.simplemobiletools.notes.pro.helpers

import java.text.Collator

/**
 * Collator-based string comparator
 *
 * Adapted from AlphanumericComparator to support numerical collation.
 */
class CollatorBasedComparator: Comparator<String> {
    override fun compare(string1: String, string2: String): Int {
        val collator = getCollator()

        var thisMarker = 0
        var thatMarker = 0

        while (thisMarker < string1.length && thatMarker < string2.length) {
            val thisChunk = getChunk(string1, string1.length, thisMarker)
            thisMarker += thisChunk.length

            val thatChunk = getChunk(string2, string2.length, thatMarker)
            thatMarker += thatChunk.length

            val result = if (isDigit(thisChunk[0]) && isDigit(thatChunk[0])) {
                collateNumerically(thisChunk, thatChunk)
            } else {
                collator.compare(thisChunk, thatChunk)
            }

            if (result != 0) {
                return coerceResult(result)
            }
        }

        return coerceResult(string1.length - string2.length)
    }

    private fun collateNumerically(string1: String, string2: String): Int {
        var result: Int
        result = string1.length - string2.length
        if (result == 0) {
            // equal length, the first different number counts
            for (i in string1.indices) {
                result = string1[i] - string2[i]
                if (result != 0) {
                    break
                }
            }
        }
        return result
    }

    private fun getChunk(string: String, length: Int, marker: Int): String {
        var current = marker
        var c = string[current]
        val chunk = StringBuilder(c.toString())
        current++
        val chunkOfDigits = isDigit(c)
        while (current < length) {
            c = string[current]
            if (isDigit(c) != chunkOfDigits) {
                break
            }
            chunk.append(c)
            current++
        }
        return chunk.toString()
    }

    private fun isDigit(ch: Char) = ch in '0'..'9'
    private fun coerceResult(compareToResult: Int) = compareToResult.coerceIn(-1, 1)

    private fun getCollator(): Collator {
        val collator = Collator.getInstance()
        collator.strength = Collator.PRIMARY
        collator.decomposition = Collator.CANONICAL_DECOMPOSITION
        return collator
    }
}
