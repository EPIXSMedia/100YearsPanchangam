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
    // Precise base value at J2000.0: 23°51′11.4″ = 23.85317°
    // Precession rate: 50.2877″/year (Newcomb formula)
    // Matches the Government of India Calendar Reform Committee standard.
    private fun lahiriAyanamsa(jd: Double): Double {
        val T = (jd - 2451545.0) / 36525.0  // Julian centuries from J2000.0
        val ayan = 23.85306 + (50.2877 * T * 100.0 / 3600.0) +
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

    // ─── Sunrise / Sunset (NOAA full algorithm) ─────────────────────────────────
    /**
     * Returns sunrise/sunset time as UT hours on [date] for [lat]/[lon] (degrees east positive).
     *
     * Uses the NOAA Solar Calculator algorithm (https://gml.noaa.gov/grad/solcalc/).
     * Includes: geometric mean longitude, equation of center, apparent longitude,
     * corrected obliquity, equation of time, and horizon hour-angle.
     * Accuracy: ~1 minute for latitudes up to ±60°.
     * Standard atmospheric refraction + sun's disc: -0.8333°.
     */
    fun sunriseUT(date: LocalDate, lat: Double, lon: Double): Double? =
        sunRiseSetUT(date, lat, lon, rising = true)

    fun sunsetUT(date: LocalDate, lat: Double, lon: Double): Double? =
        sunRiseSetUT(date, lat, lon, rising = false)

    private fun sunRiseSetUT(date: LocalDate, lat: Double, lon: Double, rising: Boolean): Double? {
        val jd = julianDay(date, 12.0)          // JD at noon UT (start estimate)
        val T  = (jd - 2451545.0) / 36525.0    // Julian centuries from J2000.0
        val T2 = T * T

        // ── Geometric Mean Longitude & Anomaly ──
        val L0   = normalizeAngle(280.46646 + 36000.76983 * T + 0.0003032 * T2)
        val M    = normalizeAngle(357.52911 + 35999.05029 * T - 0.0001537 * T2)
        val Mrad = M * DEG
        val e    = 0.016708634 - 0.000042037 * T - 0.0000001267 * T2  // eccentricity

        // ── Equation of Center ──
        val C = sin(Mrad) * (1.914602 - 0.004817 * T - 0.000014 * T2) +
                sin(2 * Mrad) * (0.019993 - 0.000101 * T) +
                sin(3 * Mrad) * 0.000289

        // ── Apparent longitude (nutation + aberration) ──
        val Omega    = normalizeAngle(125.04 - 1934.136 * T)
        val theta    = normalizeAngle(L0 + C - 0.00569 - 0.00478 * sin(Omega * DEG))
        val thetaRad = theta * DEG

        // ── Corrected obliquity of the ecliptic ──
        val eps0   = 23.0 + (26.0 + (21.448 - T * (46.8150 + T * (0.00059 - T * 0.001813))) / 60.0) / 60.0
        val eps    = eps0 + 0.00256 * cos(Omega * DEG)
        val epsRad = eps * DEG

        // ── Sun's declination ──
        val sinDec = sin(epsRad) * sin(thetaRad)
        val dec    = asin(sinDec)

        // ── Equation of Time (minutes of time) ──
        val L0rad = L0 * DEG
        val y     = tan(epsRad / 2.0).let { it * it }
        val eqT   = 4.0 * RAD * (
                y * sin(2 * L0rad)
              - 2 * e * sin(Mrad)
              + 4 * e * y * sin(Mrad) * cos(2 * L0rad)
              - 0.5 * y * y * sin(4 * L0rad)
              - 1.25 * e * e * sin(2 * Mrad))  // minutes

        // ── Hour angle for altitude = -0.8333° (refraction + disc) ──
        val cosHA = (sin(-0.8333 * DEG) - sin(lat * DEG) * sinDec) /
                    (cos(lat * DEG) * cos(dec))
        if (cosHA < -1.0 || cosHA > 1.0) return null  // polar day or night

        val HA = acos(cosHA) * RAD  // degrees

        // ── Solar noon in UT minutes (lon positive east) ──
        val noonMinUT  = 720.0 - 4.0 * lon - eqT

        // ── Rise/set in UT hours ──
        val eventMinUT = if (rising) noonMinUT - HA * 4.0 else noonMinUT + HA * 4.0
        return eventMinUT / 60.0
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
        val srUT = sunriseUT(date, lat, lon) ?: 0.5   // fallback 0:30 UT (≈6 AM IST)
        val ssUT = sunsetUT(date, lat, lon) ?: 12.5   // fallback 12:30 UT (≈18 PM IST)

        // Convert UT to local time (normalize to 0..24 to handle midnight crossings)
        val tzOffsetHours = zoneId.rules.getOffset(
            date.atStartOfDay().toInstant(ZoneOffset.UTC)
        ).totalSeconds / 3600.0

        // Normalize to 0-<24 range: sunrise may be ~23h UT for eastern timezones
        fun normalizeLocalHr(utHr: Double): Double {
            var h = (utHr + tzOffsetHours) % 24.0
            if (h < 0) h += 24.0
            return h
        }

        val srLocalHr = normalizeLocalHr(srUT)
        val ssLocalHr = run {
            var h = normalizeLocalHr(ssUT)
            // Sunset must always be after sunrise
            if (h <= srLocalHr) h += 24.0
            h
        }

        fun hrToLocalTime(hr: Double): LocalDateTime {
            val normalHr = hr % 24.0
            val h = normalHr.toInt().coerceIn(0, 23)
            val m = ((normalHr - h) * 60).roundToInt().coerceIn(0, 59)
            return date.atTime(h, m)
        }

        val sunriseLocal = hrToLocalTime(srLocalHr % 24.0)
        val sunsetLocal  = hrToLocalTime(ssLocalHr % 24.0)

        // JD at sunrise
        val jdSunrise = julianDay(date, srUT)

        // ── Tithi with udaya rule ──
        var tithiNum = tithiAt(jdSunrise)
        var tithiEndJd = findTithiEnd(tithiNum, jdSunrise)

        // Apply 48-min udaya rule (loop for kshaya)
        // Compare using JD difference (avoids timezone boundary issues)
        var safetyCount = 0
        while (tithiEndJd != null && safetyCount < 5) {
            val minAfterSunrise = (tithiEndJd - jdSunrise) * 24.0 * 60.0
            if (minAfterSunrise <= 0 || minAfterSunrise >= UDAYA_MINUTES) break
            tithiNum = (tithiNum + 1) % 30
            tithiEndJd = findTithiEnd(tithiNum, tithiEndJd)
            safetyCount++
        }

        val tithiEndLocal = tithiEndJd?.let { hrToLocalTime(normalizeLocalHr(jdToUT(it))) }

        // ── Nakshatra with udaya rule ──
        var nakNum = nakshatraAt(jdSunrise)
        var nakEndJd = findNakshatraEnd(nakNum, jdSunrise)

        safetyCount = 0
        while (nakEndJd != null && safetyCount < 5) {
            val minAfterSunrise = (nakEndJd - jdSunrise) * 24.0 * 60.0
            if (minAfterSunrise <= 0 || minAfterSunrise >= UDAYA_MINUTES) break
            nakNum = (nakNum + 1) % 27
            nakEndJd = findNakshatraEnd(nakNum, nakEndJd)
            safetyCount++
        }

        val nakEndLocal = nakEndJd?.let { hrToLocalTime(normalizeLocalHr(jdToUT(it))) }

        // ── Yoga with udaya rule ──
        var yogaNum = yogaAt(jdSunrise)
        var yogaEndJd = findYogaEnd(yogaNum, jdSunrise)

        safetyCount = 0
        while (yogaEndJd != null && safetyCount < 5) {
            val minAfterSunrise = (yogaEndJd - jdSunrise) * 24.0 * 60.0
            if (minAfterSunrise <= 0 || minAfterSunrise >= UDAYA_MINUTES) break
            yogaNum = (yogaNum + 1) % 27
            yogaEndJd = findYogaEnd(yogaNum, yogaEndJd)
            safetyCount++
        }

        val yogaEndLocal = yogaEndJd?.let { hrToLocalTime(normalizeLocalHr(jdToUT(it))) }

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
        // Use actual day length in minutes (ssLocalHr may be >24 so subtract)
        val srMinOfDay = (srLocalHr * 60).roundToInt()
        val ssMinOfDay = run {
            var m = (ssLocalHr * 60).roundToInt()
            if (m >= 24 * 60) m -= 24 * 60
            m
        }
        val dayMinutes = if (ssMinOfDay > srMinOfDay) (ssMinOfDay - srMinOfDay).toDouble()
                         else (ssMinOfDay + 24 * 60 - srMinOfDay).toDouble()
        val muhurta = dayMinutes / 8.0

        // Verified slots (Sun=7,Mon=1..Sat=6 in vara system, day split into 8 equal parts)
        // Rahu:  Sun=8,Mon=2,Tue=7,Wed=5,Thu=6,Fri=4,Sat=3
        // Yama:  Sun=5,Mon=4,Tue=3,Wed=2,Thu=1,Fri=7,Sat=6
        // Gulika:Sun=7,Mon=6,Tue=5,Wed=4,Thu=3,Fri=2,Sat=1
        val rahuSlots   = mapOf(1 to 2, 2 to 7, 3 to 5, 4 to 6, 5 to 4, 6 to 3, 7 to 8)
        val yamaSlots   = mapOf(1 to 4, 2 to 3, 3 to 2, 4 to 1, 5 to 7, 6 to 6, 7 to 5)
        val gulikaSlots = mapOf(1 to 6, 2 to 5, 3 to 4, 4 to 3, 5 to 2, 6 to 1, 7 to 7)

        fun minToHHMM(totalMin: Int): String {
            val m = ((totalMin % (24 * 60)) + 24 * 60) % (24 * 60)
            return "%02d:%02d".format(m / 60, m % 60)
        }

        fun slotTime(slot: Int): Pair<String, String> {
            val startMin = (srMinOfDay + (slot - 1) * muhurta).toInt()
            val endMin   = (srMinOfDay + slot * muhurta).toInt()
            return minToHHMM(startMin) to minToHHMM(endMin)
        }

        val rahuKalam   = slotTime(rahuSlots[vara] ?: 1)
        val yamagandam  = slotTime(yamaSlots[vara] ?: 1)
        val gulikaKalam = slotTime(gulikaSlots[vara] ?: 1)

        // ── Abhijit Muhurta ──
        val noonMin = srMinOfDay + (dayMinutes / 2).toInt()
        val abhijitStart = minToHHMM(noonMin - 24)
        val abhijitEnd   = minToHHMM(noonMin + 24)

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

    // ─── Festival Date Calculation ────────────────────────────────────────────────
    /**
     * Find first date >= [startDate] where the udaya-corrected tithi at sunrise equals [tithiIdx] (0-29).
     * Uses Hyderabad (17.38°N, 78.49°E) as the reference for sunrise. Searches up to [maxDays].
     * Applies the same 48-min udaya rule as calculate() so festival dates match the home screen.
     */
    fun findDateForTithi(startDate: LocalDate, tithiIdx: Int, maxDays: Int = 45): LocalDate? {
        val lat = 17.38; val lon = 78.49
        var d = startDate
        repeat(maxDays) {
            val srUT = sunriseUT(d, lat, lon) ?: 0.5
            val jdSr = julianDay(d, srUT)
            if (udayaTithi(jdSr) == tithiIdx) return d
            d = d.plusDays(1)
        }
        return null
    }

    /**
     * Like [findDateForTithi] but also requires sun sidereal longitude to be in [sunLonMin, sunLonMax).
     * Handles wrap-around when sunLonMin > sunLonMax (e.g., 315..45 crossing 0°).
     * Applies the 48-min udaya rule so festival dates match the home screen tithi.
     */
    fun findDateForTithiInRange(
        startDate: LocalDate,
        tithiIdx: Int,
        sunLonMin: Double,
        sunLonMax: Double,
        maxDays: Int = 60
    ): LocalDate? {
        val lat = 17.38; val lon = 78.49
        var d = startDate
        repeat(maxDays) {
            val srUT = sunriseUT(d, lat, lon) ?: 0.5
            val jdSr = julianDay(d, srUT)
            if (udayaTithi(jdSr) == tithiIdx) {
                val sunLon = sunSiderealLongitude(jdSr)
                val inRange = if (sunLonMin <= sunLonMax) {
                    sunLon >= sunLonMin && sunLon < sunLonMax
                } else {
                    sunLon >= sunLonMin || sunLon < sunLonMax
                }
                if (inRange) return d
            }
            d = d.plusDays(1)
        }
        return null
    }

    /**
     * Returns the udaya-corrected tithi for a given sunrise JD.
     * If the raw tithi ends within 48 minutes after sunrise, advances to the next tithi.
     */
    private fun udayaTithi(jdSunrise: Double): Int {
        var tithi = tithiAt(jdSunrise)
        var endJd = findTithiEnd(tithi, jdSunrise)
        var safety = 0
        while (endJd != null && safety < 5) {
            val minAfterSunrise = (endJd - jdSunrise) * 24.0 * 60.0
            if (minAfterSunrise <= 0 || minAfterSunrise >= UDAYA_MINUTES) break
            tithi = (tithi + 1) % 30
            endJd = findTithiEnd(tithi, endJd)
            safety++
        }
        return tithi
    }

    /**
     * Computes Ugadi (Chaitra Shukla Pratipada) for the given year.
     *
     * Algorithm:
     * 1. Find the Amavasya (new moon day) when the sun is in Meena–Mesha transition (315°–45°).
     * 2. If the following day has Pratipada at sunrise → that day is Ugadi (normal year).
     * 3. If the following day has Dvitiya at sunrise (kshaya/invisible Pratipada) → the
     *    Amavasya day itself is Ugadi, per South Indian tradition.
     *
     * This handles both normal and kshaya-Pratipada years correctly regardless of
     * minor moon-longitude errors that shift the computed new moon by ~1 day.
     */
    fun ugadiDate(year: Int): LocalDate {
        val lat = 17.38; val lon = 78.49
        var d = LocalDate.of(year, 3, 14)
        repeat(50) {
            val srUT = sunriseUT(d, lat, lon) ?: 0.5
            val jdSr = julianDay(d, srUT)
            if (udayaTithi(jdSr) == 29) {  // Amavasya = tithi 29
                val sunLon = sunSiderealLongitude(jdSr)
                val inRange = sunLon >= 315.0 || sunLon < 45.0
                if (inRange) {
                    val nextDay = d.plusDays(1)
                    val srUTNext = sunriseUT(nextDay, lat, lon) ?: 0.5
                    val jdSrNext = julianDay(nextDay, srUTNext)
                    return if (udayaTithi(jdSrNext) == 0) nextDay else d
                }
            }
            d = d.plusDays(1)
        }
        return LocalDate.of(year, 3, 30)
    }

    /** Date when sun enters Makara (sidereal 270°) — Makar Sankranti, always Jan 13-15. */
    fun makarSankrantiDate(year: Int): LocalDate {
        var d = LocalDate.of(year, 1, 12)
        repeat(6) {
            val jd     = julianDay(d, 12.0)
            val jdNext = julianDay(d.plusDays(1), 12.0)
            val sNow   = sunSiderealLongitude(jd)
            val sNext  = sunSiderealLongitude(jdNext)
            if (sNow < 270.0 && sNext >= 270.0) return d.plusDays(1)
            if (sNow >= 270.0) return d
            d = d.plusDays(1)
        }
        return LocalDate.of(year, 1, 14)
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
