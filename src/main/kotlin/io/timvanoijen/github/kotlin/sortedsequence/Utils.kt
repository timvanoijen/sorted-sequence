package io.timvanoijen.github.kotlin.sortedsequence

internal val <A, B>Pair<A, B>.key: A get() = first

internal val <A, B>Pair<A, B>.value: B get() = second

internal fun <T>Iterator<T>.nextOrNull() = if (hasNext()) next() else null

internal fun <A, B> cartesianProduct(iterable1: Iterable<A>?, iterable2: Iterable<B>?): List<Pair<A?, B?>> {
    return (iterable1 ?: listOf(null)).flatMap { a -> (iterable2 ?: listOf(null)).map { b -> a to b } }
}
