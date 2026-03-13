package com.panchangam100.live.ui.screens.festivals

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.panchangam100.live.astronomy.PanchangamEngine
import com.panchangam100.live.data.datastore.AppPreferences
import com.panchangam100.live.data.model.Language
import com.panchangam100.live.ui.theme.*
import com.panchangam100.live.utils.LanguageManager
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// ─── Festival Data Model ───
data class Festival(
    val nameKey: String,
    val namesByLang: Map<Language, String>,
    val date: LocalDate,
    val category: FestivalCategory,
    val isHoliday: Boolean = false,
    val description: String = ""
)

enum class FestivalCategory(val emoji: String, val color: Color) {
    VAISHNAVA("🪷", Color(0xFF6A1B9A)),
    SHAIVA("🔱", Color(0xFF1565C0)),
    DEVI("🌸", Color(0xFFAD1457)),
    SOLAR("☀️", Color(0xFFE65100)),
    NATIONAL("🇮🇳", Color(0xFF1B5E20)),
    REGIONAL("🌿", Color(0xFF2E7D32)),
    LUNAR("🌙", Color(0xFF37474F))
}

// ─── Festival database with accurate astronomical date calculation ───
object FestivalDatabase {

    // Tithi indices (0=Shukla Pratipada … 14=Pournami, 15=Krishna Pratipada … 29=Amavasya)
    private const val PRATIPADA   = 0   // Shukla 1
    private const val DVITIYA     = 1   // Shukla 2
    private const val TRITIYA     = 2   // Shukla 3
    private const val CHATURTHI   = 3   // Shukla 4
    private const val SAPTAMI     = 6   // Shukla 7
    private const val ASHTAMI     = 7   // Shukla 8
    private const val NAVAMI      = 8   // Shukla 9
    private const val DASHAMI     = 9   // Shukla 10
    private const val EKADASHI    = 10  // Shukla 11
    private const val DWADASHI    = 11  // Shukla 12
    private const val POURNAMI    = 14  // Full moon (Shukla 15)
    private const val KR_ASHTAMI = 22  // Krishna 8  (15+7)
    private const val KR_CHATURD = 28  // Krishna 14 (15+13)
    private const val AMAVASYA   = 29  // New moon   (Krishna 15)

    fun getFestivalsForYear(year: Int): List<Festival> {
        val eng = PanchangamEngine

        // ── Astronomical festivals ──

        // Makar Sankranti = Sun enters sidereal Makara (270°); always Jan 13–15
        val sankrantiDate = eng.makarSankrantiDate(year)

        // Ratha Saptami = Magha Shukla Saptami
        // Sun in Makara/Kumbha (270–330°); range Jan 22 – Feb 22
        // Start Jan 14 so we don't miss years where Saptami falls Jan 22-24 (e.g. 2026 = Jan 24)
        val rathaSaptami = eng.findDateForTithiInRange(
            LocalDate.of(year, 1, 14), SAPTAMI, 270.0, 330.0, 45)
            ?: LocalDate.of(year, 2, 4)

        // Maha Shivaratri = Magha/Phalguna Krishna Chaturdashi
        // Sun in Kumbha region (285–345°); typically Feb – early Mar
        val shivaratriDate = eng.findDateForTithiInRange(
            LocalDate.of(year, 1, 15), KR_CHATURD, 285.0, 345.0, 75)
            ?: LocalDate.of(year, 2, 26)

        // Ugadi = Chaitra Shukla Pratipada (or Amavasya day when Pratipada is kshaya).
        // Uses eng.ugadiDate() which handles both normal and kshaya-Pratipada years:
        // finds the Amavasya day with sun in Meena/Mesha (315°–45°), then checks the
        // next day; if Pratipada → next day is Ugadi, if Dvitiya (kshaya) → Amavasya = Ugadi.
        val ugadiDate = eng.ugadiDate(year)

        // Ram Navami = Chaitra Shukla Navami (~8 days after Ugadi)
        val ramNavami = eng.findDateForTithi(ugadiDate.plusDays(7), NAVAMI, 15)
            ?: ugadiDate.plusDays(8)

        // Hanuman Jayanti = Chaitra Pournami (Telugu tradition; ~14 days after Ugadi)
        val hanumanJayanti = eng.findDateForTithi(ugadiDate.plusDays(12), POURNAMI, 10)
            ?: ugadiDate.plusDays(14)

        // Akshaya Tritiya = Vaishakha Shukla Tritiya (NOT Chaitra Tritiya)
        // Start search 28 days after Ugadi — skips all of Chaitra masa and lands in Vaishakha.
        // Chaitra Tritiya (sun ~0–10°) is 2–3 days after Ugadi; Vaishakha Tritiya is ~29–32 days after Ugadi.
        val akshayaTritiya = eng.findDateForTithi(ugadiDate.plusDays(28), TRITIYA, 15)
            ?: ugadiDate.plusDays(30)

        // Krishna Janmashtami = Bhadrapada (Nija) Krishna Ashtami
        // Sun in Simha–Kanya; lower bound 118° to skip Adhika Bhadrapada (sun ~105–118°)
        // so we always land on the Nija (true) month, even in adhika-masa years.
        // Window 80 days from Jul 20 covers the widest possible shift.
        val janmashtami = eng.findDateForTithiInRange(
            LocalDate.of(year, 7, 20), KR_ASHTAMI, 118.0, 175.0, 80)
            ?: LocalDate.of(year, 8, 15)

        // Vinayaka Chaturthi = Bhadrapada (Nija) Shukla Chaturthi
        // Lower bound 118° skips Adhika Bhadrapada; 75-day window from Aug 1
        // covers adhika-masa years where Nija Bhadrapada falls in late Sep
        val vinayakaChaturthi = eng.findDateForTithiInRange(
            LocalDate.of(year, 8, 1), CHATURTHI, 118.0, 175.0, 75)
            ?: LocalDate.of(year, 8, 29)

        // Navaratri = Ashwina Shukla Pratipada
        // Sun in Kanya (148°–188°); typically Sep – mid Oct
        val navaratriDate = eng.findDateForTithiInRange(
            LocalDate.of(year, 9, 5), PRATIPADA, 148.0, 188.0, 50)
            ?: LocalDate.of(year, 10, 3)

        // Saraswati Puja / Maha Navami = Ashwina Shukla Navami (8th day of Navaratri)
        val saraswatiPuja = eng.findDateForTithi(navaratriDate.plusDays(6), NAVAMI, 6)
            ?: navaratriDate.plusDays(8)

        // Vijayadasami = Ashwina Shukla Dashami (~9 days after Navaratri)
        val vijayadasamiDate = eng.findDateForTithi(navaratriDate.plusDays(7), DASHAMI, 6)
            ?: navaratriDate.plusDays(9)

        // Diwali = Kartika Amavasya (Kartika: sun 175–228°; window 65 days covers late-Nov years)
        // Upper bound raised to 228° to cover years when Diwali falls in late November.
        val diwaliDate = eng.findDateForTithiInRange(
            LocalDate.of(year, 10, 5), AMAVASYA, 175.0, 228.0, 65)
            ?: LocalDate.of(year, 10, 24)

        // Narak Chaturdashi = always the day before Diwali (KR_CHATURD tithi 28 precedes Amavasya 29)
        val narakChaturdashi = diwaliDate.minusDays(1)

        // Karthika Pournami = Kartika Pournami
        // Lower bound 183° (not 195°) to cover years like 2026 where it falls Oct 25 (sun ~188°).
        // Ashwina Pournami has sun < 183°, so lowering to 183° still correctly excludes it.
        val karthikaPournami = eng.findDateForTithiInRange(
            LocalDate.of(year, 10, 20), POURNAMI, 183.0, 248.0, 45)
            ?: LocalDate.of(year, 11, 15)

        // Tulasi Vivah = Kartika Shukla Dwadashi (12th day after Diwali Amavasya)
        // Sun in Vrishchika (205°–248°); typically Nov
        val tulasiVivah = eng.findDateForTithiInRange(
            diwaliDate.plusDays(10), DWADASHI, 195.0, 250.0, 20)
            ?: diwaliDate.plusDays(12)

        // Vaikunta Ekadashi = Shukla Ekadashi in Dhanurmasa (sun in Dhanu 240–270°)
        // Dhanurmasa starts when sun sidereal = 240° (~Nov 16). Earliest Ekadashi
        // could be ~Nov 27. Start Nov 15 with 65-day window (→ Jan 19) for all years.
        val vaikuntaEkadashi = eng.findDateForTithiInRange(
            LocalDate.of(year, 11, 15), EKADASHI, 240.0, 272.0, 65)
            ?: LocalDate.of(year, 12, 11)

        return buildList {
            // ── Fixed-date national holidays ──
            add(festival("republic_day", year, 1, 26,
                "గణతంత్ర దినోత్సవం", "Republic Day", "குடியரசு தினம்",
                "റിപ്പബ്ലിക് ദിനം", "गणतंत्र दिवस", "ಗಣರಾಜ್ಯೋತ್ಸವ",
                FestivalCategory.NATIONAL, holiday = true))

            add(festival("independence_day", year, 8, 15,
                "స్వాతంత్ర్య దినోత్సవం", "Independence Day", "சுதந்திர தினம்",
                "സ്വാതന്ത്ര്യ ദിനം", "स्वतंत्रता दिवस", "ಸ್ವಾತಂತ್ರ್ಯ ದಿನಾಚರಣೆ",
                FestivalCategory.NATIONAL, holiday = true))

            add(festival("gandhi_jayanti", year, 10, 2,
                "గాంధీ జయంతి", "Gandhi Jayanti", "காந்தி ஜெயந்தி",
                "ഗാന്ധി ജയന്തി", "गांधी जयंती", "ಗಾಂಧಿ ಜಯಂತಿ",
                FestivalCategory.NATIONAL, holiday = true))

            // ── Astronomical (lunar) festivals ──
            add(Festival(
                nameKey = "sankranthi",
                namesByLang = mapOf(Language.TELUGU to "సంక్రాంతి", Language.ENGLISH to "Makar Sankranti",
                    Language.TAMIL to "பொங்கல்", Language.MALAYALAM to "മകർ സംക്രാന്തി",
                    Language.HINDI to "मकर संक्रांति", Language.KANNADA to "ಮಕರ ಸಂಕ್ರಾಂತಿ"),
                date = sankrantiDate, category = FestivalCategory.SOLAR, isHoliday = true))

            add(Festival(
                nameKey = "ratha_saptami",
                namesByLang = mapOf(Language.TELUGU to "రథ సప్తమి", Language.ENGLISH to "Ratha Saptami",
                    Language.TAMIL to "ரத சப்தமி", Language.MALAYALAM to "രഥ സപ്തമി",
                    Language.HINDI to "रथ सप्तमी", Language.KANNADA to "ರಥ ಸಪ್ತಮಿ"),
                date = rathaSaptami, category = FestivalCategory.SOLAR, isHoliday = false))

            add(Festival(
                nameKey = "shivaratri",
                namesByLang = mapOf(Language.TELUGU to "మహా శివరాత్రి", Language.ENGLISH to "Maha Shivaratri",
                    Language.TAMIL to "மஹா சிவராத்திரி", Language.MALAYALAM to "മഹാ ശിവരാത്രി",
                    Language.HINDI to "महा शिवरात्रि", Language.KANNADA to "ಮಹಾ ಶಿವರಾತ್ರಿ"),
                date = shivaratriDate, category = FestivalCategory.SHAIVA, isHoliday = true))

            add(Festival(
                nameKey = "ugadi",
                namesByLang = mapOf(Language.TELUGU to "ఉగాది", Language.ENGLISH to "Ugadi",
                    Language.TAMIL to "உகாதி", Language.MALAYALAM to "ഉഗാദി",
                    Language.HINDI to "उगादि", Language.KANNADA to "ಯುಗಾದಿ"),
                date = ugadiDate, category = FestivalCategory.SOLAR, isHoliday = true))

            add(Festival(
                nameKey = "ram_navami",
                namesByLang = mapOf(Language.TELUGU to "శ్రీరామ నవమి", Language.ENGLISH to "Sri Rama Navami",
                    Language.TAMIL to "ராம நவமி", Language.MALAYALAM to "ശ്രീരാമ നവമി",
                    Language.HINDI to "राम नवमी", Language.KANNADA to "ಶ್ರೀ ರಾಮ ನವಮಿ"),
                date = ramNavami, category = FestivalCategory.VAISHNAVA, isHoliday = true))

            add(Festival(
                nameKey = "hanuman_jayanti",
                namesByLang = mapOf(Language.TELUGU to "హనుమాన్ జయంతి", Language.ENGLISH to "Hanuman Jayanti",
                    Language.TAMIL to "ஆஞ்சநேயர் ஜயந்தி", Language.MALAYALAM to "ഹനുമാൻ ജയന്തി",
                    Language.HINDI to "हनुमान जयंती", Language.KANNADA to "ಹನುಮಾನ್ ಜಯಂತಿ"),
                date = hanumanJayanti, category = FestivalCategory.VAISHNAVA, isHoliday = false))

            add(Festival(
                nameKey = "akshaya_tritiya",
                namesByLang = mapOf(Language.TELUGU to "అక్షయ తృతీయ", Language.ENGLISH to "Akshaya Tritiya",
                    Language.TAMIL to "அட்சய திருதியை", Language.MALAYALAM to "അക്ഷയ തൃതീയ",
                    Language.HINDI to "अक्षय तृतीया", Language.KANNADA to "ಅಕ್ಷಯ ತೃತೀಯ"),
                date = akshayaTritiya, category = FestivalCategory.SOLAR, isHoliday = false))

            add(Festival(
                nameKey = "janmashtami",
                namesByLang = mapOf(Language.TELUGU to "కృష్ణాష్టమి", Language.ENGLISH to "Krishna Janmashtami",
                    Language.TAMIL to "கோகுலாஷ்டமி", Language.MALAYALAM to "കൃഷ്ണ ജന്മാഷ്ടമി",
                    Language.HINDI to "कृष्ण जन्माष्टमी", Language.KANNADA to "ಕೃಷ್ಣ ಜನ್ಮಾಷ್ಟಮಿ"),
                date = janmashtami, category = FestivalCategory.VAISHNAVA, isHoliday = true))

            add(Festival(
                nameKey = "vinayaka_chaturthi",
                namesByLang = mapOf(Language.TELUGU to "వినాయక చవితి", Language.ENGLISH to "Ganesh Chaturthi",
                    Language.TAMIL to "விநாயக சதுர்த்தி", Language.MALAYALAM to "വിനായക ചതുർഥി",
                    Language.HINDI to "गणेश चतुर्थी", Language.KANNADA to "ವಿನಾಯಕ ಚತುರ್ಥಿ"),
                date = vinayakaChaturthi, category = FestivalCategory.SHAIVA, isHoliday = true))

            add(Festival(
                nameKey = "navaratri",
                namesByLang = mapOf(Language.TELUGU to "నవరాత్రులు", Language.ENGLISH to "Navaratri",
                    Language.TAMIL to "நவராத்திரி", Language.MALAYALAM to "നവരാത്രി",
                    Language.HINDI to "नवरात्रि", Language.KANNADA to "ನವರಾತ್ರಿ"),
                date = navaratriDate, category = FestivalCategory.DEVI, isHoliday = false))

            add(Festival(
                nameKey = "saraswati_puja",
                namesByLang = mapOf(Language.TELUGU to "సరస్వతీ పూజ / మహానవమి", Language.ENGLISH to "Saraswati Puja / Maha Navami",
                    Language.TAMIL to "சரஸ்வதி பூஜை", Language.MALAYALAM to "സരസ്വതി പൂജ",
                    Language.HINDI to "सरस्वती पूजा / महानवमी", Language.KANNADA to "ಸರಸ್ವತಿ ಪೂಜೆ"),
                date = saraswatiPuja, category = FestivalCategory.DEVI, isHoliday = false))

            add(Festival(
                nameKey = "dasara",
                namesByLang = mapOf(Language.TELUGU to "విజయదశమి", Language.ENGLISH to "Vijayadasami / Dasara",
                    Language.TAMIL to "விஜயதசமி", Language.MALAYALAM to "വിജയദശമി",
                    Language.HINDI to "विजयादशमी", Language.KANNADA to "ವಿಜಯದಶಮಿ"),
                date = vijayadasamiDate, category = FestivalCategory.DEVI, isHoliday = true))

            add(Festival(
                nameKey = "narak_chaturdashi",
                namesByLang = mapOf(Language.TELUGU to "నరక చతుర్దశి", Language.ENGLISH to "Narak Chaturdashi",
                    Language.TAMIL to "நரக சதுர்தசி", Language.MALAYALAM to "നരക ചതുർദ്ദശി",
                    Language.HINDI to "नरक चतुर्दशी", Language.KANNADA to "ನರಕ ಚತುರ್ದಶಿ"),
                date = narakChaturdashi, category = FestivalCategory.LUNAR, isHoliday = false))

            add(Festival(
                nameKey = "diwali",
                namesByLang = mapOf(Language.TELUGU to "దీపావళి", Language.ENGLISH to "Diwali / Deepavali",
                    Language.TAMIL to "தீபாவளி", Language.MALAYALAM to "ദീപാവലി",
                    Language.HINDI to "दीपावली", Language.KANNADA to "ದೀಪಾವಳಿ"),
                date = diwaliDate, category = FestivalCategory.LUNAR, isHoliday = true))

            add(Festival(
                nameKey = "karthika_pournami",
                namesByLang = mapOf(Language.TELUGU to "కార్తీక పౌర్ణమి", Language.ENGLISH to "Karthika Pournami",
                    Language.TAMIL to "கார்த்திகை தீபம்", Language.MALAYALAM to "കാർത്തിക പൂർണ്ണിമ",
                    Language.HINDI to "कार्तिक पूर्णिमा", Language.KANNADA to "ಕಾರ್ತಿಕ ಪೌರ್ಣಮಿ"),
                date = karthikaPournami, category = FestivalCategory.SHAIVA, isHoliday = false))

            add(Festival(
                nameKey = "tulasi_vivah",
                namesByLang = mapOf(Language.TELUGU to "తులసి వివాహం", Language.ENGLISH to "Tulasi Vivah",
                    Language.TAMIL to "துளசி விவாஹம்", Language.MALAYALAM to "തുളസി വിവാഹം",
                    Language.HINDI to "तुलसी विवाह", Language.KANNADA to "ತುಳಸಿ ವಿವಾಹ"),
                date = tulasiVivah, category = FestivalCategory.VAISHNAVA, isHoliday = false))

            add(Festival(
                nameKey = "vaikunta_ekadashi",
                namesByLang = mapOf(Language.TELUGU to "వైకుంఠ ఏకాదశి", Language.ENGLISH to "Vaikunta Ekadashi",
                    Language.TAMIL to "வைகுண்ட ஏகாதசி", Language.MALAYALAM to "വൈകുണ്ഠ ഏകാദശി",
                    Language.HINDI to "वैकुण्ठ एकादशी", Language.KANNADA to "ವೈಕುಂಠ ಏಕಾದಶಿ"),
                date = vaikuntaEkadashi, category = FestivalCategory.VAISHNAVA, isHoliday = true))

        }.sortedBy { it.date }
    }

    private fun festival(
        key: String, year: Int, month: Int, day: Int,
        te: String, en: String, ta: String, ml: String, hi: String, kn: String,
        cat: FestivalCategory, holiday: Boolean
    ) = Festival(
        nameKey = key,
        namesByLang = mapOf(Language.TELUGU to te, Language.ENGLISH to en,
            Language.TAMIL to ta, Language.MALAYALAM to ml,
            Language.HINDI to hi, Language.KANNADA to kn),
        date = LocalDate.of(year, month, day),
        category = cat, isHoliday = holiday
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FestivalsScreen(
    prefs: AppPreferences,
    onDateClick: (String) -> Unit
) {
    val lang by prefs.languageFlow.collectAsState(initial = Language.TELUGU)
    val today = LocalDate.now()
    var selectedYear by remember { mutableStateOf(today.year) }
    val festivals = remember(selectedYear) { FestivalDatabase.getFestivalsForYear(selectedYear) }
    val upcoming = remember(festivals, today) { festivals.filter { !it.date.isBefore(today) } }
    val past = remember(festivals, today) { festivals.filter { it.date.isBefore(today) } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        LanguageManager.label("festivals", lang),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        "Festivals & Holidays",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { if (selectedYear > 2020) selectedYear-- }) {
                        Icon(Icons.Default.ChevronLeft, null)
                    }
                    Text(
                        selectedYear.toString(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { if (selectedYear < 2120) selectedYear++ }) {
                        Icon(Icons.Default.ChevronRight, null)
                    }
                }
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (upcoming.isNotEmpty()) {
                item {
                    Text(
                        LanguageManager.label("upcoming", lang),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                items(upcoming) { festival ->
                    FestivalCard(festival = festival, lang = lang, onClick = { onDateClick(festival.date.toString()) })
                }
            }
            if (past.isNotEmpty() && selectedYear == today.year) {
                item {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        LanguageManager.label("past", lang),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                items(past) { festival ->
                    FestivalCard(festival = festival, lang = lang, past = true, onClick = { onDateClick(festival.date.toString()) })
                }
            }
            if (selectedYear != today.year) {
                items(festivals) { festival ->
                    FestivalCard(festival = festival, lang = lang, onClick = { onDateClick(festival.date.toString()) })
                }
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun FestivalCard(
    festival: Festival,
    lang: Language,
    past: Boolean = false,
    onClick: () -> Unit
) {
    val name = festival.namesByLang[lang] ?: festival.namesByLang[Language.ENGLISH] ?: ""
    val fmt = DateTimeFormatter.ofPattern("dd MMM yyyy")

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 1.dp,
        color = if (past) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        else MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category emoji / color indicator
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(festival.category.color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(festival.category.emoji, style = MaterialTheme.typography.titleLarge)
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = if (past) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    festival.date.format(fmt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (festival.isHoliday) {
                    Text(
                        LanguageManager.label("publicHoliday", lang),
                        style = MaterialTheme.typography.labelSmall,
                        color = AuspiciousGreen
                    )
                }
            }

            Icon(
                Icons.Default.ChevronRight,
                null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
