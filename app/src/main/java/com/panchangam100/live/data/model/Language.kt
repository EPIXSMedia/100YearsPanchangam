package com.panchangam100.live.data.model

enum class Language(val code: String, val displayName: String, val nativeName: String) {
    TELUGU("te", "Telugu", "తెలుగు"),
    ENGLISH("en", "English", "English"),
    TAMIL("ta", "Tamil", "தமிழ்"),
    MALAYALAM("ml", "Malayalam", "മലയാളം"),
    HINDI("hi", "Hindi", "हिन्दी"),
    KANNADA("kn", "Kannada", "ಕನ್ನಡ");

    companion object {
        fun fromCode(code: String) = entries.find { it.code == code } ?: TELUGU
    }
}
