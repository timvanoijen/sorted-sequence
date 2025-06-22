package nl.timocode.kotlinutils.sortedsequence

import nl.timocode.kotlinutils.sortedsequence.SortedKeyValueSequence.Factory.assertSorted

// TODO: variance
// TODO: support nullable value types
class SortedSequence<TKey : Comparable<TKey>, TValue>(
    private val innerSequence: Sequence<Pair<TKey, TValue>>,
    override val sortOrder: SortOrder
) : SortedKeyValueIteratorProvider<TKey, TValue>, Sequence<TValue> {

    constructor(
        sequence: Sequence<TValue>,
        keySelector: (TValue) -> TKey,
        sortOrder: SortOrder
    ) : this(sequence.map { keySelector(it) to it }, sortOrder)

    override fun keyValueIterator() = innerSequence.assertSorted(sortOrder).iterator()

    override fun iterator(): Iterator<TValue> {
        return iterator {
            for ((_, value) in keyValueIterator()) {
                yield(value)
            }
        }
    }

    fun asSortedKeyValues(): SortedKeyValueSequence<TKey, TValue> {
        return SortedKeyValueSequence(innerSequence, sortOrder)
    }

    companion object Factory {
        fun <TKey : Comparable<TKey>, TValue> Sequence<TValue>.assertSortedBy(
            sortOrder: SortOrder = SortOrder.ASCENDING,
            keySelector: (TValue) -> TKey
        ): SortedSequence<TKey, TValue> {
            return SortedSequence(this, keySelector, sortOrder)
        }

        fun <T : Comparable<T>> Sequence<T>.assertSorted(
            sortOrder: SortOrder = SortOrder.ASCENDING
        ): SortedSequence<T, T> {
            return SortedSequence(this, { it }, sortOrder)
        }
    }
}
