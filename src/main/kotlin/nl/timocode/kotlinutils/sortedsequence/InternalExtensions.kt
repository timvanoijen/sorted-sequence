package nl.timocode.kotlinutils.sortedsequence

import nl.timocode.kotlinutils.sortedsequence.JoinType.FULL_OUTER_JOIN
import nl.timocode.kotlinutils.sortedsequence.JoinType.LEFT_OUTER_JOIN
import nl.timocode.kotlinutils.sortedsequence.JoinType.RIGHT_OUTER_JOIN
import nl.timocode.kotlinutils.sortedsequence.SortOrder.ASCENDING
import nl.timocode.kotlinutils.sortedsequence.SortOrder.DESCENDING

internal fun <TKey : Comparable<TKey>, TValue>
Sequence<Pair<TKey, TValue>>.internalGroupByKey(): Sequence<Pair<TKey, List<TValue>>> {
    return sequence {
        var currentKey: TKey? = null
        var currentGroup = mutableListOf<TValue>()
        val keyValueIterator = iterator()
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
    }
}

internal fun <TKey : Comparable<TKey>, TValue1, TValue2, TValueOut>
SortedKeyValueIteratorProvider<TKey, TValue1>.internalMergeByKey(
    other: SortedKeyValueIteratorProvider<TKey, TValue2>,
    joinType: JoinType = FULL_OUTER_JOIN,
    mergeFn: (TKey, TValue1?, TValue2?) -> TValueOut
): Sequence<Pair<TKey, TValueOut>> {
    if (sortOrder != other.sortOrder) throw SortedSequenceException.InvalidSortOrderException()

    return SortedKeyValueSequence(
        sequence {
            val iterator1 = keyValueIterator()
            val iterator2 = other.keyValueIterator()

            var el1 = iterator1.nextOrNull()
            var el2 = iterator2.nextOrNull()
            while (el1 != null || el2 != null) {
                val pair = when {
                    el1 == null -> null to el2
                    el2 == null -> el1 to null
                    el1.key < el2.key && sortOrder == ASCENDING -> el1 to null
                    el1.key < el2.key && sortOrder == DESCENDING -> null to el2
                    el1.key == el2.key -> el1 to el2
                    el1.key > el2.key && sortOrder == ASCENDING -> null to el2
                    el1.key > el2.key && sortOrder == DESCENDING -> el1 to null
                    else -> throw IllegalStateException("Unreachable code")
                }
                val key = pair.first?.key ?: pair.second!!.key

                if ((pair.first != null || joinType == FULL_OUTER_JOIN || joinType == RIGHT_OUTER_JOIN) &&
                    (pair.second != null || joinType == FULL_OUTER_JOIN || joinType == LEFT_OUTER_JOIN)
                ) {
                    val value = mergeFn(key, pair.first?.value, pair.second?.value)
                    yield(key to value)
                }

                el1 = if (pair.first != null) iterator1.nextOrNull() else el1
                el2 = if (pair.second != null) iterator2.nextOrNull() else el2
            }
        },
        sortOrder
    )
}

private val <A, B>Pair<A, B>.key: A get() = first

private val <A, B>Pair<A, B>.value: B get() = second

private fun <T>Iterator<T>.nextOrNull() = if (hasNext()) next() else null
