package com.example.honk.repository

import com.example.honk.data.entities.GooseSoundEntity
import com.example.honk.data.firebase.FirebaseModule

// repo for global goose sounds stored in goose_sounds
class GooseSoundRepository : BaseFirestoreRepository<GooseSoundEntity>(
    rootCollection = FirebaseModule.firestore
        .collection("goose_sounds"),
    clazz = GooseSoundEntity::class.java
)