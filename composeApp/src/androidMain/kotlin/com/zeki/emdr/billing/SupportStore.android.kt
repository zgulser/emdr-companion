package com.zeki.emdr.billing

import android.os.Handler
import android.os.Looper
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.zeki.emdr.haptics.AndroidHapticsHost
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume

actual fun createSupportStore(): SupportStore = AndroidSupportStore()

private class AndroidSupportStore : SupportStore, PurchasesUpdatedListener {
    private val client: BillingClient by lazy {
        val context = AndroidHapticsHost.context
            ?: error("Support store needs an application context")
        BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases(
                PendingPurchasesParams.newBuilder().enableOneTimeProducts().build(),
            )
            .enableAutoServiceReconnection()
            .build()
    }

    private val main = Handler(Looper.getMainLooper())
    private val connectLock = Mutex()
    private val details = mutableMapOf<String, ProductDetails>()
    private var pending: Continuation<SupportPurchaseResult>? = null
    @Volatile private var setupOk = false

    override suspend fun connect() {
        connectLock.withLock {
            if (client.isReady && setupOk) return
            setupOk = suspendCancellableCoroutine { cont ->
                client.startConnection(object : BillingClientStateListener {
                    override fun onBillingSetupFinished(result: BillingResult) {
                        val ok = result.responseCode == BillingClient.BillingResponseCode.OK
                        setupOk = ok
                        if (cont.isActive) cont.resume(ok)
                    }

                    override fun onBillingServiceDisconnected() {
                        setupOk = false
                    }
                })
            }
        }
    }

    override suspend fun finishOutstanding(): Boolean {
        connect()
        if (!setupOk) return false
        return suspendCancellableCoroutine { cont ->
            client.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder()
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build(),
            ) { result, purchases ->
                val ours = purchases.orEmpty().filter { it.isPurchasedTip() }
                if (result.responseCode != BillingClient.BillingResponseCode.OK || ours.isEmpty()) {
                    if (cont.isActive) cont.resume(false)
                    return@queryPurchasesAsync
                }
                var remaining = ours.size
                var any = false
                ours.forEach { purchase ->
                    consume(purchase) { ok ->
                        if (ok) any = true
                        remaining -= 1
                        if (remaining == 0 && cont.isActive) cont.resume(any)
                    }
                }
            }
        }
    }

    override suspend fun products(): List<SupportProduct> {
        connect()
        val queried = if (setupOk) queryDetails() else emptyList()
        details.clear()
        queried.forEach { details[it.productId] = it }
        return SupportTip.entries.map { tip ->
            val price = details[tip.sku]?.tipOffer()?.formattedPrice ?: tip.fallbackPrice
            SupportProduct(tip, price)
        }
    }

    override suspend fun purchase(tip: SupportTip): SupportPurchaseResult =
        withContext<SupportPurchaseResult>(Dispatchers.Main.immediate) {
            connect()
            if (!setupOk) return@withContext SupportPurchaseResult.Unavailable
            if (details[tip.sku] == null) products()
            val product = details[tip.sku] ?: return@withContext SupportPurchaseResult.Unavailable
            val offerToken = product.tipOffer()?.offerToken
                ?: return@withContext SupportPurchaseResult.Unavailable
            val activity = AndroidHapticsHost.activity
                ?: return@withContext SupportPurchaseResult.Unavailable
            suspendCancellableCoroutine { cont ->
                pending = cont
                cont.invokeOnCancellation { pending = null }
                val launched = client.launchBillingFlow(
                    activity,
                    BillingFlowParams.newBuilder()
                        .setIsOfferPersonalized(false)
                        .setProductDetailsParamsList(
                            listOf(
                                BillingFlowParams.ProductDetailsParams.newBuilder()
                                    .setProductDetails(product)
                                    .setOfferToken(offerToken)
                                    .build(),
                            ),
                        )
                        .build(),
                )
                if (launched.responseCode != BillingClient.BillingResponseCode.OK && cont.isActive) {
                    pending = null
                    when (launched.responseCode) {
                        BillingClient.BillingResponseCode.USER_CANCELED ->
                            cont.resume(SupportPurchaseResult.Cancelled)
                        BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED ->
                            cont.resume(SupportPurchaseResult.Success)
                        BillingClient.BillingResponseCode.ITEM_UNAVAILABLE,
                        BillingClient.BillingResponseCode.BILLING_UNAVAILABLE,
                        BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE,
                        -> cont.resume(SupportPurchaseResult.Unavailable)
                        else -> cont.resume(SupportPurchaseResult.Failed)
                    }
                }
            }.let { result ->
                if (result == SupportPurchaseResult.Success) {
                    finishOutstanding()
                }
                result
            }
        }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        val cont = pending ?: return
        pending = null
        when (result.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                val purchase = purchases.orEmpty().firstOrNull { it.isPurchasedTip() }
                when {
                    purchase != null -> consume(purchase) { ok ->
                        finish(cont, if (ok) SupportPurchaseResult.Success else SupportPurchaseResult.Failed)
                    }
                    purchases.orEmpty().any { it.purchaseState == Purchase.PurchaseState.PENDING } ->
                        finish(cont, SupportPurchaseResult.Cancelled)
                    else -> finish(cont, SupportPurchaseResult.Failed)
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED ->
                finish(cont, SupportPurchaseResult.Cancelled)
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED ->
                finish(cont, SupportPurchaseResult.Success)
            else -> finish(cont, SupportPurchaseResult.Failed)
        }
    }

    private fun consume(purchase: Purchase, done: (Boolean) -> Unit) {
        client.consumeAsync(
            ConsumeParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build(),
        ) { result, _ ->
            done(result.responseCode == BillingClient.BillingResponseCode.OK)
        }
    }

    private fun finish(cont: Continuation<SupportPurchaseResult>, value: SupportPurchaseResult) {
        main.post {
            try {
                cont.resume(value)
            } catch (_: IllegalStateException) {
            }
        }
    }

    private suspend fun queryDetails(): List<ProductDetails> =
        suspendCancellableCoroutine { cont ->
            val products = SupportTip.skus.map { sku ->
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(sku)
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build()
            }
            client.queryProductDetailsAsync(
                QueryProductDetailsParams.newBuilder().setProductList(products).build(),
            ) { result, queryResult ->
                if (cont.isActive) {
                    cont.resume(
                        if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                            queryResult.productDetailsList
                        } else {
                            emptyList()
                        },
                    )
                }
            }
        }
}

private fun ProductDetails.tipOffer(): ProductDetails.OneTimePurchaseOfferDetails? =
    oneTimePurchaseOfferDetailsList
        ?.firstOrNull { !it.offerToken.isNullOrEmpty() }
        ?: oneTimePurchaseOfferDetails

private fun Purchase.isPurchasedTip(): Boolean =
    purchaseState == Purchase.PurchaseState.PURCHASED &&
        products.any { sku -> SupportTip.fromSku(sku) != null }
