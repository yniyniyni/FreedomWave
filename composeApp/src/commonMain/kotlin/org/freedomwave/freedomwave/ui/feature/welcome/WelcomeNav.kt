package org.freedomwave.ui.feature.welcome

/**
 * Pure page-navigation logic for the welcome carousel, kept out of composables so it can be
 * unit-tested. Pages are 0-indexed; the last page (index pageCount-1) is the login form.
 */
object WelcomeNav {
    fun skipTarget(pageCount: Int): Int = pageCount - 1

    /** Skip is offered on every promo page, never on the login (last) page. */
    fun isSkipVisible(page: Int, pageCount: Int): Boolean = page < pageCount - 1

    /** Advance one page, clamped to the last page. */
    fun nextPage(page: Int, pageCount: Int): Int = minOf(page + 1, pageCount - 1)
}
