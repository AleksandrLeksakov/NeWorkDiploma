package ru.netology.nework.dao.post

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.netology.nework.entity.PostRemoteKeyEntity

@Dao
interface PostRemoteKeyDao {
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insert(key: PostRemoteKeyEntity)

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insert(keys: List<PostRemoteKeyEntity>)

    @Query("SELECT * FROM PostRemoteKeyEntity WHERE id = :id")
    suspend fun keyById(id: Long): PostRemoteKeyEntity?

    @Query("DELETE FROM PostRemoteKeyEntity")
    suspend fun clear()
}