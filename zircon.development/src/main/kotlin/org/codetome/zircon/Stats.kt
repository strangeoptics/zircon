package org.codetome.zircon

import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.system.measureNanoTime

object Stats {

    private val stats = ConcurrentHashMap<String, Stat>()

    @JvmStatic
    fun getAllStats() = stats.values.toList()

    @JvmStatic
    fun printStats() {
        println(getAllStats().joinToString("\n"))
    }

    @JvmStatic
    fun <T> addTimedStatFor(key: String, fn: () -> T): T {
        var optResult = Optional.empty<T>()
        val time = measureNanoTime {
            optResult = Optional.of(fn())
        }
        addStatFor(key, time)
        return optResult.get()
    }

    @JvmStatic
    fun addStatFor(key: String, timeMs: Long) {
        addEmptyStatForKeyIfNotPresent(key)
        stats[key] = stats[key]!!.let { stat ->
            stat.copy(name = key,
                    avgTimeNs = stat.avgTimeNs
                            .times(stat.measurements)
                            .plus(timeMs)
                            .div(stat.measurements + 1),
                    measurements = stat.measurements + 1)
        }
    }

    private fun addEmptyStatForKeyIfNotPresent(key: String) {
        if (stats.containsKey(key).not()) {
            stats[key] = Stat(key)
        }
    }

    data class Stat(val name: String,
                    val avgTimeNs: Double = 0.toDouble(),
                    val measurements: Long = 0,
                    val weight: Long = 1) {

        override fun toString(): String {
            val ms = avgTimeNs * weight / 1000 / 1000
            return "Stats: name='$name', " +
                    "avgTimeMs=$ms, " +
                    "fps=${1000 / ms}, " +
                    "measurements=$measurements, " +
                    "weight = $weight"
        }
    }
}
