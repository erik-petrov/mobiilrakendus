package com.example.honk.data.enums

enum class TaskPriority {
    LOW, MEDIUM, HIGH
}

enum class TaskStatus {
    PENDING, IN_PROGRESS, COMPLETED, CANCELLED
}

enum class AuthProvider {
    EMAIL, GOOGLE
}

enum class StoragePreference {
    LOCAL, CLOUD
}

enum class ReminderType {
    TIME_BASED, LOCATION_BASED
}

enum class FileType {
    IMAGE, DOCUMENT, AUDIO
}

enum class SourceType {
    CAMERA, GALLERY, FILE
}