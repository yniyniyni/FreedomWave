package art.yniyniyni.freedomwave.data.api.dto

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HostInboundBody(
    @SerialName("configProfileUuid")        val configProfileUuid: String,
    @SerialName("configProfileInboundUuid") val configProfileInboundUuid: String,
)

/** POST /api/hosts. Optional fields default → encodeDefaults=false omits unset ones. */
@Serializable
data class CreateHostRequest(
    @SerialName("inbound")                val inbound: HostInboundBody,
    @SerialName("remark")                 val remark: String,
    @SerialName("address")                val address: String,
    @SerialName("port")                   val port: Int,
    @SerialName("path")                   val path: String? = null,
    @SerialName("sni")                    val sni: String? = null,
    @SerialName("host")                   val host: String? = null,
    @SerialName("alpn")                   val alpn: String? = null,
    @SerialName("fingerprint")            val fingerprint: String? = null,
    @SerialName("isDisabled")             val isDisabled: Boolean = false,
    @SerialName("securityLayer")          val securityLayer: String = "DEFAULT",
    @SerialName("serverDescription")      val serverDescription: String? = null,
    @SerialName("tag")                    val tag: String? = null,
    @SerialName("isHidden")               val isHidden: Boolean = false,
    @SerialName("overrideSniFromAddress") val overrideSniFromAddress: Boolean = false,
    @SerialName("keepSniBlank")           val keepSniBlank: Boolean = false,
    @SerialName("allowInsecure")          val allowInsecure: Boolean = false,
    @SerialName("vlessRouteId")           val vlessRouteId: Int? = null,
    @SerialName("shuffleHost")            val shuffleHost: Boolean = false,
    @SerialName("mihomoX25519")           val mihomoX25519: Boolean = false,
    @SerialName("nodes")                  val nodes: List<String>? = null,
    @SerialName("xrayJsonTemplateUuid")   val xrayJsonTemplateUuid: String? = null,
)

/** PATCH /api/hosts. Always-managed fields forced on with @EncodeDefault so a toggle
 *  flipped to false (== default) is still sent. Text/enum/id fields the user can leave
 *  unset stay nullable+default and are omitted (not sent as null). */
@OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
@Serializable
data class UpdateHostRequest(
    @SerialName("uuid")                                                              val uuid: String,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS) @SerialName("inbound")                 val inbound: HostInboundBody,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS) @SerialName("remark")                  val remark: String,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS) @SerialName("address")                 val address: String,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS) @SerialName("port")                    val port: Int,
    @SerialName("path")                                                              val path: String? = null,
    @SerialName("sni")                                                               val sni: String? = null,
    @SerialName("host")                                                              val host: String? = null,
    @SerialName("alpn")                                                              val alpn: String? = null,
    @SerialName("fingerprint")                                                       val fingerprint: String? = null,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS) @SerialName("isDisabled")             val isDisabled: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS) @SerialName("securityLayer")          val securityLayer: String = "DEFAULT",
    @SerialName("serverDescription")                                                val serverDescription: String? = null,
    @SerialName("tag")                                                               val tag: String? = null,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS) @SerialName("isHidden")               val isHidden: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS) @SerialName("overrideSniFromAddress") val overrideSniFromAddress: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS) @SerialName("keepSniBlank")           val keepSniBlank: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS) @SerialName("allowInsecure")          val allowInsecure: Boolean = false,
    @SerialName("vlessRouteId")                                                      val vlessRouteId: Int? = null,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS) @SerialName("shuffleHost")            val shuffleHost: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS) @SerialName("mihomoX25519")           val mihomoX25519: Boolean = false,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS) @SerialName("nodes")                  val nodes: List<String> = emptyList(),
    @SerialName("xrayJsonTemplateUuid")                                              val xrayJsonTemplateUuid: String? = null,
)
