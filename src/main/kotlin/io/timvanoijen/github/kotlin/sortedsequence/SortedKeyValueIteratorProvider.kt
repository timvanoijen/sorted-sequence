package io.timvanoijen.github.kotlin.sortedsequence

interface SortedKeyValueIteratorProvider<TKey : Comparable<TKey>, out TValue> {

    /**
     * Returns an iterator over the key-value pairs in this sequence while verifying the sort order.
     *
     * The iterator validates that keys are in the correct order (ascending or descending) as it
     * traverses the sequence. If a key is found to be out of order, a [SortedSequenceException.SequenceNotSortedException]
     * is thrown.
     *
     * @return An iterator that yields key-value pairs in sorted order
     * @throws SortedSequenceException.SequenceNotSortedException if the sequence is not properly sorted
     */
    fun keyValueIterator(): Iterator<Pair<TKey, TValue>>

    /**
     * Specifies the sort order (ascending or descending) of the key-value pairs in this sequence.
     *
     * This property determines the expected ordering of keys when iterating through the sequence.
     * The [keyValueIterator] uses this value to validate that keys are properly sorted.
     */
    val sortOrder: SortOrder
}
