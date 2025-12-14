package com.example.honk.repository

import com.example.honk.data.firebase.FirebaseModule
import com.example.honk.model.Reminder

class ReminderRepositoryTest :
    BaseFirestoreRepository<Reminder>(
        rootCollection = FirebaseModule.firestore
            .collection("users")
            .document(FirebaseModule.auth.currentUser?.uid ?: "debug_user")
            .collection("reminders"),
        clazz = Reminder::class.java
    ) {
}