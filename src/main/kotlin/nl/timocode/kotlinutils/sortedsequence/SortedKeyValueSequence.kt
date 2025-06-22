package nl.timocode.kotlinutils.sortedsequence

import nl.timocode.kotlinutils.sortedsequence.SortOrder.ASCENDING
import nl.timocode.kotlinutils.sortedsequence.SortOrder.DESCENDING

// TODO: variance
// TODO: support nullable value types
class SortedKeyValueSequence<TKey : Comparable<TKey>, TValue>(
    private val innerSequence: Sequence<Pair<TKey, TValue>>,
    override val sortOrder: SortOrder
) : SortedKeyValueIteratorProvider<TKey, TValue>, Sequence<Pair<TKey, TValue>> {

    override fun keyValueIterator(): Iterator<Pair<TKey, TValue>> {
        return iterator {
            val innerIterator = innerSequence.iterator()
            var lastKey: TKey? = null
            while (innerIterator.hasNext()) {
                val (key, value) = innerIterator.next()
                if (lastKey != null) {
                    if ((sortOrder == ASCENDING && lastKey > key) || (sortOrder == DESCENDING && lastKey < key)) {
                        throw SortedSequenceException.SequenceNotSortedException()
                    }
                }
                lastKey = key
                yield(key to value)
            }
        }
    }

    override fun iterator(): Iterator<Pair<TKey, TValue>> = keyValueIterator()

    fun <TValueOut>mapValues(transform: (TValue) -> TValueOut): SortedKeyValueSequence<TKey, TValueOut> {
        return SortedKeyValueSequence(innerSequence.map { it.first to transform(it.second) }, sortOrder)
    }

    companion object Factory {
        fun <TKey : Comparable<TKey>, TValue> Sequence<Pair<TKey, TValue>>.assertSorted(
            sortOrder: SortOrder = ASCENDING
        ): SortedKeyValueSequence<TKey, TValue> {
            return SortedKeyValueSequence(this, sortOrder)
        }
    }
}
