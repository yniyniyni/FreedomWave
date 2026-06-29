package org.freedomwave.data.repository

import org.freedomwave.data.api.service.TemplateService
import org.freedomwave.data.store.AppPreferences
import org.freedomwave.domain.model.SubscriptionTemplate
import org.freedomwave.domain.model.XrayTemplate

class TemplateRepository(
    private val service: TemplateService,
    private val prefs: AppPreferences,
) {
    suspend fun getXrayTemplates(): Result<List<XrayTemplate>> = runCatching {
        service.getTemplates().response.templates
            .filter { it.templateType == "XRAY_JSON" }
            .map { XrayTemplate(it.uuid, it.name) }
    }.clearOnUnauthorized(prefs)

    suspend fun getTemplatesByType(): Result<Map<String, List<SubscriptionTemplate>>> = runCatching {
        service.getTemplates().response.templates
            .map { SubscriptionTemplate(it.uuid, it.name, it.templateType) }
            .groupBy { it.type }
    }.clearOnUnauthorized(prefs)
}
