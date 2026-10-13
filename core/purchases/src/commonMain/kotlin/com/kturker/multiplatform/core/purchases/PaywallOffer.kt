package com.kturker.multiplatform.core.purchases

// What a paywall shows: the current offering's packages, in the order of the RevenueCat dashboard.
// Plain models: app code never imports a RevenueCat type.
data class PaywallOffer(val packages: List<PaywallPackage>)

data class PaywallPackage(
    // RevenueCat's package identifier ($rc_annual, $rc_monthly, or your own): purchase() takes it
    val id: String,
    val period: PackagePeriod,
    // Formatted by the store ("$29.99", "₺349,99")
    val price: String,
    // ANNUAL and WEEKLY only: what one month costs at this plan's rate, from the store; else null
    val pricePerMonth: String?,
    // ANNUAL only, when the offering also has MONTHLY in the same currency: what a year saves, in percent
    val savingsPercent: Int?,
    // A free trial the store offers this user ("7 days", "1 month"); null without one
    val freeTrial: String?
)

enum class PackagePeriod {
    WEEKLY,
    MONTHLY,
    ANNUAL,
    LIFETIME,
    OTHER
}
