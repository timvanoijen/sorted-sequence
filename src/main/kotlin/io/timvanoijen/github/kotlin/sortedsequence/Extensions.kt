package io.timvanoijen.github.kotlin.sortedsequence

import io.timvanoijen.github.kotlin.sortedsequence.SortedKeyValueSequence.Factory.assumeSorted

/*
 /**
  * This extension file contains functions that need to be implemented as extension functions rather than
  * class member functions to maintain type-safety. The TValue type parameter is declared as covariant
  * (out-type) in the sorted sequence classes, which prevents member functions from accepting parameters
  * of the same type. This matches Kotlin's built-in Sequence interface behavior. Extension functions
  * allow us to work around this constraint while preserving type-safety.
  */
 */

/**
 * Interleaves key-value pairs from this sequence with those from another sequence while maintaining sort order.
 *
 * This operation combines elements from both sequences, preserving their relative ordering based on keys.
 * When the same key exists in both sequences, both values are included in the result.
 *
 * @param other The sequence to interleave with this sequence
 * @return A new sorted sequence containing all elements from both sequences
 */
fun <TKey : Comparable<TKey>, TValue> SortedKeyValueSequence<TKey, TValue>.interleaveByKey(
    other: SortedKeyValueIteratorProvider<TKey, TValue>
): SortedKeyValueSequence<TKey, TValue> {
    return fullOuterJoinByKey(other) { _, a, b ->
        listOfNotNull(a, b)
    }.flatMap { (key, values) -> values.map { key to it } }.assumeSorted(sortOrder)
}

/**
 * Interleaves elements from this sequence with those from another sequence while maintaining sort order.
 *
 * This operation combines elements from both sequences, preserving their relative ordering based on keys.
 * When the same key exists in both sequences, both values are included in the result.
 *
 * @param other The sequence to interleave with this sequence
 * @return A new sorted sequence containing all elements from both sequences
 */
fun <TKey : Comparable<TKey>, TValue> SortedSequence<TKey, TValue>.interleaveByKey(
    other: SortedKeyValueIteratorProvider<TKey, TValue>
): SortedSequence<TKey, TValue> = SortedSequence(asSortedKeyValues().interleaveByKey(other))
