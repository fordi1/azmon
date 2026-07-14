package com.example.utils

fun String.toPersianDigits(): String {
    var result = this
    val englishDigits = arrayOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9")
    val persianDigits = arrayOf("۰", "۱", "۲", "۳", "۴", "۵", "۶", "۷", "۸", "۹")
    val arabicDigits = arrayOf("٠", "١", "٢", "٣", "٤", "٥", "٦", "٧", "٨", "٩")

    for (i in 0..9) {
        result = result.replace(englishDigits[i], persianDigits[i])
        result = result.replace(arabicDigits[i], persianDigits[i])
    }
    return result
}

fun String.toEnglishDigits(): String {
    var result = this
    val englishDigits = arrayOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9")
    val persianDigits = arrayOf("۰", "۱", "۲", "۳", "۴", "۵", "۶", "۷", "۸", "۹")
    val arabicDigits = arrayOf("٠", "١", "٢", "٣", "٤", "٥", "٦", "٧", "٨", "٩")

    for (i in 0..9) {
        result = result.replace(persianDigits[i], englishDigits[i])
        result = result.replace(arabicDigits[i], englishDigits[i])
    }
    return result
}
