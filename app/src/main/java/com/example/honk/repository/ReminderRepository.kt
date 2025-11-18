package com.example.honk.data.repository

import BaseFirestoreRepository
import com.example.honk.data.entities.ReminderEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject

class ReminderRepository @Inject constructor(
    firestore: FirebaseFirestore,
    auth: FirebaseAuth
) : BaseFirestoreRepository<ReminderEntity>(
    rootCollection = firestore.collection("users")
        .document(auth.currentUser?.uid ?: "")
        .collection("reminders"),
    clazz = ReminderEntity::class.java
)