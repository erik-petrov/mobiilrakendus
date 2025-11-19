package com.example.honk.repository

import com.example.honk.data.entities.TaskEntity
import com.example.honk.data.firebase.FirebaseModule

class TaskRepository :
    BaseFirestoreRepository<TaskEntity>(
        rootCollection = FirebaseModule.firestore
            .collection("users")
            .document(FirebaseModule.auth.currentUser?.uid ?: "debug_user")
            .collection("tasks"),
        clazz = TaskEntity::class.java
    ) {
}
