package com.liulkovich.florapoint.domain.cloud

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.liulkovich.florapoint.domain.UserPoints
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreRepository @Inject constructor() {

    private val firestore = FirebaseFirestore.getInstance()

    suspend fun uploadPoint(point: UserPoints): Result<String> {

        return try {

            val doc =
                firestore.collection("public_points").document()

            val cloudPoint = CloudPoint(
                cloudId = doc.id,
                ownerUid = point.ownerUid ?: "",

                speciesId = point.speciesId,
                userName = point.userName,
                description = point.description,

                latitude = point.latitude,
                longitude = point.longitude,

                category = point.category ?: "custom",

                timestamp = point.timestamp,

                temperature = point.temperature,
                humidity = point.humidity,
                avgTemp5Days = point.avgTemp5Days,
                avgHumidity5Days = point.avgHumidity5Days
            )

            doc.set(cloudPoint).await()

            Result.success(doc.id)

        } catch (e: Exception) {

            Result.failure(e)
        }
    }

    suspend fun updatePoint(
        point: UserPoints
    ): Result<Unit> {

        return try {

            val cloudId =
                point.cloudId
                    ?: return Result.failure(
                        IllegalArgumentException("cloudId is null")
                    )

            val cloudPoint = CloudPoint(
                cloudId = cloudId,
                ownerUid = point.ownerUid ?: "",

                speciesId = point.speciesId,
                userName = point.userName,
                description = point.description,

                latitude = point.latitude,
                longitude = point.longitude,

                category = point.category ?: "custom",

                timestamp = point.timestamp,

                temperature = point.temperature,
                humidity = point.humidity,
                avgTemp5Days = point.avgTemp5Days,
                avgHumidity5Days = point.avgHumidity5Days
            )

            firestore.collection("public_points")
                .document(cloudId)
                .set(cloudPoint)
                .await()

            Result.success(Unit)

        } catch (e: Exception) {

            Result.failure(e)
        }
    }

    suspend fun deletePoint(
        cloudId: String
    ): Result<Unit> {

        return try {

            firestore.collection("public_points")
                .document(cloudId)
                .delete()
                .await()

            Result.success(Unit)

        } catch (e: Exception) {

            Result.failure(e)
        }
    }

    suspend fun downloadPublicPoints(): List<UserPoints> {

        return try {

            firestore.collection("public_points")
                .get()
                .await()
                .documents
                .mapNotNull { doc ->

                    doc.toObject<CloudPoint>()
                        ?.copy(cloudId = doc.id)
                        ?.toUserPoint()
                }

        } catch (e: Exception) {

            emptyList()
        }
    }
}