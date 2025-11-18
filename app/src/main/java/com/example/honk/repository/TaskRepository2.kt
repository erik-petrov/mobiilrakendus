package com.example.honk.repository

import BaseFirestoreRepository
import com.example.honk.data.entities.TaskEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject

class TaskRepository @Inject constructor(
    firestore: FirebaseFirestore,
    auth: FirebaseAuth
) : BaseFirestoreRepository<TaskEntity>(
    rootCollection = firestore.collection("users")
        .document(auth.currentUser?.uid ?: "")
        .collection("tasks"),
    clazz = TaskEntity::class.java
)