package io.timvanoijen.github.kotlin.sortedsequence

sealed class SortedSequenceException(
    message: String
) : Exception("Error while evaluating SortedSequence: $message") {

    class InvalidSortOrderException : SortedSequenceException("Invalid sort order")

    class SequenceNotSortedException : SortedSequenceException("Sequence is not sorted according to the SortOrder")
}
