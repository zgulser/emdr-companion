package com.zeki.emdr.billing

/**
 * Consumable supporter tips. SKUs are product IDs only — never user identifiers.
 *
 * Native stores are reached through [SupportStore] expect/actual (Play Billing on
 * Android, StoreKit 2 on iOS). No cloud IAP SDK and no account, email, or device
 * identifier is collected or transmitted.
 */
enum class SupportTip(
    val sku: String,
    val fallbackPrice: String,
) {
    Small("emdr_tip_small", "€1.99"),
    Medium("emdr_tip_medium", "€4.99"),
    Large("emdr_tip_large", "€9.99");

    companion object {
        val skus: List<String> = entries.map { it.sku }

        fun fromSku(sku: String): SupportTip? = entries.firstOrNull { it.sku == sku }
    }
}

data class SupportProduct(
    val tip: SupportTip,
    val priceLabel: String,
)

sealed interface SupportPurchaseResult {
    data object Success : SupportPurchaseResult
    data object Cancelled : SupportPurchaseResult
    data object Unavailable : SupportPurchaseResult
    data object Failed : SupportPurchaseResult
}

interface SupportStore {
    suspend fun connect()
    suspend fun products(): List<SupportProduct>
    suspend fun purchase(tip: SupportTip): SupportPurchaseResult
    suspend fun finishOutstanding(): Boolean = false
}

expect fun createSupportStore(): SupportStore

/**
 * Android activity resume hook so leftover Play purchases are consumed
 * without sending any identifiers off-device.
 */
object SupportForeground {
    var onResume: (() -> Unit)? = null
}
