package com.example.sixcoachconfectionery.data.models

import org.json.JSONObject

data class Item(
    val id: String,
    val name: String,
    val category: String,
    val price: Double,
    val stock: Int,
    val isActive: Boolean = true,
    val isInternalSupply: Boolean = false
) {
    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("id", id)
            put("name", name)
            put("category", category)
            put("price", price)
            put("stock", stock)
            put("isActive", isActive)
            put("isInternalSupply", isInternalSupply)
        }
    }

    companion object {
        fun generatedId(): String = "item_${System.currentTimeMillis()}"

        fun fromJson(json: JSONObject): Item {
            return Item(
                id = json.optString("id", generatedId()),
                name = json.optString("name", "Unknown"),
                category = json.optString("category", "General"),
                price = json.optDouble("price", 0.0),
                stock = json.optInt("stock", 0),
                isActive = json.optBoolean("isActive", true),
                isInternalSupply = json.optBoolean("isInternalSupply", false)
            )
        }
    }
}
