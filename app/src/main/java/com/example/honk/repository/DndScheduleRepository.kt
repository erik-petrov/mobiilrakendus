package com.example.honk.repository

import com.example.honk.data.entities.DndScheduleEntity
import com.example.honk.data.firebase.FirebaseModule

class DndScheduleRepository :
    BaseFirestoreRepository<DndScheduleEntity>(
        rootCollection = FirebaseModule.firestore
            .collection("users")
            .document(FirebaseModule.auth.currentUser?.uid ?: "debug_user")
            .collection("dnd_schedules"),
        clazz = DndScheduleEntity::class.java
    ) {
}
