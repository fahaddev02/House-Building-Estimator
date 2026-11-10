package com.example.paintcalculatorestimator.billing

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingClient.ProductType
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryProductDetailsResult
import com.android.billingclient.api.QueryPurchasesParams
import com.example.paintcalculatorestimator.presentation.premium.PremiumPlan
import com.example.paintcalculatorestimator.utils.PrefUtil
import android.content.ActivityNotFoundException
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BillingUiState(
    val isConnected: Boolean = false,
    val isPremium: Boolean = false,
    val monthlyPrice: String? = null,
    val yearlyPrice: String? = null,
    val yearlyPerMonthPrice: String? = null,
    val purchaseEnabled: Boolean = false,
    val isProcessing: Boolean = false,
    val errorMessage: String? = null
)

class BillingManager(
    context: Context,
    private val monthlyProductId: String = PRODUCT_ID_MONTHLY,
    private val yearlyProductId: String = PRODUCT_ID_YEARLY,
    private val monthlyBasePlanId: String = BASE_PLAN_MONTHLY,
    private val yearlyBasePlanId: String = BASE_PLAN_YEARLY
) : PurchasesUpdatedListener {

    companion object {
        private const val TAG = "BillingManager"

        // Google Play subscription products and base plans.
        const val PRODUCT_ID_MONTHLY = "premium_monthly"
        const val PRODUCT_ID_YEARLY = "premium_yearly"
        const val BASE_PLAN_MONTHLY = "monthly-plan"
        const val BASE_PLAN_YEARLY = "yearly-plan"

        @Volatile
        private var INSTANCE: BillingManager? = null

        fun getInstance(context: Context): BillingManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: BillingManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    private val appContext = context.applicationContext
    private val prefUtil = PrefUtil(appContext)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    val entitlement = EntitlementStore.from(appContext)
    private var connecting = false
    private var purchaseQueryInFlight = false
    private var catalogQueryInFlight = false
    private var purchaseRevision = 0L
    private var purchaseQueryGeneration = 0L
    private var closed = false
    private val activeTokens = mutableSetOf<String>()
    private val acknowledgementsInFlight = mutableSetOf<String>()
    private val acknowledgedTokens = mutableSetOf<String>()
    private val acknowledgementRetries = mutableMapOf<String, Job>()

    private val _uiState = MutableStateFlow(
        BillingUiState(isPremium = prefUtil.getPremium())
    )
    val uiState: StateFlow<BillingUiState> = _uiState.asStateFlow()

    private var productDetailsMap: Map<String, ProductDetails> = emptyMap()

    private val billingClient: BillingClient = BillingClient.newBuilder(appContext)
        .setListener(this)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .enablePrepaidPlans()
                .build()
        )
        .enableAutoServiceReconnection()
        .build()

    init {
        startConnection()
    }

    /**
     * Connects to Google Play Billing service and queries active subscriptions + product details.
     */
    fun startConnection() {
        if (closed) return
        if (billingClient.isReady) {
            _uiState.update { it.copy(isConnected = true) }
            queryActivePurchases()
            querySubscriptionDetails()
            return
        }
        if (connecting) return
        connecting = true
        entitlement.beginRefresh()
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                connecting = false
                if (closed) return
                if (billingResult.responseCode == BillingResponseCode.OK) {
                    Log.d(TAG, "BillingClient setup finished successfully")
                    _uiState.update { it.copy(isConnected = true, errorMessage = null) }
                    queryActivePurchases()
                    querySubscriptionDetails()
                } else {
                    Log.e(TAG, "Billing setup failed: ${billingResult.debugMessage}")
                    _uiState.update {
                        it.copy(
                            isConnected = false,
                            purchaseEnabled = false,
                            isProcessing = false,
                            errorMessage = billingResult.debugMessage
                        )
                    }
                }
            }

            override fun onBillingServiceDisconnected() {
                purchaseQueryGeneration++
                connecting = false
                purchaseQueryInFlight = false
                catalogQueryInFlight = false
                Log.w(TAG, "Billing service disconnected")
                _uiState.update { it.copy(isConnected = false, purchaseEnabled = false) }
            }
        })
    }

    /**
     * Queries localized prices and offer tokens for Monthly and Yearly subscription plans.
     */
    fun querySubscriptionDetails() {
        if (closed || catalogQueryInFlight) return
        if (!billingClient.isReady) {
            startConnection()
            return
        }

        catalogQueryInFlight = true
        _uiState.update { it.copy(purchaseEnabled = false) }
        val productIds = setOf(monthlyProductId, yearlyProductId)
        val productList = productIds.map { productId ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(ProductType.SUBS)
                .build()
        }

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient.queryProductDetailsAsync(params) { billingResult: BillingResult, result: QueryProductDetailsResult ->
            catalogQueryInFlight = false
            if (closed) return@queryProductDetailsAsync
            if (billingResult.responseCode == BillingResponseCode.OK) {
                val detailsList = result.productDetailsList
                productDetailsMap = detailsList.associateBy { it.productId }

                val monthlyOffer = findOfferForPlan(PremiumPlan.MONTHLY)
                val yearlyOffer = findOfferForPlan(PremiumPlan.YEARLY)

                val monthlyPricingPhase = monthlyOffer?.pricingPhases?.pricingPhaseList?.lastOrNull()
                val yearlyPricingPhase = yearlyOffer?.pricingPhases?.pricingPhaseList?.lastOrNull()

                val monthlyFormattedPrice = monthlyPricingPhase?.formattedPrice
                val yearlyFormattedPrice = yearlyPricingPhase?.formattedPrice
                val yearlyPerMonthFormattedPrice = yearlyPricingPhase?.let { formatMonthlyEquivalent(it) }

                _uiState.update {
                    it.copy(
                        monthlyPrice = monthlyFormattedPrice,
                        yearlyPrice = yearlyFormattedPrice,
                        yearlyPerMonthPrice = yearlyPerMonthFormattedPrice,
                        purchaseEnabled = entitlement.state.value.isResolved && !entitlement.state.value.isPremium &&
                            (monthlyFormattedPrice != null || yearlyFormattedPrice != null),
                        errorMessage = when {
                            monthlyFormattedPrice == null && yearlyFormattedPrice == null -> "Subscription plans are currently unavailable. Please retry."
                            else -> it.errorMessage
                        }
                    )
                }
            } else {
                productDetailsMap = emptyMap()
                Log.e(TAG, "Failed to query product details: ${billingResult.debugMessage}")
                _uiState.update {
                    it.copy(monthlyPrice = null, yearlyPrice = null, yearlyPerMonthPrice = null,
                        purchaseEnabled = false, errorMessage = billingResult.debugMessage.ifBlank { "Unable to load subscription plans. Please retry." })
                }
            }
        }
    }

    /**
     * Queries active subscriptions from Google Play and syncs [PrefUtil] premium status.
     */
    fun queryActivePurchases() {
        if (closed || purchaseQueryInFlight) return
        entitlement.beginRefresh()
        _uiState.update { it.copy(purchaseEnabled = false) }
        if (!billingClient.isReady) { startConnection(); return }
        purchaseQueryInFlight = true
        val revision = purchaseRevision
        val queryGeneration = ++purchaseQueryGeneration

        val params = QueryPurchasesParams.newBuilder()
            .setProductType(ProductType.SUBS)
            .build()

        billingClient.queryPurchasesAsync(params) { billingResult, purchasesList ->
            if (queryGeneration != purchaseQueryGeneration) return@queryPurchasesAsync
            purchaseQueryInFlight = false
            if (closed || revision != purchaseRevision) return@queryPurchasesAsync
            scope.launch {
            if (revision != purchaseRevision || queryGeneration != purchaseQueryGeneration || closed) return@launch
            if (billingResult.responseCode == BillingResponseCode.OK) {
                handlePurchasesList(purchasesList, updateFalseIfEmpty = true)
            } else {
                Log.e(TAG, "queryPurchasesAsync failed: ${billingResult.debugMessage}")
                _uiState.update { it.copy(errorMessage = "Unable to refresh your subscription. Please retry.") }
            }
            }
        }
    }

    /**
     * Launches the Google Play purchase flow for the selected [PremiumPlan].
     */
    fun launchPurchaseFlow(activity: Activity, plan: PremiumPlan) {
        if (closed || _uiState.value.isProcessing) return
        if (!entitlement.state.value.isResolved || entitlement.state.value.isPremium) {
            reportError(if (entitlement.state.value.isPremium) "Your subscription is already active. Manage it from Settings."
                else "Checking your existing subscription. Please retry shortly.")
            if (!entitlement.state.value.isResolved) queryActivePurchases()
            return
        }
        if (!billingClient.isReady) {
            _uiState.update { it.copy(errorMessage = "Billing service not ready. Reconnecting…") }
            startConnection()
            return
        }

        val productDetails = findProductDetailsForPlan(plan)
        val offerDetails = findOfferForPlan(plan)

        if (productDetails == null || offerDetails == null) {
            Log.e(TAG, "ProductDetails or OfferToken not available for plan: $plan")
            _uiState.update {
                it.copy(
                    isProcessing = false,
                    errorMessage = "Subscription plan is currently unavailable."
                )
            }
            return
        }

        _uiState.update { it.copy(isProcessing = true, errorMessage = null) }

        val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(productDetails)
            .setOfferToken(offerDetails.offerToken)
            .build()

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productDetailsParams))
            .build()

        val result = billingClient.launchBillingFlow(activity, billingFlowParams)
        if (result.responseCode != BillingResponseCode.OK) {
            Log.e(TAG, "launchBillingFlow failed: ${result.debugMessage}")
            _uiState.update {
                it.copy(
                    isProcessing = false,
                    errorMessage = result.debugMessage
                )
            }
        }
    }

    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?
    ) {
        if (closed) return
        scope.launch {
        when (billingResult.responseCode) {
            BillingResponseCode.OK -> {
                if (!purchases.isNullOrEmpty()) {
                    purchaseRevision++ // Ignore purchase queries started before this newer payment event.
                    handlePurchasesList(purchases, updateFalseIfEmpty = false)
                } else {
                    _uiState.update { it.copy(isProcessing = false) }
                }
            }

            BillingResponseCode.USER_CANCELED -> {
                Log.d(TAG, "User canceled purchase")
                _uiState.update { it.copy(isProcessing = false) }
            }

            BillingResponseCode.ITEM_ALREADY_OWNED -> {
                Log.d(TAG, "Item already owned, refreshing active purchases")
                queryActivePurchases()
                _uiState.update { it.copy(isProcessing = false) }
            }

            else -> {
                Log.e(TAG, "onPurchasesUpdated error: ${billingResult.debugMessage}")
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        errorMessage = billingResult.debugMessage
                    )
                }
            }
        }
        }
    }

    private fun handlePurchasesList(purchases: List<Purchase>, updateFalseIfEmpty: Boolean) {
        // Use completed subscriptions returned by Google Play Billing; pending payments do not grant Premium.
        val owned = purchases.filter {
            it.purchaseState == Purchase.PurchaseState.PURCHASED &&
                it.packageName == appContext.packageName && it.purchaseToken.isNotBlank() &&
                it.products.any { product -> product == monthlyProductId || product == yearlyProductId }
        }
        if (updateFalseIfEmpty) {
            activeTokens.retainAll(owned.map { it.purchaseToken }.toSet())
            acknowledgedTokens.retainAll(activeTokens)
            acknowledgementRetries.keys.toList().filter { it !in activeTokens }.forEach {
                acknowledgementRetries.remove(it)?.cancel()
            }
        }
        if (owned.isNotEmpty()) {
            activeTokens.addAll(owned.map { it.purchaseToken })
            owned.filter { it.isAcknowledged }.forEach {
                acknowledgedTokens.add(it.purchaseToken)
                acknowledgementRetries.remove(it.purchaseToken)?.cancel()
            }
            updatePremiumStatus(true)
            owned.filterNot { it.isAcknowledged }.forEach { acknowledgePurchase(it) }
        } else if (updateFalseIfEmpty) {
            updatePremiumStatus(false)
        }
        if (owned.isEmpty()) {
            _uiState.update { it.copy(isProcessing = false) }
        }
    }

    private fun acknowledgePurchase(purchase: Purchase, attempt: Int = 0) {
        val token = purchase.purchaseToken
        if (closed || token !in activeTokens || token in acknowledgedTokens || !acknowledgementsInFlight.add(token)) return
        acknowledgementRetries.remove(token)?.cancel()
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        billingClient.acknowledgePurchase(params) { billingResult ->
            scope.launch {
            acknowledgementsInFlight.remove(token)
            if (closed || token !in activeTokens || token in acknowledgedTokens) return@launch
            if (billingResult.responseCode == BillingResponseCode.OK) {
                Log.d(TAG, "Purchase acknowledged successfully")
                acknowledgedTokens.add(token)
                // A late acknowledgement must never regrant an expired/revoked entitlement.
                _uiState.update { it.copy(errorMessage = if (it.errorMessage == "Your purchase is active. Confirmation will be retried automatically.") null else it.errorMessage) }
            } else {
                Log.e(TAG, "Failed to acknowledge purchase: ${billingResult.debugMessage}")
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        errorMessage = "Your purchase is active. Confirmation will be retried automatically."
                    )
                }
                acknowledgementRetries[token] = scope.launch {
                    delay((2_000L shl attempt.coerceAtMost(7)).coerceAtMost(300_000L))
                    acknowledgementRetries.remove(token)
                    if (token in activeTokens) {
                        if (billingClient.isReady) acknowledgePurchase(purchase, attempt + 1)
                        else startConnection()
                    }
                }
            }
            }
        }
    }

    private fun updatePremiumStatus(isPremium: Boolean) {
        scope.launch {
            prefUtil.setPremium(isPremium)
            _uiState.update {
                it.copy(
                    isPremium = isPremium,
                    isProcessing = false,
                    purchaseEnabled = !isPremium &&
                        (findOfferForPlan(PremiumPlan.MONTHLY) != null || findOfferForPlan(PremiumPlan.YEARLY) != null)
                )
            }
        }
    }

    /**
     * Divides the yearly subscription price by 12 and formats it in the user's currency.
     */
    private fun formatMonthlyEquivalent(pricingPhase: ProductDetails.PricingPhase): String {
        val monthlyAmount = (pricingPhase.priceAmountMicros / 1_000_000.0) / 12.0
        return try {
            val currency = Currency.getInstance(pricingPhase.priceCurrencyCode)
            val format = NumberFormat.getCurrencyInstance(Locale.getDefault()).apply {
                this.currency = currency
                maximumFractionDigits = if (monthlyAmount % 1.0 == 0.0) 0 else 1
                minimumFractionDigits = 0
            }
            format.format(monthlyAmount)
        } catch (e: Exception) {
            String.format(Locale.getDefault(), "%s %.1f", pricingPhase.priceCurrencyCode, monthlyAmount)
        }
    }

    /**
     * Resolves [ProductDetails] for [PremiumPlan], supporting either separate product IDs
     * or a single subscription product ID with multiple base plans.
     */
    private fun findProductDetailsForPlan(plan: PremiumPlan): ProductDetails? {
        val targetProductId = when (plan) {
            PremiumPlan.MONTHLY -> monthlyProductId
            PremiumPlan.YEARLY -> yearlyProductId
        }
        return productDetailsMap[targetProductId]
    }

    /**
     * Resolves the subscription offer for [PremiumPlan] by matching basePlanId first,
     * or falling back to billing period (`P1M` / `P1Y`) or first available offer.
     */
    private fun findOfferForPlan(plan: PremiumPlan): ProductDetails.SubscriptionOfferDetails? {
        val details = findProductDetailsForPlan(plan) ?: return null
        val offers = details.subscriptionOfferDetails.orEmpty()
        if (offers.isEmpty()) return null

        val targetBasePlanId = when (plan) {
            PremiumPlan.MONTHLY -> monthlyBasePlanId
            PremiumPlan.YEARLY -> yearlyBasePlanId
        }
        val expectedPeriod = when (plan) {
            PremiumPlan.MONTHLY -> "P1M"
            PremiumPlan.YEARLY -> "P1Y"
        }

        val matching = offers.filter { it.pricingPhases.pricingPhaseList.lastOrNull()?.billingPeriod == expectedPeriod }
        return matching.firstOrNull { it.basePlanId == targetBasePlanId && it.offerId == null }
            ?: matching.firstOrNull { it.basePlanId == targetBasePlanId }
            ?: matching.firstOrNull { it.offerId == null }
            ?: matching.firstOrNull()
    }

    /**
     * Opens the Google Play Store Subscriptions management screen so the user can cancel/manage their subscription.
     */
    fun openSubscriptionManagement(context: Context, plan: PremiumPlan? = null) {
        val productId = when (plan) {
            PremiumPlan.MONTHLY -> monthlyProductId
            PremiumPlan.YEARLY -> yearlyProductId
            null -> null
        }
        val uri = if (!productId.isNullOrBlank()) {
            Uri.parse("https://play.google.com/store/account/subscriptions?sku=$productId&package=${context.packageName}")
        } else {
            Uri.parse("https://play.google.com/store/account/subscriptions?package=${context.packageName}")
        }
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try { context.startActivity(intent) }
        catch (_: ActivityNotFoundException) { reportError("No app is available to open Google Play subscriptions.") }
    }

    fun reportError(message: String) { _uiState.update { it.copy(errorMessage = message) } }
    fun refresh() { _uiState.update { it.copy(errorMessage = null) }; queryActivePurchases(); querySubscriptionDetails() }

    /**
     * Ends the BillingClient connection when no longer needed.
     */
    fun endConnection() {
        closed = true
        acknowledgementRetries.values.forEach { it.cancel() }
        acknowledgementRetries.clear()
        scope.cancel()
        if (billingClient.isReady) {
            billingClient.endConnection()
        }
    }
}
