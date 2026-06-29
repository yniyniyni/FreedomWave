package org.freedomwave

import org.freedomwave.util.reorderList
import kotlin.test.Test
import kotlin.test.assertEquals

class ReorderListTest {

    @Test fun `move item forward`() {
        assertEquals(listOf("b", "c", "a", "d"), reorderList(listOf("a", "b", "c", "d"), from = 0, to = 2))
    }

    @Test fun `move item backward`() {
        assertEquals(listOf("a", "d", "b", "c"), reorderList(listOf("a", "b", "c", "d"), from = 3, to = 1))
    }

    @Test fun `move to same index is unchanged`() {
        assertEquals(listOf("a", "b", "c"), reorderList(listOf("a", "b", "c"), from = 1, to = 1))
    }

    @Test fun `out-of-range indices return original list`() {
        assertEquals(listOf("a", "b"), reorderList(listOf("a", "b"), from = 0, to = 5))
        assertEquals(listOf("a", "b"), reorderList(listOf("a", "b"), from = -1, to = 1))
    }
}
