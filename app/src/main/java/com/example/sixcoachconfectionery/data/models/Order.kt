package com.example.sixcoachconfectionery.data.models

import org.json.JSONArray
import org.json.JSONObject

data class Order(
    val id: Int,
    val customer: String,
    val timestamp: Long,
    val items: List<CartItem>,
    val total: Double,
    val paymentMethod: String = "Cash",
    val amountTendered: Double = 0.0,
    val change: Double = 0.0,
    var status: String = "Pending" // "Pending" or "Completed"
) {
    val totalItemsCount: Int
        get() = items.sumOf { it.quantity }

    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("id", id)
            put("customer", customer)
            put("timestamp", timestamp)
            val itemsArray = JSONArray()
            items.forEach { itemsArray.put(it.toJson()) }
            put("items", itemsArray)
            put("total", total)
            put("paymentMethod", paymentMethod)
            put("amountTendered", amountTendered)
            put("change", change)
            put("status", status)
        }
    }

    companion object {
        fun fromJson(json: JSONObject): Order {
            val itemsList = mutableListOf<CartItem>()
            val itemsArray = json.optJSONArray("items")
            if (itemsArray != null) {
                for (i in 0 until itemsArray.length()) {
                    itemsList.add(CartItem.fromJson(itemsArray.getJSONObject(i)))
                }
            }
            return Order(
                id = json.optInt("id", 1),
                customer = json.optString("customer", "Walk-in"),
                timestamp = json.optLong("timestamp", System.currentTimeMillis()),
                items = itemsList,
                total = json.optDouble("total", 0.0),
                paymentMethod = json.optString("paymentMethod", "Cash"),
                amountTendered = json.optDouble("amountTendered", 0.0),
                change = json.optDouble("change", 0.0),
                status = json.optString("status", "Pending")
            )
        }
    }
}
