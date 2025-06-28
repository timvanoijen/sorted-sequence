package nl.timocode.kotlinutils.sortedsequence

import nl.timocode.kotlinutils.sortedsequence.SortOrder.ASCENDING
import nl.timocode.kotlinutils.sortedsequence.SortOrder.DESCENDING
import nl.timocode.kotlinutils.sortedsequence.SortedSequence.Factory.assertSorted
import nl.timocode.kotlinutils.sortedsequence.SortedSequence.Factory.assertSortedBy
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class SortedSequenceTest {

    @Test
    fun `merge by key works correctly`() {
        val seq1 = sequenceOf("a1", "b2").assertSortedBy { it.first() }
        val seq2 = sequenceOf("b3", "c4").assertSortedBy { it.first() }
        val merged = seq1.mergeByKey(seq2)
        assertEquals(listOf("a1" to null, "b2" to "b3", null to "c4"), merged.toList())
    }

    @Test
    fun `merge by key works correctly with descending sort order`() {
        val seq1 = sequenceOf("c1", "b2").assertSortedBy(DESCENDING) { it.first() }
        val seq2 = sequenceOf("b3", "a4").assertSortedBy(DESCENDING) { it.first() }
        val merged = seq1.mergeByKey(seq2)
        assertEquals(listOf("c1" to null, "b2" to "b3", null to "a4"), merged.toList())
    }

    @Test
    fun `merge by key with merge function works correctly`() {
        val seq1 = sequenceOf("a1", "b2").assertSortedBy { it.first() }
        val seq2 = sequenceOf("b3", "c4").assertSortedBy { it.first() }
        val merged = seq1.mergeByKey(seq2, JoinType.INNER_JOIN) { _, v1, v2 -> "$v1$v2" }
        assertEquals(listOf("b2b3"), merged.toList())
    }

    @Test
    fun `merge by key with merge function works correctly with descending sort order`() {
        val seq1 = sequenceOf("c1", "b2").assertSortedBy(DESCENDING) { it.first() }
        val seq2 = sequenceOf("b3", "a4").assertSortedBy(DESCENDING) { it.first() }
        val merged = seq1.mergeByKey(seq2, JoinType.LEFT_OUTER_JOIN) { _, v1, v2 -> "$v1${v2 ?: ""}" }
        assertEquals(listOf("c1", "b2b3"), merged.toList())
    }

    @Test
    fun `full outer join works correctly`() {
        val seq1 = sequenceOf("a1", "b2").assertSortedBy { it.first() }
        val seq2 = sequenceOf("b3", "c4").assertSortedBy { it.first() }
        val joined = seq1.fullOuterJoinByKey(seq2)
        assertEquals(listOf("a1" to null, "b2" to "b3", null to "c4"), joined.toList())
    }

    @Test
    fun `full outer join works correctly with descending sort order`() {
        val seq1 = sequenceOf("c1", "b2").assertSortedBy(DESCENDING) { it.first() }
        val seq2 = sequenceOf("b3", "a4").assertSortedBy(DESCENDING) { it.first() }
        val joined = seq1.fullOuterJoinByKey(seq2)
        assertEquals(listOf("c1" to null, "b2" to "b3", null to "a4"), joined.toList())
    }

    @Test
    fun `full outer join with merge function works correctly`() {
        val seq1 = sequenceOf("a1", "b2").assertSortedBy { it.first() }
        val seq2 = sequenceOf("b3", "c4").assertSortedBy { it.first() }
        val joined = seq1.fullOuterJoinByKey(seq2) { _, v1, v2 -> "${v1 ?: ""}${v2 ?: ""}" }
        assertEquals(listOf("a1", "b2b3", "c4"), joined.toList())
    }

    @Test
    fun `full outer join with merge function works correctly with descending sort order`() {
        val seq1 = sequenceOf("c1", "b2").assertSortedBy(DESCENDING) { it.first() }
        val seq2 = sequenceOf("b3", "a4").assertSortedBy(DESCENDING) { it.first() }
        val joined = seq1.fullOuterJoinByKey(seq2) { _, v1, v2 -> "${v1 ?: ""}${v2 ?: ""}" }
        assertEquals(listOf("c1", "b2b3", "a4"), joined.toList())
    }

    @Test
    fun `inner join works correctly`() {
        val seq1 = sequenceOf("a1", "b2").assertSortedBy { it.first() }
        val seq2 = sequenceOf("b3", "c4").assertSortedBy { it.first() }
        val joined = seq1.innerJoinByKey(seq2)
        assertEquals(listOf("b2" to "b3"), joined.toList())
    }

    @Test
    fun `inner join works correctly with descending sort order`() {
        val seq1 = sequenceOf("c1", "b2").assertSortedBy(DESCENDING) { it.first() }
        val seq2 = sequenceOf("b3", "a4").assertSortedBy(DESCENDING) { it.first() }
        val joined = seq1.innerJoinByKey(seq2)
        assertEquals(listOf("b2" to "b3"), joined.toList())
    }

    @Test
    fun `inner join with merge function works correctly`() {
        val seq1 = sequenceOf("a1", "b2").assertSortedBy { it.first() }
        val seq2 = sequenceOf("b3", "c4").assertSortedBy { it.first() }
        val joined = seq1.innerJoinByKey(seq2) { _, v1, v2 -> "${v1}$v2" }
        assertEquals(listOf("b2b3"), joined.toList())
    }

    @Test
    fun `inner join with merge function works correctly with descending sort order`() {
        val seq1 = sequenceOf("c1", "b2").assertSortedBy(DESCENDING) { it.first() }
        val seq2 = sequenceOf("b3", "a4").assertSortedBy(DESCENDING) { it.first() }
        val joined = seq1.innerJoinByKey(seq2) { _, v1, v2 -> "${v1}$v2" }
        assertEquals(listOf("b2b3"), joined.toList())
    }

    @Test
    fun `left outer join works correctly`() {
        val seq1 = sequenceOf("a1", "b2").assertSortedBy { it.first() }
        val seq2 = sequenceOf("b3", "c4").assertSortedBy { it.first() }
        val joined = seq1.leftOuterJoinByKey(seq2)
        assertEquals(listOf("a1" to null, "b2" to "b3"), joined.toList())
    }

    @Test
    fun `left outer join works correctly with descending sort order`() {
        val seq1 = sequenceOf("c1", "b2").assertSortedBy(DESCENDING) { it.first() }
        val seq2 = sequenceOf("b3", "a4").assertSortedBy(DESCENDING) { it.first() }
        val joined = seq1.leftOuterJoinByKey(seq2)
        assertEquals(listOf("c1" to null, "b2" to "b3"), joined.toList())
    }

    @Test
    fun `left outer join with merge function works correctly`() {
        val seq1 = sequenceOf("a1", "b2").assertSortedBy { it.first() }
        val seq2 = sequenceOf("b3", "c4").assertSortedBy { it.first() }
        val joined = seq1.leftOuterJoinByKey(seq2) { _, v1, v2 -> "${v1}${v2 ?: ""}" }
        assertEquals(listOf("a1", "b2b3"), joined.toList())
    }

    @Test
    fun `left outer join with merge function works correctly with descending sort order`() {
        val seq1 = sequenceOf("c1", "b2").assertSortedBy(DESCENDING) { it.first() }
        val seq2 = sequenceOf("b3", "a4").assertSortedBy(DESCENDING) { it.first() }
        val joined = seq1.leftOuterJoinByKey(seq2) { _, v1, v2 -> "${v1}${v2 ?: ""}" }
        assertEquals(listOf("c1", "b2b3"), joined.toList())
    }

    @Test
    fun `right outer join works correctly`() {
        val seq1 = sequenceOf("a1", "b2").assertSortedBy { it.first() }
        val seq2 = sequenceOf("b3", "c4").assertSortedBy { it.first() }
        val joined = seq1.rightOuterJoinByKey(seq2)
        assertEquals(listOf("b2" to "b3", null to "c4"), joined.toList())
    }

    @Test
    fun `right outer join works correctly with descending sort order`() {
        val seq1 = sequenceOf("c1", "b2").assertSortedBy(DESCENDING) { it.first() }
        val seq2 = sequenceOf("b3", "a4").assertSortedBy(DESCENDING) { it.first() }
        val joined = seq1.rightOuterJoinByKey(seq2)
        assertEquals(listOf("b2" to "b3", null to "a4"), joined.toList())
    }

    @Test
    fun `right outer join with merge function works correctly`() {
        val seq1 = sequenceOf("a1", "b2").assertSortedBy { it.first() }
        val seq2 = sequenceOf("b3", "c4").assertSortedBy { it.first() }
        val joined = seq1.rightOuterJoinByKey(seq2) { _, v1, v2 -> "${v1 ?: ""}$v2" }
        assertEquals(listOf("b2b3", "c4"), joined.toList())
    }

    @Test
    fun `right outer join with merge function works correctly with descending sort order`() {
        val seq1 = sequenceOf("c1", "b2").assertSortedBy(DESCENDING) { it.first() }
        val seq2 = sequenceOf("b3", "a4").assertSortedBy(DESCENDING) { it.first() }
        val joined = seq1.rightOuterJoinByKey(seq2) { _, v1, v2 -> "${v1 ?: ""}$v2" }
        assertEquals(listOf("b2b3", "a4"), joined.toList())
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
    fun `creating descending SortedSequence with key selector works correctly`() {
        val sequence = sequenceOf("az", "by", "cx").assertSortedBy(DESCENDING) { it.last() }
        assertEquals(listOf("az", "by", "cx"), sequence.toList())
    }

    @Test
    fun `exception is thrown when sequence is not properly sorted`() {
        val sequence = sequenceOf(1, 3, 2).assertSorted(ASCENDING)

        assertThrows<SortedSequenceException.SequenceNotSortedException> {
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
