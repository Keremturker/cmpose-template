package com.kturker.multiplatform.core.navigation

private const val SCHEME_SEPARATOR = "://"
private const val APP_LINK_SCHEME = "https"
private const val ESCAPE_LENGTH = 3
private const val HEX_DIGITS = "0123456789abcdef"

/** The destination [uri] opens, or null (another app's link, an unknown path, an invalid argument). */
fun parseDeepLink(
    uri: String,
    scheme: String,
    appLinkHost: String,
    routes: List<DeepLinkRoute>
): NavigationCommand.Destination? {
    val segments = linkPath(uri = uri, scheme = scheme, appLinkHost = appLinkHost) ?: return null
    return routes.asSequence()
        .mapNotNull { route -> match(pattern = route.pattern, segments = segments)?.let(route::destination) }
        .firstOrNull()
}

// The path segments of an own link; null for any other link
internal fun linkPath(
    uri: String,
    scheme: String,
    appLinkHost: String
): List<String>? {
    // "<scheme>://a/b?q#f" -> ["a", "b"]; "https://<appLinkHost>/a/b" -> ["a", "b"] (only when appLinkHost is set)
    val link = uri.trim().substringBefore('?').substringBefore('#')
    val separator = link.indexOf(SCHEME_SEPARATOR)
    if (separator < 0) return null
    val linkScheme = link.substring(0, separator)
    val rest = link.substring(separator + SCHEME_SEPARATOR.length)
    val path = when {
        linkScheme.equals(scheme, ignoreCase = true) -> rest

        appLinkHost.isNotEmpty() && linkScheme.equals(APP_LINK_SCHEME, ignoreCase = true) -> {
            if (!rest.substringBefore('/').equals(appLinkHost, ignoreCase = true)) return null
            rest.substringAfter('/', missingDelimiterValue = "")
        }

        else -> return null
    }
    return if (path.isEmpty()) emptyList() else path.split('/')
}

private fun match(
    pattern: String,
    segments: List<String>
): Map<String, String>? {
    // pattern segments vs link segments: same count, fixed segments equal, {name} -> decoded value (non-empty)
    val parts = pattern.split('/')
    if (parts.size != segments.size) return null
    val arguments = mutableMapOf<String, String>()
    parts.zip(segments).forEach { (part, segment) ->
        if (part.startsWith('{') && part.endsWith('}')) {
            val value = percentDecode(segment)
            if (value.isEmpty()) return null
            arguments[part.substring(1, part.length - 1)] = value
        } else if (part != segment) {
            return null
        }
    }
    return arguments
}

// kotlin.text has no URI decoder: "%XX" -> a byte, read as UTF-8; a malformed "%" is kept as it is
private fun percentDecode(value: String): String {
    if ('%' !in value) return value
    val bytes = mutableListOf<Byte>()
    var index = 0
    while (index < value.length) {
        val escaped = value.escapedByteAt(index)
        if (escaped != null) {
            bytes += escaped
            index += ESCAPE_LENGTH
        } else {
            val end = value.indexOf('%', startIndex = index + 1).takeIf { it >= 0 } ?: value.length
            bytes += value.substring(index, end).encodeToByteArray().asList()
            index = end
        }
    }
    return bytes.toByteArray().decodeToString()
}

private fun String.escapedByteAt(index: Int): Byte? {
    if (this[index] != '%' || index + ESCAPE_LENGTH > length) return null
    val high = HEX_DIGITS.indexOf(this[index + 1].lowercaseChar())
    val low = HEX_DIGITS.indexOf(this[index + 2].lowercaseChar())
    if (high < 0 || low < 0) return null
    return (high * HEX_DIGITS.length + low).toByte()
}
