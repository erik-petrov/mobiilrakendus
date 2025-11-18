package com.example.honk.repository

import com.example.honk.data.entities.FolderEntity
import com.example.honk.data.firebase.FirebaseModule

// repo for folders stored in Firestore under users/{uid}/folders
class FolderRepository : BaseFirestoreRepository<FolderEntity>(
    rootCollection = FirebaseModule.firestore
        .collection("users")
        .document(FirebaseModule.auth.currentUser?.uid ?: "debug_user")
        .collection("folders"),
    clazz = FolderEntity::class.java
)