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

    /**
     * Converts this SortedSequence to a SortedKeyValueSequence.
     *
     * Example:
     * ```
     * val sequence = sequenceOf("az", "by", "cx").assertSortedBy { it.first() }
     * val keyValueSequence = sequence.asSortedKeyValues()
     * assertEquals(listOf('a' to "az", 'b' to "by", 'c' to "cx"), keyValueSequence.toList())
     * ```
     *
     * @return A SortedKeyValueSequence with the same elements as this sequence
     */
    fun asSortedKeyValues(): SortedKeyValueSequence<TKey, TValue> = innerSortedKeyValueSequence

    /**
     * Filters the sequence to only include elements whose keys match the given predicate.
     *
     * Example:
     * ```
     * val sequence = sequenceOf(1, 2, 3).assertSorted()
     * val filtered = sequence.filterByKey { it % 2 == 0 }
     * assertEquals(listOf(2), filtered.toList())
     * ```
     *
     * @param filterFn The predicate to test keys against
     * @return A new sorted sequence containing only elements with matching keys
     */
    fun filterByKey(filterFn: (TKey) -> Boolean): SortedSequence<TKey, TValue> {
        return SortedSequence(innerSortedKeyValueSequence.filterByKey(filterFn))
    }

    /**
     * Filters the sequence to only include elements whose values match the given predicate.
     *
     * Example:
     * ```
     * val sequence = sequenceOf("a1", "b2", "c3").assertSortedBy { it.first() }
     * val filtered = sequence.filterByValue { it.last() > '1' }
     * assertEquals(listOf("b2", "c3"), filtered.toList())
     * ```
     *
     * @param filterFn The predicate to test values against
     * @return A new sorted sequence containing only elements with matching values
     */
    fun filterByValue(filterFn: (TValue) -> Boolean): SortedSequence<TKey, TValue> {
        return SortedSequence(innerSortedKeyValueSequence.filterByValue(filterFn))
    }

    /**
     * Filters the sequence to only include elements whose values match the given predicate.
     * This is an alias for filterByValue.
     *
     * Example:
     * ```
     * val sequence = sequenceOf("a1", "b2", "c3").assertSortedBy { it.first() }
     * val filtered = sequence.filter { it.last() > '1' }
     * assertEquals(listOf("b2", "c3"), filtered.toList())
     * ```
     *
     * @param filterFn The predicate to test values against
     * @return A new sorted sequence containing only elements with matching values
     */
    fun filter(filterFn: (TValue) -> Boolean) = filterByValue(filterFn)

    /**
     * Groups values by their sorting key, maintaining sort order.
     *
     * Example:
     * ```
     * val sequence = sequenceOf(1, 2, 2, 3).assertSorted()
     * val grouped = sequence.groupByKey()
     * assertEquals(listOf(listOf(1), listOf(2, 2), listOf(3)), grouped.toList())
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
     * assertEquals(listOf(1, 2), distinct.toList())
     * ```
     *
     * @return A new sorted sequence with duplicate keys removed
     */
    fun distinctByKey(): SortedSequence<TKey, TValue> =
        SortedSequence(innerSortedKeyValueSequence.distinctByKey())

    /**
     * Performs a join between two sorted sequences based on matching keys.
     *
     * Example:
     * ```
     * val seq1 = sequenceOf("1a", "2b", "2c").assertSortedBy { it.first() }
     * val seq2 = sequenceOf("2x", "2y", "3z").assertSortedBy { it.first() }
     * val result = seq1.joinByKey(seq2) { _, v1, v2 -> "${v1 ?: ""}${v2 ?: ""}" }
     * assertEquals(
     *     listOf("1a", "2b2x", "2b2y", "2c2x", "2c2y", "3z"),
     *     result.toList()
     * )
     * ```
     *
     * @param other The sequence to join with
     * @param joinType The type of join to perform (INNER_JOIN, LEFT_OUTER_JOIN, RIGHT_OUTER_JOIN, FULL_OUTER_JOIN)
     * @param mergeFn Function that defines how to merge values for matching keys
     * @return A new sorted sequence with merged values according to the join type
     */
    fun <TValue2, TValueOut> joinByKey(
        other: SortedKeyValueIteratorProvider<TKey, TValue2>,
        joinType: JoinType = FULL_OUTER_JOIN,
        mergeFn: (TKey, TValue?, TValue2?) -> TValueOut
    ): SortedSequence<TKey, TValueOut> =
        SortedSequence(innerSortedKeyValueSequence.joinByKey(other, joinType, mergeFn))

    /**
     * Performs a join between two sorted sequences based on matching keys using default pairing of values.
     *
     * Example:
     * ```
     * val seq1 = sequenceOf("1a", "2b", "2c").assertSortedBy { it.first() }
     * val seq2 = sequenceOf("2x", "2y", "3z").assertSortedBy { it.first() }
     * val result = seq1.joinByKey(seq2)
     * assertEquals(
     *     listOf(
     *         "1a" to null,
     *         "2b" to "2x",
     *         "2b" to "2y",
     *         "2c" to "2x",
     *         "2c" to "2y",
     *         null to "3z"
     *     ),
     *     result.toList()
     * )
     * ```
     *
     * @param other The sequence to join with
     * @param joinType The type of join to perform (default: FULL_OUTER_JOIN)
     * @return A new sorted sequence with paired values according to the join type
     */
    fun <TValue2> joinByKey(
        other: SortedKeyValueIteratorProvider<TKey, TValue2>,
        joinType: JoinType = FULL_OUTER_JOIN
    ): SortedSequence<TKey, Pair<TValue?, TValue2?>> = joinByKey(other, joinType) { _, a, b -> a to b }

    /**
     * Performs a full outer join with another sorted sequence.
     *
     * Example:
     * ```
     * val seq1 = sequenceOf("1a", "2b", "2c").assertSortedBy { it.first() }
     * val seq2 = sequenceOf("2x", "2y", "3z").assertSortedBy { it.first() }
     * val result = seq1.fullOuterJoinByKey(seq2) { _, v1, v2 -> "${v1 ?: ""}${v2 ?: ""}" }
     * assertEquals(
     *     listOf("1a", "2b2x", "2b2y", "2c2x", "2c2y", "3z"),
     *     result.toList()
     * )
     * ```
     *
     * @param other The sequence to join with
     * @param mergeFn Function that defines how to merge values for matching keys
     * @return A new sorted sequence with merged values for all keys from both sequences
     */
    fun <TValue2, TValueOut> fullOuterJoinByKey(
        other: SortedKeyValueIteratorProvider<TKey, TValue2>,
        mergeFn: (TKey, TValue?, TValue2?) -> TValueOut
    ): SortedSequence<TKey, TValueOut> = joinByKey(other, FULL_OUTER_JOIN, mergeFn)

    /**
     * Performs a full outer join with another sorted sequence using default pairing of values.
     *
     * Example:
     * ```
     * val seq1 = sequenceOf("1a", "2b", "2c").assertSortedBy { it.first() }
     * val seq2 = sequenceOf("2x", "2y", "3z").assertSortedBy { it.first() }
     * val result = seq1.fullOuterJoinByKey(seq2)
     * assertEquals(
     *     listOf(
     *         "1a" to null,
     *         "2b" to "2x",
     *         "2b" to "2y",
     *         "2c" to "2x",
     *         "2c" to "2y",
     *         null to "3z"
     *     ),
     *     result.toList()
     * )
     * ```
     *
     * @param other The sequence to join with
     * @return A new sorted sequence with paired values for all keys from both sequences
     */
    fun <TValue2> fullOuterJoinByKey(
        other: SortedKeyValueIteratorProvider<TKey, TValue2>
    ): SortedSequence<TKey, Pair<TValue?, TValue2?>> = fullOuterJoinByKey(other) { _, a, b -> a to b }

    /**
     * Performs an inner join with another sorted sequence.
     *
     * Example:
     * ```
     * val seq1 = sequenceOf("1a", "2b", "2c").assertSortedBy { it.first() }
     * val seq2 = sequenceOf("2x", "2y", "3z").assertSortedBy { it.first() }
     * val result = seq1.innerJoinByKey(seq2) { _, v1, v2 -> "$v1$v2" }
     * assertEquals(
     *     listOf("2b2x", "2b2y", "2c2x", "2c2y"),
     *     result.toList()
     * )
     * ```
     *
     * @param other The sequence to join with
     * @param mergeFn Function that defines how to merge values for matching keys
     * @return A new sorted sequence with merged values for keys present in both sequences
     */
    fun <TValue2, TValueOut> innerJoinByKey(
        other: SortedKeyValueIteratorProvider<TKey, TValue2>,
        mergeFn: (TKey, TValue, TValue2) -> TValueOut
    ): SortedSequence<TKey, TValueOut> = joinByKey(other, INNER_JOIN) { key, a, b -> mergeFn(key, a!!, b!!) }

    /**
     * Performs an inner join with another sorted sequence using default pairing of values.
     *
     * Example:
     * ```
     * val seq1 = sequenceOf("1a", "2b", "2c").assertSortedBy { it.first() }
     * val seq2 = sequenceOf("2x", "2y", "3z").assertSortedBy { it.first() }
     * val result = seq1.innerJoinByKey(seq2)
     * assertEquals(
     *     listOf(
     *         "2b" to "2x",
     *         "2b" to "2y",
     *         "2c" to "2x",
     *         "2c" to "2y"
     *     ),
     *     result.toList()
     * )
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
     * val seq1 = sequenceOf("1a", "2b", "2c").assertSortedBy { it.first() }
     * val seq2 = sequenceOf("2x", "2y", "3z").assertSortedBy { it.first() }
     * val result = seq1.leftOuterJoinByKey(seq2) { _, v1, v2 -> "$v1${v2 ?: ""}" }
     * assertEquals(
     *     listOf("1a", "2b2x", "2b2y", "2c2x", "2c2y"),
     *     result.toList()
     * )
     * ```
     *
     * @param other The sequence to join with
     * @param mergeFn Function that defines how to merge values for matching keys
     * @return A new sorted sequence with merged values, keeping all keys from this sequence
     */
    fun <TValue2, TValueOut> leftOuterJoinByKey(
        other: SortedKeyValueIteratorProvider<TKey, TValue2>,
        mergeFn: (TKey, TValue, TValue2?) -> TValueOut
    ): SortedSequence<TKey, TValueOut> = joinByKey(other, LEFT_OUTER_JOIN) { key, a, b -> mergeFn(key, a!!, b) }

    /**
     * Performs a left outer join with another sorted sequence using default pairing of values.
     *
     * Example:
     * ```
     * val seq1 = sequenceOf("1a", "2b", "2c").assertSortedBy { it.first() }
     * val seq2 = sequenceOf("2x", "2y", "3z").assertSortedBy { it.first() }
     * val result = seq1.leftOuterJoinByKey(seq2)
     * assertEquals(
     *     listOf(
     *         "1a" to null,
     *         "2b" to "2x",
     *         "2b" to "2y",
     *         "2c" to "2x",
     *         "2c" to "2y"
     *     ),
     *     result.toList()
     * )
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
     * val seq1 = sequenceOf("1a", "2b", "2c").assertSortedBy { it.first() }
     * val seq2 = sequenceOf("2x", "2y", "3z").assertSortedBy { it.first() }
     * val result = seq1.rightOuterJoinByKey(seq2) { _, v1, v2 -> "${v1 ?: ""}$v2" }
     * assertEquals(
     *     listOf("2b2x", "2b2y", "2c2x", "2c2y", "3z"),
     *     result.toList()
     * )
     * ```
     *
     * @param other The sequence to join with
     * @param mergeFn Function that defines how to merge values for matching keys
     * @return A new sorted sequence with merged values, keeping all keys from other sequence
     */
    fun <TValue2, TValueOut> rightOuterJoinByKey(
        other: SortedKeyValueIteratorProvider<TKey, TValue2>,
        mergeFn: (TKey, TValue?, TValue2) -> TValueOut
    ): SortedSequence<TKey, TValueOut> = joinByKey(other, RIGHT_OUTER_JOIN) { key, a, b -> mergeFn(key, a, b!!) }

    /**
     * Performs a right outer join with another sorted sequence using default pairing of values.
     *
     * Example:
     * ```
     * val seq1 = sequenceOf("1a", "2b", "2c").assertSortedBy { it.first() }
     * val seq2 = sequenceOf("2x", "2y", "3z").assertSortedBy { it.first() }
     * val result = seq1.rightOuterJoinByKey(seq2)
     * assertEquals(
     *     listOf(
     *         "2b" to "2x",
     *         "2b" to "2y",
     *         "2c" to "2x",
     *         "2c" to "2y",
     *         null to "3z"
     *     ),
     *     result.toList()
     * )
     * ```
     *
     * @param other The sequence to join with
     * @return A new sorted sequence with paired values, keeping all keys from other sequence
     */
    fun <TValue2> rightOuterJoinByKey(
        other: SortedKeyValueIteratorProvider<TKey, TValue2>
    ): SortedSequence<TKey, Pair<TValue?, TValue2?>> = rightOuterJoinByKey(other) { _, a, b -> a to b }

    /**
     * Zips the sequence with another sorted sequence based on matching keys.
     *
     * Example:
     * ```
     * val seq1 = sequenceOf("1a", "2b", "2c").assertSortedBy { it.first() }
     * val seq2 = sequenceOf("2x", "2y", "3z").assertSortedBy { it.first() }
     * val result = seq1.zipByKey(seq2) { _, v1, v2 -> "${v1 ?: ""}${v2 ?: ""}" }
     * assertEquals(
     *     listOf("1a", "2b2x", "2c2y", "3z"),
     *     result.toList()
     * )
     * ```
     *
     * @param other The sequence to merge with
     * @param joinType The type of join to perform (default: FULL_OUTER_JOIN)
     * @param mergeFn Function that defines how to merge values for matching keys
     * @return A new sorted sequence with merged values
     */
    fun <TValue2, TValueOut> zipByKey(
        other: SortedKeyValueIteratorProvider<TKey, TValue2>,
        joinType: JoinType = FULL_OUTER_JOIN,
        mergeFn: (TKey, TValue?, TValue2?) -> TValueOut
    ): SortedSequence<TKey, TValueOut> =
        SortedSequence(innerSortedKeyValueSequence.zipByKey(other, joinType, mergeFn))

    /**
     * Zips the sequence with another sorted sequence using default pairing of values.
     *
     * Example:
     * ```
     * val seq1 = sequenceOf("1a", "2b", "2c").assertSortedBy { it.first() }
     * val seq2 = sequenceOf("2x", "2y", "3z").assertSortedBy { it.first() }
     * val result = seq1.zipByKey(seq2)
     * assertEquals(
     *     listOf(
     *         "1a" to null,
     *         "2b" to "2x",
     *         "2c" to "2y",
     *         null to "3z"
     *     ),
     *     result.toList()
     * )
     * ```
     *
     * @param other The sequence to merge with
     * @param joinType The type of join to perform (default: FULL_OUTER_JOIN)
     * @return A new sorted sequence with paired values
     */
    fun <TValue2> zipByKey(
        other: SortedKeyValueIteratorProvider<TKey, TValue2>,
        joinType: JoinType = FULL_OUTER_JOIN
    ): SortedSequence<TKey, Pair<TValue?, TValue2?>> = zipByKey(other, joinType) { _, a, b -> a to b }

    /**
     * Performs a full outer zip with another sorted sequence.
     *
     * Example:
     * ```
     * val seq1 = sequenceOf("1a", "2b", "2c").assertSortedBy { it.first() }
     * val seq2 = sequenceOf("2x", "2y", "3z").assertSortedBy { it.first() }
     * val result = seq1.fullOuterZipByKey(seq2) { _, v1, v2 -> "${v1 ?: ""}${v2 ?: ""}" }
     * assertEquals(
     *     listOf("1a", "2b2x", "2c2y", "3z"),
     *     result.toList()
     * )
     * ```
     *
     * @param other The sequence to zip with
     * @param mergeFn Function that defines how to merge values for matching keys
     * @return A new sorted sequence with merged values, keeping all keys from both sequences
     */
    fun <TValue2, TValueOut> fullOuterZipByKey(
        other: SortedKeyValueIteratorProvider<TKey, TValue2>,
        mergeFn: (TKey, TValue?, TValue2?) -> TValueOut
    ): SortedSequence<TKey, TValueOut> = zipByKey(other, FULL_OUTER_JOIN, mergeFn)

    /**
     * Performs a full outer zip with another sorted sequence using default pairing of values.
     *
     * Example:
     * ```
     * val seq1 = sequenceOf("1a", "2b", "2c").assertSortedBy { it.first() }
     * val seq2 = sequenceOf("2x", "2y", "3z").assertSortedBy { it.first() }
     * val result = seq1.fullOuterZipByKey(seq2)
     * assertEquals(
     *     listOf(
     *         "1a" to null,
     *         "2b" to "2x",
     *         "2c" to "2y",
     *         null to "3z"
     *     ),
     *     result.toList()
     * )
     * ```
     *
     * @param other The sequence to zip with
     * @return A new sorted sequence with paired values, keeping all keys from both sequences
     */
    fun <TValue2> fullOuterZipByKey(
        other: SortedKeyValueIteratorProvider<TKey, TValue2>
    ): SortedSequence<TKey, Pair<TValue?, TValue2?>> = fullOuterZipByKey(other) { _, a, b -> a to b }

    /**
     * Performs an inner zip with another sorted sequence.
     *
     * Example:
     * ```
     * val seq1 = sequenceOf("1a", "2b", "2c").assertSortedBy { it.first() }
     * val seq2 = sequenceOf("2x", "2y", "3z").assertSortedBy { it.first() }
     * val result = seq1.innerZipByKey(seq2) { _, v1, v2 -> "$v1$v2" }
     * assertEquals(
     *     listOf("2b2x", "2c2y"),
     *     result.toList()
     * )
     * ```
     *
     * @param other The sequence to zip with
     * @param mergeFn Function that defines how to merge values for matching keys
     * @return A new sorted sequence with merged values for keys present in both sequences
     */
    fun <TValue2, TValueOut> innerZipByKey(
        other: SortedKeyValueIteratorProvider<TKey, TValue2>,
        mergeFn: (TKey, TValue, TValue2) -> TValueOut
    ): SortedSequence<TKey, TValueOut> = zipByKey(other, INNER_JOIN) { key, a, b -> mergeFn(key, a!!, b!!) }

    /**
     * Performs an inner zip with another sorted sequence using default pairing of values.
     *
     * Example:
     * ```
     * val seq1 = sequenceOf("1a", "2b", "2c").assertSortedBy { it.first() }
     * val seq2 = sequenceOf("2x", "2y", "3z").assertSortedBy { it.first() }
     * val result = seq1.innerZipByKey(seq2)
     * assertEquals(
     *     listOf(
     *         "2b" to "2x",
     *         "2c" to "2y"
     *     ),
     *     result.toList()
     * )
     * ```
     *
     * @param other The sequence to zip with
     * @return A new sorted sequence with paired values for keys present in both sequences
     */
    fun <TValue2> innerZipByKey(
        other: SortedKeyValueIteratorProvider<TKey, TValue2>
    ): SortedSequence<TKey, Pair<TValue, TValue2>> = innerZipByKey(other) { _, a, b -> a to b }

    /**
     * Performs a left outer zip with another sorted sequence.
     *
     * Example:
     * ```
     * val seq1 = sequenceOf("1a", "2b", "2c").assertSortedBy { it.first() }
     * val seq2 = sequenceOf("2x", "2y", "3z").assertSortedBy { it.first() }
     * val result = seq1.leftOuterZipByKey(seq2) { _, v1, v2 -> "$v1${v2 ?: ""}" }
     * assertEquals(
     *     listOf("1a", "2b2x", "2c2y"),
     *     result.toList()
     * )
     * ```
     *
     * @param other The sequence to zip with
     * @param mergeFn Function that defines how to merge values for matching keys
     * @return A new sorted sequence with merged values, keeping all keys from this sequence
     */
    fun <TValue2, TValueOut> leftOuterZipByKey(
        other: SortedKeyValueIteratorProvider<TKey, TValue2>,
        mergeFn: (TKey, TValue, TValue2?) -> TValueOut
    ): SortedSequence<TKey, TValueOut> = zipByKey(other, LEFT_OUTER_JOIN) { key, a, b -> mergeFn(key, a!!, b) }

    /**
     * Performs a left outer zip with another sorted sequence using default pairing of values.
     *
     * Example:
     * ```
     * val seq1 = sequenceOf("1a", "2b", "2c").assertSortedBy { it.first() }
     * val seq2 = sequenceOf("2x", "2y", "3z").assertSortedBy { it.first() }
     * val result = seq1.leftOuterZipByKey(seq2)
     * assertEquals(
     *     listOf(
     *         "1a" to null,
     *         "2b" to "2x",
     *         "2c" to "2y"
     *     ),
     *     result.toList()
     * )
     * ```
     *
     * @param other The sequence to zip with
     * @return A new sorted sequence with paired values, keeping all keys from this sequence
     */
    fun <TValue2> leftOuterZipByKey(
        other: SortedKeyValueIteratorProvider<TKey, TValue2>
    ): SortedSequence<TKey, Pair<TValue, TValue2?>> = leftOuterZipByKey(other) { _, a, b -> a to b }

    /**
     * Performs a right outer zip with another sorted sequence.
     *
     * Example:
     * ```
     * val seq1 = sequenceOf("1a", "2b", "2c").assertSortedBy { it.first() }
     * val seq2 = sequenceOf("2x", "2y", "3z").assertSortedBy { it.first() }
     * val result = seq1.rightOuterZipByKey(seq2) { _, v1, v2 -> "${v1 ?: ""}$v2" }
     * assertEquals(
     *     listOf("2b2x", "2c2y", "3z"),
     *     result.toList()
     * )
     * ```
     *
     * @param other The sequence to zip with
     * @param mergeFn Function that defines how to merge values for matching keys
     * @return A new sorted sequence with merged values, keeping all keys from other sequence
     */
    fun <TValue2, TValueOut> rightOuterZipByKey(
        other: SortedKeyValueIteratorProvider<TKey, TValue2>,
        mergeFn: (TKey, TValue?, TValue2) -> TValueOut
    ): SortedSequence<TKey, TValueOut> = zipByKey(other, RIGHT_OUTER_JOIN) { key, a, b -> mergeFn(key, a, b!!) }

    /**
     * Performs a right outer zip with another sorted sequence using default pairing of values.
     *
     * Example:
     * ```
     * val seq1 = sequenceOf("1a", "2b", "2c").assertSortedBy { it.first() }
     * val seq2 = sequenceOf("2x", "2y", "3z").assertSortedBy { it.first() }
     * val result = seq1.rightOuterZipByKey(seq2)
     * assertEquals(
     *     listOf(
     *         "2b" to "2x",
     *         "2c" to "2y",
     *         null to "3z"
     *     ),
     *     result.toList()
     * )
     * ```
     *
     * @param other The sequence to zip with
     * @return A new sorted sequence with paired values, keeping all keys from other sequence
     */
    fun <TValue2> rightOuterZipByKey(
        other: SortedKeyValueIteratorProvider<TKey, TValue2>
    ): SortedSequence<TKey, Pair<TValue?, TValue2?>> = rightOuterZipByKey(other) { _, a, b -> a to b }

    companion object Factory {

        /**
         * Creates a [SortedSequence] from this sequence, asserting that elements are sorted by the given key selector.
         *
         * Example:
         * ```
         * val sequence = sequenceOf("az", "by", "cx").assertSortedBy { it.first() }
         * assertEquals(listOf("az", "by", "cx"), sequence.toList())
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
         * val sequence = sequenceOf(1, 2, 3).assertSorted()
         * assertEquals(listOf(1, 2, 3), sequence.toList())
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
