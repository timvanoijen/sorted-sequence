package nl.timocode.kotlinutils.sortedsequence

import nl.timocode.kotlinutils.sortedsequence.JoinType.INNER_JOIN
import nl.timocode.kotlinutils.sortedsequence.JoinType.LEFT_OUTER_JOIN
import nl.timocode.kotlinutils.sortedsequence.JoinType.RIGHT_OUTER_JOIN
import nl.timocode.kotlinutils.sortedsequence.SortOrder.DESCENDING
import nl.timocode.kotlinutils.sortedsequence.SortedKeyValueSequence.Factory.assertSorted
import nl.timocode.kotlinutils.sortedsequence.SortedSequence.Factory.assertSorted
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ExtensionsTest {

    @Test
    fun `groupByKey works correctly for duplicate keys at start, middle and end`() {
        val sequence = sequenceOf(1, 1, 1, 2, 2, 3, 3).assertSorted()
        val result = sequence.groupByKey().toList()

        assertEquals(
            listOf(
                1 to listOf(1, 1, 1),
                2 to listOf(2, 2),
                3 to listOf(3, 3)
            ),
            result
        )
    }

    @Test
    fun `groupByKey works correctly for unique keys at start, middle and end`() {
        val sequence = sequenceOf(1, 2, 3).assertSorted()
        val result = sequence.groupByKey().toList()

        assertEquals(
            listOf(
                1 to listOf(1),
                2 to listOf(2),
                3 to listOf(3)
            ),
            result
        )
    }

    @Test
    fun `groupByKey works correctly for descending sequence`() {
        val sequence = sequenceOf(3, 3, 2, 1, 1).assertSorted(DESCENDING)
        val result = sequence.groupByKey().toList()

        assertEquals(
            listOf(
                3 to listOf(3, 3),
                2 to listOf(2),
                1 to listOf(1, 1)
            ),
            result
        )
    }

    @Test
    fun `mergeByKey works correctly for ascending sequences`() {
        val seq1 = sequenceOf(
            1 to "A",
            3 to "B",
            5 to "C"
        ).assertSorted()

        val seq2 = sequenceOf(
            2 to 100L,
            3 to 200L,
            4 to 300L
        ).assertSorted()

        val resultFullOuterJoin = seq1.mergeByKey(seq2) { key, a, b -> "$key-$a-$b" }.toList()
        val resultLeftOuterJoin = seq1.mergeByKey(seq2, LEFT_OUTER_JOIN) { key, a, b -> "$key-$a-$b" }.toList()
        val resultRightOuterJoin = seq1.mergeByKey(seq2, RIGHT_OUTER_JOIN) { key, a, b -> "$key-$a-$b" }.toList()
        val resultInnerJoin = seq1.mergeByKey(seq2, INNER_JOIN) { key, a, b -> "$key-$a-$b" }.toList()

        assertEquals(listOf("1-A-null", "2-null-100", "3-B-200", "4-null-300", "5-C-null"), resultFullOuterJoin)
        assertEquals(listOf("1-A-null", "3-B-200", "5-C-null"), resultLeftOuterJoin)
        assertEquals(listOf("2-null-100", "3-B-200", "4-null-300"), resultRightOuterJoin)
        assertEquals(listOf("3-B-200"), resultInnerJoin)
    }

    @Test
    fun `mergeByKey works correctly for descending sequences`() {
        val seq1 = sequenceOf(
            5 to "C",
            3 to "B",
            1 to "A"
        ).assertSorted(DESCENDING)

        val seq2 = sequenceOf(
            4 to 300L,
            3 to 200L,
            2 to 100L
        ).assertSorted(DESCENDING)

        val result = seq1.mergeByKey(seq2) { key, a, b -> "$key-$a-$b" }.toList()

        assertEquals(listOf("5-C-null", "4-null-300", "3-B-200", "2-null-100", "1-A-null"), result)
    }

    @Test
    fun `mergeByKey throws InvalidSortOrderException when sequences have different sort orders`() {
        val seq1 = sequenceOf(1, 2, 3).assertSorted()
        val seq2 = sequenceOf(3, 2, 1).assertSorted(DESCENDING)

        assertThrows<SortedSequenceException.InvalidSortOrderException> {
            seq1.mergeByKey(seq2) { key, a, b -> Triple(key, a, b) }.toList()
        }
    }
}
