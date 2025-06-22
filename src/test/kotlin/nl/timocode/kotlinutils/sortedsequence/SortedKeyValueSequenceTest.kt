package nl.timocode.kotlinutils.sortedsequence

import nl.timocode.kotlinutils.sortedsequence.SortOrder.ASCENDING
import nl.timocode.kotlinutils.sortedsequence.SortOrder.DESCENDING
import nl.timocode.kotlinutils.sortedsequence.SortedKeyValueSequence.Factory.assertSorted
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class SortedKeyValueSequenceTest {

    @Test
    fun `creating ascending SortedKeyValueSequence works correctly`() {
        val sequence = sequenceOf(
            1 to "a",
            2 to "b",
            3 to "c"
        ).assertSorted()

        assertEquals(
            listOf(
                1 to "a",
                2 to "b",
                3 to "c"
            ),
            sequence.toList()
        )
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
