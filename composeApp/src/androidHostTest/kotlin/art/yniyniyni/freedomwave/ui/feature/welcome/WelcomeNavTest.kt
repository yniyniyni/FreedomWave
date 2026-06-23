package art.yniyniyni.freedomwave.ui.feature.welcome

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WelcomeNavTest {

    @Test
    fun skip_visible_on_promo_pages_only() {
        assertTrue(WelcomeNav.isSkipVisible(page = 0, pageCount = 3))
        assertTrue(WelcomeNav.isSkipVisible(page = 1, pageCount = 3))
        assertFalse(WelcomeNav.isSkipVisible(page = 2, pageCount = 3))
    }

    @Test
    fun skip_target_is_last_page() {
        assertEquals(2, WelcomeNav.skipTarget(pageCount = 3))
    }

    @Test
    fun next_page_advances_and_clamps() {
        assertEquals(1, WelcomeNav.nextPage(page = 0, pageCount = 3))
        assertEquals(2, WelcomeNav.nextPage(page = 1, pageCount = 3))
        assertEquals(2, WelcomeNav.nextPage(page = 2, pageCount = 3)) // clamp at last
    }
}
