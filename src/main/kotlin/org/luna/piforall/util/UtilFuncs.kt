package org.luna.piforall.util

fun <T> List<T>.prepend(elem: T): List<T> = listOf(elem) + this