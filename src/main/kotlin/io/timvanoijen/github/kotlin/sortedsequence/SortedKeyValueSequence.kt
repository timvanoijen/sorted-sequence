package io.timvanoijen.github.kotlin.sortedsequence

import io.timvanoijen.github.kotlin.sortedsequence.JoinType.FULL_OUTER_JOIN
import io.timvanoijen.github.kotlin.sortedsequence.JoinType.INNER_JOIN
import io.timvanoijen.github.kotlin.sortedsequence.JoinType.LEFT_OUTER_JOIN
import io.timvanoijen.github.kotlin.sortedsequence.JoinType.RIGHT_OUTER_JOIN
import io.timvanoijen.github.kotlin.sortedsequence.SortOrder.ASCENDING
import io.timvanoijen.github.kotlin.sortedsequence.SortOrder.DESCENDING

/**
 * Represents a sequence of key-value pairs that are sorted by key.
 * This class provides operations for working with sorted sequences like mapping values, grouping and joining.
 *
 * Example:
 * ```
 * val sequence = sequenceOf(1 to "a", 2 to "b", 3 to "c").assertSorted()
 * ```
 *
 * @param TKey The type of keys in the sequence, must be comparable
 * @param TValue The type of values in the sequence
 * @property sortOrder The sort order (ascending/descending) of the sequence
 */
class SortedKeyValueSequence<TKey : Comparable<TKey>, out TValue> private constructor(
    private val innerSequence: Sequence<Pair<TKey, TValue>>,
    override val sortOrder: SortOrder,
    private val doVerifySortOrder: Boolean
) : SortedKeyValueIteratorProvider<TKey, TValue>, Sequence<Pair<TKey, TValue>> {

    override fun keyValueIterator(): Iterator<Pair<TKey, TValue>> {
        if (!doVerifySortOrder) return innerSequence.iterator()

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

    /**
     * Returns a new sequence with transformed values while preserving keys and sort order.
     *
     * Example:
     * ```
     * val sequence = sequenceOf(1 to "a", 2 to "b").assertSorted()
     * val upperCase = sequence.mapValues { it.uppercase() }
     * // Results in: (1 to "A", 2 to "B")
     * ```
     *
     * @param transform The function to transform each value
     * @return A new sorted sequence with transformed values
     */
    fun <TValueOut>mapValues(transform: (TValue) -> TValueOut): SortedKeyValueSequence<TKey, TValueOut> {
        return map { it.first to transform(it.second) }
            .assumeSorted(sortOrder)
    }

    /**
     * Groups values by their keys, maintaining sort order.
     *
     * Example:
     * ```
     * val sequence = sequenceOf(1 to "a", 1 to "b", 2 to "c").assertSorted()
     * val grouped = sequence.groupByKey()
     * // Results in: (1 to ["a", "b"], 2 to ["c"])
     * ```
     *
     * @return A new sorted sequence with grouped values
     */
    fun groupByKey(): SortedKeyValueSequence<TKey, List<TValue>> {
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
        }.assumeSorted(sortOrder)
    }

    /**
     * Merges this sequence with another sorted sequence based on matching keys.
     *
     * Example:
     * ```
     * val seq1 = sequenceOf(1 to "a", 2 to "b").assertSorted()
     * val seq2 = sequenceOf(1 to "x", 3 to "z").assertSorted()
     * val merged = seq1.mergeByKey(seq2) { key, v1, v2 -> "${v1 ?: ""}${v2 ?: ""}" }
     * // Results in: (1 to "ax", 2 to "b", 3 to "z")
     * ```
     *
     * @param other The sequence to merge with
     * @param joinType The type of join to perform (default: FULL_OUTER_JOIN)
     * @param mergeFn Function that defines how to merge values for matching keys
     * @return A new sorted sequence with merged values
     */
    fun <TValue2, TValueOut>mergeByKey(
        other: SortedKeyValueIteratorProvider<TKey, TValue2>,
        joinType: JoinType = FULL_OUTER_JOIN,
        mergeFn: (TKey, TValue?, TValue2?) -> TValueOut
    ): SortedKeyValueSequence<TKey, TValueOut> {
        if (sortOrder != other.sortOrder) throw SortedSequenceException.InvalidSortOrderException()

        return sequence {
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
        }.assumeSorted(sortOrder)
    }

    /**
     * Merges this sequence with another sorted sequence using default pairing of values.
     *
     * Example:
     * ```
     * val seq1 = sequenceOf(1 to "a", 2 to "b").assertSorted()
     * val seq2 = sequenceOf(1 to "x", 3 to "z").assertSorted()
     * val merged = seq1.mergeByKey(seq2)
     * // Results in: (1 to ("a" to "x"), 2 to ("b" to null), 3 to (null to "z"))
     * ```
     *
     * @param other The sequence to merge with
     * @param joinType The type of join to perform (default: FULL_OUTER_JOIN)
     * @return A new sorted sequence with paired values
     */
    fun <TValue2> mergeByKey(
        other: SortedKeyValueIteratorProvider<TKey, TValue2>,
        joinType: JoinType = FULL_OUTER_JOIN
    ): SortedKeyValueSequence<TKey, Pair<TValue?, TValue2?>> = mergeByKey(other, joinType) { _, a, b -> a to b }

    /**
     * Performs a full outer join with another sorted sequence.
     *
     * Example:
     * ```
     * val seq1 = sequenceOf(1 to "a", 2 to "b").assertSorted()
     * val seq2 = sequenceOf(1 to "x", 3 to "z").assertSorted()
     * val joined = seq1.fullOuterJoinByKey(seq2) { key, v1, v2 -> "${v1 ?: ""}${v2 ?: ""}" }
     * // Results in: (1 to "ax", 2 to "b", 3 to "z")
     * ```
     *
     * @param other The sequence to join with
     * @param mergeFn Function that defines how to merge values for matching keys
     * @return A new sorted sequence with merged values, keeping all keys from both sequences
     */
    fun <TValue2, TValueOut> fullOuterJoinByKey(
        other: SortedKeyValueIteratorProvider<TKey, TValue2>,
        mergeFn: (TKey, TValue?, TValue2?) -> TValueOut
    ): SortedKeyValueSequence<TKey, TValueOut> =
        mergeByKey(other, FULL_OUTER_JOIN, mergeFn)

    /**
     * Performs a full outer join with another sorted sequence using default pairing of values.
     *
     * Example:
     * ```
     * val seq1 = sequenceOf(1 to "a", 2 to "b").assertSorted()
     * val seq2 = sequenceOf(1 to "x", 3 to "z").assertSorted()
     * val joined = seq1.fullOuterJoinByKey(seq2)
     * // Results in: (1 to ("a" to "x"), 2 to ("b" to null), 3 to (null to "z"))
     * ```
     *
     * @param other The sequence to join with
     * @return A new sorted sequence with paired values, keeping all keys from both sequences
     */
    fun <TValue2> fullOuterJoinByKey(
        other: SortedKeyValueIteratorProvider<TKey, TValue2>
    ): SortedKeyValueSequence<TKey, Pair<TValue?, TValue2?>> = fullOuterJoinByKey(other) { _, a, b -> a to b }

    /**
     * Performs an inner join with another sorted sequence.
     *
     * Example:
     * ```
     * val seq1 = sequenceOf(1 to "a", 2 to "b").assertSorted()
     * val seq2 = sequenceOf(1 to "x", 3 to "z").assertSorted()
     * val joined = seq1.innerJoinByKey(seq2) { key, v1, v2 -> "$v1$v2" }
     * // Results in: (1 to "ax")
     * ```
     *
     * @param other The sequence to join with
     * @param mergeFn Function that defines how to merge values for matching keys
     * @return A new sorted sequence with merged values for keys present in both sequences
     */
    fun <TValue2, TValueOut> innerJoinByKey(
        other: SortedKeyValueIteratorProvider<TKey, TValue2>,
        mergeFn: (TKey, TValue, TValue2) -> TValueOut
    ): SortedKeyValueSequence<TKey, TValueOut> =
        mergeByKey(other, INNER_JOIN) { key, a, b -> mergeFn(key, a!!, b!!) }

    /**
     * Performs an inner join with another sorted sequence using default pairing of values.
     *
     * Example:
     * ```
     * val seq1 = sequenceOf(1 to "a", 2 to "b").assertSorted()
     * val seq2 = sequenceOf(1 to "x", 3 to "z").assertSorted()
     * val joined = seq1.innerJoinByKey(seq2)
     * // Results in: (1 to ("a" to "x"))
     * ```
     *
     * @param other The sequence to join with
     * @return A new sorted sequence with paired values for keys present in both sequences
     */
    fun <TValue2> innerJoinByKey(
        other: SortedKeyValueIteratorProvider<TKey, TValue2>
    ): SortedKeyValueSequence<TKey, Pair<TValue, TValue2>> = innerJoinByKey(other) { _, a, b -> a to b }

    /**
     * Performs a left outer join with another sorted sequence.
     *
     * Example:
     * ```
     * val seq1 = sequenceOf(1 to "a", 2 to "b").assertSorted()
     * val seq2 = sequenceOf(1 to "x", 3 to "z").assertSorted()
     * val joined = seq1.leftOuterJoinByKey(seq2) { key, v1, v2 -> "${v1 ?: ""}$v2" }
     * // Results in: (1 to "ax", 2 to "b")
     * ```
     *
     * @param other The sequence to join with
     * @param mergeFn Function that defines how to merge values for matching keys
     * @return A new sorted sequence with merged values, keeping all keys from this sequence
     */
    fun <TValue2, TValueOut> leftOuterJoinByKey(
        other: SortedKeyValueIteratorProvider<TKey, TValue2>,
        mergeFn: (TKey, TValue, TValue2?) -> TValueOut
    ): SortedKeyValueSequence<TKey, TValueOut> =
        mergeByKey(other, LEFT_OUTER_JOIN) { key, a, b -> mergeFn(key, a!!, b) }

    /**
     * Performs a left outer join with another sorted sequence using default pairing of values.
     *
     * Example:
     * ```
     * val seq1 = sequenceOf(1 to "a", 2 to "b").assertSorted()
     * val seq2 = sequenceOf(1 to "x", 3 to "z").assertSorted()
     * val joined = seq1.leftOuterJoinByKey(seq2)
     * // Results in: (1 to ("a" to "x"), 2 to ("b" to null))
     * ```
     *
     * @param other The sequence to join with
     * @return A new sorted sequence with paired values, keeping all keys from this sequence
     */
    fun <TValue2> leftOuterJoinByKey(
        other: SortedKeyValueIteratorProvider<TKey, TValue2>
    ): SortedKeyValueSequence<TKey, Pair<TValue, TValue2?>> = leftOuterJoinByKey(other) { _, a, b -> a to b }

    /**
     * Performs a right outer join with another sorted sequence.
     *
     * Example:
     * ```
     * val seq1 = sequenceOf(1 to "a", 2 to "b").assertSorted()
     * val seq2 = sequenceOf(1 to "x", 3 to "z").assertSorted()
     * val joined = seq1.rightOuterJoinByKey(seq2) { key, v1, v2 -> "$v1${v2 ?: ""}" }
     * // Results in: (1 to "ax", 3 to "z")
     * ```
     *
     * @param other The sequence to join with
     * @param mergeFn Function that defines how to merge values for matching keys
     * @return A new sorted sequence with merged values, keeping all keys from other sequence
     */
    fun <TValue2, TValueOut> rightOuterJoinByKey(
        other: SortedKeyValueIteratorProvider<TKey, TValue2>,
        mergeFn: (TKey, TValue?, TValue2) -> TValueOut
    ): SortedKeyValueSequence<TKey, TValueOut> =
        mergeByKey(other, RIGHT_OUTER_JOIN) { key, a, b -> mergeFn(key, a, b!!) }

    /**
     * Performs a right outer join with another sorted sequence using default pairing of values.
     *
     * Example:
     * ```
     * val seq1 = sequenceOf(1 to "a", 2 to "b").assertSorted()
     * val seq2 = sequenceOf(1 to "x", 3 to "z").assertSorted()
     * val joined = seq1.rightOuterJoinByKey(seq2)
     * // Results in: (1 to ("a" to "x"), 3 to (null to "z"))
     * ```
     *
     * @param other The sequence to join with
     * @return A new sorted sequence with paired values, keeping all keys from other sequence
     */
    fun <TValue2> rightOuterJoinByKey(
        other: SortedKeyValueIteratorProvider<TKey, TValue2>
    ): SortedKeyValueSequence<TKey, Pair<TValue?, TValue2?>> = rightOuterJoinByKey(other) { _, a, b -> a to b }

    companion object Factory {
        /**
         * Creates a [SortedKeyValueSequence] from this sequence, asserting that elements are sorted by their key.
         *
         * Example:
         * ```
         * val sorted = sequenceOf(1 to "a", 2 to "b").assertSorted()
         * ```
         * @param TKey The type of keys in the sequence, must be comparable
         * @param TValue The type of values in the sequence
         * @param sortOrder The sort order to verify against (default: ASCENDING)
         * @return A sorted sequence wrapper
         */
        fun <TKey : Comparable<TKey>, TValue> Sequence<Pair<TKey, TValue>>.assertSorted(
            sortOrder: SortOrder = ASCENDING
        ): SortedKeyValueSequence<TKey, TValue> {
            return SortedKeyValueSequence(this, sortOrder, doVerifySortOrder = true)
        }

        // Internal helper function to map a sequence of key-value pairs and a sort order to a
        // SortedKeyValueSequence that does not verify the sort order.
        internal fun <TKey : Comparable<TKey>, TValue> Sequence<Pair<TKey, TValue>>.assumeSorted(
            sortOrder: SortOrder = ASCENDING
        ): SortedKeyValueSequence<TKey, TValue> {
            return SortedKeyValueSequence(this, sortOrder, doVerifySortOrder = false)
        }
    }

    private val <A, B>Pair<A, B>.key: A get() = first

    private val <A, B>Pair<A, B>.value: B get() = second

    private fun <T>Iterator<T>.nextOrNull() = if (hasNext()) next() else null
}
