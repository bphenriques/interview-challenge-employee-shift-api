package com.bphenriques.test

import java.util.Random
import java.util.UUID

object Generator {
    private val random = Random(10L) // Fixed seed for reproducibility!

    fun randomInt() = random.nextInt()
    fun uuid() = UUID.randomUUID().toString()
}