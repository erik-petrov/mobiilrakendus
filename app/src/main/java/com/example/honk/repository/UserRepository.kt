package com.example.honk.repository

import com.example.honk.data.entities.UserEntity
import kotlinx.coroutines.flow.Flow
import com.example.honk.data.firebase.FirebaseModule
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

class UserRepository :
    BaseFirestoreRepository<UserEntity>(
        rootCollection = FirebaseModule.firestore.collection("users"),
        clazz = UserEntity::class.java
    ) {

    override fun getAll(): Flow<List<UserEntity>> {
        throw UnsupportedOperationException("Fetching ALL users is not supported on client.")
    }

    fun getCurrentUser(): Flow<UserEntity?> = callbackFlow {
        val uid = FirebaseModule.auth.currentUser?.uid

        if (uid == null) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val docRef = FirebaseModule.firestore.collection("users").document(uid)

        val listener = docRef.addSnapshotListener { snapshot, _ ->
            val user = snapshot?.toObject(UserEntity::class.java)
            trySend(user)
        }

        awaitClose { listener.remove() }
    }
}
