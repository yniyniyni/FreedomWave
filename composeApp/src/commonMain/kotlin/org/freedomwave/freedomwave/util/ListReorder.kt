package org.freedomwave.util

/** Move the item at [from] to [to], returning a new list. Returns the original list unchanged
 *  when either index is out of range or they are equal. */
fun <T> reorderList(list: List<T>, from: Int, to: Int): List<T> {
    if (from !in list.indices || to !in list.indices || from == to) return list
    return list.toMutableList().apply { add(to, removeAt(from)) }
}
