package io.timvanoijen.github.kotlin.sortedsequence

import io.timvanoijen.github.kotlin.sortedsequence.SortOrder.ASCENDING
import io.timvanoijen.github.kotlin.sortedsequence.SortOrder.DESCENDING
import io.timvanoijen.github.kotlin.sortedsequence.SortedKeyValueSequence.Factory.assertSorted
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class SortedKeyValueSequenceTest {

    @Test
    fun `zip by key works correctly`() {
        val seq1 = sequenceOf(1 to "a", 2 to "b").assertSorted()
        val seq2 = sequenceOf(1 to "x", 3 to "z").assertSorted()
        val result = seq1.zipByKey(seq2)
        assertEquals(listOf(1 to ("a" to "x"), 2 to ("b" to null), 3 to (null to "z")), result.toList())
    }

    @Test
    fun `zip by key works correctly with descending sort order`() {
        val seq1 = sequenceOf(2 to "b", 1 to "a").assertSorted(DESCENDING)
        val seq2 = sequenceOf(3 to "z", 1 to "x").assertSorted(DESCENDING)
        val result = seq1.zipByKey(seq2, JoinType.INNER_JOIN)
        assertEquals(listOf(1 to ("a" to "x")), result.toList())
    }

    @Test
    fun `zip by key with merge function works correctly`() {
        val seq1 = sequenceOf(1 to "a", 2 to "b").assertSorted()
        val seq2 = sequenceOf(1 to "x", 3 to "z").assertSorted()
        val result = seq1.zipByKey(seq2, JoinType.RIGHT_OUTER_JOIN) { _, v1, v2 -> "${v1 ?: ""}$v2" }
        assertEquals(listOf(1 to "ax", 3 to "z"), result.toList())
    }

    @Test
    fun `zip by key with merge function works correctly with descending sort order`() {
        val seq1 = sequenceOf(2 to "b", 1 to "a").assertSorted(DESCENDING)
        val seq2 = sequenceOf(3 to "z", 1 to "x").assertSorted(DESCENDING)
        val result = seq1.zipByKey(seq2, JoinType.LEFT_OUTER_JOIN) { _, v1, v2 -> "$v1${v2 ?: ""}" }
        assertEquals(listOf(2 to "b", 1 to "ax"), result.toList())
    }

    @Test
    fun `zip by key fails with incompatible sort orders`() {
        val seq1 = sequenceOf(1 to "a", 2 to "b").assertSorted(ASCENDING)
        val seq2 = sequenceOf(2 to "x", 1 to "y").assertSorted(DESCENDING)

        assertThrows<SortedSequenceException.InvalidSortOrderException> {
            seq1.zipByKey(seq2).toList()
        }
    }

    @Test
    fun `full outer zip works correctly`() {
        val seq1 = sequenceOf(1 to "a", 2 to "b").assertSorted()
        val seq2 = sequenceOf(1 to "x", 3 to "z").assertSorted()
        val result = seq1.fullOuterZipByKey(seq2)
        assertEquals(listOf(1 to ("a" to "x"), 2 to ("b" to null), 3 to (null to "z")), result.toList())
    }

    @Test
    fun `full outer zip works correctly with descending sort order`() {
        val seq1 = sequenceOf(2 to "b", 1 to "a").assertSorted(DESCENDING)
        val seq2 = sequenceOf(3 to "z", 1 to "x").assertSorted(DESCENDING)
        val result = seq1.fullOuterZipByKey(seq2)
        assertEquals(listOf(3 to (null to "z"), 2 to ("b" to null), 1 to ("a" to "x")), result.toList())
    }

    @Test
    fun `full outer zip with merge function works correctly`() {
        val seq1 = sequenceOf(1 to "a", 2 to "b").assertSorted()
        val seq2 = sequenceOf(1 to "x", 3 to "z").assertSorted()
        val result = seq1.fullOuterZipByKey(seq2) { _, v1, v2 -> "${v1 ?: ""}${v2 ?: ""}" }
        assertEquals(listOf(1 to "ax", 2 to "b", 3 to "z"), result.toList())
    }

    @Test
    fun `full outer zip with merge function works correctly with descending sort order`() {
        val seq1 = sequenceOf(2 to "b", 1 to "a").assertSorted(DESCENDING)
        val seq2 = sequenceOf(3 to "z", 1 to "x").assertSorted(DESCENDING)
        val result = seq1.fullOuterZipByKey(seq2) { _, v1, v2 -> "${v1 ?: ""}${v2 ?: ""}" }
        assertEquals(listOf(3 to "z", 2 to "b", 1 to "ax"), result.toList())
    }

    @Test
    fun `inner zip works correctly`() {
        val seq1 = sequenceOf(1 to "a", 2 to "b").assertSorted()
        val seq2 = sequenceOf(1 to "x", 3 to "z").assertSorted()
        val result = seq1.innerZipByKey(seq2)
        assertEquals(listOf(1 to ("a" to "x")), result.toList())
    }

    @Test
    fun `inner zip works correctly with descending sort order`() {
        val seq1 = sequenceOf(2 to "b", 1 to "a").assertSorted(DESCENDING)
        val seq2 = sequenceOf(3 to "z", 1 to "x").assertSorted(DESCENDING)
        val result = seq1.innerZipByKey(seq2)
        assertEquals(listOf(1 to ("a" to "x")), result.toList())
    }

    @Test
    fun `inner zip with merge function works correctly`() {
        val seq1 = sequenceOf(1 to "a", 2 to "b").assertSorted()
        val seq2 = sequenceOf(1 to "x", 3 to "z").assertSorted()
        val result = seq1.innerZipByKey(seq2) { _, v1, v2 -> "$v1$v2" }
        assertEquals(listOf(1 to "ax"), result.toList())
    }

    @Test
    fun `inner zip with merge function works correctly with descending sort order`() {
        val seq1 = sequenceOf(2 to "b", 1 to "a").assertSorted(DESCENDING)
        val seq2 = sequenceOf(3 to "z", 1 to "x").assertSorted(DESCENDING)
        val result = seq1.innerZipByKey(seq2) { _, v1, v2 -> "$v1$v2" }
        assertEquals(listOf(1 to "ax"), result.toList())
    }

    @Test
    fun `left outer zip works correctly`() {
        val seq1 = sequenceOf(1 to "a", 2 to "b").assertSorted()
        val seq2 = sequenceOf(1 to "x", 3 to "z").assertSorted()
        val result = seq1.leftOuterZipByKey(seq2)
        assertEquals(listOf(1 to ("a" to "x"), 2 to ("b" to null)), result.toList())
    }

    @Test
    fun `left outer zip works correctly with descending sort order`() {
        val seq1 = sequenceOf(2 to "b", 1 to "a").assertSorted(DESCENDING)
        val seq2 = sequenceOf(3 to "z", 1 to "x").assertSorted(DESCENDING)
        val result = seq1.leftOuterZipByKey(seq2)
        assertEquals(listOf(2 to ("b" to null), 1 to ("a" to "x")), result.toList())
    }

    @Test
    fun `left outer zip with merge function works correctly`() {
        val seq1 = sequenceOf(1 to "a", 2 to "b").assertSorted()
        val seq2 = sequenceOf(1 to "x", 3 to "z").assertSorted()
        val result = seq1.leftOuterZipByKey(seq2) { _, v1, v2 -> "$v1${v2 ?: ""}" }
        assertEquals(listOf(1 to "ax", 2 to "b"), result.toList())
    }

    @Test
    fun `left outer zip with merge function works correctly with descending sort order`() {
        val seq1 = sequenceOf(2 to "b", 1 to "a").assertSorted(DESCENDING)
        val seq2 = sequenceOf(3 to "z", 1 to "x").assertSorted(DESCENDING)
        val result = seq1.leftOuterZipByKey(seq2) { _, v1, v2 -> "$v1${v2 ?: ""}" }
        assertEquals(listOf(2 to "b", 1 to "ax"), result.toList())
    }

    @Test
    fun `right outer zip works correctly`() {
        val seq1 = sequenceOf(1 to "a", 2 to "b").assertSorted()
        val seq2 = sequenceOf(1 to "x", 3 to "z").assertSorted()
        val result = seq1.rightOuterZipByKey(seq2)
        assertEquals(listOf(1 to ("a" to "x"), 3 to (null to "z")), result.toList())
    }

    @Test
    fun `right outer zip works correctly with descending sort order`() {
        val seq1 = sequenceOf(2 to "b", 1 to "a").assertSorted(DESCENDING)
        val seq2 = sequenceOf(3 to "z", 1 to "x").assertSorted(DESCENDING)
        val result = seq1.rightOuterZipByKey(seq2)
        assertEquals(listOf(3 to (null to "z"), 1 to ("a" to "x")), result.toList())
    }

    @Test
    fun `right outer zip with merge function works correctly`() {
        val seq1 = sequenceOf(1 to "a", 2 to "b").assertSorted()
        val seq2 = sequenceOf(1 to "x", 3 to "z").assertSorted()
        val result = seq1.rightOuterZipByKey(seq2) { _, v1, v2 -> "${v1 ?: ""}$v2" }
        assertEquals(listOf(1 to "ax", 3 to "z"), result.toList())
    }

    @Test
    fun `right outer zip with merge function works correctly with descending sort order`() {
        val seq1 = sequenceOf(2 to "b", 1 to "a").assertSorted(DESCENDING)
        val seq2 = sequenceOf(3 to "z", 1 to "x").assertSorted(DESCENDING)
        val result = seq1.rightOuterZipByKey(seq2) { _, v1, v2 -> "${v1 ?: ""}$v2" }
        assertEquals(listOf(3 to "z", 1 to "ax"), result.toList())
    }

    @Test
    fun `group by key works correctly`() {
        val sequence = sequenceOf(
            1 to "a",
            1 to "b",
            2 to "c"
        ).assertSorted()

        val grouped = sequence.groupByKey()

        assertEquals(listOf(1 to listOf("a", "b"), 2 to listOf("c")), grouped.toList())
    }

    @Test
    fun `group by key works correctly with descending sort order`() {
        val sequence = sequenceOf(
            2 to "c",
            1 to "b",
            1 to "a"
        ).assertSorted(DESCENDING)
        val grouped = sequence.groupByKey()
        assertEquals(listOf(2 to listOf("c"), 1 to listOf("b", "a")), grouped.toList())
    }

    @Test
    fun `distinctByKey works correctly`() {
        val sequence = sequenceOf(1 to "a", 1 to "b", 2 to "c").assertSorted()
        val distinct = sequence.distinctByKey()
        assertEquals(listOf(1 to "a", 2 to "c"), distinct.toList())
    }

    @Test
    fun `distinctByKey works correctly with descending sort order`() {
        val sequence = sequenceOf(2 to "c", 1 to "b", 1 to "a").assertSorted(DESCENDING)
        val distinct = sequence.distinctByKey()
        assertEquals(listOf(2 to "c", 1 to "b"), distinct.toList())
    }

    @Test
    fun `interleaveByKey works correctly`() {
        val seq1 = sequenceOf(1 to "a", 2 to "c").assertSorted()
        val seq2 = sequenceOf(1 to "x", 3 to "z").assertSorted()
        val interleaved = seq1.interleaveByKey(seq2)
        assertEquals(listOf(1 to "a", 1 to "x", 2 to "c", 3 to "z"), interleaved.toList())
    }

    @Test
    fun `interleaveByKey works correctly with descending sort order`() {
        val seq1 = sequenceOf(3 to "c", 2 to "b", 2 to "a").assertSorted(DESCENDING)
        val seq2 = sequenceOf(2 to "z", 1 to "x").assertSorted(DESCENDING)
        val interleaved = seq1.interleaveByKey(seq2)
        assertEquals(listOf(3 to "c", 2 to "b", 2 to "z", 2 to "a", 1 to "x"), interleaved.toList())
    }

    @Test
    fun `filterByKey works correctly`() {
        val sequence = sequenceOf(1 to "a", 2 to "b", 3 to "c").assertSorted()
        val filtered = sequence.filterByKey { it % 2 == 1 }
        assertEquals(listOf(1 to "a", 3 to "c"), filtered.toList())
    }

    @Test
    fun `filterByValue works correctly`() {
        val sequence = sequenceOf(1 to "a", 2 to "b", 3 to "c").assertSorted()
        val filtered = sequence.filterByValue { it in "ac" }
        assertEquals(listOf(1 to "a", 3 to "c"), filtered.toList())
    }

    @Test
    fun `filter works correctly`() {
        val sequence = sequenceOf(1 to "a", 2 to "b", 3 to "c").assertSorted()
        val filtered = sequence.filter { (key, value) -> key % 2 == 1 && value in "ab" }
        assertEquals(listOf(1 to "a"), filtered.toList())
    }

    @Test
    fun `creating descending SortedKeyValueSequence works correctly`() {
        val sequence = sequenceOf(
            3 to "a",
            2 to "b",
            1 to "c"
        ).assertSorted(DESCENDING)

        assertEquals(
            listOf(
                3 to "a",
                2 to "b",
                1 to "c"
            ),
            sequence.toList()
        )
    }

    @Test
    fun `exception is thrown when sequence is not properly sorted`() {
        val sequence = sequenceOf(
            1 to "a",
            3 to "b",
            2 to "c"
        ).assertSorted(ASCENDING)

        assertThrows<SortedSequenceException.SequenceNotSortedException> {
            sequence.toList()
        }
    }

    @Test
    fun `mapValues transforms values while maintaining sort order`() {
        val sequence = sequenceOf(
            1 to "a",
            2 to "b",
            3 to "c"
        ).assertSorted()

        val transformed = sequence.mapValues { it.uppercase() }

        assertEquals(ASCENDING, transformed.sortOrder)
        assertEquals(
            listOf(
                1 to "A",
                2 to "B",
                3 to "C"
            ),
            transformed.toList()
        )
    }
}
