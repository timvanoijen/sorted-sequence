# Kotlin Sorted Sequences

A Kotlin library providing efficient streaming operations on sorted sequences. This library enables operations that are only possible in a streaming fashion when working with sorted data.

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
- Various types of joins (inner, left outer, right outer, full outer)
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

## Grouping

Implemented for both `SortedSequence` and `SortedKeyValueSequence`:

```kotlin
// Group duplicate elements
val sequence = sequenceOf(1, 1, 1, 2, 2, 3, 3).assertSorted()
val grouped = sequence.groupByKey().toList()
// Results in: [(1, [1, 1, 1]), (2, [2, 2]), (3, [3, 3])]
```

## Join Operations

The library provides efficient streaming join operations between `SortedSequence` and `SortedKeyValueSequence`.

### Full Outer Join

Keeps all keys from both sequences.

```kotlin
val seq1 = sequenceOf("a1", "b2").assertSortedBy { it.first() }
val seq2 = sequenceOf("b3", "c4").assertSortedBy { it.first() }

// With default pairing
val joined = seq1.fullOuterJoinByKey(seq2)
// Results in: [("a1" to null), ("b2" to "b3"), (null to "c4")]

// With custom merge function
val merged = seq1.fullOuterJoinByKey(seq2) { key, v1, v2 -> "${v1 ?: ""}${v2 ?: ""}" }
// Results in: ["a1", "b2b3", "c4"]
```

### Inner Join

Only keeps keys present in both sequences.

```kotlin
val seq1 = sequenceOf("a1", "b2").assertSortedBy { it.first() }
val seq2 = sequenceOf("b3", "c4").assertSortedBy { it.first() }

// With default pairing
val joined = seq1.innerJoinByKey(seq2)
// Results in: [("b2" to "b3")]

// With custom merge function
val merged = seq1.innerJoinByKey(seq2) { key, v1, v2 -> "$v1$v2" }
// Results in: ["b2b3"]
```

### Left Outer Join

Keeps all keys from the first sequence.

```kotlin
val seq1 = sequenceOf("a1", "b2").assertSortedBy { it.first() }
val seq2 = sequenceOf("b3", "c4").assertSortedBy { it.first() }

// With default pairing
val joined = seq1.leftOuterJoinByKey(seq2)
// Results in: [("a1" to null), ("b2" to "b3")]

// With custom merge function
val merged = seq1.leftOuterJoinByKey(seq2) { key, v1, v2 -> "${v1}${v2 ?: ""}" }
// Results in: ["a1", "b2b3"]
```

### Right Outer Join

Keeps all keys from the second sequence.

```kotlin
val seq1 = sequenceOf("a1", "b2").assertSortedBy { it.first() }
val seq2 = sequenceOf("b3", "c4").assertSortedBy { it.first() }

// With default pairing
val joined = seq1.rightOuterJoinByKey(seq2)
// Results in: [("b2" to "b3"), (null to "c4")]

// With custom merge function
val merged = seq1.rightOuterJoinByKey(seq2) { key, v1, v2 -> "${v1 ?: ""}$v2" }
// Results in: ["b2b3", "c4"]
```

## Error Handling

The library verifies that sequences are properly sorted and throws exceptions if they're not:

```kotlin
// This will throw SortedSequenceException.SequenceNotSortedException
val notSorted = sequenceOf(3, 1, 2).assertSorted()

// This will throw SortedSequenceException.InvalidSortOrderException
val seq1 = sequenceOf(1, 2, 3).assertSorted()
val seq2 = sequenceOf(3, 2, 1).assertSorted(SortOrder.DESCENDING)
seq1.mergeByKey(seq2)
```

## License

This library is available under the [LICENSE](LICENSE) included in the repository.
