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
    fun `creating ascending SortedSequence with key selector works correctly`() {
        val sequence = sequenceOf("az", "by", "cx").assertSortedBy { it.first() }
        assertEquals(listOf("az", "by", "cx"), sequence.toList())
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
