package com.panchangam100.live.data.repository

import com.panchangam100.live.astronomy.PanchangamEngine
import com.panchangam100.live.astronomy.PanchangamResult
import com.panchangam100.live.data.model.AppLocation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.util.Collections
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PanchangamRepository @Inject constructor() {

    // Simple LRU memory cache: key = "date|lat|lon"
    private val cache = Collections.synchronizedMap(
        object : LinkedHashMap<String, PanchangamResult>(128, 0.75f, true) {
            override fun removeEldestEntry(eldest: Map.Entry<String, PanchangamResult>) = size > 500
        }
    )

    suspend fun getPanchangam(date: LocalDate, location: AppLocation): PanchangamResult {
        val key = "${date}|${location.latitude}|${location.longitude}"
        cache[key]?.let { return it }

        return withContext(Dispatchers.Default) {
            val result = PanchangamEngine.calculate(
                date = date,
                lat = location.latitude,
                lon = location.longitude,
                timezone = location.timezone
            )
            cache[key] = result
            result
        }
    }

    suspend fun getPanchangamRange(
        startDate: LocalDate,
        endDate: LocalDate,
        location: AppLocation
    ): List<PanchangamResult> = withContext(Dispatchers.Default) {
        val results = mutableListOf<PanchangamResult>()
        var current = startDate
        while (!current.isAfter(endDate)) {
            results.add(getPanchangam(current, location))
            current = current.plusDays(1)
        }
        results
    }
}
