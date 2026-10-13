package com.kturker.multiplatform.about

import com.kturker.multiplatform.core.about.licenses.LicensesSource
import com.kturker.multiplatform.shared.generated.resources.Res
import org.koin.core.annotation.Single

@Single(binds = [LicensesSource::class])
internal class ComposeResourcesLicensesSource : LicensesSource {
    override suspend fun read(): String =
        Res.readBytes("files/aboutlibraries.json").decodeToString()
}
