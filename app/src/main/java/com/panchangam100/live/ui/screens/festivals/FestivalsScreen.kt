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

// ─── Static festival database (lunar/solar based — approximate Gregorian for demo) ───
object FestivalDatabase {

    fun getFestivalsForYear(year: Int): List<Festival> {
        return buildList {
            // Ugadi / Telugu & Kannada New Year (Chaitra Shukla Pratipada) — typically March/April
            add(Festival(
                nameKey = "ugadi",
                namesByLang = mapOf(
                    Language.TELUGU to "ఉగాది", Language.ENGLISH to "Ugadi",
                    Language.TAMIL to "உகாதி", Language.MALAYALAM to "ഉഗാദി",
                    Language.HINDI to "उगादि", Language.KANNADA to "ಯುಗಾದಿ"
                ),
                date = LocalDate.of(year, if (year % 4 == 2) 3 else 3, if (year % 2 == 0) 30 else 22),
                category = FestivalCategory.SOLAR,
                isHoliday = true
            ))
            // Ram Navami
            add(Festival(
                nameKey = "ram_navami",
                namesByLang = mapOf(
                    Language.TELUGU to "శ్రీరామ నవమి", Language.ENGLISH to "Sri Rama Navami",
                    Language.TAMIL to "ராம நவமி", Language.MALAYALAM to "ശ്രീരാമ നവമി",
                    Language.HINDI to "राम नवमी", Language.KANNADA to "ಶ್ರೀ ರಾಮ ನವಮಿ"
                ),
                date = LocalDate.of(year, 4, if (year % 3 == 0) 6 else 9),
                category = FestivalCategory.VAISHNAVA,
                isHoliday = true
            ))
            // Hanuman Jayanti
            add(Festival(
                nameKey = "hanuman_jayanti",
                namesByLang = mapOf(
                    Language.TELUGU to "హనుమాన్ జయంతి", Language.ENGLISH to "Hanuman Jayanti",
                    Language.TAMIL to "ஆஞ்சநேயர் ஜயந்தி", Language.MALAYALAM to "ഹനുമാൻ ജയന്തി",
                    Language.HINDI to "हनुमान जयंती", Language.KANNADA to "ಹನುಮಾನ್ ಜಯಂತಿ"
                ),
                date = LocalDate.of(year, 4, 23),
                category = FestivalCategory.VAISHNAVA,
                isHoliday = true
            ))
            // Akshaya Tritiya
            add(Festival(
                nameKey = "akshaya_tritiya",
                namesByLang = mapOf(
                    Language.TELUGU to "అక్షయ తృతీయ", Language.ENGLISH to "Akshaya Tritiya",
                    Language.TAMIL to "அட்சய திருதியை", Language.MALAYALAM to "അക്ഷയ തൃതീയ",
                    Language.HINDI to "अक्षय तृतीया", Language.KANNADA to "ಅಕ್ಷಯ ತೃತೀಯ"
                ),
                date = LocalDate.of(year, 5, 1),
                category = FestivalCategory.SOLAR,
                isHoliday = false
            ))
            // Vaikunta Ekadashi (December)
            add(Festival(
                nameKey = "vaikunta_ekadashi",
                namesByLang = mapOf(
                    Language.TELUGU to "వైకుంఠ ఏకాదశి", Language.ENGLISH to "Vaikunta Ekadashi",
                    Language.TAMIL to "வைகுண்ட ஏகாதசி", Language.MALAYALAM to "വൈകുണ്ഠ ഏകാദശി",
                    Language.HINDI to "वैकुण्ठ एकादशी", Language.KANNADA to "ವೈಕುಂಠ ಏಕಾದಶಿ"
                ),
                date = LocalDate.of(year, 12, if (year % 2 == 0) 11 else 27),
                category = FestivalCategory.VAISHNAVA,
                isHoliday = true
            ))
            // Maha Shivaratri (February/March)
            add(Festival(
                nameKey = "shivaratri",
                namesByLang = mapOf(
                    Language.TELUGU to "మహా శివరాత్రి", Language.ENGLISH to "Maha Shivaratri",
                    Language.TAMIL to "மஹா சிவராத்திரி", Language.MALAYALAM to "മഹാ ശിവരാത്രി",
                    Language.HINDI to "महा शिवरात्रि", Language.KANNADA to "ಮಹಾ ಶಿವರಾತ್ರಿ"
                ),
                date = LocalDate.of(year, 2, if (year % 3 == 0) 18 else 26),
                category = FestivalCategory.SHAIVA,
                isHoliday = true
            ))
            // Navaratri / Dasara
            add(Festival(
                nameKey = "navaratri",
                namesByLang = mapOf(
                    Language.TELUGU to "నవరాత్రులు", Language.ENGLISH to "Navaratri",
                    Language.TAMIL to "நவராத்திரி", Language.MALAYALAM to "നവരാത്രി",
                    Language.HINDI to "नवरात्रि", Language.KANNADA to "ನವರಾತ್ರಿ"
                ),
                date = LocalDate.of(year, 10, if (year % 2 == 0) 3 else 15),
                category = FestivalCategory.DEVI,
                isHoliday = false
            ))
            add(Festival(
                nameKey = "dasara",
                namesByLang = mapOf(
                    Language.TELUGU to "విజయదశమి", Language.ENGLISH to "Vijayadasami / Dasara",
                    Language.TAMIL to "விஜயதசமி", Language.MALAYALAM to "വിജയദശമി",
                    Language.HINDI to "विजयादशमी", Language.KANNADA to "ವಿಜಯದಶಮಿ"
                ),
                date = LocalDate.of(year, 10, if (year % 2 == 0) 12 else 24),
                category = FestivalCategory.DEVI,
                isHoliday = true
            ))
            // Diwali
            add(Festival(
                nameKey = "diwali",
                namesByLang = mapOf(
                    Language.TELUGU to "దీపావళి", Language.ENGLISH to "Diwali",
                    Language.TAMIL to "தீபாவளி", Language.MALAYALAM to "ദീപാവലി",
                    Language.HINDI to "दीपावली", Language.KANNADA to "ದೀಪಾವಳಿ"
                ),
                date = LocalDate.of(year, 10, if (year % 2 == 0) 20 else 31),
                category = FestivalCategory.LUNAR,
                isHoliday = true
            ))
            // Karthika Pournami
            add(Festival(
                nameKey = "karthika_pournami",
                namesByLang = mapOf(
                    Language.TELUGU to "కార్తీక పౌర్ణమి", Language.ENGLISH to "Karthika Pournami",
                    Language.TAMIL to "கார்த்திகை தீபம்", Language.MALAYALAM to "കാർത്തിക പൂർണ്ണിമ",
                    Language.HINDI to "कार्तिक पूर्णिमा", Language.KANNADA to "ಕಾರ್ತಿಕ ಪೌರ್ಣಮಿ"
                ),
                date = LocalDate.of(year, 11, if (year % 2 == 0) 15 else 27),
                category = FestivalCategory.SHAIVA,
                isHoliday = false
            ))
            // Sankranthi
            add(Festival(
                nameKey = "sankranthi",
                namesByLang = mapOf(
                    Language.TELUGU to "సంక్రాంతి", Language.ENGLISH to "Makar Sankranti",
                    Language.TAMIL to "பொங்கல்", Language.MALAYALAM to "മകർ വിളക്ക്",
                    Language.HINDI to "मकर संक्रांति", Language.KANNADA to "ಮಕರ ಸಂಕ್ರಾಂತಿ"
                ),
                date = LocalDate.of(year, 1, 14),
                category = FestivalCategory.SOLAR,
                isHoliday = true
            ))
            // Vinayaka Chaturthi
            add(Festival(
                nameKey = "vinayaka_chaturthi",
                namesByLang = mapOf(
                    Language.TELUGU to "వినాయక చవితి", Language.ENGLISH to "Ganesh Chaturthi",
                    Language.TAMIL to "விநாயக சதுர்த்தி", Language.MALAYALAM to "വിനായക ചതുർഥി",
                    Language.HINDI to "गणेश चतुर्थी", Language.KANNADA to "ವಿನಾಯಕ ಚತುರ್ಥಿ"
                ),
                date = LocalDate.of(year, 8, if (year % 2 == 0) 29 else 19),
                category = FestivalCategory.VAISHNAVA,
                isHoliday = true
            ))
            // Krishna Janmashtami
            add(Festival(
                nameKey = "janmashtami",
                namesByLang = mapOf(
                    Language.TELUGU to "కృష్ణాష్టమి", Language.ENGLISH to "Krishna Janmashtami",
                    Language.TAMIL to "கோகுலாஷ்டமி", Language.MALAYALAM to "കൃഷ്ണ ജന്മാഷ്ടമി",
                    Language.HINDI to "कृष्ण जन्माष्टमी", Language.KANNADA to "ಕೃಷ್ಣ ಜನ್ಮಾಷ್ಟಮಿ"
                ),
                date = LocalDate.of(year, 8, if (year % 2 == 0) 19 else 12),
                category = FestivalCategory.VAISHNAVA,
                isHoliday = true
            ))
            // National Holidays
            add(Festival(
                nameKey = "republic_day",
                namesByLang = mapOf(
                    Language.TELUGU to "గణతంత్ర దినోత్సవం", Language.ENGLISH to "Republic Day",
                    Language.TAMIL to "குடியரசு தினம்", Language.MALAYALAM to "റിപ്പബ്ലിക് ദിനം",
                    Language.HINDI to "गणतंत्र दिवस", Language.KANNADA to "ಗಣರಾಜ್ಯೋತ್ಸವ"
                ),
                date = LocalDate.of(year, 1, 26),
                category = FestivalCategory.NATIONAL,
                isHoliday = true
            ))
            add(Festival(
                nameKey = "independence_day",
                namesByLang = mapOf(
                    Language.TELUGU to "స్వాతంత్ర్య దినోత్సవం", Language.ENGLISH to "Independence Day",
                    Language.TAMIL to "சுதந்திர தினம்", Language.MALAYALAM to "സ്വാതന്ത്ര്യ ദിനം",
                    Language.HINDI to "स्वतंत्रता दिवस", Language.KANNADA to "ಸ್ವಾತಂತ್ರ್ಯ ದಿನಾಚರಣೆ"
                ),
                date = LocalDate.of(year, 8, 15),
                category = FestivalCategory.NATIONAL,
                isHoliday = true
            ))
            add(Festival(
                nameKey = "gandhi_jayanti",
                namesByLang = mapOf(
                    Language.TELUGU to "గాంధీ జయంతి", Language.ENGLISH to "Gandhi Jayanti",
                    Language.TAMIL to "காந்தி ஜெயந்தி", Language.MALAYALAM to "ഗാന്ധി ജയന്തി",
                    Language.HINDI to "गांधी जयंती", Language.KANNADA to "ಗಾಂಧಿ ಜಯಂತಿ"
                ),
                date = LocalDate.of(year, 10, 2),
                category = FestivalCategory.NATIONAL,
                isHoliday = true
            ))
        }.sortedBy { it.date }
    }
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
                        "Upcoming",
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
                        "Past",
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
                        "Public Holiday",
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
