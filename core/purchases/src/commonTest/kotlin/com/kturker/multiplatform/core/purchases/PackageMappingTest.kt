package com.kturker.multiplatform.core.purchases

import com.revenuecat.purchases.kmp.models.PackageType
import com.revenuecat.purchases.kmp.models.Period
import com.revenuecat.purchases.kmp.models.PeriodUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PackageMappingTest {

    private fun facts(
        id: String,
        type: PackageType,
        priceMicros: Long = 1_000_000,
        currencyCode: String = "USD",
        pricePerMonth: String? = null,
        freeTrial: Period? = null
    ) = PackageFacts(
        id = id,
        type = type,
        price = "price-$id",
        priceMicros = priceMicros,
        currencyCode = currencyCode,
        pricePerMonth = pricePerMonth,
        freeTrial = freeTrial
    )

    @Test
    fun keepsTheOfferingOrderAndTheStorePrices() {
        val offer = buildOffer(listOf(facts("m", PackageType.MONTHLY), facts("a", PackageType.ANNUAL)))

        assertEquals(listOf("m", "a"), offer.packages.map { it.id })
        assertEquals(listOf("price-m", "price-a"), offer.packages.map { it.price })
    }

    @Test
    fun annualSavesAgainstTwelveMonthlyPayments() {
        val offer = buildOffer(
            listOf(
                facts("m", PackageType.MONTHLY, priceMicros = 9_990_000),
                facts("a", PackageType.ANNUAL, priceMicros = 59_990_000)
            )
        )

        // 12 x 9.99 = 119.88; (119.88 - 59.99) / 119.88 = 49.96 % -> 50
        assertEquals(50, offer.packages.single { it.id == "a" }.savingsPercent)
        assertNull(offer.packages.single { it.id == "m" }.savingsPercent)
    }

    @Test
    fun savingsRoundToTheNearestPercent() {
        val monthly = facts("m", PackageType.MONTHLY, priceMicros = 10_000_000)

        // (120 - 100) / 120 = 16.67 % -> 17
        assertEquals(17, savingsPercent(facts("a", PackageType.ANNUAL, priceMicros = 100_000_000), monthly))
        // (120 - 119.5) / 120 = 0.42 % -> 0 -> no badge
        assertNull(savingsPercent(facts("a", PackageType.ANNUAL, priceMicros = 119_500_000), monthly))
    }

    @Test
    fun savingsDoNotDependOnHowTheStoreRoundsMicros() {
        val monthly = facts("m", PackageType.MONTHLY, priceMicros = 2_990_000)

        // The Test Store's 19.99 annual comes as 19990000 micros on Android and 19989999 on iOS: both save 44 %
        assertEquals(44, savingsPercent(facts("a", PackageType.ANNUAL, priceMicros = 19_990_000), monthly))
        assertEquals(44, savingsPercent(facts("a", PackageType.ANNUAL, priceMicros = 19_989_999), monthly))
    }

    @Test
    fun noSavingsWhenAnnualCostsAsMuchOrMore() {
        val monthly = facts("m", PackageType.MONTHLY, priceMicros = 1_000_000)

        assertNull(savingsPercent(facts("a", PackageType.ANNUAL, priceMicros = 12_000_000), monthly))
        assertNull(savingsPercent(facts("a", PackageType.ANNUAL, priceMicros = 13_000_000), monthly))
    }

    @Test
    fun noSavingsWithoutAMonthlyPackage() {
        val offer = buildOffer(listOf(facts("w", PackageType.WEEKLY), facts("a", PackageType.ANNUAL)))

        assertNull(offer.packages.single { it.id == "a" }.savingsPercent)
    }

    @Test
    fun noSavingsAcrossCurrenciesOrWithAFreePrice() {
        val annual = facts("a", PackageType.ANNUAL, priceMicros = 50_000_000)

        assertNull(savingsPercent(annual, facts("m", PackageType.MONTHLY, priceMicros = 9_990_000, currencyCode = "EUR")))
        assertNull(savingsPercent(annual, facts("m", PackageType.MONTHLY, priceMicros = 0)))
    }

    @Test
    fun pricePerMonthOnlyForAnnualAndWeekly() {
        val offer = buildOffer(
            listOf(
                facts("w", PackageType.WEEKLY, pricePerMonth = "w/mo"),
                facts("m", PackageType.MONTHLY, pricePerMonth = "m/mo"),
                facts("a", PackageType.ANNUAL, pricePerMonth = "a/mo"),
                facts("l", PackageType.LIFETIME, pricePerMonth = "l/mo")
            )
        )

        assertEquals(listOf("w/mo", null, "a/mo", null), offer.packages.map { it.pricePerMonth })
    }

    @Test
    fun lifetimeIsLifetimeWithoutSavingsOrMonthlyPrice() {
        val lifetime = buildOffer(listOf(facts("l", PackageType.LIFETIME))).packages.single()

        assertEquals(PackagePeriod.LIFETIME, lifetime.period)
        assertNull(lifetime.savingsPercent)
        assertNull(lifetime.pricePerMonth)
    }

    @Test
    fun otherPackageTypesAreOther() {
        for (type in listOf(PackageType.UNKNOWN, PackageType.CUSTOM, PackageType.SIX_MONTH, PackageType.THREE_MONTH, PackageType.TWO_MONTH)) {
            assertEquals(PackagePeriod.OTHER, packagePeriod(type), type.name)
        }
        assertEquals(PackagePeriod.WEEKLY, packagePeriod(PackageType.WEEKLY))
        assertEquals(PackagePeriod.MONTHLY, packagePeriod(PackageType.MONTHLY))
        assertEquals(PackagePeriod.ANNUAL, packagePeriod(PackageType.ANNUAL))
    }

    @Test
    fun freeTrialTexts() {
        assertEquals("7 days", freeTrialText(Period(7, PeriodUnit.DAY)))
        assertEquals("1 week", freeTrialText(Period(1, PeriodUnit.WEEK)))
        assertEquals("1 month", freeTrialText(Period(1, PeriodUnit.MONTH)))
        assertEquals("3 months", freeTrialText(Period(3, PeriodUnit.MONTH)))
        assertEquals("1 year", freeTrialText(Period(1, PeriodUnit.YEAR)))
        assertNull(freeTrialText(Period(0, PeriodUnit.DAY)))
        assertNull(freeTrialText(Period(1, PeriodUnit.UNKNOWN)))
    }

    @Test
    fun theOfferCarriesTheFreeTrialText() {
        val offer = buildOffer(listOf(facts("a", PackageType.ANNUAL, freeTrial = Period(1, PeriodUnit.WEEK)), facts("m", PackageType.MONTHLY)))

        assertEquals(listOf("1 week", null), offer.packages.map { it.freeTrial })
    }
}
