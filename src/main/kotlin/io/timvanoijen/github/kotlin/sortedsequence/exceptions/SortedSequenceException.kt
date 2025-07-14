package io.timvanoijen.github.kotlin.sortedsequence.exceptions

sealed class SortedSequenceException(
    message: String
) : Exception("Error while evaluating SortedSequence: $message")
