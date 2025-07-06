package io.timvanoijen.github.kotlin.sortedsequence

import io.timvanoijen.github.kotlin.sortedsequence.JoinType.FULL_OUTER_JOIN
import io.timvanoijen.github.kotlin.sortedsequence.JoinType.INNER_JOIN
import io.timvanoijen.github.kotlin.sortedsequence.JoinType.LEFT_OUTER_JOIN
import io.timvanoijen.github.kotlin.sortedsequence.JoinType.RIGHT_OUTER_JOIN
import io.timvanoijen.github.kotlin.sortedsequence.SortOrder.ASCENDING
import io.timvanoijen.github.kotlin.sortedsequence.SortedKeyValueSequence.Factory.assertSorted
import kotlin.collections.iterator

/**
 * Represents a sequence of elements that are sorted by key.
 * This class provides operations for working with sorted sequences like mapping values, grouping and joining.
 *
 * Example:
 * ```
 * val sequence = sequenceOf("az", "by", "cx").assertSortedBy { it.first() }
 * ```
 *
 * @param TKey The type of keys in the sequence, must be comparable
 * @param TValue The type of values in the sequence
 * @property sortOrder The sort order (ascending/descending) of the sequence
 */
class SortedSequence<TKey : Comparable<TKey>, out TValue> internal constructor(
    private val innerSortedKeyValueSequence: SortedKeyValueSequence<TKey, TValue>
) : SortedKeyValueIteratorProvider<TKey, TValue>, Sequence<TValue> {

    override val sortOrder: SortOrder = innerSortedKeyValueSequence.sortOrder

    override fun keyValueIterator(): Iterator<Pair<TKey, TValue>> = innerSortedKeyValueSequence.keyValueIterator()

    override fun iterator(): Iterator<TValue> {
        return iterator {
            for ((_, value) in keyValueIterator()) {
                yield(value)
            }
        }
    }

    fun asSortedKeyValues(): SortedKeyValueSequence<TKey, TValue> = innerSortedKeyValueSequence

    /**
     * Groups values by their sorting key, maintaining sort order.
     *
     * Example:
     * ```
     * val sequence = sequenceOf(1, 2, 2, 3).assertSorted()
     * val grouped = sequence.groupByKey()
     * // Results in: ([1], [2, 2], [3])
     * ```
     *
     * @return A new sorted sequence with grouped values
     */
    fun groupByKey(): SortedSequence<TKey, List<TValue>> =
        SortedSequence(innerSortedKeyValueSequence.groupByKey())

    /**
     * Returns a new sequence containing only the first occurrence of each key.
     *
     * Example:
     * ```
     * val sequence = sequenceOf(1, 1, 2).assertSorted()
     * val distinct = sequence.distinctByKey()
     * // Results in: (1, 2)
     * ```
     *
     * @return A new sorted sequence with duplicate keys removed
     */
    fun distinctByKey(): SortedSequence<TKey, TValue> =
        SortedSequence(innerSortedKeyValueSequence.distinctByKey())

    /**
     * Merges this sequence with another sorted sequence based on matching keys.
     *
     * Example:
     * ```
     * val seq1 = sequenceOf("a1", "b2").assertSortedBy { it.first() }
     * val seq2 = sequenceOf("b3", "c4").assertSortedBy { it.first() }
     * val merged = seq1.mergeByKey(seq2) { key, v1, v2 -> "${v1 ?: ""}${v2 ?: ""}" }
     * // Results in: ("a1", "b2b3", "c4")
     * ```
     *
     * @param other The sequence to merge with
     * @param joinType The type of join to perform (default: FULL_OUTER_JOIN)
     * @param mergeFn Function that defines how to merge values for matching keys
     * @return A new sorted sequence with merged values
     */
    fun <TValue2, TValueOut> mergeByKey(
        other: SortedKeyValueIteratorProvider<TKey, TValue2>,
        joinType: JoinType = FULL_OUTER_JOIN,
        mergeFn: (TKey, TValue?, TValue2?) -> TValueOut
    ): SortedSequence<TKey, TValueOut> =
        SortedSequence(innerSortedKeyValueSequence.mergeByKey(other, joinType, mergeFn))

    /**
     * Merges this sequence with another sorted sequence using default pairing of values.
     *
     * Example:
     * ```
     * val seq1 = sequenceOf("a1", "b2").assertSortedBy { it.first() }
     * val seq2 = sequenceOf("b3", "c4").assertSortedBy { it.first() }
     * val merged = seq1.mergeByKey(seq2)
     * // Results in: ("a1" to null, "b2" to "b3", null to "c4")
     * ```
     *
     * @param other The sequence to merge with
     * @param joinType The type of join to perform (default: FULL_OUTER_JOIN)
     * @return A new sorted sequence with paired values
     */
    fun <TValue2> mergeByKey(
        other: SortedKeyValueIteratorProvider<TKey, TValue2>,
        joinType: JoinType = FULL_OUTER_JOIN
    ): SortedSequence<TKey, Pair<TValue?, TValue2?>> = mergeByKey(other, joinType) { _, a, b -> a to b }

    /**
     * Performs a full outer join with another sorted sequence.
     *
     * Example:
     * ```
     * val seq1 = sequenceOf("a1", "b2").assertSortedBy { it.first() }
     * val seq2 = sequenceOf("b3", "c4").assertSortedBy { it.first() }
     * val joined = seq1.fullOuterJoinByKey(seq2) { key, v1, v2 -> "${v1 ?: ""}${v2 ?: ""}" }
     * // Results in: ("a1", "b2b3", "c4")
     * ```
     *
     * @param other The sequence to join with
     * @param mergeFn Function that defines how to merge values for matching keys
     * @return A new sorted sequence with merged values, keeping all keys from both sequences
     */
    fun <TValue2, TValueOut> fullOuterJoinByKey(
        other: SortedKeyValueIteratorProvider<TKey, TValue2>,
        mergeFn: (TKey, TValue?, TValue2?) -> TValueOut
    ): SortedSequence<TKey, TValueOut> = mergeByKey(other, FULL_OUTER_JOIN, mergeFn)

    /**
     * Performs a full outer join with another sorted sequence using default pairing of values.
     *
     * Example:
     * ```
     * val seq1 = sequenceOf("a1", "b2").assertSortedBy { it.first() }
     * val seq2 = sequenceOf("b3", "c4").assertSortedBy { it.first() }
     * val joined = seq1.fullOuterJoinByKey(seq2)
     * // Results in: ("a1" to null, "b2" to "b3", null to "c4")
     * ```
     *
     * @param other The sequence to join with
     * @return A new sorted sequence with paired values, keeping all keys from both sequences
     */
    fun <TValue2> fullOuterJoinByKey(
        other: SortedKeyValueIteratorProvider<TKey, TValue2>
    ): SortedSequence<TKey, Pair<TValue?, TValue2?>> = fullOuterJoinByKey(other) { _, a, b -> a to b }

    /**
     * Performs an inner join with another sorted sequence.
     *
     * Example:
     * ```
     * val seq1 = sequenceOf("a1", "b2").assertSortedBy { it.first() }
     * val seq2 = sequenceOf("b3", "c4").assertSortedBy { it.first() }
     * val joined = seq1.innerJoinByKey(seq2) { key, v1, v2 -> "$v1$v2" }
     * // Results in: ("b2b3")
     * ```
     *
     * @param other The sequence to join with
     * @param mergeFn Function that defines how to merge values for matching keys
     * @return A new sorted sequence with merged values for keys present in both sequences
     */
    fun <TValue2, TValueOut> innerJoinByKey(
        other: SortedKeyValueIteratorProvider<TKey, TValue2>,
        mergeFn: (TKey, TValue, TValue2) -> TValueOut
    ): SortedSequence<TKey, TValueOut> = mergeByKey(other, INNER_JOIN) { key, a, b -> mergeFn(key, a!!, b!!) }

    /**
     * Performs an inner join with another sorted sequence using default pairing of values.
     *
     * Example:
     * ```
     * val seq1 = sequenceOf("a1", "b2").assertSortedBy { it.first() }
     * val seq2 = sequenceOf("b3", "c4").assertSortedBy { it.first() }
     * val joined = seq1.innerJoinByKey(seq2)
     * // Results in: ("b2" to "b3")
     * ```
     *
     * @param other The sequence to join with
     * @return A new sorted sequence with paired values for keys present in both sequences
     */
    fun <TValue2> innerJoinByKey(
        other: SortedKeyValueIteratorProvider<TKey, TValue2>
    ): SortedSequence<TKey, Pair<TValue, TValue2>> = innerJoinByKey(other) { _, a, b -> a to b }

    /**
     * Performs a left outer join with another sorted sequence.
     *
     * Example:
     * ```
     * val seq1 = sequenceOf("a1", "b2").assertSortedBy { it.first() }
     * val seq2 = sequenceOf("b3", "c4").assertSortedBy { it.first() }
     * val joined = seq1.leftOuterJoinByKey(seq2) { key, v1, v2 -> "${v1}${v2 ?: ""}" }
     * // Results in: ("a1", "b2b3")
     * ```
     *
     * @param other The sequence to join with
     * @param mergeFn Function that defines how to merge values for matching keys
     * @return A new sorted sequence with merged values, keeping all keys from this sequence
     */
    fun <TValue2, TValueOut> leftOuterJoinByKey(
        other: SortedKeyValueIteratorProvider<TKey, TValue2>,
        mergeFn: (TKey, TValue, TValue2?) -> TValueOut
    ): SortedSequence<TKey, TValueOut> = mergeByKey(other, LEFT_OUTER_JOIN) { key, a, b -> mergeFn(key, a!!, b) }

    /**
     * Performs a left outer join with another sorted sequence using default pairing of values.
     *
     * Example:
     * ```
     * val seq1 = sequenceOf("a1", "b2").assertSortedBy { it.first() }
     * val seq2 = sequenceOf("b3", "c4").assertSortedBy { it.first() }
     * val joined = seq1.leftOuterJoinByKey(seq2)
     * // Results in: ("a1" to null, "b2" to "b3")
     * ```
     *
     * @param other The sequence to join with
     * @return A new sorted sequence with paired values, keeping all keys from this sequence
     */
    fun <TValue2> leftOuterJoinByKey(
        other: SortedKeyValueIteratorProvider<TKey, TValue2>
    ): SortedSequence<TKey, Pair<TValue, TValue2?>> = leftOuterJoinByKey(other) { _, a, b -> a to b }

    /**
     * Performs a right outer join with another sorted sequence.
     *
     * Example:
     * ```
     * val seq1 = sequenceOf("a1", "b2").assertSortedBy { it.first() }
     * val seq2 = sequenceOf("b3", "c4").assertSortedBy { it.first() }
     * val joined = seq1.rightOuterJoinByKey(seq2) { key, v1, v2 -> "${v1 ?: ""}$v2" }
     * // Results in: ("b2b3", "c4")
     * ```
     *
     * @param other The sequence to join with
     * @param mergeFn Function that defines how to merge values for matching keys
     * @return A new sorted sequence with merged values, keeping all keys from other sequence
     */
    fun <TValue2, TValueOut> rightOuterJoinByKey(
        other: SortedKeyValueIteratorProvider<TKey, TValue2>,
        mergeFn: (TKey, TValue?, TValue2) -> TValueOut
    ): SortedSequence<TKey, TValueOut> = mergeByKey(other, RIGHT_OUTER_JOIN) { key, a, b -> mergeFn(key, a, b!!) }

    /**
     * Performs a right outer join with another sorted sequence using default pairing of values.
     *
     * Example:
     * ```
     * val seq1 = sequenceOf("a1", "b2").assertSortedBy { it.first() }
     * val seq2 = sequenceOf("b3", "c4").assertSortedBy { it.first() }
     * val joined = seq1.rightOuterJoinByKey(seq2)
     * // Results in: ("b2" to "b3", null to "c4")
     * ```
     *
     * @param other The sequence to join with
     * @return A new sorted sequence with paired values, keeping all keys from other sequence
     */
    fun <TValue2> rightOuterJoinByKey(
        other: SortedKeyValueIteratorProvider<TKey, TValue2>
    ): SortedSequence<TKey, Pair<TValue?, TValue2?>> = rightOuterJoinByKey(other) { _, a, b -> a to b }

    companion object Factory {

        /**
         * Creates a [SortedSequence] from this sequence, asserting that elements are sorted by the given key selector.
         *
         * Example:
         * ```
         * val sequence = sequenceOf("az", "by", "cx").assertSortedBy { it.first() }
         * ```
         *
         * @param TKey The type of keys in the sequence, must be comparable
         * @param TValue The type of values in the sequence
         * @param sortOrder The sort order to verify against (default: ASCENDING)
         * @param keySelector Function that extracts the sorting key from each element
         * @return A sorted sequence wrapper
         */
        fun <TKey : Comparable<TKey>, TValue> Sequence<TValue>.assertSortedBy(
            sortOrder: SortOrder = ASCENDING,
            keySelector: (TValue) -> TKey
        ): SortedSequence<TKey, TValue> {
            val innerSortedKeyValueSequence = this.map { keySelector(it) to it }.assertSorted(sortOrder)
            return SortedSequence(innerSortedKeyValueSequence)
        }

        /**
         * Creates a [SortedSequence] from this sequence, asserting that elements are sorted according to their natural
         * order.
         *
         * Example:
         * ```
         * val sequence = sequenceOf("a", "b", "c").assertSorted()
         * ```
         *
         * @param T The type of elements in the sequence, must be comparable
         * @param sortOrder The sort order to verify against (default: ASCENDING)
         * @return A sorted sequence wrapper
         */
        fun <T : Comparable<T>> Sequence<T>.assertSorted(
            sortOrder: SortOrder = ASCENDING
        ): SortedSequence<T, T> = assertSortedBy(sortOrder) { it }
    }
}
