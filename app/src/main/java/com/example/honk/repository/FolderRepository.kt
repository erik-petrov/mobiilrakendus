package com.example.honk.repository

import com.example.honk.data.entities.FolderEntity
import com.example.honk.data.firebase.FirebaseModule

class FolderRepository :
    BaseFirestoreRepository<FolderEntity>(
        rootCollection = FirebaseModule.firestore
            .collection("users")
            .document(FirebaseModule.auth.currentUser?.uid ?: "debug_user")
            .collection("folders"),
        clazz = FolderEntity::class.java
    ) {

    override suspend fun add(item: FolderEntity) {
        save(item.id, item)
    }
}
