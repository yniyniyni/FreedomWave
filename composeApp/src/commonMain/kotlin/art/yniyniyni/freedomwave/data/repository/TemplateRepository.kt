package art.yniyniyni.freedomwave.data.repository

import art.yniyniyni.freedomwave.data.api.service.TemplateService
import art.yniyniyni.freedomwave.data.store.AppPreferences
import art.yniyniyni.freedomwave.domain.model.SubscriptionTemplate
import art.yniyniyni.freedomwave.domain.model.XrayTemplate

class TemplateRepository(
    private val service: TemplateService,
    private val prefs: AppPreferences,
) {
    suspend fun getXrayTemplates(): Result<List<XrayTemplate>> = runCatching {
        service.getTemplates(prefs.getServerUrl()).response.templates
            .filter { it.templateType == "XRAY_JSON" }
            .map { XrayTemplate(it.uuid, it.name) }
    }.also { it.clearOnUnauthorized(prefs) }

    suspend fun getTemplatesByType(): Result<Map<String, List<SubscriptionTemplate>>> = runCatching {
        service.getTemplates(prefs.getServerUrl()).response.templates
            .map { SubscriptionTemplate(it.uuid, it.name, it.templateType) }
            .groupBy { it.type }
    }.also { it.clearOnUnauthorized(prefs) }
}
