package com.zeki.emdr.billing

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

/**
 * Swift [EmdrStoreKit2] registers hosts that talk to StoreKit 2.
 * Kotlin only receives local outcome codes — never transaction IDs or Apple IDs.
 */
fun interface IosStoreKit2Host {
    fun purchase(sku: String, done: (String) -> Unit)
}

fun interface IosStoreKit2Catalog {
    fun prices(skus: List<String>, done: (Map<String, String>) -> Unit)
}

fun interface IosStoreKit2Restorer {
    fun finishOutstanding(skus: List<String>, done: (Boolean) -> Unit)
}

object IosStoreKit2 {
    var purchaseHost: IosStoreKit2Host? = null
    var catalog: IosStoreKit2Catalog? = null
    var restorer: IosStoreKit2Restorer? = null
}

actual fun createSupportStore(): SupportStore = IosSupportStore()

private class IosSupportStore : SupportStore {
    override suspend fun connect() = Unit

    override suspend fun finishOutstanding(): Boolean {
        val restorer = IosStoreKit2.restorer ?: return false
        return suspendCancellableCoroutine { cont ->
            restorer.finishOutstanding(SupportTip.skus) { any ->
                if (cont.isActive) cont.resume(any)
            }
        }
    }

    override suspend fun products(): List<SupportProduct> {
        val catalog = IosStoreKit2.catalog ?: return fallback()
        val loaded = suspendCancellableCoroutine { cont ->
            catalog.prices(SupportTip.skus) { map ->
                if (cont.isActive) cont.resume(map)
            }
        }
        return SupportTip.entries.map { tip ->
            SupportProduct(tip, loaded[tip.sku] ?: tip.fallbackPrice)
        }
    }

    override suspend fun purchase(tip: SupportTip): SupportPurchaseResult =
        withContext<SupportPurchaseResult>(Dispatchers.Main.immediate) {
            val host = IosStoreKit2.purchaseHost ?: return@withContext SupportPurchaseResult.Unavailable
            val code = suspendCancellableCoroutine { cont ->
                host.purchase(tip.sku) { outcome ->
                    if (cont.isActive) cont.resume(outcome)
                }
            }
            when (code) {
                "success" -> SupportPurchaseResult.Success
                "cancelled", "pending" -> SupportPurchaseResult.Cancelled
                "unavailable" -> SupportPurchaseResult.Unavailable
                else -> SupportPurchaseResult.Failed
            }
        }

    private fun fallback(): List<SupportProduct> =
        SupportTip.entries.map { SupportProduct(it, it.fallbackPrice) }
}
