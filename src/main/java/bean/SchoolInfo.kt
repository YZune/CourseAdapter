package bean

import com.google.gson.annotations.SerializedName

data class SchoolInfo(
    val school: String,
    @SerializedName("sort_key")
    var sortKey: String,
    val type: String,
    val url: String
)