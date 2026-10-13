package com.kturker.multiplatform.core.update

// Compares part by part as numbers ("1.10.0" is newer than "1.9.9"); missing parts count as 0 ("1.4" equals
// "1.4.0") and a suffix such as "-beta" is ignored. Negative: a is older, 0: the same, positive: a is newer.
fun compareVersionNames(
    a: String,
    b: String
): Int {
    val left = versionParts(a)
    val right = versionParts(b)
    for (index in 0 until maxOf(left.size, right.size)) {
        val difference = left.getOrElse(index) { 0L }.compareTo(right.getOrElse(index) { 0L })
        if (difference != 0) return difference
    }
    return 0
}

private fun versionParts(version: String): List<Long> =
    version.trim().takeWhile { it.isDigit() || it == '.' }.split('.').map { it.toLongOrNull() ?: 0L }
