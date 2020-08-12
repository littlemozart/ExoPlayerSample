package com.lee.videoview.utils

fun Long.toMediaTime(): String {
    val hour = this / 3600
    val min = (this % 3600) / 60
    val sec = this % 60
    val res = "${String.format("%02d", min.toInt())}:${String.format("%02d", sec)}"
    return if (hour > 0) {
        "${String.format("%02d", hour.toInt())}:$res"
    } else {
        res
    }
}