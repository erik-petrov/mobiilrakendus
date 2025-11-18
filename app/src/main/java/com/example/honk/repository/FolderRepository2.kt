package com.example.honk.repository

import BaseFirestoreRepository
import com.example.honk.data.entities.FolderEntity
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class FolderRepository : BaseFirestoreRepository<FolderEntity>(
    rootCollection = Firebase.firestore.collection("users")
        .document(Firebase.auth.currentUser?.uid ?: "coudlnt auth")
        .collection("folders"),
    clazz = FolderEntity::class.java
)