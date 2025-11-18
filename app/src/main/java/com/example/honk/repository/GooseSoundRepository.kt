package com.example.honk.data.repository

import BaseFirestoreRepository
import com.example.honk.data.entities.GooseSoundEntity
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class GooseSoundRepository : BaseFirestoreRepository<GooseSoundEntity>(
    rootCollection = Firebase.firestore.collection("goose_sounds"),
    clazz = GooseSoundEntity::class.java
)