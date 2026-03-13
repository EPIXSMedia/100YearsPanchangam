package com.panchangam100.live.data.model

import kotlinx.serialization.Serializable

@Serializable
data class AppLocation(
    val name: String,
    val stateName: String = "",
    val country: String = "India",
    val latitude: Double,
    val longitude: Double,
    val timezone: String = "Asia/Kolkata"
) {
    val displayName: String get() = if (stateName.isNotBlank()) "$name, $stateName" else name
}

/** Built-in city list for quick selection */
object CityDatabase {
    val cities: List<AppLocation> = listOf(
        // Andhra Pradesh / Telangana
        AppLocation("Tirupati", "Andhra Pradesh", latitude = 13.6288, longitude = 79.4192),
        AppLocation("Tirumala", "Andhra Pradesh", latitude = 13.6833, longitude = 79.3500),
        AppLocation("Vijayawada", "Andhra Pradesh", latitude = 16.5062, longitude = 80.6480),
        AppLocation("Visakhapatnam", "Andhra Pradesh", latitude = 17.6868, longitude = 83.2185),
        AppLocation("Hyderabad", "Telangana", latitude = 17.3850, longitude = 78.4867),
        AppLocation("Warangal", "Telangana", latitude = 17.9784, longitude = 79.5941),
        AppLocation("Guntur", "Andhra Pradesh", latitude = 16.3067, longitude = 80.4365),
        AppLocation("Nellore", "Andhra Pradesh", latitude = 14.4426, longitude = 79.9865),
        AppLocation("Rajahmundry", "Andhra Pradesh", latitude = 17.0005, longitude = 81.8040),
        AppLocation("Kurnool", "Andhra Pradesh", latitude = 15.8281, longitude = 78.0373),
        // Tamil Nadu
        AppLocation("Chennai", "Tamil Nadu", latitude = 13.0827, longitude = 80.2707),
        AppLocation("Madurai", "Tamil Nadu", latitude = 9.9252, longitude = 78.1198),
        AppLocation("Coimbatore", "Tamil Nadu", latitude = 11.0168, longitude = 76.9558),
        AppLocation("Salem", "Tamil Nadu", latitude = 11.6643, longitude = 78.1460),
        AppLocation("Tiruchirappalli", "Tamil Nadu", latitude = 10.7905, longitude = 78.7047),
        AppLocation("Tirunelveli", "Tamil Nadu", latitude = 8.7139, longitude = 77.7567),
        AppLocation("Kanchipuram", "Tamil Nadu", latitude = 12.8342, longitude = 79.7036),
        AppLocation("Thanjavur", "Tamil Nadu", latitude = 10.7870, longitude = 79.1378),
        AppLocation("Vellore", "Tamil Nadu", latitude = 12.9165, longitude = 79.1325),
        AppLocation("Rameswaram", "Tamil Nadu", latitude = 9.2881, longitude = 79.3129),
        // Karnataka
        AppLocation("Bengaluru", "Karnataka", latitude = 12.9716, longitude = 77.5946),
        AppLocation("Mysuru", "Karnataka", latitude = 12.2958, longitude = 76.6394),
        AppLocation("Mangaluru", "Karnataka", latitude = 12.9141, longitude = 74.8560),
        AppLocation("Hubballi", "Karnataka", latitude = 15.3647, longitude = 75.1240),
        AppLocation("Shivamogga", "Karnataka", latitude = 13.9299, longitude = 75.5681),
        AppLocation("Udupi", "Karnataka", latitude = 13.3409, longitude = 74.7421),
        // Kerala
        AppLocation("Thiruvananthapuram", "Kerala", latitude = 8.5241, longitude = 76.9366),
        AppLocation("Kochi", "Kerala", latitude = 9.9312, longitude = 76.2673),
        AppLocation("Kozhikode", "Kerala", latitude = 11.2588, longitude = 75.7804),
        AppLocation("Thrissur", "Kerala", latitude = 10.5276, longitude = 76.2144),
        AppLocation("Palakkad", "Kerala", latitude = 10.7867, longitude = 76.6548),
        AppLocation("Guruvayur", "Kerala", latitude = 10.5950, longitude = 76.0416),
        // Maharashtra
        AppLocation("Mumbai", "Maharashtra", latitude = 19.0760, longitude = 72.8777),
        AppLocation("Pune", "Maharashtra", latitude = 18.5204, longitude = 73.8567),
        AppLocation("Nashik", "Maharashtra", latitude = 20.0059, longitude = 73.7797),
        AppLocation("Nagpur", "Maharashtra", latitude = 21.1458, longitude = 79.0882),
        AppLocation("Aurangabad", "Maharashtra", latitude = 19.8762, longitude = 75.3433),
        AppLocation("Shirdi", "Maharashtra", latitude = 19.7653, longitude = 74.4769),
        // Delhi & North India
        AppLocation("New Delhi", "Delhi", latitude = 28.6139, longitude = 77.2090),
        AppLocation("Mathura", "Uttar Pradesh", latitude = 27.4924, longitude = 77.6737),
        AppLocation("Vrindavan", "Uttar Pradesh", latitude = 27.5794, longitude = 77.6968),
        AppLocation("Varanasi", "Uttar Pradesh", latitude = 25.3176, longitude = 82.9739),
        AppLocation("Prayagraj", "Uttar Pradesh", latitude = 25.4358, longitude = 81.8463),
        AppLocation("Ayodhya", "Uttar Pradesh", latitude = 26.7922, longitude = 82.1998),
        AppLocation("Agra", "Uttar Pradesh", latitude = 27.1767, longitude = 78.0081),
        AppLocation("Lucknow", "Uttar Pradesh", latitude = 26.8467, longitude = 80.9462),
        AppLocation("Haridwar", "Uttarakhand", latitude = 29.9457, longitude = 78.1642),
        AppLocation("Rishikesh", "Uttarakhand", latitude = 30.0869, longitude = 78.2676),
        AppLocation("Jaipur", "Rajasthan", latitude = 26.9124, longitude = 75.7873),
        AppLocation("Pushkar", "Rajasthan", latitude = 26.4899, longitude = 74.5510),
        AppLocation("Amritsar", "Punjab", latitude = 31.6340, longitude = 74.8723),
        AppLocation("Chandigarh", "Punjab", latitude = 30.7333, longitude = 76.7794),
        // Gujarat
        AppLocation("Ahmedabad", "Gujarat", latitude = 23.0225, longitude = 72.5714),
        AppLocation("Surat", "Gujarat", latitude = 21.1702, longitude = 72.8311),
        AppLocation("Vadodara", "Gujarat", latitude = 22.3072, longitude = 73.1812),
        AppLocation("Dwarka", "Gujarat", latitude = 22.2443, longitude = 68.9685),
        AppLocation("Somnath", "Gujarat", latitude = 20.8880, longitude = 70.4013),
        // Odisha
        AppLocation("Bhubaneswar", "Odisha", latitude = 20.2961, longitude = 85.8245),
        AppLocation("Puri", "Odisha", latitude = 19.8135, longitude = 85.8312),
        // West Bengal
        AppLocation("Kolkata", "West Bengal", latitude = 22.5726, longitude = 88.3639),
        // Madhya Pradesh
        AppLocation("Bhopal", "Madhya Pradesh", latitude = 23.2599, longitude = 77.4126),
        AppLocation("Ujjain", "Madhya Pradesh", latitude = 23.1765, longitude = 75.7885),
        // Himachal Pradesh
        AppLocation("Shimla", "Himachal Pradesh", latitude = 31.1048, longitude = 77.1734),
        // Bihar
        AppLocation("Patna", "Bihar", latitude = 25.5941, longitude = 85.1376),
        AppLocation("Gaya", "Bihar", latitude = 24.7955, longitude = 85.0002),
        // International
        AppLocation("Sri Lanka", "Colombo", country = "Sri Lanka", latitude = 6.9271, longitude = 79.8612, timezone = "Asia/Colombo"),
        AppLocation("Singapore", "", country = "Singapore", latitude = 1.3521, longitude = 103.8198, timezone = "Asia/Singapore"),
        AppLocation("Dubai", "", country = "UAE", latitude = 25.2048, longitude = 55.2708, timezone = "Asia/Dubai"),
        AppLocation("London", "", country = "UK", latitude = 51.5074, longitude = -0.1278, timezone = "Europe/London"),
        AppLocation("New York", "", country = "USA", latitude = 40.7128, longitude = -74.0060, timezone = "America/New_York"),
        AppLocation("Toronto", "", country = "Canada", latitude = 43.6532, longitude = -79.3832, timezone = "America/Toronto"),
        AppLocation("Sydney", "", country = "Australia", latitude = -33.8688, longitude = 151.2093, timezone = "Australia/Sydney"),
    )

    fun search(query: String): List<AppLocation> {
        val q = query.lowercase().trim()
        if (q.length < 2) return emptyList()
        return cities.filter {
            it.name.lowercase().contains(q) ||
                    it.stateName.lowercase().contains(q) ||
                    it.country.lowercase().contains(q)
        }.take(20)
    }
}
