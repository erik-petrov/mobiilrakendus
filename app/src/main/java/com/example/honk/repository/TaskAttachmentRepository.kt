package com.example.honk.repository

import com.example.honk.data.entities.TaskAttachmentEntity
import com.example.honk.data.firebase.FirebaseModule

// repo for task attachments stored in users/{uid}/task_attachments
class TaskAttachmentRepository : BaseFirestoreRepository<TaskAttachmentEntity>(
    rootCollection = FirebaseModule.firestore
        .collection("users")
        .document(FirebaseModule.auth.currentUser?.uid ?: "debug_user")
        .collection("task_attachments"),
    clazz = TaskAttachmentEntity::class.java
)
