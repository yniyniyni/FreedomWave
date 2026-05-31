package art.yniyniyni.freedomwave.domain.model

import art.yniyniyni.freedomwave.data.api.dto.HostDto

data class Host(
    val uuid: String,
    val remark: String,
    val address: String,
    val port: Int,
    val path: String?,
    val sni: String?,
    val host: String?,
    val alpn: String?,
    val fingerprint: String?,
    val isDisabled: Boolean,
    val isHidden: Boolean,
    val securityLayer: String,
    val tag: String?,
    val serverDescription: String?,
    val allowInsecure: Boolean,
    val overrideSniFromAddress: Boolean,
    val keepSniBlank: Boolean,
    val shuffleHost: Boolean,
    val nodes: List<String>
) {
    companion object {
        fun from(dto: HostDto) = Host(
            uuid                 = dto.uuid,
            remark               = dto.remark,
            address              = dto.address,
            port                 = dto.port,
            path                 = dto.path,
            sni                  = dto.sni,
            host                 = dto.host,
            alpn                 = dto.alpn,
            fingerprint          = dto.fingerprint,
            isDisabled           = dto.isDisabled,
            isHidden             = dto.isHidden,
            securityLayer        = dto.securityLayer,
            tag                  = dto.tag,
            serverDescription    = dto.serverDescription,
            allowInsecure        = dto.allowInsecure,
            overrideSniFromAddress = dto.overrideSniFromAddress,
            keepSniBlank         = dto.keepSniBlank,
            shuffleHost          = dto.shuffleHost,
            nodes                = dto.nodes
        )
    }
}
