package nl.timocode.kotlinutils.sortedsequence

import nl.timocode.kotlinutils.sortedsequence.JoinType.FULL_OUTER_JOIN
import nl.timocode.kotlinutils.sortedsequence.JoinType.INNER_JOIN
import nl.timocode.kotlinutils.sortedsequence.JoinType.LEFT_OUTER_JOIN
import nl.timocode.kotlinutils.sortedsequence.JoinType.RIGHT_OUTER_JOIN
import nl.timocode.kotlinutils.sortedsequence.SortedKeyValueSequence.Factory.assertSorted

class SortedSequence<TKey : Comparable<TKey>, out TValue>(
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

    fun groupByKey(): SortedSequence<TKey, List<TValue>> =
        SortedSequence(asSortedKeyValues().internalGroupByKey(), sortOrder)

    fun <TValue2, TValueOut>mergeByKey(
        other: SortedKeyValueIteratorProvider<TKey, TValue2>,
        joinType: JoinType = FULL_OUTER_JOIN,
        mergeFn: (TKey, TValue?, TValue2?) -> TValueOut
    ): SortedSequence<TKey, TValueOut> =
        SortedSequence(internalMergeByKey(other, joinType, mergeFn), sortOrder)

    fun <TValue2>mergeByKey(
        other: SortedKeyValueIteratorProvider<TKey, TValue2>,
        joinType: JoinType = FULL_OUTER_JOIN
    ): SortedSequence<TKey, Pair<TValue?, TValue2?>> = mergeByKey(other, joinType) { _, a, b -> a to b }

    fun <TValue2, TValueOut>fullOuterJoinByKey(
        other: SortedKeyValueIteratorProvider<TKey, TValue2>,
        mergeFn: (TKey, TValue?, TValue2?) -> TValueOut
    ): SortedSequence<TKey, TValueOut> =
        mergeByKey(other, FULL_OUTER_JOIN, mergeFn)

    fun <TValue2>fullOuterJoinByKey(
        other: SortedKeyValueIteratorProvider<TKey, TValue2>
    ): SortedSequence<TKey, Pair<TValue?, TValue2?>> = fullOuterJoinByKey(other) { _, a, b -> a to b }

    fun <TValue2, TValueOut>innerJoinByKey(
        other: SortedKeyValueIteratorProvider<TKey, TValue2>,
        mergeFn: (TKey, TValue, TValue2) -> TValueOut
    ): SortedSequence<TKey, TValueOut> =
        mergeByKey(other, INNER_JOIN) { key, a, b -> mergeFn(key, a!!, b!!) }

    fun <TValue2>innerJoinByKey(
        other: SortedKeyValueIteratorProvider<TKey, TValue2>
    ): SortedSequence<TKey, Pair<TValue, TValue2>> = innerJoinByKey(other) { _, a, b -> a to b }

    fun <TValue2, TValueOut>leftOuterJoinByKey(
        other: SortedKeyValueIteratorProvider<TKey, TValue2>,
        mergeFn: (TKey, TValue, TValue2?) -> TValueOut
    ): SortedSequence<TKey, TValueOut> =
        mergeByKey(other, LEFT_OUTER_JOIN) { key, a, b -> mergeFn(key, a!!, b) }

    fun <TValue2>leftOuterJoinByKey(
        other: SortedKeyValueIteratorProvider<TKey, TValue2>
    ): SortedSequence<TKey, Pair<TValue, TValue2?>> = leftOuterJoinByKey(other) { _, a, b -> a to b }

    fun <TValue2, TValueOut>rightOuterJoinByKey(
        other: SortedKeyValueIteratorProvider<TKey, TValue2>,
        mergeFn: (TKey, TValue?, TValue2) -> TValueOut
    ): SortedSequence<TKey, TValueOut> =
        mergeByKey(other, RIGHT_OUTER_JOIN) { key, a, b -> mergeFn(key, a, b!!) }

    fun <TValue2>rightOuterJoinByKey(
        other: SortedKeyValueIteratorProvider<TKey, TValue2>
    ): SortedSequence<TKey, Pair<TValue?, TValue2?>> = rightOuterJoinByKey(other) { _, a, b -> a to b }

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
