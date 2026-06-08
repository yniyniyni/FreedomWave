package art.yniyniyni.freedomwave.domain.model

import art.yniyniyni.freedomwave.data.api.dto.HwidDeviceDto

data class HwidDevice(
    val hwid: String,
    val userUuid: String,
    val platform: String?,
    val osVersion: String?,
    val deviceModel: String?,
    val userAgent: String?,
    val createdAt: String,
    val updatedAt: String
) {
    companion object {
        fun from(dto: HwidDeviceDto) = HwidDevice(
            hwid        = dto.hwid,
            userUuid    = dto.userUuid,
            platform    = dto.platform,
            osVersion   = dto.osVersion,
            deviceModel = dto.deviceModel,
            userAgent   = dto.userAgent,
            createdAt   = dto.createdAt,
            updatedAt   = dto.updatedAt
        )
    }
}
