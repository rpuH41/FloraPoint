package com.liulkovich.florapoint.data


import androidx.room.Dao
import androidx.room.Query
import com.liulkovich.florapoint.domain.Tip

@Dao
interface TipDao {
    @Query("SELECT * FROM tips ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomTip(): Tip?
}