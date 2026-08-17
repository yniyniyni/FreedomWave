package org.freedomwave.domain.model

import org.freedomwave.data.api.dto.HwidDeviceDto

data class HwidDevice(
    val hwid: String,
    /** Owner identity as this panel reports it — a uuid on 2.8.x, a numeric id on 3.x. */
    val ownerRef: String,
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
            ownerRef    = dto.ownerRef,
            platform    = dto.platform,
            osVersion   = dto.osVersion,
            deviceModel = dto.deviceModel,
            userAgent   = dto.userAgent,
            createdAt   = dto.createdAt,
            updatedAt   = dto.updatedAt
        )
    }
}
