package org.freedomwave.data.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SubscriptionTemplatesResponse(
    @SerialName("response") val response: SubscriptionTemplatesData,
)

@Serializable
data class SubscriptionTemplatesData(
    @SerialName("total")     val total: Int = 0,
    @SerialName("templates") val templates: List<SubscriptionTemplateDto> = emptyList(),
)

@Serializable
data class SubscriptionTemplateDto(
    @SerialName("uuid")         val uuid: String,
    @SerialName("name")         val name: String,
    @SerialName("templateType") val templateType: String,
)
