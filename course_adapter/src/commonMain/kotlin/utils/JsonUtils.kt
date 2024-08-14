package utils

import kotlinx.serialization.json.Json

val jsonUtil =
    Json {
        isLenient = true
        ignoreUnknownKeys = true
        explicitNulls = false
    }
