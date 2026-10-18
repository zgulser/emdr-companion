package com.zeki.emdr.ui.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zeki.emdr.billing.SupportForeground
import com.zeki.emdr.billing.SupportPurchaseResult
import com.zeki.emdr.billing.SupportTip
import com.zeki.emdr.billing.createSupportStore
import com.zeki.emdr.data.SessionSettingsRepository
import com.zeki.emdr.model.SessionSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SupportViewModel(
    private val repository: SessionSettingsRepository,
) : ViewModel() {
    val settings: StateFlow<SessionSettings> = repository.state

    private val store = createSupportStore()
    private val resumeHook: () -> Unit = { viewModelScope.launch { hydrate() } }
    private val _prices = MutableStateFlow(SupportTip.entries.associateWith { it.fallbackPrice })
    val prices: StateFlow<Map<SupportTip, String>> = _prices.asStateFlow()

    private val _busy = MutableStateFlow<SupportTip?>(null)
    val busy: StateFlow<SupportTip?> = _busy.asStateFlow()

    private val _status = MutableStateFlow<SupportPurchaseResult?>(null)
    val status: StateFlow<SupportPurchaseResult?> = _status.asStateFlow()

    init {
        viewModelScope.launch { hydrate() }
        SupportForeground.onResume = resumeHook
    }

    override fun onCleared() {
        if (SupportForeground.onResume === resumeHook) {
            SupportForeground.onResume = null
        }
        super.onCleared()
    }

    private suspend fun hydrate() {
        store.connect()
        if (store.finishOutstanding()) {
            withContext(Dispatchers.Main.immediate) { acknowledgeSupport() }
        }
        val loaded = store.products()
        if (loaded.isNotEmpty()) {
            withContext(Dispatchers.Main.immediate) {
                _prices.value = SupportTip.entries.associateWith { tip ->
                    loaded.firstOrNull { it.tip == tip }?.priceLabel ?: tip.fallbackPrice
                }
            }
        }
    }

    fun purchase(tip: SupportTip) {
        if (_busy.value != null) return
        viewModelScope.launch(Dispatchers.Main.immediate) {
            _busy.value = tip
            val result = store.purchase(tip)
            _busy.value = null
            when (result) {
                SupportPurchaseResult.Success -> {
                    acknowledgeSupport()
                    _status.value = null
                }
                SupportPurchaseResult.Cancelled -> _status.value = null
                SupportPurchaseResult.Unavailable,
                SupportPurchaseResult.Failed,
                -> _status.value = result
            }
        }
    }

    private fun acknowledgeSupport() {
        repository.markSupported()
    }
}
