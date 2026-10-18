package com.zeki.emdr.billing

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SupportTipTest {
    @Test
    fun mapsConsumableSkus() {
        assertEquals("emdr_tip_small", SupportTip.Small.sku)
        assertEquals("emdr_tip_medium", SupportTip.Medium.sku)
        assertEquals("emdr_tip_large", SupportTip.Large.sku)
        assertEquals("€1.99", SupportTip.Small.fallbackPrice)
        assertEquals("€4.99", SupportTip.Medium.fallbackPrice)
        assertEquals("€9.99", SupportTip.Large.fallbackPrice)
        assertEquals(
            listOf("emdr_tip_small", "emdr_tip_medium", "emdr_tip_large"),
            SupportTip.skus,
        )
        assertEquals(SupportTip.Medium, SupportTip.fromSku("emdr_tip_medium"))
        assertNull(SupportTip.fromSku("unknown"))
    }

    @Test
    fun purchaseResultsCarryNoPayload() {
        assertEquals("Success", SupportPurchaseResult.Success.toString())
        assertEquals("Failed", SupportPurchaseResult.Failed.toString())
    }
}
