package nl.timocode.kotlinutils

import nl.timocode.kotlinutils.SortOrder.ASCENDING
import nl.timocode.kotlinutils.SortOrder.DESCENDING

// TODO: variance
// TODO: support nullable value types
class SortedSequence<TKey : Comparable<TKey>, TValue> private constructor(
    private val innerSequence: Sequence<Pair<TKey, TValue>>,
    val sortOrder: SortOrder = ASCENDING
) : Sequence<TValue> {

    fun iteratorWithKeys(): Iterator<Pair<TKey, TValue>> {
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

    override fun iterator(): Iterator<TValue> {
        return iterator {
            for ((_, value) in iteratorWithKeys()) {
                yield(value)
            }
        }
    }

    fun <TValueOut>map(transform: (TValue) -> TValueOut): SortedSequence<TKey, TValueOut> {
        return innerSequence
            .map { (key, value) -> key to transform(value) }
            .let { SortedSequence(it, sortOrder) }
    }

    fun groupByKey(): SortedSequence<TKey, Pair<TKey, List<TValue>>> {
        return SortedSequence(
            sequence {
                var currentKey: TKey? = null
                var currentGroup = mutableListOf<TValue>()
                val iteratorWithKeys = iteratorWithKeys()
                while (iteratorWithKeys.hasNext()) {
                    val (key, value) = iteratorWithKeys.next()
                    if (currentKey != null && key != currentKey) {
                        yield(currentKey to (currentKey to currentGroup))
                        currentGroup = mutableListOf()
                    }
                    currentKey = key
                    currentGroup.add(value)
                }
                if (currentKey != null) yield(currentKey to (currentKey to currentGroup))
            },
            sortOrder
        )
    }

    fun <TValue2, TValueOut>zipByKey(
        other: SortedSequence<TKey, TValue2>,
        zipper: (TKey, TValue?, TValue2?) -> TValueOut
    ): SortedSequence<TKey, TValueOut> {
        if (sortOrder != other.sortOrder) throw SortedSequenceException.InvalidSortOrderException()

        return SortedSequence(
            sequence {
                val iterator1 = iteratorWithKeys()
                val iterator2 = other.iteratorWithKeys()

                var el1 = iterator1.nextOrNull()
                var el2 = iterator2.nextOrNull()
                while (el1 != null || el2 != null) {
                    val zip = when {
                        el1 == null -> null to el2
                        el2 == null -> el1 to null
                        el1.key < el2.key && sortOrder == ASCENDING -> el1 to null
                        el1.key < el2.key && sortOrder == DESCENDING -> null to el2
                        el1.key == el2.key -> el1 to el2
                        el1.key > el2.key && sortOrder == ASCENDING -> null to el2
                        el1.key > el2.key && sortOrder == DESCENDING -> el1 to null
                        else -> throw IllegalStateException("Unreachable code")
                    }
                    val key = zip.first?.key ?: zip.second!!.key
                    val value = zipper(key, zip.first?.value, zip.second?.value)
                    yield(key to value)
                    el1 = if (zip.first != null) iterator1.nextOrNull() else el1
                    el2 = if (zip.second != null) iterator2.nextOrNull() else el2
                }
            },
            sortOrder
        )
    }

    private val <A, B>Pair<A, B>.key: A get() = first

    private val <A, B>Pair<A, B>.value: B get() = second

    private fun <T>Iterator<T>.nextOrNull() = if (hasNext()) next() else null

    companion object Factory {
        fun <TKey : Comparable<TKey>, TValue>Sequence<TValue>.asAscendingSortedSequence(
            keySelector: (TValue) -> TKey
        ): SortedSequence<TKey, TValue> = asSortedSequence(keySelector, ASCENDING)

        fun <TKey : Comparable<TKey>, TValue>Sequence<TValue>.asDescendingSortedSequence(
            keySelector: (TValue) -> TKey
        ): SortedSequence<TKey, TValue> = asSortedSequence(keySelector, DESCENDING)

        fun <TKey : Comparable<TKey>, TValue>Sequence<TValue>.asSortedSequence(
            keySelector: (TValue) -> TKey,
            sortOrder: SortOrder
        ): SortedSequence<TKey, TValue> {
            return SortedSequence(this.map { keySelector(it) to it }, sortOrder)
        }
    }
}
