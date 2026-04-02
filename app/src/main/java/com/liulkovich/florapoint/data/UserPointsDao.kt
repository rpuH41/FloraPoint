package com.liulkovich.florapoint.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.liulkovich.florapoint.domain.UserPoints
import kotlinx.coroutines.flow.Flow

@Dao
interface UserPointsDao {
    @Insert
    suspend fun insert(point: UserPoints)

    @Update
    suspend fun updateUsers(point: UserPoints)
    @Delete
    suspend fun delete(pointId: Int)

    @Query("SELECT * FROM user_points_table")
     fun getAll(): Flow<List<UserPoints>>
}