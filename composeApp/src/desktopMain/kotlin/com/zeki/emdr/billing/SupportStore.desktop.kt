package com.zeki.emdr.billing

actual fun createSupportStore(): SupportStore = DesktopSupportStore()

private class DesktopSupportStore : SupportStore {
    override suspend fun connect() = Unit

    override suspend fun products(): List<SupportProduct> =
        SupportTip.entries.map { SupportProduct(it, it.fallbackPrice) }

    override suspend fun purchase(tip: SupportTip): SupportPurchaseResult =
        SupportPurchaseResult.Unavailable
}
