package com.google.firebase

import java.util.Date

class Timestamp(val seconds: Long, val nanoseconds: Int) : Comparable<Timestamp> {
    constructor(date: Date) : this(date.time / 1000, ((date.time % 1000) * 1000000).toInt())
    constructor() : this(Date())

    fun toDate(): Date = Date(seconds * 1000 + nanoseconds / 1000000)

    override fun compareTo(other: Timestamp): Int {
        val secondsDiff = this.seconds.compareTo(other.seconds)
        if (secondsDiff != 0) return secondsDiff
        return this.nanoseconds.compareTo(other.nanoseconds)
    }

    companion object {
        @JvmStatic
        fun now() = Timestamp()
    }
}
