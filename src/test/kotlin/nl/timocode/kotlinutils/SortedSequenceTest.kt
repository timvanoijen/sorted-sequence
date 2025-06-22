package nl.timocode.kotlinutils

import nl.timocode.kotlinutils.SortedSequence.Factory.asAscendingSortedSequence
import nl.timocode.kotlinutils.SortedSequence.Factory.asDescendingSortedSequence
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class SortedSequenceTest {

    @Test
    fun `exception is thrown when sequence is not properly sorted`() {
        val sequence = sequenceOf(1, 3, 2).asAscendingSortedSequence { it }

        assertThrows<SortedSequenceException.SequenceNotSortedException> {
            sequence.toList()
        }
    }

    @Test
    fun `groupByKey works correctly for duplicate keys at start, middle and end`() {
        val sequence = sequenceOf(1, 1, 1, 2, 2, 3, 3).asAscendingSortedSequence { it }
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
        val sequence = sequenceOf(1, 2, 3).asAscendingSortedSequence { it }
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
        val sequence = sequenceOf(3, 3, 2, 1, 1).asDescendingSortedSequence { it }
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
    fun `zipByKey works correctly`() {
        val seq1 = sequenceOf(
            1 to "A",
            3 to "B",
            5 to "C"
        ).asAscendingSortedSequence { it.first }.map { it.second } // TODO: add factory method with value-selector

        val seq2 = sequenceOf(
            2 to 100L,
            3 to 200L,
            4 to 300L
        ).asAscendingSortedSequence { it.first }.map { it.second }

        val result = seq1.zipByKey(seq2) { key, a, b -> key to (a to b) }.toList()

        assertEquals(
            listOf(
                1 to ("A" to null),
                2 to (null to 100L),
                3 to ("B" to 200L),
                4 to (null to 300L),
                5 to ("C" to null)
            ),
            result
        )
    }

    @Test
    fun `zipByKey works correctly for descending sequences`() {
        val seq1 = sequenceOf(
            5 to "C",
            3 to "B",
            1 to "A"
        ).asDescendingSortedSequence { it.first }.map { it.second }

        val seq2 = sequenceOf(
            4 to 300L,
            3 to 200L,
            2 to 100L
        ).asDescendingSortedSequence { it.first }.map { it.second }

        val result = seq1.zipByKey(seq2) { key, a, b -> key to (a to b) }.toList()

        assertEquals(
            listOf(
                5 to ("C" to null),
                4 to (null to 300L),
                3 to ("B" to 200L),
                2 to (null to 100L),
                1 to ("A" to null)
            ),
            result
        )
    }

    @Test
    fun `zipByKey throws InvalidSortOrderException when sequences have different sort orders`() {
        val seq1 = sequenceOf(1, 2, 3).asAscendingSortedSequence { it }
        val seq2 = sequenceOf(3, 2, 1).asDescendingSortedSequence { it }

        assertThrows<SortedSequenceException.InvalidSortOrderException> {
            seq1.zipByKey(seq2) { key, a, b -> Triple(key, a, b) }.toList()
        }
    }
}
