package org.freedomwave

import org.freedomwave.domain.model.Host
import org.freedomwave.ui.feature.hosts.HostStatusKind
import org.freedomwave.ui.feature.hosts.hostStatusKind
import kotlin.test.Test
import kotlin.test.assertEquals

class HostStatusKindTest {

    private fun host(disabled: Boolean, hidden: Boolean) = Host(
        uuid = "u", remark = "r", address = "a", port = 443, path = null, sni = null,
        host = null, alpn = null, fingerprint = null, isDisabled = disabled, isHidden = hidden,
        securityLayer = "DEFAULT", tag = null, serverDescription = null, allowInsecure = false,
        overrideSniFromAddress = false, keepSniBlank = false, shuffleHost = false,
        mihomoX25519 = false, nodes = emptyList(), configProfileUuid = null,
        configProfileInboundUuid = null, vlessRouteId = null, xrayJsonTemplateUuid = null,
    )

    @Test fun `disabled wins over hidden`() {
        assertEquals(HostStatusKind.DISABLED, hostStatusKind(host(disabled = true, hidden = true)))
    }

    @Test fun `disabled only is DISABLED`() {
        assertEquals(HostStatusKind.DISABLED, hostStatusKind(host(disabled = true, hidden = false)))
    }

    @Test fun `hidden only is HIDDEN`() {
        assertEquals(HostStatusKind.HIDDEN, hostStatusKind(host(disabled = false, hidden = true)))
    }

    @Test fun `neither is ENABLED`() {
        assertEquals(HostStatusKind.ENABLED, hostStatusKind(host(disabled = false, hidden = false)))
    }
}
