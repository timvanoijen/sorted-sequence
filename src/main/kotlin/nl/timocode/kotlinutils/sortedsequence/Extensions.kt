package nl.timocode.kotlinutils.sortedsequence

import nl.timocode.kotlinutils.sortedsequence.SortOrder.ASCENDING
import nl.timocode.kotlinutils.sortedsequence.SortOrder.DESCENDING
import nl.timocode.kotlinutils.sortedsequence.SortedKeyValueSequence.Factory.assertSorted

fun <TKey : Comparable<TKey>, TValue>
SortedKeyValueIteratorProvider<TKey, TValue>.groupByKey(): SortedKeyValueSequence<TKey, List<TValue>> {
    return sequence {
        var currentKey: TKey? = null
        var currentGroup = mutableListOf<TValue>()
        val keyValueIterator = keyValueIterator()
        while (keyValueIterator.hasNext()) {
            val (key, value) = keyValueIterator.next()
            if (currentKey != null && key != currentKey) {
                yield(currentKey to currentGroup)
                currentGroup = mutableListOf()
            }
            currentKey = key
            currentGroup.add(value)
        }
        if (currentKey != null) yield(currentKey to currentGroup)
    }.assertSorted(sortOrder = sortOrder)
}

fun <TKey : Comparable<TKey>, TValue1, TValue2, TValueOut>
SortedKeyValueIteratorProvider<TKey, TValue1>.zipByKey(
    other: SortedKeyValueIteratorProvider<TKey, TValue2>,
    zipper: (TKey, TValue1?, TValue2?) -> TValueOut
): SortedSequence<TKey, TValueOut> {
    if (sortOrder != other.sortOrder) throw SortedSequenceException.InvalidSortOrderException()

    return SortedSequence(
        sequence {
            val iterator1 = keyValueIterator()
            val iterator2 = other.keyValueIterator()

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
