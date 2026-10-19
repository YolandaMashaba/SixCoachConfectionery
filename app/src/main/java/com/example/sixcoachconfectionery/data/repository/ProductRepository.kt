package com.example.sixcoachconfectionery.data.repository

import com.example.sixcoachconfectionery.data.model.Category
import com.example.sixcoachconfectionery.data.model.InventoryTransaction
import com.example.sixcoachconfectionery.data.model.Product
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ProductRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val categoriesCollection = firestore.collection("categories")
    private val productsCollection = firestore.collection("products")
    private val inventoryCollection = firestore.collection("inventory_transactions")

    // Categories
    suspend fun getCategories(): Result<List<Category>> {
        return try {
            val snapshot = categoriesCollection.get().await()
            Result.success(snapshot.toObjects(Category::class.java))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveCategory(category: Category): Result<Unit> {
        return try {
            val id = category.categoryId.ifEmpty { categoriesCollection.document().id }
            val categoryToSave = category.copy(categoryId = id)
            categoriesCollection.document(id).set(categoryToSave).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Products
    suspend fun getProducts(): Result<List<Product>> {
        return try {
            val snapshot = productsCollection.get().await()
            Result.success(snapshot.toObjects(Product::class.java))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProductById(productId: String): Result<Product?> {
        return try {
            val doc = productsCollection.document(productId).get().await()
            Result.success(doc.toObject(Product::class.java))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveProduct(product: Product): Result<Unit> {
        return try {
            val id = product.productId.ifEmpty { productsCollection.document().id }
            val productToSave = product.copy(productId = id)
            productsCollection.document(id).set(productToSave).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Adds new inventory (Menu Item) to the database by the Admin, matching the Inventory Control UI in the screenshot:
     * - Item Name (productName)
     * - Category (categoryId / category)
     * - Price (price)
     * - Stock (quantityInStock)
     * - Status (availability: "In Stock" / "Out of Stock")
     */
    suspend fun addNewInventoryItem(
        itemName: String,
        category: String,
        price: Double,
        stock: Int
    ): Result<Product> {
        return try {
            val id = productsCollection.document().id
            val availabilityStatus = if (stock > 0) "In Stock" else "Out of Stock"
            val newProduct = Product(
                productId = id,
                productName = itemName,
                categoryId = category,
                price = price,
                costPrice = price * 0.6,
                quantityInStock = stock,
                availability = availabilityStatus,
                imageURL = "",
                dateAdded = System.currentTimeMillis()
            )

            productsCollection.document(id).set(newProduct).await()
            Result.success(newProduct)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteProduct(productId: String): Result<Unit> {
        return try {
            productsCollection.document(productId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Inventory Transactions & Stock update
    suspend fun addInventoryTransaction(
        transaction: InventoryTransaction,
        product: Product
    ): Result<Unit> {
        return try {
            val transId = transaction.transactionId.ifEmpty { inventoryCollection.document().id }
            val transToSave = transaction.copy(transactionId = transId)

            val newQuantity = when (transaction.transactionType) {
                "Add", "Return" -> product.quantityInStock + transaction.quantity
                "Sale", "Adjust" -> maxOf(0, product.quantityInStock - transaction.quantity)
                else -> product.quantityInStock
            }
            val newAvailability = if (newQuantity > 0) "In Stock" else "Out of Stock"

            val batch = firestore.batch()
            batch.set(inventoryCollection.document(transId), transToSave)
            batch.update(
                productsCollection.document(product.productId),
                mapOf(
                    "quantityInStock" to newQuantity,
                    "availability" to newAvailability
                )
            )
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
