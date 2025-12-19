package ru.netology.nmedia.dao

import androidx.room.*
import ru.netology.nmedia.entity.PostRemoteKeyEntity

@Dao
interface PostRemoteKeyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(key: PostRemoteKeyEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(keys: List<PostRemoteKeyEntity>)

    @Query("SELECT * FROM PostRemoteKeyEntity WHERE id = :id")
    suspend fun keyById(id: Long): PostRemoteKeyEntity?

    @Query("DELETE FROM PostRemoteKeyEntity")
    suspend fun clear()
}