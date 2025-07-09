# Kotlin Sorted Sequences

[![Gradle build](https://github.com/timvanoijen/sorted-sequence/actions/workflows/gradle.yml/badge.svg)](https://github.com/timvanoijen/sorted-sequence/actions/workflows/gradle.yml)

A Kotlin library providing efficient streaming operations on sorted sequences. This library enables operations that are only possible in a streaming fashion when working with sorted data.

For all details, see the [API-docs](https://timvanoijen.github.io/sorted-sequence)

## Installation

Add the dependency to your project:

```kotlin
// build.gradle.kts
dependencies {
    implementation("io.github.timvanoijen.kotlin:sorted-sequence:<latest version>")
}
```

## Introduction

While regular Kotlin sequences process elements one by one without knowledge of the full sequence, some operations require the sequence to be sorted to work efficiently in a streaming manner. This library provides specialized wrappers and operations for sorted sequences, enabling efficient:

- Deduplication
- Range filtering
- Frequency counting
- Various types of zip-/join-merges (inner, left outer, right outer, full outer)
- Grouping by key

## Core Components

The library provides two main sequence types that serve different purposes:

- `SortedSequence`: a sequence of elements. The sort key is implicit - derived from the element itself. Operations on
  this type must preserve the sorting order of the derived keys, limiting the kinds of transformations possible.
- `SortedKeyValueSequence`: a sequence of explicit key-value pairs, where the keys determine the sort order. Since keys 
  and values are separate, values can be freely transformed while maintaining the sort order defined by the keys.

### SortedSequence

A sequence wrapper that asserts its elements are sorted according to their natural order or a provided comparator.

#### Creating a SortedSequence

```kotlin
// From a sequence of comparable elements (natural order)
val naturalOrder = sequenceOf(1, 2, 3, 4, 5).assertSorted()

// From a sequence with a key selector
val byFirstChar = sequenceOf("apple", "banana", "cherry").assertSortedBy { it.first() }

// With descending order
val descending = sequenceOf(5, 4, 3, 2, 1).assertSorted(SortOrder.DESCENDING)
```

### SortedKeyValueSequence

A specialized sequence for key-value pairs sorted by their keys.

#### Creating a SortedKeyValueSequence

```kotlin
// From a sequence of pairs
val keyValues = sequenceOf(1 to "a", 2 to "b", 3 to "c").assertSorted()

// Convert from SortedSequence
val sequence = sequenceOf(1, 2, 3).assertSorted()
val keyValueSeq = sequence.asSortedKeyValues()
```

## Mapping Values

Only implemented for `SortedKeyValueSequence`:

```kotlin
val sequence = sequenceOf(1 to "a", 2 to "b").assertSorted()
val upperCase = sequence.mapValues { it.uppercase() }
// Results in: [(1 to "A"), (2 to "B")]
```

## Filtering

Sorted sequences can be filtered by key and by value, while preserving their sort order:

```kotlin
val sequence = sequenceOf("a1", "b2", "c3").assertSortedBy { it.first() }

// Filter by key
val filteredByKey = sequence.filterByKey { it == 'b' }
// Results in: ["b2"]

// Filter by value
val filteredByValue = sequence.filterByValue { it.last() > '1' }
// Results in: ["b2", "c3"]
```

## Grouping

Implemented for both `SortedSequence` and `SortedKeyValueSequence`:

```kotlin
val sequence = sequenceOf(1, 1, 1, 2, 2, 3, 3).assertSorted()
val grouped = sequence.groupByKey().toList()
// Results in: [[1, 1, 1], [2, 2], [3, 3]]
```

## Distinct (deduplication)

Implemented for both `SortedSequence` and `SortedKeyValueSequence`, without the need to keep all previously seen elements in memory:

```kotlin
val sequence = sequenceOf(2, 1, 1).assertSorted(DESCENDING)
val distinct = sequence.distinctByKey()
// Results in: [2, 1]
```

## Zip Operations

The library provides efficient streaming zip operations between `SortedSequence` and `SortedKeyValueSequence`.
Zip operations differ from join operations in their handling of duplicate keys. While join operations create a cartesian
product for matching keys, zip operations maintain a one-to-one correspondence between the input sequences.

### Full Outer Zip 

Keeps all keys from both sequences.

```kotlin
val seq1 = sequenceOf("a1", "b2").assertSortedBy { it.first() }
val seq2 = sequenceOf("b3", "c4").assertSortedBy { it.first() }

// With default pairing
val result = seq1.fullOuterZipByKey(seq2)
// Results in: [("a1" to null), ("b2" to "b3"), (null to "c4")]

// With custom merge function
val result = seq1.fullOuterZipByKey(seq2) { key, v1, v2 -> "${v1 ?: ""}${v2 ?: ""}" }
// Results in: ["a1", "b2b3", "c4"]
```

### Inner Zip 

Only keeps keys present in both sequences.

```kotlin
val seq1 = sequenceOf("a1", "b2").assertSortedBy { it.first() }
val seq2 = sequenceOf("b3", "c4").assertSortedBy { it.first() }

// With default pairing
val result = seq1.innerZipByKey(seq2)
// Results in: [("b2" to "b3")]

// With custom merge function
val result = seq1.innerZipByKey(seq2) { key, v1, v2 -> "$v1$v2" }
// Results in: ["b2b3"]
```

### Left Outer Zip 

Keeps all keys from the first sequence.

```kotlin
val seq1 = sequenceOf("a1", "b2").assertSortedBy { it.first() }
val seq2 = sequenceOf("b3", "c4").assertSortedBy { it.first() }

// With default pairing
val result = seq1.leftOuterZipByKey(seq2)
// Results in: [("a1" to null), ("b2" to "b3")]

// With custom merge function
val result = seq1.leftOuterZipByKey(seq2) { key, v1, v2 -> "${v1}${v2 ?: ""}" }
// Results in: ["a1", "b2b3"]
```

### Right Outer Zip 

Keeps all keys from the second sequence.

```kotlin
val seq1 = sequenceOf("a1", "b2").assertSortedBy { it.first() }
val seq2 = sequenceOf("b3", "c4").assertSortedBy { it.first() }

// With default pairing
val result = seq1.rightOuterZipByKey(seq2)
// Results in: [("b2" to "b3"), (null to "c4")]

// With custom merge function
val result = seq1.rightOuterZipByKey(seq2) { key, v1, v2 -> "${v1 ?: ""}$v2" }
// Results in: ["b2b3", "c4"]
```

## Join Operations

The library also provides efficient streaming join operations between `SortedSequence` and `SortedKeyValueSequence`.
Unlike zip operations, join operations create a cartesian product for matching keys, combining each value from the first sequence with each value from the second sequence that shares the same key.

### Full Outer Join

Keeps all keys from both sequences, creating combinations of all matching values.

```kotlin
val seq1 = sequenceOf("1a", "2b", "2c").assertSortedBy { it.first() }
val seq2 = sequenceOf("2x", "2y", "3z").assertSortedBy { it.first() }

// With default pairing
val result = seq1.fullOuterJoinByKey(seq2)
// Results in: [
//   ("1a" to null),
//   ("2b" to "2x"),
//   ("2b" to "2y"),
//   ("2c" to "2x"),
//   ("2c" to "2y"),
//   (null to "3z")
// ]

// With custom merge function
val result = seq1.fullOuterJoinByKey(seq2) { _, v1, v2 -> "${v1 ?: ""}${v2 ?: ""}" }
// Results in: ["1a", "2b2x", "2b2y", "2c2x", "2c2y", "3z"]
```

### Inner Join

Only keeps keys present in both sequences, creating combinations of all matching values.

```kotlin
val seq1 = sequenceOf("1a", "2b", "2c").assertSortedBy { it.first() }
val seq2 = sequenceOf("2x", "2y", "3z").assertSortedBy { it.first() }

// With default pairing
val result = seq1.innerJoinByKey(seq2)
// Results in: [
//   ("2b" to "2x"),
//   ("2b" to "2y"),
//   ("2c" to "2x"),
//   ("2c" to "2y")
// ]

// With custom merge function
val result = seq1.innerJoinByKey(seq2) { _, v1, v2 -> "$v1$v2" }
// Results in: ["2b2x", "2b2y", "2c2x", "2c2y"]
```

### Left Outer Join

Keeps all keys from the first sequence, creating combinations with matching values from the second sequence.

```kotlin
val seq1 = sequenceOf("1a", "2b", "2c").assertSortedBy { it.first() }
val seq2 = sequenceOf("2x", "2y", "3z").assertSortedBy { it.first() }

// With default pairing
val result = seq1.leftOuterJoinByKey(seq2)
// Results in: [
//   ("1a" to null),
//   ("2b" to "2x"),
//   ("2b" to "2y"),
//   ("2c" to "2x"),
//   ("2c" to "2y")
// ]

// With custom merge function
val result = seq1.leftOuterJoinByKey(seq2) { _, v1, v2 -> "$v1${v2 ?: ""}" }
// Results in: ["1a", "2b2x", "2b2y", "2c2x", "2c2y"]
```

### Right Outer Join

Keeps all keys from the second sequence, creating combinations with matching values from the first sequence.

```kotlin
val seq1 = sequenceOf("1a", "2b", "2c").assertSortedBy { it.first() }
val seq2 = sequenceOf("2x", "2y", "3z").assertSortedBy { it.first() }

// With default pairing
val result = seq1.rightOuterJoinByKey(seq2)
// Results in: [
//   ("2b" to "2x"),
//   ("2b" to "2y"),
//   ("2c" to "2x"),
//   ("2c" to "2y"),
//   (null to "3z")
// ]

// With custom merge function
val result = seq1.rightOuterJoinByKey(seq2) { _, v1, v2 -> "${v1 ?: ""}$v2" }
// Results in: ["2b2x", "2b2y", "2c2x", "2c2y", "3z"]
```

## Interleaving

A `SortedSequence` or `SortedKeyValueSequence` can be interleaved with another one. 
This operation combines elements from both sequences, preserving their relative ordering based on keys. 
When the same key exists in both sequences, both values are included in the result.

```kotlin
val seq1 = sequenceOf("a1", "b2", "b4").assertSortedBy { it.first() }
val seq2 = sequenceOf("b3", "c4").assertSortedBy { it.first() }
val interleaved = seq1.interleaveByKey(seq2)
// Results in: ["a1", "b2", "b3", "b4", "c4"]
```

## Error Handling

The library verifies that sequences are properly sorted and throws exceptions if they're not:

```kotlin
// This will throw SortedSequenceException.SequenceNotSortedException
val notSorted = sequenceOf(3, 1, 2).assertSorted()

// This will throw SortedSequenceException.InvalidSortOrderException
val seq1 = sequenceOf(1, 2, 3).assertSorted()
val seq2 = sequenceOf(3, 2, 1).assertSorted(SortOrder.DESCENDING)
seq1.zipByKey(seq2)
```

## License

This library is available under the [LICENSE](LICENSE) included in the repository.
