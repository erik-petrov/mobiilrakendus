package com.example.honk.repository

import com.example.honk.data.entities.ReminderEntity
import com.example.honk.data.firebase.FirebaseModule

// repo for reminders stored in users/{uid}/reminders
class ReminderRepository : BaseFirestoreRepository<ReminderEntity>(
    rootCollection = FirebaseModule.firestore
        .collection("users")
        .document(FirebaseModule.auth.currentUser?.uid ?: "debug_user")
        .collection("reminders"),
    clazz = ReminderEntity::class.java
)