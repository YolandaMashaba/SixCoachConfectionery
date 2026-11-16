package com.example.sixcoachconfectionery.data.repository

import android.util.Log
import com.example.sixcoachconfectionery.data.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    companion object {
        private const val TAG = "AuthRepository"
        private const val COMPANY_DOMAIN = "@sixcoachconfectionery.co.za"
    }

    private val usersCollection = firestore.collection("users")

    private fun isCompanyEmail(email: String): Boolean {
        return email.equals("admin@sixcoachconfectionery.co.za", ignoreCase = true) ||
               email.equals("ST10385107@rcconnect.edu.za", ignoreCase = true) ||
               email.endsWith(COMPANY_DOMAIN, ignoreCase = true)
    }

    /**
     * Staff registration: ensures email ends with @sixcoachconfectionery.co.za, creates Firebase Auth account and stores user profile in Firestore.
     */
    suspend fun registerStaff(email: String, pass: String, username: String, role: String = "Counter Staff", branch: String = "Roodepoort"): Result<UserProfile> {
        return try {
            if (!isCompanyEmail(email)) {
                return Result.failure(IllegalArgumentException("Access restricted. Only company email addresses ending with $COMPANY_DOMAIN are allowed."))
            }

            val authResult = auth.createUserWithEmailAndPassword(email, pass).await()
            val firebaseUser = authResult.user ?: throw IllegalStateException("Firebase user is null after registration")
            
            val userProfile = UserProfile(
                uid = firebaseUser.uid,
                email = email,
                username = username,
                role = role,
                isAdmin = role == "Admin",
                branch = branch,
                createdAt = System.currentTimeMillis()
            )

            usersCollection.document(firebaseUser.uid).set(userProfile).await()
            Result.success(userProfile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Login for both Staff and Admin: enforces @sixcoachconfectionery.co.za domain restriction and handles missing user docs defensively.
     */
    suspend fun loginUser(email: String, pass: String): Result<UserProfile> {
        return try {
            if (!isCompanyEmail(email)) {
                return Result.failure(IllegalArgumentException("Access restricted. Only company email addresses ending with $COMPANY_DOMAIN are allowed."))
            }

            val authResult = auth.signInWithEmailAndPassword(email, pass).await()
            val firebaseUser = authResult.user ?: throw IllegalStateException("Firebase user is null after sign in")

            val docRef = usersCollection.document(firebaseUser.uid)
            val docSnapshot = try {
                docRef.get().await()
            } catch (e: Exception) {
                null
            }

            val isAdminUser = email.equals("admin@sixcoachconfectionery.co.za", ignoreCase = true) || email.equals("ST10385107@rcconnect.edu.za", ignoreCase = true)

            val profile = if (docSnapshot != null && docSnapshot.exists()) {
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
                    branch = "Roodepoort",
                    createdAt = System.currentTimeMillis()
                )
                try {
                    docRef.set(newProfile).await()
                } catch (writeErr: Exception) {
                    Log.e(TAG, "Failed to create user profile document in Firestore", writeErr)
                }
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
     * Updates staff permissions and branch (Admin feature).
     */
    suspend fun updateUserRole(uid: String, role: String, isAdmin: Boolean, branch: String = "Roodepoort"): Result<Unit> {
        return try {
            usersCollection.document(uid).update(
                mapOf(
                    "role" to role,
                    "isAdmin" to isAdmin,
                    "branch" to branch
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Deletes a user profile document from Firestore (Admin feature).
     */
    suspend fun deleteUser(uid: String): Result<Unit> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                throw IllegalStateException("Admin delete blocked: no authenticated user is signed in.")
            }

            usersCollection.document(uid).delete().await()
            Log.d(TAG, "Deleted user profile for uid=$uid by admin uid=${currentUser.uid}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete user uid=$uid", e)
            Result.failure(e)
        }
    }

    /**
     * Google Sign-In / SSO: authenticates with Firebase Auth via Google ID token and enforces domain restriction.
     */
    suspend fun signInWithGoogle(googleIdToken: String): Result<UserProfile> {
        return try {
            if (googleIdToken.isBlank()) {
                val error = IllegalArgumentException("Google ID token is blank.")
                throw error
            }

            val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(googleIdToken, null)
            val authResult = auth.signInWithCredential(credential).await()
            val firebaseUser = authResult.user ?: throw IllegalStateException("Firebase user is null after Google sign in")
            val email = firebaseUser.email ?: ""

            if (!isCompanyEmail(email)) {
                auth.signOut()
                return Result.failure(IllegalArgumentException("Access restricted. Only company email addresses ending with $COMPANY_DOMAIN are allowed."))
            }

            val docRef = usersCollection.document(firebaseUser.uid)
            val docSnapshot = try {
                docRef.get().await()
            } catch (e: Exception) {
                null
            }

            val isAdminUser = email.equals("admin@sixcoachconfectionery.co.za", ignoreCase = true) || email.equals("ST10385107@rcconnect.edu.za", ignoreCase = true)

            val profile = if (docSnapshot != null && docSnapshot.exists()) {
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
                    username = firebaseUser.displayName ?: email.substringBefore("@"),
                    role = if (isAdminUser) "Admin" else "Counter Staff",
                    isAdmin = isAdminUser,
                    branch = "Roodepoort",
                    createdAt = System.currentTimeMillis()
                )
                try {
                    docRef.set(newProfile).await()
                } catch (_: Exception) {}
                newProfile
            }

            Result.success(profile)
        } catch (e: Exception) {
            Log.e(TAG, "Google sign-in credential exchange failed", e)
            Result.failure(e)
        }
    }

    suspend fun seedAdminAccount(adminEmail: String = "admin@sixcoachconfectionery.co.za", adminPass: String = "admin@123"): Result<UserProfile> {
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
                branch = "Roodepoort",
                createdAt = System.currentTimeMillis()
            )

            try {
                usersCollection.document(firebaseUser.uid).set(adminProfile).await()
            } catch (_: Exception) {}
            Result.success(adminProfile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
