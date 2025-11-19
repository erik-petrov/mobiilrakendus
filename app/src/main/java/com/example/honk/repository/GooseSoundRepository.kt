package com.example.honk.repository

import com.example.honk.data.entities.GooseSoundEntity
import com.example.honk.data.firebase.FirebaseModule

class GooseSoundRepository :
    BaseFirestoreRepository<GooseSoundEntity>(
        rootCollection = FirebaseModule.firestore
            .collection("goose_sounds"),
        clazz = GooseSoundEntity::class.java
    ) {

    override suspend fun add(item: GooseSoundEntity) {
        save(item.id, item)
    }
}
