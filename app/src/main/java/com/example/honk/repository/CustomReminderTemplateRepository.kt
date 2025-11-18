package com.example.honk.repository

import com.example.honk.data.entities.CustomReminderTemplateEntity
import com.example.honk.data.firebase.FirebaseModule

// repo for user-defined reminder templates stored in users/{uid}/custom_reminder_templates
class CustomReminderTemplateRepository :
    BaseFirestoreRepository<CustomReminderTemplateEntity>(
        rootCollection = FirebaseModule.firestore
            .collection("users")
            .document(FirebaseModule.auth.currentUser?.uid ?: "debug_user")
            .collection("custom_reminder_templates"),
        clazz = CustomReminderTemplateEntity::class.java
    )
