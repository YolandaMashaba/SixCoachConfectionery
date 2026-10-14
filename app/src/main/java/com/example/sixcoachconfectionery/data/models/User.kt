package com.example.sixcoachconfectionery.data.models

import org.json.JSONObject

data class User(
    val id: String,
    val username: String,
    val passwordHash: String,
    val isAdmin: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("id", id)
            put("username", username)
            put("passwordHash", passwordHash)
            put("isAdmin", isAdmin)
            put("createdAt", createdAt)
        }
    }

    companion object {
        fun generatedId(): String = "usr_${System.currentTimeMillis()}"

        fun fromJson(json: JSONObject): User {
            return User(
                id = json.optString("id", generatedId()),
                username = json.optString("username", ""),
                passwordHash = json.optString("passwordHash", ""),
                isAdmin = json.optBoolean("isAdmin", false),
                createdAt = json.optLong("createdAt", System.currentTimeMillis())
            )
        }
    }
}
