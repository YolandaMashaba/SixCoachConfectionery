package com.example.sixcoachconfectionery.data.repository

import com.example.sixcoachconfectionery.data.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val usersCollection = firestore.collection("users")

    /**
     * Staff self-registration: creates Firebase Auth account and stores user profile in Firestore with role specified ("Counter Staff" or "Kitchen Staff"), isAdmin = false.
     */
    suspend fun registerStaff(email: String, pass: String, username: String, role: String = "Counter Staff"): Result<UserProfile> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, pass).await()
            val firebaseUser = authResult.user ?: throw IllegalStateException("Firebase user is null after registration")
            
            val userProfile = UserProfile(
                uid = firebaseUser.uid,
                email = email,
                username = username,
                role = role,
                isAdmin = false,
                createdAt = System.currentTimeMillis()
            )

            usersCollection.document(firebaseUser.uid).set(userProfile).await()
            Result.success(userProfile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Login for both Staff and Admin: signs in via Firebase Auth and fetches/ensures their Firestore user profile to resolve role/isAdmin status.
     */
    suspend fun loginUser(email: String, pass: String): Result<UserProfile> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, pass).await()
            val firebaseUser = authResult.user ?: throw IllegalStateException("Firebase user is null after sign in")

            val docRef = usersCollection.document(firebaseUser.uid)
            val docSnapshot = docRef.get().await()

            val isAdminUser = email.equals("admin@email.com", ignoreCase = true)

            val profile = if (docSnapshot.exists()) {
                val existing = docSnapshot.toObject(UserProfile::class.java) ?: UserProfile(uid = firebaseUser.uid, email = email)
                if (isAdminUser && (!existing.isAdmin || existing.role != "Admin")) {
                    val updated = existing.copy(isAdmin = true, role = "Admin")
                    docRef.set(updated).await()
                    updated
                } else {
                    existing
                }
            } else {
                val newProfile = UserProfile(
                    uid = firebaseUser.uid,
                    email = email,
                    username = email.substringBefore("@"),
                    role = if (isAdminUser) "Admin" else "Counter Staff",
                    isAdmin = isAdminUser,
                    createdAt = System.currentTimeMillis()
                )
                docRef.set(newProfile).await()
                newProfile
            }

            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetches all system users for Admin User Management.
     */
    suspend fun getAllUsers(): Result<List<UserProfile>> {
        return try {
            val snapshot = usersCollection.get().await()
            Result.success(snapshot.toObjects(UserProfile::class.java))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Updates staff permissions (Admin feature).
     */
    suspend fun updateUserRole(uid: String, role: String, isAdmin: Boolean): Result<Unit> {
        return try {
            usersCollection.document(uid).update(
                mapOf(
                    "role" to role,
                    "isAdmin" to isAdmin
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Programmatically seeds the default Admin account (email: admin@email.com, password: admin@123)
     * in Firebase Authentication and ensures its Firestore document exists with isAdmin = true and role = "Admin".
     */
    suspend fun seedAdminAccount(adminEmail: String = "admin@email.com", adminPass: String = "admin@123"): Result<UserProfile> {
        return try {
            val firebaseUser = try {
                val createResult = auth.createUserWithEmailAndPassword(adminEmail, adminPass).await()
                createResult.user
            } catch (e: Exception) {
                val signInResult = auth.signInWithEmailAndPassword(adminEmail, adminPass).await()
                signInResult.user
            } ?: throw IllegalStateException("Failed to authenticate admin user")

            val adminProfile = UserProfile(
                uid = firebaseUser.uid,
                email = adminEmail,
                username = "SystemAdmin",
                role = "Admin",
                isAdmin = true,
                createdAt = System.currentTimeMillis()
            )

            usersCollection.document(firebaseUser.uid).set(adminProfile).await()
            Result.success(adminProfile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
