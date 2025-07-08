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
class SortedKeyValueSequence<TKey : Comparable<TKey>, out TValue> internal constructor(
    private val innerSequence: Sequence<Pair<TKey, TValue>>,
    override val sortOrder: SortOrder,
    private val doVerifySortOrder: Boolean
) : SortedKeyValueIteratorProvider<TKey, TValue>, Sequence<Pair<TKey, TValue>> {

    override fun keyValueIterator(): Iterator<Pair<TKey, TValue>> {
        if (!doVerifySortOrder) return innerSequence.iterator()

        return iterator {
            var lastKey: TKey? = null
            innerSequence.forEach { (key, value) ->
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
     * @param transformFn The function to transform each value
     * @return A new sorted sequence with transformed values
     */
    fun <TValueOut>mapValues(transformFn: (TValue) -> TValueOut): SortedKeyValueSequence<TKey, TValueOut> {
        return withSortingPreserved { map { it.first to transformFn(it.second) } }
    }

    /**
     * Filters key-value pairs based on their key using the provided filter function.
     *
     * Example:
     * ```
     * val sequence = sequenceOf(1 to "a", 2 to "b", 3 to "c").assertSorted()
     * val filtered = sequence.filterByKey { it > 1 }
     * // Results in: (2 to "b", 3 to "c")
     * ```
     *
     * @param filterFn The predicate to test values against
     * @return A new sorted sequence containing only the key-value pairs whose keys match the filter
     */
    fun filterByKey(filterFn: (TKey) -> Boolean): SortedKeyValueSequence<TKey, TValue> {
        return filter { (key, _) -> filterFn(key) }
    }

    /**
     * Filters key-value pairs based on their value using the provided filter function.
     *
     * Example:
     * ```
     * val sequence = sequenceOf(1 to "a", 2 to "b", 3 to "c").assertSorted()
     * val filtered = sequence.filterByValue { it == "b" }
     * // Results in: (2 to "b")
     * ```
     *
     * @param filterFn The predicate to test values against
     * @return A new sorted sequence containing only the key-value pairs whose values match the filter
     */
    fun filterByValue(filterFn: (TValue) -> Boolean): SortedKeyValueSequence<TKey, TValue> {
        return filter { (_, value) -> filterFn(value) }
    }

    /**
     * Filters key-value pairs using the provided filter function.
     *
     * Example:
     * ```
     * val sequence = sequenceOf(1 to "a", 2 to "b", 3 to "c").assertSorted()
     * val filtered = sequence.filter { (key, value) -> key > 1 && value == "b" }
     * // Results in: (2 to "b")
     * ```
     *
     * @param filterFn The predicate to test values against
     * @return A new sorted sequence containing only the pairs that match the filter
     */
    fun filter(filterFn: (Pair<TKey, TValue>) -> Boolean): SortedKeyValueSequence<TKey, TValue> {
        return withSortingPreserved { innerSequence.filter(filterFn) }
    }

    /**
     * Returns a new sequence containing only the first occurrence of each key.
     *
     * Example:
     * ```
     * val sequence = sequenceOf(1 to "a", 1 to "b", 2 to "c").assertSorted()
     * val distinct = sequence.distinctByKey()
     * // Results in: (1 to "a", 2 to "c")
     * ```
     *
     * @return A new sorted sequence with duplicate keys removed
     */
    fun distinctByKey(): SortedKeyValueSequence<TKey, TValue> {
        return sequence {
            var curKey: TKey? = null
            keyValueIterator().forEach { kvp ->
                if (kvp.key != curKey) {
                    yield(kvp)
                    curKey = kvp.key
                }
            }
        }.assumeSorted(sortOrder)
    }

    /**
     * Groups values by their keys, maintaining sort order.
     *
     * Example:
     * ```
     * val sequence = sequenceOf(1 to "a", 1 to "b", 2 to "c").assertSorted()
     * val result = sequence.groupByKey()
     * // Results in: (1 to ["a", "b"], 2 to ["c"])
     * ```
     *
     * @return A new sorted sequence with grouped values
     */
    fun groupByKey(): SortedKeyValueSequence<TKey, List<TValue>> {
        return sequence {
            var currentKey: TKey? = null
            var currentGroup = mutableListOf<TValue>()
            forEach { (key, value) ->
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
     * Zips this sequence with another sorted sequence based on matching keys.
     *
     * Example:
     * ```
     * val seq1 = sequenceOf(1 to "a", 2 to "b").assertSorted()
     * val seq2 = sequenceOf(1 to "x", 3 to "z").assertSorted()
     * val result = seq1.zipByKey(seq2) { key, v1, v2 -> "${v1 ?: ""}${v2 ?: ""}" }
     * // Results in: (1 to "ax", 2 to "b", 3 to "z")
     * ```
     *
     * @param other The sequence to zip with
     * @param joinType The type of join to perform (default: FULL_OUTER_JOIN)
     * @param mergeFn Function that defines how to merge values for matching keys
     * @return A new sorted sequence with merged values
     */
    fun <TValue2, TValueOut>zipByKey(
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
     * Zips the sequence with another sorted sequence using default pairing of values.
     *
     * Example:
     * ```
     * val seq1 = sequenceOf(1 to "a", 2 to "b").assertSorted()
     * val seq2 = sequenceOf(1 to "x", 3 to "z").assertSorted()
     * val result = seq1.zipByKey(seq2)
     * // Results in: (1 to ("a" to "x"), 2 to ("b" to null), 3 to (null to "z"))
     * ```
     *
     * @param other The sequence to merge with
     * @param joinType The type of join to perform (default: FULL_OUTER_JOIN)
     * @return A new sorted sequence with paired values
     */
    fun <TValue2> zipByKey(
        other: SortedKeyValueIteratorProvider<TKey, TValue2>,
        joinType: JoinType = FULL_OUTER_JOIN
    ): SortedKeyValueSequence<TKey, Pair<TValue?, TValue2?>> = zipByKey(other, joinType) { _, a, b -> a to b }

    /**
     * Performs a full outer zip with another sorted sequence.
     *
     * Example:
     * ```
     * val seq1 = sequenceOf(1 to "a", 2 to "b").assertSorted()
     * val seq2 = sequenceOf(1 to "x", 3 to "z").assertSorted()
     * val result = seq1.fullOuterZipByKey(seq2) { key, v1, v2 -> "${v1 ?: ""}${v2 ?: ""}" }
     * // Results in: (1 to "ax", 2 to "b", 3 to "z")
     * ```
     *
     * @param other The sequence to zip with
     * @param mergeFn Function that defines how to merge values for matching keys
     * @return A new sorted sequence with merged values, keeping all keys from both sequences
     */
    fun <TValue2, TValueOut> fullOuterZipByKey(
        other: SortedKeyValueIteratorProvider<TKey, TValue2>,
        mergeFn: (TKey, TValue?, TValue2?) -> TValueOut
    ): SortedKeyValueSequence<TKey, TValueOut> =
        zipByKey(other, FULL_OUTER_JOIN, mergeFn)

    /**
     * Performs a full outer zip with another sorted sequence using default pairing of values.
     *
     * Example:
     * ```
     * val seq1 = sequenceOf(1 to "a", 2 to "b").assertSorted()
     * val seq2 = sequenceOf(1 to "x", 3 to "z").assertSorted()
     * val result = seq1.fullOuterZipByKey(seq2)
     * // Results in: (1 to ("a" to "x"), 2 to ("b" to null), 3 to (null to "z"))
     * ```
     *
     * @param other The sequence to zip with
     * @return A new sorted sequence with paired values, keeping all keys from both sequences
     */
    fun <TValue2> fullOuterZipByKey(
        other: SortedKeyValueIteratorProvider<TKey, TValue2>
    ): SortedKeyValueSequence<TKey, Pair<TValue?, TValue2?>> = fullOuterZipByKey(other) { _, a, b -> a to b }

    /**
     * Performs an inner zip with another sorted sequence.
     *
     * Example:
     * ```
     * val seq1 = sequenceOf(1 to "a", 2 to "b").assertSorted()
     * val seq2 = sequenceOf(1 to "x", 3 to "z").assertSorted()
     * val result = seq1.innerZipByKey(seq2) { key, v1, v2 -> "$v1$v2" }
     * // Results in: (1 to "ax")
     * ```
     *
     * @param other The sequence to zip with
     * @param mergeFn Function that defines how to merge values for matching keys
     * @return A new sorted sequence with merged values for keys present in both sequences
     */
    fun <TValue2, TValueOut> innerZipByKey(
        other: SortedKeyValueIteratorProvider<TKey, TValue2>,
        mergeFn: (TKey, TValue, TValue2) -> TValueOut
    ): SortedKeyValueSequence<TKey, TValueOut> =
        zipByKey(other, INNER_JOIN) { key, a, b -> mergeFn(key, a!!, b!!) }

    /**
     * Performs an inner zip with another sorted sequence using default pairing of values.
     *
     * Example:
     * ```
     * val seq1 = sequenceOf(1 to "a", 2 to "b").assertSorted()
     * val seq2 = sequenceOf(1 to "x", 3 to "z").assertSorted()
     * val result = seq1.innerZipByKey(seq2)
     * // Results in: (1 to ("a" to "x"))
     * ```
     *
     * @param other The sequence to zip with
     * @return A new sorted sequence with paired values for keys present in both sequences
     */
    fun <TValue2> innerZipByKey(
        other: SortedKeyValueIteratorProvider<TKey, TValue2>
    ): SortedKeyValueSequence<TKey, Pair<TValue, TValue2>> = innerZipByKey(other) { _, a, b -> a to b }

    /**
     * Performs a left outer zip with another sorted sequence.
     *
     * Example:
     * ```
     * val seq1 = sequenceOf(1 to "a", 2 to "b").assertSorted()
     * val seq2 = sequenceOf(1 to "x", 3 to "z").assertSorted()
     * val result = seq1.leftOuterZipByKey(seq2) { key, v1, v2 -> "${v1 ?: ""}$v2" }
     * // Results in: (1 to "ax", 2 to "b")
     * ```
     *
     * @param other The sequence to zip with
     * @param mergeFn Function that defines how to merge values for matching keys
     * @return A new sorted sequence with merged values, keeping all keys from this sequence
     */
    fun <TValue2, TValueOut> leftOuterZipByKey(
        other: SortedKeyValueIteratorProvider<TKey, TValue2>,
        mergeFn: (TKey, TValue, TValue2?) -> TValueOut
    ): SortedKeyValueSequence<TKey, TValueOut> =
        zipByKey(other, LEFT_OUTER_JOIN) { key, a, b -> mergeFn(key, a!!, b) }

    /**
     * Performs a left outer zip with another sorted sequence using default pairing of values.
     *
     * Example:
     * ```
     * val seq1 = sequenceOf(1 to "a", 2 to "b").assertSorted()
     * val seq2 = sequenceOf(1 to "x", 3 to "z").assertSorted()
     * val result = seq1.leftOuterZipByKey(seq2)
     * // Results in: (1 to ("a" to "x"), 2 to ("b" to null))
     * ```
     *
     * @param other The sequence to zip with
     * @return A new sorted sequence with paired values, keeping all keys from this sequence
     */
    fun <TValue2> leftOuterZipByKey(
        other: SortedKeyValueIteratorProvider<TKey, TValue2>
    ): SortedKeyValueSequence<TKey, Pair<TValue, TValue2?>> = leftOuterZipByKey(other) { _, a, b -> a to b }

    /**
     * Performs a right outer zip with another sorted sequence.
     *
     * Example:
     * ```
     * val seq1 = sequenceOf(1 to "a", 2 to "b").assertSorted()
     * val seq2 = sequenceOf(1 to "x", 3 to "z").assertSorted()
     * val result = seq1.rightOuterZipByKey(seq2) { key, v1, v2 -> "$v1${v2 ?: ""}" }
     * // Results in: (1 to "ax", 3 to "z")
     * ```
     *
     * @param other The sequence to zip with
     * @param mergeFn Function that defines how to merge values for matching keys
     * @return A new sorted sequence with merged values, keeping all keys from other sequence
     */
    fun <TValue2, TValueOut> rightOuterZipByKey(
        other: SortedKeyValueIteratorProvider<TKey, TValue2>,
        mergeFn: (TKey, TValue?, TValue2) -> TValueOut
    ): SortedKeyValueSequence<TKey, TValueOut> =
        zipByKey(other, RIGHT_OUTER_JOIN) { key, a, b -> mergeFn(key, a, b!!) }

    /**
     * Performs a right outer zip with another sorted sequence using default pairing of values.
     *
     * Example:
     * ```
     * val seq1 = sequenceOf(1 to "a", 2 to "b").assertSorted()
     * val seq2 = sequenceOf(1 to "x", 3 to "z").assertSorted()
     * val result = seq1.rightOuterZipByKey(seq2)
     * // Results in: (1 to ("a" to "x"), 3 to (null to "z"))
     * ```
     *
     * @param other The sequence to zip with
     * @return A new sorted sequence with paired values, keeping all keys from other sequence
     */
    fun <TValue2> rightOuterZipByKey(
        other: SortedKeyValueIteratorProvider<TKey, TValue2>
    ): SortedKeyValueSequence<TKey, Pair<TValue?, TValue2?>> = rightOuterZipByKey(other) { _, a, b -> a to b }

    // Applies a transformation to the sequence into another sequence, assuming the sort order is preserved.
    private fun <TValue2> withSortingPreserved(
        operation: (Sequence<Pair<TKey, TValue>>) -> Sequence<Pair<TKey, TValue2>>
    ): SortedKeyValueSequence<TKey, TValue2> = operation(this).assumeSorted(sortOrder)

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
}
