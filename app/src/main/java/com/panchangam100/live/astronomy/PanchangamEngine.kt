package com.panchangam100.live.astronomy

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import kotlin.math.*
import kotlin.math.roundToInt

/**
 * Self-contained Panchangam astronomical engine.
 *
 * Implements high-accuracy astronomical algorithms from Jean Meeus
 * "Astronomical Algorithms" (2nd ed.) for:
 * - Sun apparent longitude (accuracy ~0.01°)
 * - Moon apparent longitude (accuracy ~0.01°)
 * - Sunrise / Sunset (accuracy ~1 minute)
 * - Lahiri Ayanamsa for sidereal calculations
 *
 * Udaya Rule:
 *   If tithi/nakshatra ends within 48 minutes after sunrise → use the NEXT one.
 *   Loop handles kshaya (very short) tithis/nakshatras.
 */
object PanchangamEngine {

    private const val PI = Math.PI
    private const val DEG = PI / 180.0
    private const val RAD = 180.0 / PI

    // ─── Spans ───
    private const val TITHI_SPAN = 12.0   // degrees per tithi
    private const val NAK_SPAN = 360.0 / 27.0  // ~13.333° per nakshatra
    private const val YOGA_SPAN = 360.0 / 27.0
    private const val UDAYA_MINUTES = 48.0

    // ─── Lahiri Ayanamsa (Chitrapaksha) ─────────────────────────────────────────
    // Based on: 23.85° at J2000.0 + precession ~50.2877"/year
    private fun lahiriAyanamsa(jd: Double): Double {
        val T = (jd - 2451545.0) / 36525.0
        // From IAU 1976 precession, adapted for Lahiri:
        val ayan = 23.85 + (50.2877 * T * 100.0 / 3600.0) +
                0.000222 * T * T - 0.0000002 * T * T * T
        return normalizeAngle(ayan)
    }

    // ─── Julian Day ──────────────────────────────────────────────────────────────
    fun julianDay(year: Int, month: Int, day: Int, hourUT: Double = 0.0): Double {
        var y = year; var m = month
        if (m <= 2) { y--; m += 12 }
        val A = (y / 100).toInt()
        val B = 2 - A + (A / 4).toInt()
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + hourUT / 24.0 + B - 1524.5
    }

    fun julianDay(date: LocalDate, hourUT: Double = 0.0): Double =
        julianDay(date.year, date.monthValue, date.dayOfMonth, hourUT)

    // ─── Sun Calculations (Meeus Ch.25) ─────────────────────────────────────────
    data class SunPos(val lon: Double, val lat: Double = 0.0, val dist: Double = 1.0)

    fun sunApparentLongitude(jd: Double): Double {
        val T = (jd - 2451545.0) / 36525.0
        val T2 = T * T; val T3 = T2 * T

        // Geometric mean longitude (degrees)
        val L0 = normalizeAngle(280.46646 + 36000.76983 * T + 0.0003032 * T2)
        // Mean anomaly
        val M = normalizeAngle(357.52911 + 35999.05029 * T - 0.0001537 * T2)
        val Mrad = M * DEG

        // Equation of centre
        val C = (1.914602 - 0.004817 * T - 0.000014 * T2) * sin(Mrad) +
                (0.019993 - 0.000101 * T) * sin(2 * Mrad) +
                0.000289 * sin(3 * Mrad)

        // Sun's true longitude
        val sunLon = L0 + C

        // Apparent longitude (correct for nutation & aberration)
        val Omega = normalizeAngle(125.04 - 1934.136 * T)
        val apparent = sunLon - 0.00569 - 0.00478 * sin(Omega * DEG)
        return normalizeAngle(apparent)
    }

    fun sunSiderealLongitude(jd: Double): Double =
        normalizeAngle(sunApparentLongitude(jd) - lahiriAyanamsa(jd))

    // ─── Moon Calculations (Meeus Ch.47, simplified ELP2000) ────────────────────
    fun moonApparentLongitude(jd: Double): Double {
        val T = (jd - 2451545.0) / 36525.0
        val T2 = T * T; val T3 = T2 * T; val T4 = T3 * T

        // Fundamental arguments (degrees)
        val Lp = normalizeAngle(218.3164477 + 481267.88123421 * T - 0.0015786 * T2 + T3 / 538841.0 - T4 / 65194000.0)
        val D  = normalizeAngle(297.8501921 + 445267.1114034 * T - 0.0018819 * T2 + T3 / 545868.0 - T4 / 113065000.0)
        val M  = normalizeAngle(357.5291092 + 35999.0502909 * T - 0.0001536 * T2 + T3 / 24490000.0)
        val Mp = normalizeAngle(134.9633964 + 477198.8676313 * T + 0.0089970 * T2 + T3 / 69699.0 - T4 / 14712000.0)
        val F  = normalizeAngle(93.2720950  + 483202.0175233 * T - 0.0036539 * T2 - T3 / 3526000.0 + T4 / 863310000.0)

        val Lrad = Lp * DEG; val Drad = D * DEG; val Mrad = M * DEG
        val Mprad = Mp * DEG; val Frad = F * DEG

        // Periodic terms for longitude (arcseconds)
        var sumL = 0.0
        // Major terms from Meeus Table 47.A
        sumL += 6288774 * sin(Mprad)
        sumL += 1274027 * sin(2 * Drad - Mprad)
        sumL += 658314  * sin(2 * Drad)
        sumL += 213618  * sin(2 * Mprad)
        sumL -= 185116  * sin(Mrad)
        sumL -= 114332  * sin(2 * Frad)
        sumL += 58793   * sin(2 * Drad - 2 * Mprad)
        sumL += 57066   * sin(2 * Drad - Mrad - Mprad)
        sumL += 53322   * sin(2 * Drad + Mprad)
        sumL += 45758   * sin(2 * Drad - Mrad)
        sumL -= 40923   * sin(Mrad - Mprad)
        sumL -= 34720   * sin(Drad)
        sumL -= 30383   * sin(Mrad + Mprad)
        sumL += 15327   * sin(2 * Drad - 2 * Frad)
        sumL -= 12528   * sin(Mprad + 2 * Frad)
        sumL += 10980   * sin(Mprad - 2 * Frad)
        sumL += 10675   * sin(4 * Drad - Mprad)
        sumL += 10034   * sin(3 * Mprad)
        sumL += 8548    * sin(4 * Drad - 2 * Mprad)
        sumL -= 7888    * sin(2 * Drad + Mrad - Mprad)
        sumL -= 6766    * sin(2 * Drad + Mrad)
        sumL -= 5163    * sin(Drad - Mprad)
        sumL += 4987    * sin(Drad + Mrad)
        sumL += 4036    * sin(2 * Drad - Mrad + Mprad)
        sumL += 3994    * sin(2 * Drad + 2 * Mprad)
        sumL += 3861    * sin(4 * Drad)
        sumL += 3665    * sin(2 * Drad - 3 * Mprad)
        sumL -= 2689    * sin(Mrad - 2 * Mprad)
        sumL -= 2602    * sin(2 * Drad - Mprad + 2 * Frad)
        sumL += 2390    * sin(2 * Drad - Mrad - 2 * Mprad)
        sumL -= 2348    * sin(Drad + Mprad)
        sumL += 2236    * sin(2 * Drad - 2 * Mrad)
        sumL -= 2120    * sin(Mrad + 2 * Mprad)
        sumL -= 2069    * sin(2 * Mrad)
        sumL += 2048    * sin(2 * Drad - 2 * Mrad - Mprad)
        sumL -= 1773    * sin(2 * Drad + Mprad - 2 * Frad)
        sumL -= 1595    * sin(2 * Drad + 2 * Frad)
        sumL += 1215    * sin(4 * Drad - Mrad - Mprad)
        sumL -= 1110    * sin(2 * Mprad + 2 * Frad)
        sumL -= 892     * sin(3 * Drad - Mprad)
        sumL -= 810     * sin(2 * Drad + Mrad + Mprad)
        sumL += 759     * sin(4 * Drad - Mrad - 2 * Mprad)
        sumL -= 713     * sin(2 * Mrad - Mprad)
        sumL -= 700     * sin(2 * Drad + 2 * Mrad - Mprad)
        sumL += 691     * sin(2 * Drad + Mrad - 2 * Mprad)
        sumL += 596     * sin(2 * Drad - Mrad - 2 * Frad)
        sumL += 549     * sin(4 * Drad + Mprad)
        sumL += 537     * sin(4 * Mprad)
        sumL += 520     * sin(4 * Drad - Mrad)
        sumL -= 487     * sin(Drad - 2 * Mprad)
        sumL -= 399     * sin(2 * Drad + Mrad - 2 * Frad)

        // Convert arcseconds to degrees
        val moonLon = Lp + sumL / 1000000.0

        // Add nutation (simplified)
        val Omega = normalizeAngle(125.04452 - 1934.136261 * T)
        val nutat = -17.2 / 3600.0 * sin(Omega * DEG)

        return normalizeAngle(moonLon + nutat)
    }

    fun moonSiderealLongitude(jd: Double): Double =
        normalizeAngle(moonApparentLongitude(jd) - lahiriAyanamsa(jd))

    // ─── Tithi, Nakshatra, Yoga, Karana ─────────────────────────────────────────
    fun elongation(jd: Double): Double =
        normalizeAngle(moonApparentLongitude(jd) - sunApparentLongitude(jd))

    fun tithiAt(jd: Double): Int = floor(elongation(jd) / TITHI_SPAN).toInt().coerceIn(0, 29)

    fun nakshatraAt(jd: Double): Int =
        floor(moonSiderealLongitude(jd) / NAK_SPAN).toInt().coerceIn(0, 26)

    fun yogaAt(jd: Double): Int =
        (floor(normalizeAngle(moonSiderealLongitude(jd) + sunSiderealLongitude(jd)) / YOGA_SPAN).toInt() % 27)
            .coerceIn(0, 26)

    fun karanaAt(jd: Double): Int {
        val elg = elongation(jd)
        val karNum = floor(elg / 6.0).toInt()
        return when {
            karNum == 0 -> 10  // Kimstughna
            karNum in 57..57 -> 7  // Shakuni
            karNum == 58 -> 8  // Chatushpada
            karNum == 59 -> 9  // Naga
            else -> ((karNum - 1) % 7)
        }
    }

    // ─── Find end time via bisection ─────────────────────────────────────────────
    /**
     * Find JD when [fn] changes sign, between [jdStart] and [jdStart + maxDays].
     * [fn] should return negative before the event and positive after.
     */
    private fun bisect(jdStart: Double, maxDays: Double, fn: (Double) -> Double): Double? {
        var lo = jdStart
        var hi = jdStart + maxDays
        if (fn(lo) > 0) return null  // Already past the boundary
        if (fn(hi) < 0) return null  // Never crosses within maxDays

        repeat(50) {
            val mid = (lo + hi) / 2.0
            if (fn(mid) < 0) lo = mid else hi = mid
        }
        return (lo + hi) / 2.0
    }

    fun findTithiEnd(tithiIdx: Int, jdStart: Double): Double? {
        val target = (tithiIdx + 1) * TITHI_SPAN
        return bisect(jdStart, 2.5) { jd ->
            var e = elongation(jd)
            if (tithiIdx >= 28 || target > 350) {
                // Handle wrap-around near 360°
                if (e < 30 && target > 300) e += 360
            }
            e - target
        }
    }

    fun findNakshatraEnd(nakIdx: Int, jdStart: Double): Double? {
        val target = (nakIdx + 1) * NAK_SPAN
        return bisect(jdStart, 2.0) { jd ->
            var moonLon = moonSiderealLongitude(jd)
            if (target > 340 && moonLon < 20) moonLon += 360
            moonLon - target
        }
    }

    fun findYogaEnd(yogaIdx: Int, jdStart: Double): Double? {
        val target = (yogaIdx + 1) * YOGA_SPAN
        return bisect(jdStart, 2.0) { jd ->
            var combined = normalizeAngle(moonSiderealLongitude(jd) + sunSiderealLongitude(jd))
            if (target > 340 && combined < 20) combined += 360
            combined - target
        }
    }

    // ─── Sunrise / Sunset (NOAA / Meeus algorithm) ──────────────────────────────
    /**
     * Returns sunrise time as hour UT on [date] for [lat]/[lon].
     * Uses the standard solar position + hour angle formula.
     * Accounts for refraction (~0.833°).
     */
    fun sunriseUT(date: LocalDate, lat: Double, lon: Double): Double? {
        return sunRiseSetUT(date, lat, lon, rising = true)
    }

    fun sunsetUT(date: LocalDate, lat: Double, lon: Double): Double? {
        return sunRiseSetUT(date, lat, lon, rising = false)
    }

    private fun sunRiseSetUT(date: LocalDate, lat: Double, lon: Double, rising: Boolean): Double? {
        val jd0 = julianDay(date, 12.0)  // noon on that day
        val T = (jd0 - 2451545.0) / 36525.0

        // Sun mean longitude and anomaly
        val L0 = normalizeAngle(280.46646 + 36000.76983 * T)
        val M  = normalizeAngle(357.52911 + 35999.05029 * T)
        val C  = 1.914602 * sin(M * DEG) + 0.019993 * sin(2 * M * DEG) + 0.000289 * sin(3 * M * DEG)
        val sunLon = L0 + C

        // Mean obliquity
        val epsilon = 23.439291 - 0.013004 * T

        // Right ascension and declination
        val sunRA  = atan2(cos(epsilon * DEG) * sin(sunLon * DEG), cos(sunLon * DEG)) * RAD
        val sunDec = asin(sin(epsilon * DEG) * sin(sunLon * DEG)) * RAD

        // Hour angle for standard altitude -0.833° (horizon + refraction)
        val h0 = -0.8333
        val cosHA = (sin(h0 * DEG) - sin(lat * DEG) * sin(sunDec * DEG)) /
                (cos(lat * DEG) * cos(sunDec * DEG))

        if (cosHA < -1.0 || cosHA > 1.0) return null  // Polar night / day

        val HA = acos(cosHA) * RAD  // hour angle in degrees

        // Transit time (approx)
        val transit = 12.0 - lon / 15.0 - (sunRA - T * 360.0) / 360.0

        return normalizeHour(if (rising) transit - HA / 15.0 else transit + HA / 15.0)
    }

    private fun normalizeHour(h: Double): Double {
        var r = h
        while (r < 0) r += 24.0
        while (r >= 24) r -= 24.0
        return r
    }

    // ─── Main calculation ────────────────────────────────────────────────────────
    fun calculate(
        date: LocalDate,
        lat: Double,
        lon: Double,
        timezone: String = "Asia/Kolkata"
    ): PanchangamResult {
        val zoneId = ZoneId.of(timezone)

        // ── Sunrise ──
        val srUT = sunriseUT(date, lat, lon) ?: 6.0
        val ssUT = sunsetUT(date, lat, lon) ?: 18.0

        // Convert UT to local time
        val tzOffsetHours = zoneId.rules.getOffset(
            date.atStartOfDay().toInstant(ZoneOffset.UTC)
        ).totalSeconds / 3600.0

        val srLocalHr = srUT + tzOffsetHours
        val ssLocalHr = ssUT + tzOffsetHours

        val srLocalMin = (srLocalHr * 60).roundToInt()
        val ssLocalMin = (ssLocalHr * 60).roundToInt()

        val sunriseLocal = date.atTime(srLocalMin / 60, srLocalMin % 60)
        val sunsetLocal  = date.atTime(ssLocalMin / 60, ssLocalMin % 60)

        // JD at sunrise
        val jdSunrise = julianDay(date, srUT)

        // ── Tithi with udaya rule ──
        var tithiNum = tithiAt(jdSunrise)
        var tithiEndJd = findTithiEnd(tithiNum, jdSunrise)

        // Apply 48-min udaya rule (loop for kshaya)
        var safetyCount = 0
        while (tithiEndJd != null && safetyCount < 5) {
            val tithiEndUT = jdToUT(tithiEndJd)
            val tithiEndLocalHr = tithiEndUT + tzOffsetHours
            val minAfterSunrise = (tithiEndLocalHr - srLocalHr) * 60.0
            if (minAfterSunrise <= 0 || minAfterSunrise >= UDAYA_MINUTES) break
            tithiNum = (tithiNum + 1) % 30
            tithiEndJd = findTithiEnd(tithiNum, tithiEndJd)
            safetyCount++
        }

        val tithiEndLocal = tithiEndJd?.let {
            val hr = jdToUT(it) + tzOffsetHours
            val min = (hr * 60).roundToInt()
            date.atTime(min / 60, min % 60)
        }

        // ── Nakshatra with udaya rule ──
        var nakNum = nakshatraAt(jdSunrise)
        var nakEndJd = findNakshatraEnd(nakNum, jdSunrise)

        safetyCount = 0
        while (nakEndJd != null && safetyCount < 5) {
            val nakEndUT = jdToUT(nakEndJd)
            val nakEndLocalHr = nakEndUT + tzOffsetHours
            val minAfterSunrise = (nakEndLocalHr - srLocalHr) * 60.0
            if (minAfterSunrise <= 0 || minAfterSunrise >= UDAYA_MINUTES) break
            nakNum = (nakNum + 1) % 27
            nakEndJd = findNakshatraEnd(nakNum, nakEndJd)
            safetyCount++
        }

        val nakEndLocal = nakEndJd?.let {
            val hr = jdToUT(it) + tzOffsetHours
            val min = (hr * 60).roundToInt()
            date.atTime(min / 60, min % 60)
        }

        // ── Yoga ──
        val yogaNum = yogaAt(jdSunrise)
        val yogaEndLocal = findYogaEnd(yogaNum, jdSunrise)?.let {
            val hr = jdToUT(it) + tzOffsetHours
            val min = (hr * 60).roundToInt()
            date.atTime(min / 60, min % 60)
        }

        // ── Karana ──
        val karanaNum = karanaAt(jdSunrise)

        // ── Vara (weekday) ──
        val vara = date.dayOfWeek.value  // 1=Mon..7=Sun

        // ── Paksha ──
        val paksha = if (tithiNum < 15) "Shukla" else "Krishna"

        // ── Moon and Sun Rashi ──
        val moonRashi = floor(moonSiderealLongitude(jdSunrise) / 30.0).toInt().coerceIn(0, 11)
        val sunRashi  = floor(sunSiderealLongitude(jdSunrise) / 30.0).toInt().coerceIn(0, 11)

        // ── Inauspicious periods ──
        val dayMinutes = (ssLocalHr - srLocalHr) * 60.0
        val muhurta = dayMinutes / 8.0

        val rahuSlots  = mapOf(1 to 7, 2 to 1, 3 to 6, 4 to 4, 5 to 5, 6 to 3, 7 to 2)
        val yamaSlots  = mapOf(1 to 4, 2 to 3, 3 to 2, 4 to 1, 5 to 6, 6 to 5, 7 to 7)
        val gulikaSlots = mapOf(1 to 6, 2 to 5, 3 to 4, 4 to 3, 5 to 2, 6 to 1, 7 to 7)

        fun slotTime(slot: Int): Pair<String, String> {
            val startMin = ((slot - 1) * muhurta + srLocalMin).toInt()
            val endMin   = (slot * muhurta + srLocalMin).toInt()
            return "%02d:%02d".format(startMin / 60, startMin % 60) to
                    "%02d:%02d".format(endMin / 60, endMin % 60)
        }

        val rahuKalam   = slotTime(rahuSlots[vara] ?: 1)
        val yamagandam  = slotTime(yamaSlots[vara] ?: 1)
        val gulikaKalam = slotTime(gulikaSlots[vara] ?: 1)

        // ── Abhijit Muhurta ──
        val noonMin = ((srLocalHr + ssLocalHr) / 2.0 * 60).toInt()
        val abhijitStart = "%02d:%02d".format((noonMin - 24) / 60, (noonMin - 24) % 60)
        val abhijitEnd   = "%02d:%02d".format((noonMin + 24) / 60, (noonMin + 24) % 60)

        return PanchangamResult(
            date = date,
            tithiIndex = tithiNum,
            tithiEnd = tithiEndLocal,
            nakshatraIndex = nakNum,
            nakshatraEnd = nakEndLocal,
            yogaIndex = yogaNum,
            yogaEnd = yogaEndLocal,
            karanaIndex = karanaNum,
            vara = vara,
            paksha = paksha,
            moonRashiIndex = moonRashi,
            sunRashiIndex = sunRashi,
            sunrise = sunriseLocal,
            sunset = sunsetLocal,
            rahuKalam = rahuKalam,
            yamagandam = yamagandam,
            gulikaKalam = gulikaKalam,
            abhijitStart = abhijitStart,
            abhijitEnd = abhijitEnd
        )
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────────
    private fun normalizeAngle(deg: Double): Double {
        var d = deg % 360.0
        if (d < 0) d += 360.0
        return d
    }

    /** Julian day → UT hours (fractional hours of the day's fraction) */
    private fun jdToUT(jd: Double): Double = ((jd + 0.5) % 1.0) * 24.0

}

data class PanchangamResult(
    val date: LocalDate,
    val tithiIndex: Int,
    val tithiEnd: LocalDateTime?,
    val nakshatraIndex: Int,
    val nakshatraEnd: LocalDateTime?,
    val yogaIndex: Int,
    val yogaEnd: LocalDateTime?,
    val karanaIndex: Int,
    val vara: Int,
    val paksha: String,
    val moonRashiIndex: Int,
    val sunRashiIndex: Int,
    val sunrise: LocalDateTime,
    val sunset: LocalDateTime,
    val rahuKalam: Pair<String, String>,
    val yamagandam: Pair<String, String>,
    val gulikaKalam: Pair<String, String>,
    val abhijitStart: String,
    val abhijitEnd: String
)
