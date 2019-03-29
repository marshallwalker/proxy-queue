package ca.pureplugins.proxyqueue.util

fun Int.toOrdinal(): String {
    val mod100 = this % 100
    val mod10 = this % 10

    return "$this" + if (mod10 == 1 && mod100 != 11) {
        "st"
    } else if (mod10 == 2 && mod100 != 12) {
        "nd"
    } else if (mod10 == 3 && mod100 != 13) {
        "rd"
    } else {
        "th"
    }
}