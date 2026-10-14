package com.example.sixcoachconfectionery.data

import android.content.Context
import com.example.sixcoachconfectionery.data.models.CartItem
import com.example.sixcoachconfectionery.data.models.Item
import com.example.sixcoachconfectionery.data.models.Order
import com.example.sixcoachconfectionery.data.models.User
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class SixCoachConfectioneryRepository private constructor(private val context: Context) {

    private val storeFile: File = File(context.filesDir, "six_coach_confectionery_store.json")

    private val users = mutableListOf<User>()
    private val items = mutableListOf<Item>()
    private val orders = mutableListOf<Order>()
    private var currentUser: User? = null

    private val listeners = mutableListOf<() -> Unit>()

    init {
        loadFromDisk()
    }

    fun addChangeListener(listener: () -> Unit) {
        listeners.add(listener)
    }

    fun removeChangeListener(listener: () -> Unit) {
        listeners.remove(listener)
    }

    private fun notifyDataChanged() {
        saveToDisk()
        listeners.forEach { it.invoke() }
    }

    private fun loadFromDisk() {
        if (!storeFile.exists()) {
            seedDefaultData()
            saveToDisk()
            return
        }

        try {
            val jsonStr = storeFile.readText()
            val root = JSONObject(jsonStr)

            users.clear()
            val usersArray = root.optJSONArray("users")
            if (usersArray != null) {
                for (i in 0 until usersArray.length()) {
                    users.add(User.fromJson(usersArray.getJSONObject(i)))
                }
            }

            items.clear()
            val itemsArray = root.optJSONArray("items")
            if (itemsArray != null) {
                for (i in 0 until itemsArray.length()) {
                    items.add(Item.fromJson(itemsArray.getJSONObject(i)))
                }
            }

            orders.clear()
            val ordersArray = root.optJSONArray("orders")
            if (ordersArray != null) {
                for (i in 0 until ordersArray.length()) {
                    orders.add(Order.fromJson(ordersArray.getJSONObject(i)))
                }
            }

            val currentUserId = root.optString("currentUserId", null)
            if (currentUserId != null) {
                currentUser = users.find { it.id == currentUserId || it.username == currentUserId }
            }

        } catch (e: Exception) {
            e.printStackTrace()
            seedDefaultData()
            saveToDisk()
        }
    }

    private fun seedDefaultData() {
        users.clear()
        items.clear()
        orders.clear()

        // Default Admin user
        val adminUser = User(
            id = "usr_admin",
            username = "ST10385107@rcconnect.edu.za",
            passwordHash = "password123",
            isAdmin = true
        )
        val staffUser = User(
            id = "usr_zwide",
            username = "Zwide",
            passwordHash = "password123",
            isAdmin = false
        )
        users.add(adminUser)
        users.add(staffUser)
        currentUser = adminUser

        // Default Items
        val gwinya = Item(
            id = "item_1",
            name = "gwinya",
            category = "Bread",
            price = 5.00,
            stock = 14,
            isActive = true
        )
        val latte = Item(
            id = "item_2",
            name = "latte",
            category = "Coffee",
            price = 24.00,
            stock = 11,
            isActive = true
        )
        items.add(gwinya)
        items.add(latte)

        // Seed initial completed orders matching screenshot
        val order1 = Order(
            id = 1,
            customer = "Walk-in",
            timestamp = System.currentTimeMillis() - 86400000, // 1 day ago
            items = listOf(CartItem(gwinya, 1)),
            total = 5.00,
            paymentMethod = "Cash",
            amountTendered = 10.00,
            change = 5.00,
            status = "Completed"
        )
        val order2 = Order(
            id = 2,
            customer = "Walk-in",
            timestamp = System.currentTimeMillis() - 3600000, // 1 hr ago
            items = listOf(CartItem(latte, 1)),
            total = 24.00,
            paymentMethod = "Card",
            amountTendered = 24.00,
            change = 0.00,
            status = "Completed"
        )
        orders.add(order1)
        orders.add(order2)
    }

    @Synchronized
    private fun saveToDisk() {
        try {
            val root = JSONObject()
            val usersArray = JSONArray()
            users.forEach { usersArray.put(it.toJson()) }
            root.put("users", usersArray)

            val itemsArray = JSONArray()
            items.forEach { itemsArray.put(it.toJson()) }
            root.put("items", itemsArray)

            val ordersArray = JSONArray()
            orders.forEach { ordersArray.put(it.toJson()) }
            root.put("orders", ordersArray)

            root.put("currentUserId", currentUser?.id ?: "")

            storeFile.writeText(root.toString(2))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // --- USER MANAGEMENT ---
    fun getCurrentUser(): User? = currentUser

    fun setCurrentUser(user: User?) {
        currentUser = user
        notifyDataChanged()
    }

    fun getUsers(): List<User> = users.toList()

    fun registerUser(username: String, passwordHash: String): Pair<Boolean, String> {
        val trimmed = username.trim()
        if (users.any { it.username.equals(trimmed, ignoreCase = true) }) {
            return Pair(false, "Username '$trimmed' is already taken.")
        }
        val isFirst = users.isEmpty()
        val newUser = User(
            id = User.generatedId(),
            username = trimmed,
            passwordHash = passwordHash,
            isAdmin = isFirst // First user becomes admin
        )
        users.add(newUser)
        currentUser = newUser
        notifyDataChanged()
        return Pair(true, "Registration successful")
    }

    fun loginUser(username: String, passwordHash: String): Pair<Boolean, String> {
        val trimmed = username.trim()
        val user = users.find { it.username.equals(trimmed, ignoreCase = true) }
            ?: return Pair(false, "User not found")

        if (user.passwordHash != passwordHash) {
            return Pair(false, "Invalid password")
        }

        currentUser = user
        notifyDataChanged()
        return Pair(true, "Login successful")
    }

    fun logout() {
        currentUser = null
        notifyDataChanged()
    }

    fun toggleUserAdminStatus(userId: String): Boolean {
        val index = users.indexOfFirst { it.id == userId }
        if (index != -1) {
            val user = users[index]
            val updated = user.copy(isAdmin = !user.isAdmin)
            users[index] = updated
            if (currentUser?.id == userId) {
                currentUser = updated
            }
            notifyDataChanged()
            return true
        }
        return false
    }

    // --- ITEMS / INVENTORY ---
    fun getActiveItems(): List<Item> = items.filter { it.isActive }

    fun getAllItems(): List<Item> = items.toList()

    fun getItemCategories(): List<String> {
        val categories = mutableListOf("All")
        items.filter { it.isActive }.map { it.category }.distinct().forEach {
            if (!categories.contains(it)) categories.add(it)
        }
        return categories
    }

    fun saveItem(item: Item) {
        val index = items.indexOfFirst { it.id == item.id }
        if (index != -1) {
            items[index] = item
        } else {
            items.add(item)
        }
        notifyDataChanged()
    }

    fun deleteItem(itemId: String) {
        // Soft delete / deactivate or remove
        val index = items.indexOfFirst { it.id == itemId }
        if (index != -1) {
            items[index] = items[index].copy(isActive = false)
            notifyDataChanged()
        }
    }

    // --- ORDERS & CHECKOUT ---
    fun getOrders(): List<Order> = orders.toList()

    fun getPendingOrders(): List<Order> = orders.filter { it.status == "Pending" }

    fun createOrder(
        cartItems: List<CartItem>,
        paymentMethod: String,
        amountTendered: Double
    ): Pair<Boolean, String> {
        if (cartItems.isEmpty()) {
            return Pair(false, "Cart is empty.")
        }

        // Validate stock
        for (cartItem in cartItems) {
            val itemInStore = items.find { it.id == cartItem.item.id }
            if (itemInStore == null || !itemInStore.isActive) {
                return Pair(false, "Item '${cartItem.item.name}' is no longer available.")
            }
            if (itemInStore.stock < cartItem.quantity) {
                return Pair(false, "Not enough stock for '${cartItem.item.name}'. Available: ${itemInStore.stock}")
            }
        }

        val totalAmount = cartItems.sumOf { it.totalPrice }
        val change = if (paymentMethod == "Cash") (amountTendered - totalAmount).coerceAtLeast(0.0) else 0.0

        if (paymentMethod == "Cash" && amountTendered < totalAmount) {
            return Pair(false, "Amount tendered (R %.2f) is less than order total (R %.2f).".format(amountTendered, totalAmount))
        }

        // Deduct stock
        for (cartItem in cartItems) {
            val index = items.indexOfFirst { it.id == cartItem.item.id }
            if (index != -1) {
                val current = items[index]
                items[index] = current.copy(stock = (current.stock - cartItem.quantity).coerceAtLeast(0))
            }
        }

        val newOrderId = (orders.maxOfOrNull { it.id } ?: 0) + 1
        val customerName = currentUser?.username ?: "Walk-in"

        val newOrder = Order(
            id = newOrderId,
            customer = customerName,
            timestamp = System.currentTimeMillis(),
            items = cartItems.map { it.copy() },
            total = totalAmount,
            paymentMethod = paymentMethod,
            amountTendered = if (paymentMethod == "Cash") amountTendered else totalAmount,
            change = change,
            status = "Pending"
        )

        orders.add(newOrder)
        notifyDataChanged()

        return Pair(true, "Order #$newOrderId placed successfully!")
    }

    fun completeOrder(orderId: Int): Boolean {
        val index = orders.indexOfFirst { it.id == orderId }
        if (index != -1) {
            orders[index] = orders[index].copy(status = "Completed")
            notifyDataChanged()
            return true
        }
        return false
    }

    companion object {
        @Volatile
        private var instance: SixCoachConfectioneryRepository? = null

        fun getInstance(context: Context): SixCoachConfectioneryRepository {
            return instance ?: synchronized(this) {
                instance ?: SixCoachConfectioneryRepository(context.applicationContext).also { instance = it }
            }
        }
    }
}
