package com.panchangam100.live.utils

import com.panchangam100.live.data.model.Language

/**
 * Multi-language lookup tables for all Panchangam elements.
 * Supports: Telugu, English, Tamil, Malayalam, Hindi, Kannada
 */
object LanguageManager {

    // ─── Tithi names (30 tithis: 0=Pratipada Shukla … 14=Pournami, 15=Pratipada Krishna … 29=Amavasya) ───
    private val TITHI = mapOf(
        Language.TELUGU to listOf(
            "పాడ్యమి","విదియ","తదియ","చవితి","పంచమి","షష్టి","సప్తమి","అష్టమి","నవమి","దశమి",
            "ఏకాదశి","ద్వాదశి","త్రయోదశి","చతుర్దశి","పౌర్ణమి",
            "పాడ్యమి","విదియ","తదియ","చవితి","పంచమి","షష్టి","సప్తమి","అష్టమి","నవమి","దశమి",
            "ఏకాదశి","ద్వాదశి","త్రయోదశి","చతుర్దశి","అమావాస్య"
        ),
        Language.ENGLISH to listOf(
            "Pratipada","Dvitiya","Tritiya","Chaturthi","Panchami","Shashthi","Saptami","Ashtami","Navami","Dashami",
            "Ekadashi","Dwadashi","Trayodashi","Chaturdashi","Pournami",
            "Pratipada","Dvitiya","Tritiya","Chaturthi","Panchami","Shashthi","Saptami","Ashtami","Navami","Dashami",
            "Ekadashi","Dwadashi","Trayodashi","Chaturdashi","Amavasya"
        ),
        Language.TAMIL to listOf(
            "பிரதமை","துவிதியை","திருதியை","சதுர்த்தி","பஞ்சமி","சஷ்டி","சப்தமி","அஷ்டமி","நவமி","தசமி",
            "ஏகாதசி","துவாதசி","திரயோதசி","சதுர்தசி","பௌர்ணமி",
            "பிரதமை","துவிதியை","திருதியை","சதுர்த்தி","பஞ்சமி","சஷ்டி","சப்தமி","அஷ்டமி","நவமி","தசமி",
            "ஏகாதசி","துவாதசி","திரயோதசி","சதுர்தசி","அமாவாசை"
        ),
        Language.MALAYALAM to listOf(
            "പ്രതിപദ","ദ്വിതീയ","തൃതീയ","ചതുർത്ഥി","പഞ്ചമി","ഷഷ്ഠി","സപ്തമി","അഷ്ടമി","നവമി","ദശമി",
            "ഏകാദശി","ദ്വാദശി","ത്രയോദശി","ചതുർദ്ദശി","പൗർണ്ണമി",
            "പ്രതിപദ","ദ്വിതീയ","തൃതീയ","ചതുർത്ഥി","പഞ്ചമി","ഷഷ്ഠി","സപ്തമി","അഷ്ടമി","നവമി","ദശമി",
            "ഏകാദശി","ദ്വാദശി","ത്രയോദശി","ചതുർദ്ദശി","അമാവാസ്യ"
        ),
        Language.HINDI to listOf(
            "प्रतिपदा","द्वितीया","तृतीया","चतुर्थी","पंचमी","षष्ठी","सप्तमी","अष्टमी","नवमी","दशमी",
            "एकादशी","द्वादशी","त्रयोदशी","चतुर्दशी","पूर्णिमा",
            "प्रतिपदा","द्वितीया","तृतीया","चतुर्थी","पंचमी","षष्ठी","सप्तमी","अष्टमी","नवमी","दशमी",
            "एकादशी","द्वादशी","त्रयोदशी","चतुर्दशी","अमावस्या"
        ),
        Language.KANNADA to listOf(
            "ಪ್ರತಿಪದ","ದ್ವಿತೀಯ","ತೃತೀಯ","ಚತುರ್ಥಿ","ಪಂಚಮಿ","ಷಷ್ಠಿ","ಸಪ್ತಮಿ","ಅಷ್ಟಮಿ","ನವಮಿ","ದಶಮಿ",
            "ಏಕಾದಶಿ","ದ್ವಾದಶಿ","ತ್ರಯೋದಶಿ","ಚತುರ್ದಶಿ","ಹುಣ್ಣಿಮೆ",
            "ಪ್ರತಿಪದ","ದ್ವಿತೀಯ","ತೃತೀಯ","ಚತುರ್ಥಿ","ಪಂಚಮಿ","ಷಷ್ಠಿ","ಸಪ್ತಮಿ","ಅಷ್ಟಮಿ","ನವಮಿ","ದಶಮಿ",
            "ಏಕಾದಶಿ","ದ್ವಾದಶಿ","ತ್ರಯೋದಶಿ","ಚತುರ್ದಶಿ","ಅಮಾವಾಸ್ಯೆ"
        )
    )

    // ─── Nakshatra names (27) ───
    private val NAKSHATRA = mapOf(
        Language.TELUGU to listOf(
            "అశ్వని","భరణి","కృత్తిక","రోహిణి","మృగశిర","ఆర్ద్ర","పునర్వసు","పుష్యమి","ఆశ్లేష",
            "మఖ","పూర్వాఫల్గుని","ఉత్తరాఫల్గుని","హస్త","చిత్త","స్వాతి","విశాఖ","అనూరాధ",
            "జ్యేష్ఠ","మూల","పూర్వాషాఢ","ఉత్తరాషాఢ","శ్రవణ","ధనిష్ఠ","శతభిష","పూర్వాభాద్ర","ఉత్తరాభాద్ర","రేవతి"
        ),
        Language.ENGLISH to listOf(
            "Ashwini","Bharani","Krittika","Rohini","Mrigashira","Ardra","Punarvasu","Pushya","Ashlesha",
            "Magha","Purva Phalguni","Uttara Phalguni","Hasta","Chitra","Swati","Vishakha","Anuradha",
            "Jyeshtha","Mula","Purva Ashadha","Uttara Ashadha","Shravana","Dhanishtha","Shatabhisha","Purva Bhadra","Uttara Bhadra","Revati"
        ),
        Language.TAMIL to listOf(
            "அஸ்வினி","பரணி","கார்த்திகை","ரோகிணி","மிருகசீர்ஷம்","திருவாதிரை","புனர்பூசம்","பூசம்","ஆயில்யம்",
            "மகம்","பூரம்","உத்திரம்","அஸ்தம்","சித்திரை","சுவாதி","விசாகம்","அனுஷம்",
            "கேட்டை","மூலம்","பூராடம்","உத்திராடம்","திருவோணம்","அவிட்டம்","சதயம்","பூரட்டாதி","உத்திரட்டாதி","ரேவதி"
        ),
        Language.MALAYALAM to listOf(
            "അശ്വതി","ഭരണി","കാർത്തിക","രോഹിണി","മകീര്യം","തിരുവാതിര","പുനർതം","പൂയം","ആയില്യം",
            "മകം","പൂരം","ഉത്രം","അത്തം","ചിത്തിര","ചോതി","വിശാഖം","അനിഴം",
            "തൃക്കേട്ട","മൂലം","പൂരാടം","ഉത്രാടം","തിരുവോണം","അവിട്ടം","ചതയം","പൂരുരുട്ടാതി","ഉത്തൃട്ടാതി","രേവതി"
        ),
        Language.HINDI to listOf(
            "अश्विनी","भरणी","कृत्तिका","रोहिणी","मृगशिरा","आर्द्रा","पुनर्वसु","पुष्य","आश्लेषा",
            "मघा","पूर्व फाल्गुनी","उत्तर फाल्गुनी","हस्त","चित्रा","स्वाती","विशाखा","अनुराधा",
            "ज्येष्ठा","मूल","पूर्व आषाढ़","उत्तर आषाढ़","श्रवण","धनिष्ठा","शतभिषा","पूर्व भाद्रपद","उत्तर भाद्रपद","रेवती"
        ),
        Language.KANNADA to listOf(
            "ಅಶ್ವಿನಿ","ಭರಣಿ","ಕೃತ್ತಿಕ","ರೋಹಿಣಿ","ಮೃಗಶಿರ","ಆರ್ದ್ರ","ಪುನರ್ವಸು","ಪುಷ್ಯ","ಆಶ್ಲೇಷ",
            "ಮಘ","ಪೂರ್ವ ಫಾಲ್ಗುನಿ","ಉತ್ತರ ಫಾಲ್ಗುನಿ","ಹಸ್ತ","ಚಿತ್ತ","ಸ್ವಾತಿ","ವಿಶಾಖ","ಅನೂರಾಧ",
            "ಜ್ಯೇಷ್ಠ","ಮೂಲ","ಪೂರ್ವಾಷಾಢ","ಉತ್ತರಾಷಾಢ","ಶ್ರವಣ","ಧನಿಷ್ಠ","ಶತಭಿಷ","ಪೂರ್ವಾಭಾದ್ರ","ಉತ್ತರಾಭಾದ್ರ","ರೇವತಿ"
        )
    )

    // ─── Yoga names (27) ───
    private val YOGA = mapOf(
        Language.TELUGU to listOf(
            "విష్కంభ","ప్రీతి","ఆయుష్మాన్","సౌభాగ్య","శోభన","అతిగండ","సుకర్మ","ధృతి","శూల","గండ",
            "వృద్ధి","ధ్రువ","వ్యాఘాత","హర్షణ","వజ్ర","సిద్ధి","వ్యతీపాత","వరీయాన్","పరిఘ","శివ",
            "సిద్ధ","సాధ్య","శుభ","శుక్ల","బ్రహ్మ","ఐంద్ర","వైధృతి"
        ),
        Language.ENGLISH to listOf(
            "Vishkambha","Priti","Ayushman","Saubhagya","Shobhana","Atiganda","Sukarma","Dhriti","Shoola","Ganda",
            "Vriddhi","Dhruva","Vyaghata","Harshana","Vajra","Siddhi","Vyatipata","Variyan","Parigha","Shiva",
            "Siddha","Sadhya","Shubha","Shukla","Brahma","Aindra","Vaidhriti"
        ),
        Language.TAMIL to listOf(
            "விஷ்கம்பம்","பிரீதி","ஆயுஷ்மான்","சௌபாக்யம்","சோபனம்","அதிகண்டம்","சுகர்மம்","திருதி","சூலம்","கண்டம்",
            "விருத்தி","திருவம்","வியாகாதம்","அர்ஷணம்","வஜ்ரம்","சித்தி","வியதீபாதம்","வரீயான்","பரிகம்","சிவம்",
            "சித்தம்","சாத்யம்","சுபம்","சுக்லம்","பிரம்மம்","ஐந்திரம்","வைத்ரிதி"
        ),
        Language.MALAYALAM to listOf(
            "വിഷ്കംഭം","പ്രീതി","ആയുഷ്മാൻ","സൗഭാഗ്യം","ശോഭനം","അതിഗണ്ഡം","സുകർമ്മം","ധൃതി","ശൂലം","ഗണ്ഡം",
            "വൃദ്ധി","ധ്രുവം","വ്യാഘാതം","ഹർഷണം","വജ്രം","സിദ്ധി","വ്യതീപാതം","വരീയാൻ","പരിഘം","ശിവം",
            "സിദ്ധം","സാദ്ധ്യം","ശുഭം","ശുക്ലം","ബ്രഹ്മം","ഐന്ദ്രം","വൈധൃതി"
        ),
        Language.HINDI to listOf(
            "विष्कंभ","प्रीति","आयुष्मान्","सौभाग्य","शोभन","अतिगण्ड","सुकर्मा","धृति","शूल","गण्ड",
            "वृद्धि","ध्रुव","व्याघात","हर्षण","वज्र","सिद्धि","व्यतीपात","वरीयान","परिघ","शिव",
            "सिद्ध","साध्य","शुभ","शुक्ल","ब्रह्म","इन्द्र","वैधृति"
        ),
        Language.KANNADA to listOf(
            "ವಿಷ್ಕಂಭ","ಪ್ರೀತಿ","ಆಯುಷ್ಮಾನ್","ಸೌಭಾಗ್ಯ","ಶೋಭನ","ಅತಿಗಂಡ","ಸುಕರ್ಮ","ಧೃತಿ","ಶೂಲ","ಗಂಡ",
            "ವೃದ್ಧಿ","ಧ್ರುವ","ವ್ಯಾಘಾತ","ಹರ್ಷಣ","ವಜ್ರ","ಸಿದ್ಧಿ","ವ್ಯತೀಪಾತ","ವರೀಯಾನ್","ಪರಿಘ","ಶಿವ",
            "ಸಿದ್ಧ","ಸಾಧ್ಯ","ಶುಭ","ಶುಕ್ಲ","ಬ್ರಹ್ಮ","ಐಂದ್ರ","ವೈಧೃತಿ"
        )
    )

    // ─── Karana names (11 karanas; 0-6 = movable Bava..Vishti, 7-10 = fixed) ───
    private val KARANA = mapOf(
        Language.TELUGU to listOf("బవ","బాలవ","కౌలవ","తైతిల","గరజ","వణిజ","విష్టి","శకుని","చతుష్పద","నాగ","కింస్తుఘ్న"),
        Language.ENGLISH to listOf("Bava","Balava","Kaulava","Taitila","Garaja","Vanija","Vishti","Shakuni","Chatushpada","Naga","Kimstughna"),
        Language.TAMIL to listOf("பவ","பாலவ","கௌலவ","தைதில","கரஜ","வணிஜ","விஷ்டி","சகுனி","சதுஷ்பாத","நாக","கிம்ஸ்துக்ன"),
        Language.MALAYALAM to listOf("ബവ","ബാലവ","കൗലവ","തൈതില","ഗരജ","വണിജ","വിഷ്ടി","ശകുനി","ചതുഷ്പദ","നാഗ","കിംസ്തുഘ്ന"),
        Language.HINDI to listOf("बव","बालव","कौलव","तैतिल","गरज","वणिज","विष्टि","शकुनि","चतुष्पद","नाग","किंस्तुघ्न"),
        Language.KANNADA to listOf("ಬವ","ಬಾಲವ","ಕೌಲವ","ತೈತಿಲ","ಗರಜ","ವಣಿಜ","ವಿಷ್ಟಿ","ಶಕುನಿ","ಚತುಷ್ಪದ","ನಾಗ","ಕಿಂಸ್ತುಘ್ನ")
    )

    // ─── Vara (weekday) names ─── (1=Mon … 7=Sun)
    private val VARA = mapOf(
        Language.TELUGU to listOf("సోమవారం","మంగళవారం","బుధవారం","గురువారం","శుక్రవారం","శనివారం","ఆదివారం"),
        Language.ENGLISH to listOf("Monday","Tuesday","Wednesday","Thursday","Friday","Saturday","Sunday"),
        Language.TAMIL to listOf("திங்கள்","செவ்வாய்","புதன்","வியாழன்","வெள்ளி","சனி","ஞாயிறு"),
        Language.MALAYALAM to listOf("തിങ്കൾ","ചൊവ്വ","ബുധൻ","വ്യാഴം","വെള്ളി","ശനി","ഞായർ"),
        Language.HINDI to listOf("सोमवार","मंगलवार","बुधवार","गुरुवार","शुक्रवार","शनिवार","रविवार"),
        Language.KANNADA to listOf("ಸೋಮವಾರ","ಮಂಗಳವಾರ","ಬುಧವಾರ","ಗುರುವಾರ","ಶುಕ್ರವಾರ","ಶನಿವಾರ","ಭಾನುವಾರ")
    )

    // ─── Paksha (fortnight) ───
    private val PAKSHA = mapOf(
        Language.TELUGU to mapOf("Shukla" to "శుక్ల పక్షం", "Krishna" to "కృష్ణ పక్షం"),
        Language.ENGLISH to mapOf("Shukla" to "Shukla Paksha", "Krishna" to "Krishna Paksha"),
        Language.TAMIL to mapOf("Shukla" to "சுக்ல பக்ஷம்", "Krishna" to "கிருஷ்ண பக்ஷம்"),
        Language.MALAYALAM to mapOf("Shukla" to "ശുക്ലപക്ഷം", "Krishna" to "കൃഷ്ണപക്ഷം"),
        Language.HINDI to mapOf("Shukla" to "शुक्ल पक्ष", "Krishna" to "कृष्ण पक्ष"),
        Language.KANNADA to mapOf("Shukla" to "ಶುಕ್ಲ ಪಕ್ಷ", "Krishna" to "ಕೃಷ್ಣ ಪಕ್ಷ")
    )

    // ─── Rashi names (12) ───
    private val RASHI = mapOf(
        Language.TELUGU to listOf("మేషం","వృషభం","మిధునం","కర్కాటకం","సింహం","కన్య","తుల","వృశ్చికం","ధనుస్సు","మకరం","కుంభం","మీనం"),
        Language.ENGLISH to listOf("Aries","Taurus","Gemini","Cancer","Leo","Virgo","Libra","Scorpio","Sagittarius","Capricorn","Aquarius","Pisces"),
        Language.TAMIL to listOf("மேஷம்","ரிஷபம்","மிதுனம்","கடகம்","சிம்மம்","கன்னி","துலாம்","விருச்சிகம்","தனுசு","மகரம்","கும்பம்","மீனம்"),
        Language.MALAYALAM to listOf("മേടം","ഇടവം","മിഥുനം","കർക്കടകം","ചിങ്ങം","കന്നി","തുലാം","വൃശ്ചികം","ധനു","മകരം","കുംഭം","മീനം"),
        Language.HINDI to listOf("मेष","वृषभ","मिथुन","कर्क","सिंह","कन्या","तुला","वृश्चिक","धनु","मकर","कुंभ","मीन"),
        Language.KANNADA to listOf("ಮೇಷ","ವೃಷಭ","ಮಿಥುನ","ಕರ್ಕ","ಸಿಂಹ","ಕನ್ಯ","ತುಲ","ವೃಶ್ಚಿಕ","ಧನು","ಮಕರ","ಕುಂಭ","ಮೀನ")
    )

    // ─── UI Labels ───
    private val LABELS = mapOf(
        Language.TELUGU to mapOf(
            "tithi" to "తిథి", "nakshatra" to "నక్షత్రం", "yoga" to "యోగం", "karana" to "కరణం",
            "vara" to "వారం", "paksha" to "పక్షం", "sunrise" to "సూర్యోదయం", "sunset" to "సూర్యాస్తమయం",
            "rahu" to "రాహు కాలం", "yama" to "యమగండం", "gulika" to "గులిక కాలం",
            "abhijit" to "అభిజిత్ ముహూర్తం", "moonRashi" to "చంద్ర రాశి", "sunRashi" to "సూర్య రాశి",
            "today" to "ఈరోజు", "calendar" to "క్యాలెండర్", "festivals" to "పండుగలు",
            "settings" to "సెట్టింగులు", "location" to "స్థానం", "search" to "వెతకండి",
            "useGPS" to "GPS ఉపయోగించండి", "selectCity" to "నగరం ఎంచుకోండి",
            "language" to "భాష", "ends" to "ముగుస్తుంది", "auspicious" to "శుభ సమయాలు",
            "inauspicious" to "అశుభ సమయాలు", "panchangam" to "పంచాంగం"
        ),
        Language.ENGLISH to mapOf(
            "tithi" to "Tithi", "nakshatra" to "Nakshatra", "yoga" to "Yoga", "karana" to "Karana",
            "vara" to "Vara", "paksha" to "Paksha", "sunrise" to "Sunrise", "sunset" to "Sunset",
            "rahu" to "Rahu Kalam", "yama" to "Yamagandam", "gulika" to "Gulika Kalam",
            "abhijit" to "Abhijit Muhurta", "moonRashi" to "Moon Sign", "sunRashi" to "Sun Sign",
            "today" to "Today", "calendar" to "Calendar", "festivals" to "Festivals",
            "settings" to "Settings", "location" to "Location", "search" to "Search",
            "useGPS" to "Use GPS", "selectCity" to "Select City",
            "language" to "Language", "ends" to "Ends", "auspicious" to "Auspicious Times",
            "inauspicious" to "Inauspicious Times", "panchangam" to "Panchangam"
        ),
        Language.TAMIL to mapOf(
            "tithi" to "திதி", "nakshatra" to "நட்சத்திரம்", "yoga" to "யோகம்", "karana" to "கரணம்",
            "vara" to "வாரம்", "paksha" to "பக்ஷம்", "sunrise" to "சூர்யோதயம்", "sunset" to "சூர்யாஸ்தமயம்",
            "rahu" to "ராகு காலம்", "yama" to "யமகண்டம்", "gulika" to "குளிகை காலம்",
            "abhijit" to "அபிஜித் முஹூர்த்தம்", "moonRashi" to "சந்திர ராசி", "sunRashi" to "சூர்ய ராசி",
            "today" to "இன்று", "calendar" to "நாட்காட்டி", "festivals" to "திருவிழாக்கள்",
            "settings" to "அமைப்புகள்", "location" to "இடம்", "search" to "தேடு",
            "useGPS" to "GPS பயன்படுத்து", "selectCity" to "நகரம் தேர்ந்தெடு",
            "language" to "மொழி", "ends" to "முடிகிறது", "auspicious" to "சுப நேரங்கள்",
            "inauspicious" to "அசுப நேரங்கள்", "panchangam" to "பஞ்சாங்கம்"
        ),
        Language.MALAYALAM to mapOf(
            "tithi" to "തിഥി", "nakshatra" to "നക്ഷത്രം", "yoga" to "യോഗം", "karana" to "കരണം",
            "vara" to "വാരം", "paksha" to "പക്ഷം", "sunrise" to "സൂര്യോദയം", "sunset" to "സൂര്യാസ്തമയം",
            "rahu" to "രാഹു കാലം", "yama" to "യമഗണ്ഡം", "gulika" to "ഗുളിക കാലം",
            "abhijit" to "അഭിജിത് മുഹൂർത്തം", "moonRashi" to "ചന്ദ്രരാശി", "sunRashi" to "സൂര്യരാശി",
            "today" to "ഇന്ന്", "calendar" to "കലണ്ടർ", "festivals" to "ഉത്സവങ്ങൾ",
            "settings" to "ക്രമീകരണങ്ങൾ", "location" to "സ്ഥലം", "search" to "തിരയുക",
            "useGPS" to "GPS ഉപയോഗിക്കുക", "selectCity" to "നഗരം തിരഞ്ഞെടുക്കുക",
            "language" to "ഭാഷ", "ends" to "അവസാനിക്കുന്നു", "auspicious" to "ശുഭ സമയങ്ങൾ",
            "inauspicious" to "അശുഭ സമയങ്ങൾ", "panchangam" to "പഞ്ചാംഗം"
        ),
        Language.HINDI to mapOf(
            "tithi" to "तिथि", "nakshatra" to "नक्षत्र", "yoga" to "योग", "karana" to "करण",
            "vara" to "वार", "paksha" to "पक्ष", "sunrise" to "सूर्योदय", "sunset" to "सूर्यास्त",
            "rahu" to "राहु काल", "yama" to "यमगण्ड", "gulika" to "गुलिक काल",
            "abhijit" to "अभिजित मुहूर्त", "moonRashi" to "चंद्र राशि", "sunRashi" to "सूर्य राशि",
            "today" to "आज", "calendar" to "कैलेंडर", "festivals" to "त्योहार",
            "settings" to "सेटिंग्स", "location" to "स्थान", "search" to "खोजें",
            "useGPS" to "GPS उपयोग करें", "selectCity" to "शहर चुनें",
            "language" to "भाषा", "ends" to "समाप्त होता है", "auspicious" to "शुभ समय",
            "inauspicious" to "अशुभ समय", "panchangam" to "पंचांग"
        ),
        Language.KANNADA to mapOf(
            "tithi" to "ತಿಥಿ", "nakshatra" to "ನಕ್ಷತ್ರ", "yoga" to "ಯೋಗ", "karana" to "ಕರಣ",
            "vara" to "ವಾರ", "paksha" to "ಪಕ್ಷ", "sunrise" to "ಸೂರ್ಯೋದಯ", "sunset" to "ಸೂರ್ಯಾಸ್ತ",
            "rahu" to "ರಾಹು ಕಾಲ", "yama" to "ಯಮಗಂಡ", "gulika" to "ಗುಳಿಗ ಕಾಲ",
            "abhijit" to "ಅಭಿಜಿತ್ ಮುಹೂರ್ತ", "moonRashi" to "ಚಂದ್ರ ರಾಶಿ", "sunRashi" to "ಸೂರ್ಯ ರಾಶಿ",
            "today" to "ಇಂದು", "calendar" to "ಕ್ಯಾಲೆಂಡರ್", "festivals" to "ಹಬ್ಬಗಳು",
            "settings" to "ಸೆಟ್ಟಿಂಗ್ಸ್", "location" to "ಸ್ಥಳ", "search" to "ಹುಡುಕಿ",
            "useGPS" to "GPS ಬಳಸಿ", "selectCity" to "ನಗರ ಆಯ್ಕೆಮಾಡಿ",
            "language" to "ಭಾಷೆ", "ends" to "ಮುಗಿಯುತ್ತದೆ", "auspicious" to "ಶುಭ ಸಮಯಗಳು",
            "inauspicious" to "ಅಶುಭ ಸಮಯಗಳು", "panchangam" to "ಪಂಚಾಂಗ"
        )
    )

    // ─── Public API ───

    fun getTithi(index: Int, lang: Language): String =
        TITHI[lang]?.getOrNull(index.coerceIn(0, 29)) ?: index.toString()

    fun getNakshatra(index: Int, lang: Language): String =
        NAKSHATRA[lang]?.getOrNull(index.coerceIn(0, 26)) ?: index.toString()

    fun getYoga(index: Int, lang: Language): String =
        YOGA[lang]?.getOrNull(index.coerceIn(0, 26)) ?: index.toString()

    fun getKarana(index: Int, lang: Language): String =
        KARANA[lang]?.getOrNull(index.coerceIn(0, 10)) ?: index.toString()

    fun getVara(dayOfWeek: Int, lang: Language): String {
        // dayOfWeek: 1=Mon..7=Sun → list index 0..6
        return VARA[lang]?.getOrNull((dayOfWeek - 1).coerceIn(0, 6)) ?: ""
    }

    fun getPaksha(paksha: String, lang: Language): String =
        PAKSHA[lang]?.get(paksha) ?: paksha

    fun getRashi(index: Int, lang: Language): String =
        RASHI[lang]?.getOrNull(index.coerceIn(0, 11)) ?: ""

    fun label(key: String, lang: Language): String =
        LABELS[lang]?.get(key) ?: LABELS[Language.ENGLISH]?.get(key) ?: key
}
