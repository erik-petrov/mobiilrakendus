package com.example.honk.repository

import com.example.honk.model.Reminder
import com.google.firebase.firestore.CollectionReference
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

abstract class BaseFirestoreRepository<T : Any>(
    private val rootCollection: CollectionReference,
    private val clazz: Class<T>
) {

    // GET ALL
    open fun getAll(): Flow<List<T>> = callbackFlow {
        val listener = rootCollection.addSnapshotListener { snapshot, error ->
            if (error != null) { close(error); return@addSnapshotListener }
            val items = snapshot?.toObjects(clazz) ?: emptyList()
            trySend(items)
        }
        awaitClose { listener.remove() }
    }

    // GET ONE
    open fun getById(id: String): Flow<T?> = callbackFlow {
        val listener = rootCollection.document(id).addSnapshotListener { snapshot, _ ->
            val item = snapshot?.toObject(clazz)
            trySend(item)
        }
        awaitClose { listener.remove() }
    }

    // INSERT / UPDATE
    open suspend fun save(id: String, item: T) {
        rootCollection.document(id).set(item).await()
    }

    // DELETE
    open suspend fun delete(id: String) {
        rootCollection.document(id).delete().await()
    }
}