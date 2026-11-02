# VOLTAR Production ProGuard / R8 Rules

# Room Database
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Dao interface * { *; }
-keep class * implements androidx.room.RoomDatabase$Callback
-dontwarn androidx.room.paging.**

# Data Models & Entities
-keep class com.voltar.battery.core.database.entity.** { *; }
-keep class com.voltar.battery.core.model.** { *; }
-keep class com.voltar.battery.core.datastore.** { *; }

# WorkManager
-keep class * extends androidx.work.Worker { *; }
-keep class * extends androidx.work.ListenableWorker { *; }

# Kotlin Coroutines & Flow
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-dontwarn kotlinx.coroutines.**

# Compose Runtime
-keepclassmembers class * {
    @androidx.compose.runtime.Composable *;
}
