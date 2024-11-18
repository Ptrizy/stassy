package com.example.pamoj.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

fun getRelativeTime(timeString: String): String {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    val postDate = dateFormat.parse(timeString)
    val now = Date()

    val diff = now.time - (postDate?.time ?: 0)

    return when {
        diff < TimeUnit.MINUTES.toMillis(1) -> "${TimeUnit.MILLISECONDS.toSeconds(diff)} detik yang lalu"
        diff < TimeUnit.HOURS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toMinutes(diff)} menit yang lalu"
        diff < TimeUnit.DAYS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toHours(diff)} jam yang lalu"
        diff < TimeUnit.DAYS.toMillis(30) -> "${TimeUnit.MILLISECONDS.toDays(diff)} hari yang lalu"
        diff < TimeUnit.DAYS.toMillis(365) -> "${TimeUnit.MILLISECONDS.toDays(diff) / 30} bulan yang lalu"
        else -> "${TimeUnit.MILLISECONDS.toDays(diff) / 365} tahun yang lalu"
    }
}
