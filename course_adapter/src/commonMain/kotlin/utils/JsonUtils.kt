package utils

import kotlinx.serialization.json.Json

val jsonUtil by lazy {
    Json {
        isLenient = true
        ignoreUnknownKeys = true
        explicitNulls = false
    }
}