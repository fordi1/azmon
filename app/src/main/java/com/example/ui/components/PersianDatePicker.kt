package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.utils.toEnglishDigits
import com.example.utils.toPersianDigits
import java.util.Calendar

private val monthNames = listOf(
    "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
    "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
)

private val weekDays = listOf("ش", "ی", "د", "س", "چ", "پ", "ج")

@Composable
fun PersianDatePicker(
    initialDate: String,
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val initialParts = initialDate.toEnglishDigits().split("/")
    var year by remember {
        mutableIntStateOf(initialParts.getOrNull(0)?.toIntOrNull() ?: 1405)
    }
    var month by remember {
        mutableIntStateOf((initialParts.getOrNull(1)?.toIntOrNull() ?: 1).coerceIn(1, 12))
    }
    var selectedDay by remember {
        mutableIntStateOf((initialParts.getOrNull(2)?.toIntOrNull() ?: 0).coerceAtLeast(0))
    }

    val daysInMonth = jalaliMonthLength(year, month)
    val firstDayOffset = jalaliFirstDayOffset(year, month)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    if (month == 1) {
                        month = 12
                        year--
                    } else {
                        month--
                    }
                    selectedDay = 0
                }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "ماه قبل")
                }
                Text(
                    "${monthNames[month - 1]} ${year.toString().toPersianDigits()}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = {
                    if (month == 12) {
                        month = 1
                        year++
                    } else {
                        month++
                    }
                    selectedDay = 0
                }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "ماه بعد")
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    weekDays.forEach { day ->
                        Box(
                            modifier = Modifier.weight(1f).padding(vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                day,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                val cells = firstDayOffset + daysInMonth
                repeat((cells + 6) / 7) { row ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        repeat(7) { column ->
                            val day = row * 7 + column - firstDayOffset + 1
                            Box(
                                modifier = Modifier.weight(1f).aspectRatio(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                if (day in 1..daysInMonth) {
                                    val selected = day == selectedDay
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .aspectRatio(1f)
                                            .clickable { selectedDay = day },
                                        shape = CircleShape,
                                        color = if (selected) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.surface
                                        }
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxWidth(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                day.toString().toPersianDigits(),
                                                color = if (selected) {
                                                    MaterialTheme.colorScheme.onPrimary
                                                } else {
                                                    MaterialTheme.colorScheme.onSurface
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = selectedDay > 0,
                onClick = {
                    val value = "%04d/%02d/%02d".format(year, month, selectedDay)
                    onDateSelected(value.toPersianDigits())
                }
            ) {
                Text("تأیید")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("انصراف") }
        }
    )
}

private fun jalaliMonthLength(year: Int, month: Int): Int = when {
    month <= 6 -> 31
    month <= 11 -> 30
    isJalaliLeapYear(year) -> 30
    else -> 29
}

private fun isJalaliLeapYear(year: Int): Boolean {
    val breaks = intArrayOf(
        -61, 9, 38, 199, 426, 686, 756, 818, 1111, 1181,
        1210, 1635, 2060, 2097, 2192, 2262, 2324, 2394, 2456, 3178
    )
    var leapJ = -14
    var previous = breaks[0]
    var jump = 0
    for (index in 1 until breaks.size) {
        val current = breaks[index]
        jump = current - previous
        if (year < current) break
        leapJ += jump / 33 * 8 + jump % 33 / 4
        previous = current
    }
    var n = year - previous
    if (jump - n < 6) n = n - jump + (jump + 4) / 33 * 33
    var leap = ((n + 1) % 33 - 1) % 4
    if (leap == -1) leap = 4
    return leap == 0
}

private fun jalaliFirstDayOffset(year: Int, month: Int): Int {
    val (gy, gm, gd) = jalaliToGregorian(year, month, 1)
    val calendar = Calendar.getInstance().apply {
        set(gy, gm - 1, gd)
    }
    return calendar.get(Calendar.DAY_OF_WEEK) % 7
}

private fun jalaliToGregorian(jy: Int, jm: Int, jd: Int): Triple<Int, Int, Int> {
    var year = jy + 1595
    var days = -355668 + 365 * year + (year / 33) * 8 + ((year % 33 + 3) / 4) + jd
    days += if (jm < 7) (jm - 1) * 31 else (jm - 7) * 30 + 186

    var gy = 400 * (days / 146097)
    days %= 146097
    if (days > 36524) {
        gy += 100 * (--days / 36524)
        days %= 36524
        if (days >= 365) days++
    }
    gy += 4 * (days / 1461)
    days %= 1461
    if (days > 365) {
        gy += (days - 1) / 365
        days = (days - 1) % 365
    }
    var gd = days + 1
    val monthDays = intArrayOf(
        0, 31, if (isGregorianLeapYear(gy)) 29 else 28, 31, 30, 31, 30,
        31, 31, 30, 31, 30, 31
    )
    var gm = 1
    while (gm <= 12 && gd > monthDays[gm]) {
        gd -= monthDays[gm]
        gm++
    }
    return Triple(gy, gm, gd)
}

private fun isGregorianLeapYear(year: Int): Boolean =
    year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)
