# Project-specific R8 rules for the release build (isMinifyEnabled = true).
# Koin annotations (KSP), kotlinx.serialization, Ktor and Compose ship their own consumer rules,
# so this file only holds what the release build proved it needs. Each rule says why.

# Play In-App Review's ktx (2.0.2) references this build-time annotation, which the
# play-services-basement on the classpath does not ship; it is not needed at runtime.
-dontwarn com.google.android.gms.common.annotation.NoNullnessRewrite
