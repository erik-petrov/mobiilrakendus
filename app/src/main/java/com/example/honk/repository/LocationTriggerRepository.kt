package com.example.honk.repository

import com.example.honk.data.entities.LocationTriggerEntity
import com.example.honk.data.firebase.FirebaseModule

// repo for location-based triggers stored in users/{uid}/location_triggers
class LocationTriggerRepository : BaseFirestoreRepository<LocationTriggerEntity>(
    rootCollection = FirebaseModule.firestore
        .collection("users")
        .document(FirebaseModule.auth.currentUser?.uid ?: "debug_user")
        .collection("location_triggers"),
    clazz = LocationTriggerEntity::class.java
)
