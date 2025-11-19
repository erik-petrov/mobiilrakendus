package com.example.honk.repository

import com.example.honk.data.entities.TaskAttachmentEntity
import com.example.honk.data.firebase.FirebaseModule

class TaskAttachmentRepository :
    BaseFirestoreRepository<TaskAttachmentEntity>(
        rootCollection = FirebaseModule.firestore
            .collection("users")
            .document(FirebaseModule.auth.currentUser?.uid ?: "debug_user")
            .collection("task_attachments"),
        clazz = TaskAttachmentEntity::class.java
    ) {

    override suspend fun add(item: TaskAttachmentEntity) {
        save(item.id, item)
    }
}
