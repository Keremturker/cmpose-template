package com.kturker.multiplatform.core.purchases

import com.revenuecat.purchases.kmp.models.PackageType
import com.revenuecat.purchases.kmp.models.Period
import com.revenuecat.purchases.kmp.models.PeriodUnit
import kotlin.math.roundToInt

private const val MONTHS_PER_YEAR = 12
private const val PERCENT = 100.0

// One RevenueCat package, read once (RevenueCatPurchasesManager): the functions below turn a list of them
// into a PaywallOffer, so the rules are unit tested without the SDK
internal data class PackageFacts(
    val id: String,
    val type: PackageType,
    val price: String,
    val priceMicros: Long,
    val currencyCode: String,
    val pricePerMonth: String?,
    val freeTrial: Period?
)

internal fun buildOffer(packages: List<PackageFacts>): PaywallOffer {
    val annual = packages.firstOrNull { it.type == PackageType.ANNUAL }
    val monthly = packages.firstOrNull { it.type == PackageType.MONTHLY }
    val annualSavings = if (annual != null && monthly != null) savingsPercent(annual, monthly) else null
    return PaywallOffer(
        packages = packages.map { facts ->
            val period = packagePeriod(facts.type)
            val showsPricePerMonth = period == PackagePeriod.ANNUAL || period == PackagePeriod.WEEKLY
            PaywallPackage(
                id = facts.id,
                period = period,
                price = facts.price,
                pricePerMonth = facts.pricePerMonth.takeIf { showsPricePerMonth },
                savingsPercent = annualSavings.takeIf { facts === annual },
                freeTrial = facts.freeTrial?.let(::freeTrialText)
            )
        }
    )
}

internal fun packagePeriod(type: PackageType): PackagePeriod = when (type) {
    PackageType.WEEKLY -> PackagePeriod.WEEKLY
    PackageType.MONTHLY -> PackagePeriod.MONTHLY
    PackageType.ANNUAL -> PackagePeriod.ANNUAL
    PackageType.LIFETIME -> PackagePeriod.LIFETIME
    else -> PackagePeriod.OTHER
}

// What ANNUAL saves against twelve MONTHLY payments, rounded to a whole percent; null when the currencies
// differ, a price is missing (0) or ANNUAL saves nothing
internal fun savingsPercent(
    annual: PackageFacts,
    monthly: PackageFacts
): Int? {
    val comparable = annual.currencyCode == monthly.currencyCode && annual.priceMicros > 0 && monthly.priceMicros > 0
    if (!comparable) return null
    val yearOfMonthly = monthly.priceMicros * MONTHS_PER_YEAR
    val percent = ((yearOfMonthly - annual.priceMicros) * PERCENT / yearOfMonthly).roundToInt()
    return percent.takeIf { it > 0 }
}

// "7 days", "1 week", "3 months"; null for an empty or unknown period
internal fun freeTrialText(period: Period): String? {
    val unit = when (period.unit) {
        PeriodUnit.DAY -> "day"
        PeriodUnit.WEEK -> "week"
        PeriodUnit.MONTH -> "month"
        PeriodUnit.YEAR -> "year"
        PeriodUnit.UNKNOWN -> null
    }
    return when {
        unit == null || period.value <= 0 -> null
        period.value == 1 -> "1 $unit"
        else -> "${period.value} ${unit}s"
    }
}
