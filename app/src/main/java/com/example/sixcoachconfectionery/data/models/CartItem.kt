package com.example.sixcoachconfectionery.data.models

import org.json.JSONObject

data class CartItem(
    val item: Item,
    var quantity: Int
) {
    val totalPrice: Double
        get() = item.price * quantity

    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("item", item.toJson())
            put("quantity", quantity)
        }
    }

    companion object {
        fun fromJson(json: JSONObject): CartItem {
            val itemJson = json.getJSONObject("item")
            return CartItem(
                item = Item.fromJson(itemJson),
                quantity = json.optInt("quantity", 1)
            )
        }
    }
}
