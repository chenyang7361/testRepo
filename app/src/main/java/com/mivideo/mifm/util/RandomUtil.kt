package com.mivideo.mifm.util

import java.util.*

private val random = Random(System.currentTimeMillis())

fun randomInt(n: Int) = random.nextInt(n)
