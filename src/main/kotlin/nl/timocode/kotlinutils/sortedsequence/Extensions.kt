package nl.timocode.kotlinutils.sortedsequence

import nl.timocode.kotlinutils.sortedsequence.JoinType.FULL_OUTER_JOIN
import nl.timocode.kotlinutils.sortedsequence.JoinType.INNER_JOIN
import nl.timocode.kotlinutils.sortedsequence.JoinType.LEFT_OUTER_JOIN
import nl.timocode.kotlinutils.sortedsequence.JoinType.RIGHT_OUTER_JOIN
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
SortedKeyValueIteratorProvider<TKey, TValue1>.innerJoinByKey(
    other: SortedKeyValueIteratorProvider<TKey, TValue2>,
    mergeFn: (TKey, TValue1, TValue2) -> TValueOut
): SortedSequence<TKey, TValueOut> = mergeByKey(other, INNER_JOIN) {
        key, a, b ->
    mergeFn(key, a!!, b!!)
}

fun <TKey : Comparable<TKey>, TValue1, TValue2>
SortedKeyValueIteratorProvider<TKey, TValue1>.innerJoinByKey(
    other: SortedKeyValueIteratorProvider<TKey, TValue2>
): SortedSequence<TKey, Pair<TValue1, TValue2>> = innerJoinByKey(other) { _, a, b -> a to b }

fun <TKey : Comparable<TKey>, TValue1, TValue2, TValueOut>
SortedKeyValueIteratorProvider<TKey, TValue1>.leftOuterJoinByKey(
    other: SortedKeyValueIteratorProvider<TKey, TValue2>,
    mergeFn: (TKey, TValue1, TValue2?) -> TValueOut
): SortedSequence<TKey, TValueOut> = mergeByKey(other, LEFT_OUTER_JOIN) {
        key, a, b ->
    mergeFn(key, a!!, b)
}

fun <TKey : Comparable<TKey>, TValue1, TValue2>
SortedKeyValueIteratorProvider<TKey, TValue1>.leftOuterJoinByKey(
    other: SortedKeyValueIteratorProvider<TKey, TValue2>
): SortedSequence<TKey, Pair<TValue1, TValue2?>> = leftOuterJoinByKey(other) { _, a, b -> a to b }

fun <TKey : Comparable<TKey>, TValue1, TValue2, TValueOut>
SortedKeyValueIteratorProvider<TKey, TValue1>.rightOuterJoinByKey(
    other: SortedKeyValueIteratorProvider<TKey, TValue2>,
    mergeFn: (TKey, TValue1?, TValue2) -> TValueOut
): SortedSequence<TKey, TValueOut> = mergeByKey(other, RIGHT_OUTER_JOIN) {
        key, a, b ->
    mergeFn(key, a, b!!)
}

fun <TKey : Comparable<TKey>, TValue1, TValue2>
SortedKeyValueIteratorProvider<TKey, TValue1>.rightOuterJoinByKey(
    other: SortedKeyValueIteratorProvider<TKey, TValue2>
): SortedSequence<TKey, Pair<TValue1?, TValue2>> = rightOuterJoinByKey(other) { _, a, b -> a to b }

fun <TKey : Comparable<TKey>, TValue1, TValue2, TValueOut>
SortedKeyValueIteratorProvider<TKey, TValue1>.fullOuterJoinByKey(
    other: SortedKeyValueIteratorProvider<TKey, TValue2>,
    mergeFn: (TKey, TValue1?, TValue2?) -> TValueOut
): SortedSequence<TKey, TValueOut> = mergeByKey(other, FULL_OUTER_JOIN, mergeFn)

fun <TKey : Comparable<TKey>, TValue1, TValue2>
SortedKeyValueIteratorProvider<TKey, TValue1>.fullOuterJoinByKey(
    other: SortedKeyValueIteratorProvider<TKey, TValue2>
): SortedSequence<TKey, Pair<TValue1?, TValue2?>> = fullOuterJoinByKey(other) { _, a, b -> a to b }

fun <TKey : Comparable<TKey>, TValue1, TValue2, TValueOut>
SortedKeyValueIteratorProvider<TKey, TValue1>.mergeByKey(
    other: SortedKeyValueIteratorProvider<TKey, TValue2>,
    joinType: JoinType = FULL_OUTER_JOIN,
    mergeFn: (TKey, TValue1?, TValue2?) -> TValueOut
): SortedSequence<TKey, TValueOut> {
    if (sortOrder != other.sortOrder) throw SortedSequenceException.InvalidSortOrderException()

    return SortedSequence(
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

enum class JoinType {
    FULL_OUTER_JOIN,
    LEFT_OUTER_JOIN,
    RIGHT_OUTER_JOIN,
    INNER_JOIN
}

private val <A, B>Pair<A, B>.key: A get() = first

private val <A, B>Pair<A, B>.value: B get() = second

private fun <T>Iterator<T>.nextOrNull() = if (hasNext()) next() else null
