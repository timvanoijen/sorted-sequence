package nl.timocode.kotlinutils.sortedsequence

interface SortedKeyValueIteratorProvider<TKey : Comparable<TKey>, TValue> {
    fun keyValueIterator(): Iterator<Pair<TKey, TValue>>

    val sortOrder: SortOrder
}
