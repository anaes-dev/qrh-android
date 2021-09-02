package dev.anaes.qrh.model

data class DetailItem (
    val type: Int,
    val step: String,
    val head: String,
    val body: String,
    var collapsed: Boolean
)