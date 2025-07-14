package io.timvanoijen.github.kotlin.sortedsequence

import io.timvanoijen.github.kotlin.sortedsequence.SortOrder.ASCENDING
import io.timvanoijen.github.kotlin.sortedsequence.SortOrder.DESCENDING
import io.timvanoijen.github.kotlin.sortedsequence.SortedSequence.Factory.assertSorted
import io.timvanoijen.github.kotlin.sortedsequence.SortedSequence.Factory.assertSortedBy
import io.timvanoijen.github.kotlin.sortedsequence.exceptions.SequenceNotSortedException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class SortedSequenceTest {

    @Test
    fun `zip operations work correctly`() {
        val seq1 = sequenceOf("1a", "2b", "2c").assertSortedBy { it.first() }
        val seq2 = sequenceOf("2x", "2y", "3z").assertSortedBy { it.first() }

        // full outer
        val result = seq1.fullOuterZipByKey(seq2)
        assertEquals(
            listOf(
                "1a" to null,
                "2b" to "2x",
                "2c" to "2y",
                null to "3z"
            ),
            result.toList()
        )

        val result2 = seq1.fullOuterZipByKey(seq2) { _, v1, v2 -> "${v1 ?: ""}${v2 ?: ""}" }
        assertEquals(
            listOf("1a", "2b2x", "2c2y", "3z"),
            result2.toList()
        )

        // inner join
        val result3 = seq1.innerZipByKey(seq2)
        assertEquals(
            listOf(
                "2b" to "2x",
                "2c" to "2y"
            ),
            result3.toList()
        )

        val result4 = seq1.innerZipByKey(seq2) { _, v1, v2 -> "$v1$v2" }
        assertEquals(
            listOf("2b2x", "2c2y"),
            result4.toList()
        )

        // left outer join
        val result5 = seq1.leftOuterZipByKey(seq2)
        assertEquals(
            listOf(
                "1a" to null,
                "2b" to "2x",
                "2c" to "2y"
            ),
            result5.toList()
        )

        val result6 = seq1.leftOuterZipByKey(seq2) { _, v1, v2 -> "$v1${v2 ?: ""}" }
        assertEquals(
            listOf("1a", "2b2x", "2c2y"),
            result6.toList()
        )

        // right outer join
        val result7 = seq1.rightOuterZipByKey(seq2)
        assertEquals(
            listOf(
                "2b" to "2x",
                "2c" to "2y",
                null to "3z"
            ),
            result7.toList()
        )

        val result8 = seq1.rightOuterZipByKey(seq2) { _, v1, v2 -> "${v1 ?: ""}$v2" }
        assertEquals(
            listOf("2b2x", "2c2y", "3z"),
            result8.toList()
        )

        // descending
        val seq1Desc = sequenceOf("2c", "2b", "1a").assertSortedBy(DESCENDING) { it.first() }
        val seq2Desc = sequenceOf("3z", "2y", "2x").assertSortedBy(DESCENDING) { it.first() }
        val result9 = seq1Desc.fullOuterZipByKey(seq2Desc)
        assertEquals(
            listOf(
                null to "3z",
                "2c" to "2y",
                "2b" to "2x",
                "1a" to null
            ),
            result9.toList()
        )
    }

    @Test
    fun `join operations work correctly`() {
        val seq1 = sequenceOf("1a", "2b", "2c").assertSortedBy { it.first() }
        val seq2 = sequenceOf("2x", "2y", "3z").assertSortedBy { it.first() }

        // full outer
        val result = seq1.fullOuterJoinByKey(seq2)
        assertEquals(
            listOf(
                "1a" to null,
                "2b" to "2x",
                "2b" to "2y",
                "2c" to "2x",
                "2c" to "2y",
                null to "3z"
            ),
            result.toList()
        )

        val result2 = seq1.fullOuterJoinByKey(seq2) { _, v1, v2 -> "${v1 ?: ""}${v2 ?: ""}" }
        assertEquals(
            listOf("1a", "2b2x", "2b2y", "2c2x", "2c2y", "3z"),
            result2.toList()
        )

        // inner join
        val result3 = seq1.innerJoinByKey(seq2)
        assertEquals(
            listOf(
                "2b" to "2x",
                "2b" to "2y",
                "2c" to "2x",
                "2c" to "2y"
            ),
            result3.toList()
        )

        val result4 = seq1.innerJoinByKey(seq2) { _, v1, v2 -> "$v1$v2" }
        assertEquals(
            listOf("2b2x", "2b2y", "2c2x", "2c2y"),
            result4.toList()
        )

        // left outer join
        val result5 = seq1.leftOuterJoinByKey(seq2)
        assertEquals(
            listOf(
                "1a" to null,
                "2b" to "2x",
                "2b" to "2y",
                "2c" to "2x",
                "2c" to "2y"
            ),
            result5.toList()
        )

        val result6 = seq1.leftOuterJoinByKey(seq2) { _, v1, v2 -> "$v1${v2 ?: ""}" }
        assertEquals(
            listOf("1a", "2b2x", "2b2y", "2c2x", "2c2y"),
            result6.toList()
        )

        // right outer join
        val result7 = seq1.rightOuterJoinByKey(seq2)
        assertEquals(
            listOf(
                "2b" to "2x",
                "2b" to "2y",
                "2c" to "2x",
                "2c" to "2y",
                null to "3z"
            ),
            result7.toList()
        )

        val result8 = seq1.rightOuterJoinByKey(seq2) { _, v1, v2 -> "${v1 ?: ""}$v2" }
        assertEquals(
            listOf("2b2x", "2b2y", "2c2x", "2c2y", "3z"),
            result8.toList()
        )

        // descending
        val seq1Desc = sequenceOf("2c", "2b", "1a").assertSortedBy(DESCENDING) { it.first() }
        val seq2Desc = sequenceOf("3z", "2y", "2x").assertSortedBy(DESCENDING) { it.first() }
        val result9 = seq1Desc.fullOuterJoinByKey(seq2Desc)
        assertEquals(
            listOf(
                null to "3z",
                "2c" to "2y",
                "2c" to "2x",
                "2b" to "2y",
                "2b" to "2x",
                "1a" to null
            ),
            result9.toList()
        )
    }

    @Test
    fun `creating ascending SortedSequence with key selector works correctly`() {
        val sequence = sequenceOf("az", "by", "cx").assertSortedBy { it.first() }
        assertEquals(listOf("az", "by", "cx"), sequence.toList())
    }

    @Test
    fun `group by key works correctly`() {
        val sequence = sequenceOf(1, 2, 2, 3).assertSorted()
        val grouped = sequence.groupByKey()
        assertEquals(listOf(listOf(1), listOf(2, 2), listOf(3)), grouped.toList())
    }

    @Test
    fun `group by key works correctly with descending sort order`() {
        val sequence = sequenceOf(3, 2, 2, 1).assertSorted(DESCENDING)
        val grouped = sequence.groupByKey()
        assertEquals(listOf(listOf(3), listOf(2, 2), listOf(1)), grouped.toList())
    }

    @Test
    fun `filterByKey works correctly`() {
        val sequence = sequenceOf(1, 2, 3).assertSorted()
        val filtered = sequence.filterByKey { it % 2 == 0 }
        assertEquals(listOf(2), filtered.toList())
    }

    @Test
    fun `filterByValue works correctly`() {
        val sequence = sequenceOf("a1", "b2", "c3").assertSortedBy { it.first() }
        val filtered = sequence.filterByValue { it.last() > '1' }
        assertEquals(listOf("b2", "c3"), filtered.toList())
    }

    @Test
    fun `filter works correctly`() {
        val sequence = sequenceOf("a1", "b2", "c3").assertSortedBy { it.first() }
        val filtered = sequence.filter { it.last() > '1' }
        assertEquals(listOf("b2", "c3"), filtered.toList())
    }

    @Test
    fun `distinctByKey works correctly`() {
        val sequence = sequenceOf(1, 1, 2).assertSorted()
        val distinct = sequence.distinctByKey()
        assertEquals(listOf(1, 2), distinct.toList())
    }

    @Test
    fun `distinctByKey works correctly with descending sort order`() {
        val sequence = sequenceOf(2, 1, 1).assertSorted(DESCENDING)
        val distinct = sequence.distinctByKey()
        assertEquals(listOf(2, 1), distinct.toList())
    }

    @Test
    fun `interleaveByKey works correctly`() {
        val seq1 = sequenceOf("a1", "b2", "b4").assertSortedBy { it.first() }
        val seq2 = sequenceOf("b3", "c4").assertSortedBy { it.first() }
        val interleaved = seq1.interleaveByKey(seq2)
        assertEquals(listOf("a1", "b2", "b3", "b4", "c4"), interleaved.toList())
    }

    @Test
    fun `interleaveByKey works correctly with descending sort order`() {
        val seq1 = sequenceOf("c1", "b2").assertSortedBy(DESCENDING) { it.first() }
        val seq2 = sequenceOf("b3", "a4").assertSortedBy(DESCENDING) { it.first() }
        val interleaved = seq1.interleaveByKey(seq2)
        assertEquals(listOf("c1", "b2", "b3", "a4"), interleaved.toList())
    }

    @Test
    fun `creating descending SortedSequence with key selector works correctly`() {
        val sequence = sequenceOf("az", "by", "cx").assertSortedBy(DESCENDING) { it.last() }
        assertEquals(listOf("az", "by", "cx"), sequence.toList())
    }

    @Test
    fun `exception is thrown when sequence is not properly sorted`() {
        val sequence = sequenceOf(1, 3, 2).assertSorted(ASCENDING)

        assertThrows<SequenceNotSortedException> {
            sequence.toList()
        }
    }

    @Test
    fun `converting to SortedKeyValueSequence in ascending order works correctly`() {
        val sequence = sequenceOf("az", "by", "cx").assertSortedBy { it.first() }
        val keyValueSequence = sequence.asSortedKeyValues()
        assertEquals(listOf('a' to "az", 'b' to "by", 'c' to "cx"), keyValueSequence.toList())
    }

    @Test
    fun `converting to SortedKeyValueSequence in descending order works correctly`() {
        val sequence = sequenceOf("az", "by", "cx").assertSortedBy(DESCENDING) { it.last() }
        val keyValueSequence = sequence.asSortedKeyValues()
        assertEquals(listOf('z' to "az", 'y' to "by", 'x' to "cx"), keyValueSequence.toList())
    }
}
