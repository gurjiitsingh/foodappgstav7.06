package com.it10x.foodappgstav7_06.firebase


object ClientRegistry {

    private val pizzaitalia_2345 = ClientFirebaseConfig(
        apiKey = "AIzaSyC4395B3kbMG1vPxvGEe0M0hTlx-Tv727M",
        applicationId = "1:247663732100:android:5000de54e9a2321315e9d1",
        projectId = "pizza-italia-f97c8"
    )

    private val grillhut_2345 = ClientFirebaseConfig(
        apiKey = "AIzaSyCt-EGdghFw8YMXSwazGdd9c6IQU97sSLc",
        applicationId = "1:462589171716:android:dbf6e9995c2fd578bffd51",
        projectId = "grill-hut-bh"
    )

    private val foodapp_2345 = ClientFirebaseConfig(
        apiKey = "AIzaSyABUApBoiX96KEDwRhX1UWifVWYcPZ30-g",
        applicationId = "1:694719081868:android:0834adbecfc0f833fbbaa9",
        projectId = "food-demo-d69f0"
    )

    // Hardcode first 20 clients, later fetch from API
    // 🔑 PASSWORD == MAP KEY
    private val clients = mapOf(
        "pizzaitalia_2345" to pizzaitalia_2345,
        "grillhut_2345" to grillhut_2345,
        "foodapp_2345" to foodapp_2345
    )

    fun get(clientId: String): ClientFirebaseConfig {
        return clients[clientId]
            ?: error("Client not found: $clientId")
    }
    fun getOrNull(clientId: String): ClientFirebaseConfig? {
        return clients[clientId]
    }
}





