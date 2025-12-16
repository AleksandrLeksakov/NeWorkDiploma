package ru.netology.nmedia.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.netology.nmedia.entity.PostRemoteKeyEntity

@Dao
interface PostRemoteKeyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(key: PostRemoteKeyEntity)

    @Query("SELECT * FROM PostRemoteKeyEntity WHERE id = :id")
    suspend fun keyById(id: Long): PostRemoteKeyEntity?

    @Query("DELETE FROM PostRemoteKeyEntity")
    suspend fun clear()
}